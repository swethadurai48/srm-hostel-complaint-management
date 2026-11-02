/**
 * SRM Hostel Complaint Management System - Admin & Warden Portal Scripts
 * Recreated to match Visual Design Reference
 */

let currentAdmin = null;
let activeComplaintId = null;

document.addEventListener('DOMContentLoaded', async () => {
    // 1. Verify Authentication & Role
    try {
        currentAdmin = await API.getMe();
        if (!currentAdmin || currentAdmin.role !== 'ADMIN') {
            window.location.href = '/index.html';
            return;
        }

        const nameEl = document.getElementById('admin-display-name');
        if (nameEl) nameEl.innerText = currentAdmin.name || 'Warden';
    } catch {
        window.location.href = '/index.html';
        return;
    }

    // 2. Setup Event Listeners
    const logoutBtn = document.getElementById('btn-logout');
    if (logoutBtn) logoutBtn.addEventListener('click', () => API.logout());

    // Filter changes
    document.getElementById('filter-category').addEventListener('change', () => loadComplaints());
    document.getElementById('filter-status').addEventListener('change', () => loadComplaints());
    const filterActionEl = document.getElementById('filter-action');
    if (filterActionEl) {
        filterActionEl.addEventListener('change', () => loadComplaints());
    }
    document.getElementById('btn-clear-filters').addEventListener('click', () => {
        document.getElementById('filter-category').value = '';
        document.getElementById('filter-status').value = '';
        if (document.getElementById('filter-action')) {
            document.getElementById('filter-action').value = '';
        }
        updateSidebarActive('');
        loadComplaints();
    });

    // Modal management listeners
    const modal = document.getElementById('admin-action-modal');
    const closeBtn = document.getElementById('btn-close-modal');
    if (closeBtn && modal) {
        closeBtn.addEventListener('click', () => modal.classList.remove('active'));
        modal.addEventListener('click', (e) => {
            if (e.target === modal) modal.classList.remove('active');
        });
    }

    document.getElementById('form-admin-action').addEventListener('submit', handleSaveAdminAction);

    // 3. Initial Load
    await loadAdminDashboard();
});

function filterByCategory(categoryKey) {
    document.getElementById('filter-category').value = categoryKey;
    updateSidebarActive(categoryKey);
    loadComplaints();
}

function updateSidebarActive(categoryKey) {
    document.querySelectorAll('.app-sidebar .sidebar-item').forEach(item => {
        item.classList.remove('active');
    });
    // Find the one matching
    const items = document.querySelectorAll('.app-sidebar .sidebar-item');
    items.forEach(btn => {
        const onclickAttr = btn.getAttribute('onclick');
        if (onclickAttr && onclickAttr.includes(`'${categoryKey}'`)) {
            btn.classList.add('active');
        }
    });
}

async function loadAdminDashboard() {
    await Promise.all([loadStats(), loadComplaints()]);
}

async function loadStats() {
    try {
        const stats = await API.getAdminStats();
        if (stats) {
            const totalEl = document.getElementById('admin-stat-total');
            const pendingEl = document.getElementById('admin-stat-pending');
            const progressEl = document.getElementById('admin-stat-progress');
            const resolvedEl = document.getElementById('admin-stat-resolved');

            if (totalEl) totalEl.innerText = stats.total ?? 0;
            if (pendingEl) pendingEl.innerText = stats.pending ?? 0;
            if (progressEl) progressEl.innerText = stats.inProgress ?? 0;
            if (resolvedEl) resolvedEl.innerText = stats.resolved ?? 0;
        }
    } catch (err) {
        console.error('Failed to load admin stats:', err);
    }
}

async function loadComplaints() {
    const tableBody = document.getElementById('admin-complaints-table-body');
    if (!tableBody) return;

    tableBody.innerHTML = '<tr><td colspan="7" style="text-align: center; color: var(--text-muted); padding: 24px;">Loading complaints...</td></tr>';

    const category = document.getElementById('filter-category').value;
    const status = document.getElementById('filter-status').value;
    const actionFilter = document.getElementById('filter-action') ? document.getElementById('filter-action').value : '';

    try {
        let complaints = await API.getAdminComplaints(category, status);

        if (actionFilter) {
            complaints = complaints.filter(c => {
                if (actionFilter === 'NEEDS_ACTION') return c.status === 'PENDING';
                if (actionFilter === 'WAITING') return c.status === 'IN_PROGRESS';
                if (actionFilter === 'RESPONDED') return c.status === 'RESOLVED';
                return true;
            });
        }

        if (!complaints || complaints.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="7" style="text-align: center; color: var(--text-muted); padding: 32px;">No complaints found matching the selected filters.</td></tr>';
            return;
        }

        tableBody.innerHTML = '';
        complaints.forEach((c, idx) => {
            const tr = document.createElement('tr');
            tr.onclick = () => openActionModal(c.id);

            const studentName = c.student ? c.student.name : 'Resident';
            const studentRoom = c.location || (c.student && c.student.roomOrHostel ? c.student.roomOrHostel : 'Hostel');

            tr.innerHTML = `
                <td style="font-weight: 600; color: var(--text-muted);">${idx + 1}</td>
                <td><strong style="color: var(--srm-navy);">${getCategoryLabel(c.category)}</strong></td>
                <td>
                    <div style="font-weight: 600; color: var(--text-main); margin-bottom: 2px;">${escapeHtml(c.title)}</div>
                    <div style="font-size: 0.82rem; color: var(--text-muted); max-width: 260px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">${escapeHtml(c.description)}</div>
                </td>
                <td>
                    <div style="font-weight: 600; font-size: 0.85rem;">${escapeHtml(studentName)}</div>
                    <div style="font-size: 0.8rem; color: var(--text-muted);">📍 ${escapeHtml(studentRoom)}</div>
                </td>
                <td>${getStatusPillHtml(c.status)}</td>
                <td>${getWardenActionHtml(c)}</td>
                <td style="color: var(--text-muted); font-size: 0.82rem;">${formatSimpleDate(c.createdAt)}</td>
            `;

            tableBody.appendChild(tr);
        });
    } catch (err) {
        tableBody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: #ef4444; padding: 24px;">Error: ${err.message}</td></tr>`;
    }
}

function getWardenActionHtml(c) {
    if (c.status === 'RESOLVED') {
        return `
            <span style="display: inline-flex; align-items: center; gap: 4px; color: #16a34a; font-size: 0.8rem; font-weight: 600;">
                ✓ Responded
            </span>
        `;
    } else if (c.status === 'IN_PROGRESS') {
        return `
            <span style="display: inline-flex; align-items: center; gap: 4px; color: #d97706; font-size: 0.8rem; font-weight: 600;">
                🕒 Waiting
            </span>
        `;
    } else { // PENDING
        return `
            <span style="display: inline-flex; align-items: center; gap: 4px; color: #dc2626; font-size: 0.8rem; font-weight: 600;">
                ⏳ Needs Action
            </span>
        `;
    }
}

function getStatusPillHtml(status) {
    if (status === 'RESOLVED') {
        return '<span class="status-pill resolved">Resolved</span>';
    } else if (status === 'IN_PROGRESS') {
        return '<span class="status-pill progress">In Progress</span>';
    } else {
        return '<span class="status-pill pending">Pending</span>';
    }
}

async function openActionModal(id) {
    activeComplaintId = id;
    const modal = document.getElementById('admin-action-modal');

    try {
        const c = await API.getComplaint(id);
        if (!c) return;

        document.getElementById('action-modal-title').innerText = c.title;
        document.getElementById('action-modal-category').innerText = getCategoryLabel(c.category);
        document.getElementById('action-modal-location').innerText = c.location || 'Hostel';
        document.getElementById('action-modal-created').innerText = formatDate(c.createdAt);
        document.getElementById('action-modal-desc').innerText = c.description;

        // Student Info
        const s = c.student;
        document.getElementById('action-modal-student-name').innerText = s ? s.name : 'Resident';
        document.getElementById('action-modal-student-email').innerText = s ? s.email : '-';
        document.getElementById('action-modal-student-id').innerText = (s && s.studentId) ? s.studentId : 'N/A';
        document.getElementById('action-modal-student-dept').innerText = (s && s.department) ? s.department : 'N/A';
        document.getElementById('action-modal-student-room').innerText = (s && s.roomOrHostel) ? s.roomOrHostel : (c.location || 'N/A');

        // Form fields
        document.getElementById('action-select-status').value = c.status;
        const staffInput = document.getElementById('action-assigned-staff');
        if (staffInput) staffInput.value = c.assignedStaff || '';
        document.getElementById('action-text-response').value = c.adminResponse || '';

        modal.classList.add('active');
    } catch (err) {
        showToast('Error opening complaint details: ' + err.message, 'error');
    }
}

async function handleSaveAdminAction(e) {
    e.preventDefault();
    if (!activeComplaintId) return;

    const saveBtn = document.getElementById('btn-save-action');
    const originalText = saveBtn.innerHTML;
    saveBtn.innerText = 'Saving...';
    saveBtn.disabled = true;

    const newStatus = document.getElementById('action-select-status').value;
    const assignedStaff = document.getElementById('action-assigned-staff') ? document.getElementById('action-assigned-staff').value.trim() : '';
    const responseText = document.getElementById('action-text-response').value.trim();

    try {
        // 1. Update status and assigned staff
        await API.updateComplaintStatus(activeComplaintId, newStatus, assignedStaff);

        // 2. Update response if provided, or provide official resolution note when marking resolved
        if (responseText.length > 0) {
            await API.updateAdminResponse(activeComplaintId, responseText);
        } else if (newStatus === 'RESOLVED') {
            const currentComp = await API.getComplaint(activeComplaintId);
            if (!currentComp.adminResponse || currentComp.adminResponse.trim().length === 0) {
                await API.updateAdminResponse(activeComplaintId, 'Issue inspected and resolved by hostel administration.');
            }
        }

        showToast('Complaint status and response saved successfully!', 'success');
        document.getElementById('admin-action-modal').classList.remove('active');

        await loadAdminDashboard();
    } catch (err) {
        showToast('Failed to save changes: ' + err.message, 'error');
    } finally {
        saveBtn.innerHTML = originalText;
        saveBtn.disabled = false;
    }
}

function formatSimpleDate(isoString) {
    if (!isoString) return 'N/A';
    const date = new Date(isoString);
    return date.toLocaleDateString(undefined, {
        day: 'numeric',
        month: 'short',
        year: 'numeric'
    });
}

function escapeHtml(str) {
    if (!str) return '';
    return str
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}
