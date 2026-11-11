/**
 * SRM Enterprise ERP Portal Controller
 * Connects frontend modules to Spring Boot Enterprise REST APIs
 */

let currentUser = null;
let currentDashboardData = null;
let allComplaints = [];
let allDepartments = [];
let allEmployees = [];
let allUsers = [];
let activeComplaintId = null;

document.addEventListener('DOMContentLoaded', async () => {
    // 1. Initialize Theme from local storage
    initTheme();

    // 2. Verify Session & Permissions
    try {
        currentUser = await API.getMe();
        if (!currentUser) {
            window.location.href = '/index.html';
            return;
        }
        renderUserProfile(currentUser);
    } catch (e) {
        window.location.href = '/index.html';
        return;
    }

    // 3. Setup Navigation & Layout Listeners
    setupNavigation();
    setupModals();
    setupGlobalSearch();

    // 4. Initial Load
    await loadInitialData();
});

/* ==========================================================================
   Theme & Profile Handlers
   ========================================================================== */

function initTheme() {
    const savedTheme = localStorage.getItem('srm_erp_theme') || 'light';
    document.documentElement.setAttribute('data-theme', savedTheme);
    updateThemeIcon(savedTheme);

    const themeToggleBtn = document.getElementById('btn-toggle-theme');
    if (themeToggleBtn) {
        themeToggleBtn.addEventListener('click', () => {
            const current = document.documentElement.getAttribute('data-theme');
            const next = current === 'dark' ? 'light' : 'dark';
            document.documentElement.setAttribute('data-theme', next);
            localStorage.setItem('srm_erp_theme', next);
            updateThemeIcon(next);
        });
    }
}

function updateThemeIcon(theme) {
    const btn = document.getElementById('btn-toggle-theme');
    if (btn) {
        btn.innerText = theme === 'dark' ? '☀️' : '🌙';
    }
}

function renderUserProfile(user) {
    const nameEl = document.getElementById('topbar-user-name');
    const roleEl = document.getElementById('topbar-user-role');
    const avatarEl = document.getElementById('topbar-user-avatar');

    if (nameEl) nameEl.innerText = user.name || 'User';
    if (roleEl) roleEl.innerText = user.role || 'STAFF';
    if (avatarEl && user.name) {
        const parts = user.name.trim().split(' ');
        avatarEl.innerText = parts.length > 1 ? (parts[0][0] + parts[1][0]).toUpperCase() : parts[0].slice(0, 2).toUpperCase();
    }
}

/* ==========================================================================
   Navigation & UI Shell Handlers
   ========================================================================== */

function setupNavigation() {
    // Sidebar collapse toggle
    const toggleBtn = document.getElementById('btn-toggle-sidebar');
    const sidebar = document.getElementById('erp-sidebar');
    if (toggleBtn && sidebar) {
        toggleBtn.addEventListener('click', () => {
            sidebar.classList.toggle('collapsed');
        });
    }

    // Module Nav Links
    document.querySelectorAll('.sidebar-nav .nav-item[data-module]').forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            const moduleTarget = item.getAttribute('data-module');
            switchModule(moduleTarget);
        });
    });

    // Notifications flyout toggle
    const notifBtn = document.getElementById('btn-notifications');
    const notifFlyout = document.getElementById('notifications-flyout');
    if (notifBtn && notifFlyout) {
        notifBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            notifFlyout.classList.toggle('active');
            if (notifFlyout.classList.contains('active')) {
                loadNotifications();
            }
        });

        document.addEventListener('click', (e) => {
            if (!notifFlyout.contains(e.target) && e.target !== notifBtn) {
                notifFlyout.classList.remove('active');
            }
        });
    }
}

function switchModule(moduleId) {
    // Update active nav link
    document.querySelectorAll('.sidebar-nav .nav-item[data-module]').forEach(item => {
        if (item.getAttribute('data-module') === moduleId) {
            item.classList.add('active');
        } else {
            item.classList.remove('active');
        }
    });

    // Update active module content
    document.querySelectorAll('.portal-module').forEach(mod => {
        if (mod.id === `mod-${moduleId}`) {
            mod.classList.add('active');
        } else {
            mod.classList.remove('active');
        }
    });

    // Refresh module specific data if needed
    if (moduleId === 'dashboard') loadDashboard();
    if (moduleId === 'complaints') loadComplaintsTable();
    if (moduleId === 'employees') loadEmployeesTable();
    if (moduleId === 'departments') loadDepartmentsTable();
    if (moduleId === 'workflows') loadApprovalsTable();
    if (moduleId === 'users') loadUsersTable();
    if (moduleId === 'settings') loadAuditLogsTable();
}

/* ==========================================================================
   Data Fetching & State
   ========================================================================== */

async function loadInitialData() {
    await Promise.all([
        loadDashboard(),
        loadDepartmentsList(),
        loadEmployeesList(),
        loadNotifications()
    ]);
}

async function loadDashboard() {
    try {
        const res = await API.request('/api/erp/dashboard');
        if (!res) return;
        currentDashboardData = res;

        // Populate KPI Counters
        document.getElementById('kpi-total-complaints').innerText = res.totalComplaints;
        document.getElementById('kpi-open-complaints').innerText = res.openComplaints;
        document.getElementById('kpi-progress-complaints').innerText = res.inProgressComplaints;
        document.getElementById('kpi-resolved-complaints').innerText = res.resolvedComplaints;
        document.getElementById('kpi-overdue-complaints').innerText = res.overdueComplaints;
        document.getElementById('kpi-total-employees').innerText = res.totalEmployees;
        document.getElementById('kpi-total-departments').innerText = res.totalDepartments;
        document.getElementById('kpi-sla-compliance').innerText = `${res.slaComplianceRate}%`;

        // Update badge for pending approvals in sidebar
        const approvalBadge = document.getElementById('badge-pending-approvals');
        if (approvalBadge) {
            if (res.pendingApprovals > 0) {
                approvalBadge.innerText = res.pendingApprovals;
                approvalBadge.style.display = 'inline-block';
            } else {
                approvalBadge.style.display = 'none';
            }
        }

        // Render Charts & Widgets
        renderResolutionChart(res);
        renderCategoryBreakdown(res.categoryDistribution);
        renderDepartmentWidget(res.departmentPerformance);
        renderActivityTimeline(res.recentActivities);

    } catch (err) {
        console.error('Failed to load dashboard:', err);
    }
}

/* ==========================================================================
   Interactive SVG Charts & Visualizations
   ========================================================================== */

function renderResolutionChart(data) {
    const container = document.getElementById('chart-resolution-trends');
    if (!container) return;

    // Build a crisp SVG area/line chart
    const total = data.totalComplaints || 10;
    const resolved = data.resolvedComplaints || 6;
    const progress = data.inProgressComplaints || 2;
    const open = data.openComplaints || 2;

    container.innerHTML = `
        <svg viewBox="0 0 500 180" style="width: 100%; height: 100%; overflow: visible;">
            <defs>
                <linearGradient id="areaGrad" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" stop-color="#1e70e9" stop-opacity="0.35"/>
                    <stop offset="100%" stop-color="#1e70e9" stop-opacity="0.0"/>
                </linearGradient>
            </defs>
            <!-- Grid Lines -->
            <line x1="0" y1="30" x2="500" y2="30" stroke="var(--border-subtle)" stroke-dasharray="3,3"/>
            <line x1="0" y1="80" x2="500" y2="80" stroke="var(--border-subtle)" stroke-dasharray="3,3"/>
            <line x1="0" y1="130" x2="500" y2="130" stroke="var(--border-subtle)" stroke-dasharray="3,3"/>
            
            <!-- Area & Line Path -->
            <path d="M 0,140 Q 100,110 200,90 T 350,50 T 500,30 L 500,160 L 0,160 Z" fill="url(#areaGrad)"/>
            <path d="M 0,140 Q 100,110 200,90 T 350,50 T 500,30" fill="none" stroke="#1e70e9" stroke-width="3" stroke-linecap="round"/>

            <!-- Data Points -->
            <circle cx="0" cy="140" r="4" fill="#1e70e9"/>
            <circle cx="200" cy="90" r="4" fill="#1e70e9"/>
            <circle cx="350" cy="50" r="4" fill="#1e70e9"/>
            <circle cx="500" cy="30" r="5" fill="#15803d" stroke="#ffffff" stroke-width="2"/>

            <!-- Axis Labels -->
            <text x="0" y="175" font-size="10" fill="var(--text-muted)">Wk 1</text>
            <text x="125" y="175" font-size="10" fill="var(--text-muted)">Wk 2</text>
            <text x="250" y="175" font-size="10" fill="var(--text-muted)">Wk 3</text>
            <text x="375" y="175" font-size="10" fill="var(--text-muted)">Wk 4</text>
            <text x="470" y="175" font-size="10" fill="var(--text-muted)">Today</text>
        </svg>
    `;
}

function renderCategoryBreakdown(categories) {
    const container = document.getElementById('chart-category-breakdown');
    if (!container || !categories) return;

    let total = Object.values(categories).reduce((a, b) => a + b, 0) || 1;
    let html = '<div style="display: flex; flex-direction: column; gap: 10px;">';

    for (const [cat, count] of Object.entries(categories)) {
        const pct = Math.round((count / total) * 100);
        html += `
            <div>
                <div style="display: flex; justify-content: space-between; font-size: 0.82rem; margin-bottom: 3px;">
                    <span style="font-weight: 600; color: var(--text-primary);">${getCategoryLabel(cat)}</span>
                    <span style="color: var(--text-muted); font-weight: 500;">${count} tickets (${pct}%)</span>
                </div>
                <div style="width: 100%; height: 7px; background: var(--bg-app); border-radius: var(--radius-full); overflow: hidden;">
                    <div style="width: ${pct}%; height: 100%; background: var(--brand-primary); border-radius: var(--radius-full);"></div>
                </div>
            </div>
        `;
    }
    html += '</div>';
    container.innerHTML = html;
}

function renderDepartmentWidget(depts) {
    const tbody = document.getElementById('dashboard-dept-tbody');
    if (!tbody || !depts) return;

    tbody.innerHTML = '';
    depts.forEach(d => {
        const tr = document.createElement('tr');
        tr.onclick = () => switchModule('departments');
        const resolvedPct = d.totalComplaints > 0 ? Math.round((d.resolvedComplaints / d.totalComplaints) * 100) : 100;
        tr.innerHTML = `
            <td><strong style="color: var(--brand-navy);">${escapeHtml(d.code)}</strong> - ${escapeHtml(d.name)}</td>
            <td>${d.employeeCount}</td>
            <td><strong>${d.totalComplaints}</strong></td>
            <td><span class="status-pill ${d.pendingComplaints > 0 ? 'pending' : 'resolved'}">${d.pendingComplaints} Pending</span></td>
            <td>
                <div style="display: flex; align-items: center; gap: 8px;">
                    <div style="flex: 1; height: 6px; background: var(--bg-app); border-radius: 4px; overflow: hidden;">
                        <div style="width: ${resolvedPct}%; height: 100%; background: #16a34a;"></div>
                    </div>
                    <span style="font-size: 0.78rem; font-weight: 600;">${resolvedPct}%</span>
                </div>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function renderActivityTimeline(activities) {
    const container = document.getElementById('dashboard-activity-stream');
    if (!container || !activities) return;

    if (activities.length === 0) {
        container.innerHTML = '<div style="color: var(--text-muted); font-size: 0.85rem; padding: 12px 0;">No recent activities logged yet.</div>';
        return;
    }

    container.innerHTML = '';
    activities.forEach(act => {
        const node = document.createElement('div');
        node.className = 'activity-node';
        node.innerHTML = `
            <div class="activity-bullet"></div>
            <div class="activity-body">
                <div class="activity-header">
                    <span class="activity-user">${escapeHtml(act.userName)}</span>
                    <span class="activity-time">${act.timestamp}</span>
                </div>
                <div class="activity-text">${escapeHtml(act.description)}</div>
            </div>
        `;
        container.appendChild(node);
    });
}

/* ==========================================================================
   Complaint Management Module
   ========================================================================== */

async function loadComplaintsTable() {
    const tbody = document.getElementById('erp-complaints-tbody');
    if (!tbody) return;

    tbody.innerHTML = '<tr><td colspan="8" style="text-align: center; padding: 24px; color: var(--text-muted);">Loading complaints...</td></tr>';

    try {
        allComplaints = await API.getAdminComplaints();
        applyComplaintFilters();
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="8" style="text-align: center; padding: 24px; color: #ef4444;">Error loading complaints: ${err.message}</td></tr>`;
    }
}

function applyComplaintFilters() {
    const tbody = document.getElementById('erp-complaints-tbody');
    if (!tbody || !allComplaints) return;

    const catFilter = document.getElementById('filter-comp-category')?.value || '';
    const statusFilter = document.getElementById('filter-comp-status')?.value || '';
    const searchFilter = document.getElementById('filter-comp-search')?.value.toLowerCase().trim() || '';

    const filtered = allComplaints.filter(c => {
        if (catFilter && c.category !== catFilter) return false;
        if (statusFilter && c.status !== statusFilter) return false;
        if (searchFilter) {
            const titleMatch = c.title && c.title.toLowerCase().includes(searchFilter);
            const descMatch = c.description && c.description.toLowerCase().includes(searchFilter);
            const studentMatch = c.student && c.student.name && c.student.name.toLowerCase().includes(searchFilter);
            const locMatch = c.location && c.location.toLowerCase().includes(searchFilter);
            if (!titleMatch && !descMatch && !studentMatch && !locMatch) return false;
        }
        return true;
    });

    if (filtered.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" style="text-align: center; padding: 32px; color: var(--text-muted);">No complaints match the selected criteria.</td></tr>';
        return;
    }

    tbody.innerHTML = '';
    filtered.forEach((c, idx) => {
        const tr = document.createElement('tr');
        tr.onclick = () => openComplaintDetailDrawer(c.id);

        const deptCode = c.department ? c.department.code : 'MAINT';
        const prio = c.priority || 'MEDIUM';
        const assigned = c.assignedStaff || '<span style="color: var(--text-muted); font-style: italic;">Unassigned</span>';
        const isEscalatedBadge = c.isEscalated ? '<span style="color: #ef4444; font-weight: 700; margin-left: 4px;" title="Escalated">⚠️</span>' : '';

        tr.innerHTML = `
            <td style="font-weight: 700; color: var(--text-muted);">#${c.id}</td>
            <td><strong style="color: var(--brand-navy);">${getCategoryLabel(c.category)}</strong></td>
            <td>
                <div style="font-weight: 600; color: var(--text-primary);">${escapeHtml(c.title)} ${isEscalatedBadge}</div>
                <div style="font-size: 0.8rem; color: var(--text-muted); max-width: 260px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">${escapeHtml(c.description)}</div>
            </td>
            <td><span class="priority-chip ${prio}">${prio}</span></td>
            <td><span class="status-pill ${c.status.toLowerCase()}">${c.status.replace('_', ' ')}</span></td>
            <td><span style="font-size: 0.82rem; font-weight: 600; color: var(--text-secondary);">${deptCode}</span></td>
            <td><span style="font-size: 0.82rem;">${assigned}</span></td>
            <td style="font-size: 0.8rem; color: var(--text-muted);">${formatDate(c.createdAt)}</td>
        `;
        tbody.appendChild(tr);
    });
}

async function openComplaintDetailDrawer(id) {
    activeComplaintId = id;
    const modal = document.getElementById('modal-complaint-detail');
    if (!modal) return;

    try {
        const c = await API.getComplaint(id);
        if (!c) return;

        document.getElementById('detail-ticket-id').innerText = `#${c.id}`;
        document.getElementById('detail-ticket-title').innerText = c.title;
        document.getElementById('detail-ticket-desc').innerText = c.description;
        document.getElementById('detail-ticket-category').innerText = getCategoryLabel(c.category);
        document.getElementById('detail-ticket-status').innerHTML = `<span class="status-pill ${c.status.toLowerCase()}">${c.status.replace('_', ' ')}</span>`;
        document.getElementById('detail-ticket-priority').innerHTML = `<span class="priority-chip ${c.priority || 'MEDIUM'}">${c.priority || 'MEDIUM'}</span>`;
        document.getElementById('detail-ticket-location').innerText = c.location || 'Campus / Hostel';
        document.getElementById('detail-ticket-dept').innerText = c.department ? `${c.department.code} - ${c.department.name}` : 'Not Assigned';
        document.getElementById('detail-ticket-staff').innerText = c.assignedStaff || 'No technician assigned yet';
        document.getElementById('detail-ticket-student').innerText = c.student ? `${c.student.name} (${c.student.email})` : 'Anonymous';

        // Load Comments
        await loadComplaintComments(id);

        // Load History
        await loadComplaintHistory(id);

        modal.classList.add('active');
    } catch (err) {
        showToast('Error opening ticket details: ' + err.message, 'error');
    }
}

async function loadComplaintComments(complaintId) {
    const container = document.getElementById('detail-comments-stream');
    if (!container) return;

    try {
        const comments = await API.request(`/api/erp/complaints/${complaintId}/comments`);
        if (!comments || comments.length === 0) {
            container.innerHTML = '<div style="color: var(--text-muted); font-size: 0.82rem; padding: 8px 0;">No comments or internal notes recorded yet.</div>';
            return;
        }

        container.innerHTML = '';
        comments.forEach(cm => {
            const div = document.createElement('div');
            div.style.padding = '10px 14px';
            div.style.borderRadius = 'var(--radius-md)';
            div.style.marginBottom = '8px';
            div.style.background = cm.internal ? 'rgba(197, 155, 39, 0.12)' : 'var(--bg-app)';
            div.style.border = cm.internal ? '1px dashed var(--brand-gold)' : '1px solid var(--border-subtle)';

            div.innerHTML = `
                <div style="display: flex; justify-content: space-between; font-size: 0.78rem; margin-bottom: 4px;">
                    <strong>${escapeHtml(cm.author ? cm.author.name : 'Staff')} ${cm.internal ? '<span style="color: var(--brand-gold); font-weight: 700;">[INTERNAL NOTE]</span>' : ''}</strong>
                    <span style="color: var(--text-muted);">${formatDate(cm.createdAt)}</span>
                </div>
                <div style="font-size: 0.85rem; color: var(--text-primary); white-space: pre-wrap;">${escapeHtml(cm.message)}</div>
            `;
            container.appendChild(div);
        });
    } catch (err) {
        container.innerHTML = `<div style="color: #ef4444; font-size: 0.82rem;">Failed to load comments: ${err.message}</div>`;
    }
}

async function loadComplaintHistory(complaintId) {
    const container = document.getElementById('detail-history-stream');
    if (!container) return;

    try {
        const history = await API.request(`/api/erp/complaints/${complaintId}/history`);
        if (!history || history.length === 0) {
            container.innerHTML = '<div style="color: var(--text-muted); font-size: 0.82rem; padding: 8px 0;">No audit records found.</div>';
            return;
        }

        container.innerHTML = '';
        history.forEach(h => {
            const row = document.createElement('div');
            row.style.fontSize = '0.8rem';
            row.style.padding = '6px 0';
            row.style.borderBottom = '1px solid var(--border-subtle)';
            row.innerHTML = `
                <span style="color: var(--text-muted);">${formatDate(h.timestamp)}</span> &bull; 
                <strong>${escapeHtml(h.action)}</strong> by <em>${escapeHtml(h.changedBy ? h.changedBy.name : 'System')}</em>: 
                <span style="color: var(--text-secondary);">${escapeHtml(h.notes || h.newValue || '')}</span>
            `;
            container.appendChild(row);
        });
    } catch (err) {
        container.innerHTML = `<div style="color: #ef4444; font-size: 0.82rem;">Failed to load history: ${err.message}</div>`;
    }
}

async function handleAddComment(e) {
    e.preventDefault();
    if (!activeComplaintId) return;

    const input = document.getElementById('input-comment-msg');
    const isInternal = document.getElementById('check-comment-internal')?.checked || false;
    const msg = input.value.trim();
    if (!msg) return;

    try {
        await API.request(`/api/erp/complaints/${activeComplaintId}/comments`, {
            method: 'POST',
            body: JSON.stringify({ message: msg, isInternal: isInternal })
        });
        input.value = '';
        showToast('Comment recorded', 'success');
        await loadComplaintComments(activeComplaintId);
    } catch (err) {
        showToast('Failed to post comment: ' + err.message, 'error');
    }
}

async function handleEscalateTicket() {
    if (!activeComplaintId) return;
    const reason = prompt("Enter justification for escalating this complaint to Executive Administration:");
    if (!reason || reason.trim().length === 0) return;

    try {
        await API.request(`/api/erp/complaints/${activeComplaintId}/escalate`, {
            method: 'POST',
            body: JSON.stringify({ reason: reason.trim() })
        });
        showToast('Complaint marked as ESCALATED to Department Head and Executive Wardens', 'success');
        openComplaintDetailDrawer(activeComplaintId);
        loadComplaintsTable();
    } catch (err) {
        showToast('Escalation failed: ' + err.message, 'error');
    }
}

function openAssignModalForActive() {
    if (!activeComplaintId) return;
    const modal = document.getElementById('modal-assign-employee');
    if (!modal) return;

    // Populate employee select
    const select = document.getElementById('assign-select-emp');
    if (select && allEmployees) {
        select.innerHTML = '<option value="" disabled selected>Choose qualified technician / employee</option>';
        allEmployees.forEach(emp => {
            select.innerHTML += `<option value="${emp.id}">${escapeHtml(emp.name)} - ${escapeHtml(emp.designation)} (${emp.department ? emp.department.code : ''})</option>`;
        });
    }

    modal.classList.add('active');
}

async function handleSaveAssignment(e) {
    e.preventDefault();
    if (!activeComplaintId) return;

    const employeeId = document.getElementById('assign-select-emp').value;
    const notes = document.getElementById('assign-text-notes').value.trim();

    try {
        await API.request(`/api/erp/complaints/${activeComplaintId}/assign`, {
            method: 'POST',
            body: JSON.stringify({ employeeId: parseInt(employeeId), notes: notes })
        });
        showToast('Technician assigned successfully', 'success');
        document.getElementById('modal-assign-employee').classList.remove('active');
        openComplaintDetailDrawer(activeComplaintId);
        loadComplaintsTable();
    } catch (err) {
        showToast('Assignment failed: ' + err.message, 'error');
    }
}

/* ==========================================================================
   Human Resource Management (HRM) Module
   ========================================================================== */

async function loadEmployeesList() {
    try {
        allEmployees = await API.request('/api/employees');
    } catch (err) {
        console.error('Failed to pre-fetch employees:', err);
    }
}

async function loadEmployeesTable() {
    const tbody = document.getElementById('erp-employees-tbody');
    if (!tbody) return;

    tbody.innerHTML = '<tr><td colspan="8" style="text-align: center; padding: 24px; color: var(--text-muted);">Loading employee directory...</td></tr>';

    try {
        allEmployees = await API.request('/api/employees');
        if (!allEmployees || allEmployees.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" style="text-align: center; padding: 32px; color: var(--text-muted);">No employees registered yet. Click "Add Employee" to register staff.</td></tr>';
            return;
        }

        tbody.innerHTML = '';
        allEmployees.forEach(emp => {
            const tr = document.createElement('tr');
            const isActive = emp.status === 'ACTIVE';
            const deptName = emp.department ? emp.department.name : 'Unassigned';
            const deptCode = emp.department ? ` (${emp.department.code})` : '';

            tr.innerHTML = `
                <td><strong style="color: var(--brand-navy); font-family: var(--erp-mono);">${escapeHtml(emp.employeeCode)}</strong></td>
                <td>
                    <div style="font-weight: 600;">${escapeHtml(emp.name)}</div>
                    <div style="font-size: 0.8rem; color: var(--text-muted);">${escapeHtml(emp.email)}</div>
                </td>
                <td><strong style="color: var(--brand-navy);">${escapeHtml(deptName)}${escapeHtml(deptCode)}</strong></td>
                <td>${escapeHtml(emp.designation)}</td>
                <td><span class="status-pill ${isActive ? 'resolved' : 'pending'}">${escapeHtml(emp.status)}</span></td>
                <td><strong>${emp.assignedTasksCount || 0}</strong> assigned</td>
                <td style="font-size: 0.8rem; color: var(--text-muted);">${escapeHtml(emp.phone || 'N/A')}</td>
                <td>
                    <div style="display: flex; gap: 6px;">
                        <button class="btn btn-secondary btn-sm" onclick="openEditEmployeeModal(${emp.id})">Edit</button>
                        <button class="btn btn-danger btn-sm" onclick="handleDeleteEmployee(${emp.id}, '${escapeHtml(emp.name)}')">Delete</button>
                    </div>
                </td>
            `;
            tbody.appendChild(tr);
        });
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="8" style="text-align: center; padding: 24px; color: #ef4444;">Error: ${err.message}</td></tr>`;
    }
}

/**
 * Dynamically fetches and populates the Department dropdown from PostgreSQL.
 * Shows loading state, handles errors, and displays human-readable department names.
 */
async function populateDepartmentDropdown(selectId, loadingId, errorId, selectedId = null) {
    const select = document.getElementById(selectId);
    const loading = document.getElementById(loadingId);
    const error = document.getElementById(errorId);

    if (!select) return;

    if (loading) loading.style.display = 'block';
    if (error) error.style.display = 'none';
    select.disabled = true;
    select.innerHTML = '<option value="" disabled selected>Loading departments from database...</option>';

    try {
        let depts = null;
        try {
            depts = await API.request('/api/departments');
        } catch {
            depts = await API.request('/api/erp/departments');
        }

        allDepartments = Array.isArray(depts) ? depts : [];

        if (loading) loading.style.display = 'none';

        if (allDepartments.length === 0) {
            select.innerHTML = '<option value="" disabled selected>No departments available</option>';
            if (error) {
                error.textContent = 'No departments found in PostgreSQL database. Please create a department first in Department Management.';
                error.style.display = 'block';
            }
            return;
        }

        // Populate with human-readable department names.
        // NOTE: Static "All Departments" option is explicitly excluded here.
        select.innerHTML = '<option value="" disabled selected>Select Department...</option>';
        allDepartments.forEach(d => {
            const isSelected = (selectedId && Number(selectedId) === Number(d.id)) ? 'selected' : '';
            const statusLabel = (d.status && d.status !== 'ACTIVE') ? ` [${d.status}]` : '';
            select.innerHTML += `<option value="${d.id}" ${isSelected}>${escapeHtml(d.name)} (${escapeHtml(d.code)})${statusLabel}</option>`;
        });

        select.disabled = false;
    } catch (err) {
        console.error('Failed to load departments dropdown:', err);
        if (loading) loading.style.display = 'none';
        select.innerHTML = '<option value="" disabled selected>Failed to load departments</option>';
        if (error) {
            error.textContent = `Error loading departments: ${err.message}. Please check connection and try again.`;
            error.style.display = 'block';
        }
    }
}

function openAddEmployeeModal() {
    const modal = document.getElementById('modal-add-employee');
    const form = document.getElementById('form-add-employee');
    if (form) form.reset();
    if (modal) modal.classList.add('active');

    // Fetch and populate department options dynamically upon form open
    populateDepartmentDropdown('emp-dept', 'emp-dept-loading', 'emp-dept-error');
}

async function handleRegisterEmployee(e) {
    e.preventDefault();
    const deptSelect = document.getElementById('emp-dept');
    const deptVal = deptSelect ? deptSelect.value : '';

    if (!deptVal) {
        showToast('Please select a valid department for this employee.', 'error');
        deptSelect?.focus();
        return;
    }

    const payload = {
        employeeCode: document.getElementById('emp-code').value.trim(),
        name: document.getElementById('emp-name').value.trim(),
        email: document.getElementById('emp-email').value.trim(),
        phone: document.getElementById('emp-phone').value.trim(),
        departmentId: parseInt(deptVal),
        designation: document.getElementById('emp-designation').value.trim(),
        skills: document.getElementById('emp-skills').value.trim()
    };

    try {
        await API.request('/api/employees', {
            method: 'POST',
            body: JSON.stringify(payload)
        });
        showToast('Employee registered successfully in HR directory', 'success');
        document.getElementById('modal-add-employee').classList.remove('active');
        document.getElementById('form-add-employee').reset();
        await loadEmployeesList();
        loadEmployeesTable();
        loadDepartmentsTable();
        loadDashboard();
    } catch (err) {
        showToast('Failed to register employee: ' + err.message, 'error');
    }
}

async function openEditEmployeeModal(empId) {
    const emp = (allEmployees || []).find(e => Number(e.id) === Number(empId));
    if (!emp) {
        showToast('Employee record not found', 'error');
        return;
    }

    document.getElementById('edit-emp-id').value = emp.id;
    document.getElementById('edit-emp-code').value = emp.employeeCode || '';
    document.getElementById('edit-emp-name').value = emp.name || '';
    document.getElementById('edit-emp-email').value = emp.email || '';
    document.getElementById('edit-emp-designation').value = emp.designation || '';
    document.getElementById('edit-emp-status').value = emp.status || 'ACTIVE';
    document.getElementById('edit-emp-phone').value = emp.phone || '';
    document.getElementById('edit-emp-skills').value = emp.skills || '';

    const modal = document.getElementById('modal-edit-employee');
    if (modal) modal.classList.add('active');

    const currentDeptId = emp.department ? emp.department.id : null;
    await populateDepartmentDropdown('edit-emp-dept', 'edit-emp-dept-loading', 'edit-emp-dept-error', currentDeptId);
}

async function handleUpdateEmployee(e) {
    e.preventDefault();
    const empId = document.getElementById('edit-emp-id').value;
    const deptSelect = document.getElementById('edit-emp-dept');
    const deptVal = deptSelect ? deptSelect.value : '';

    if (!deptVal) {
        showToast('Please select a valid department.', 'error');
        deptSelect?.focus();
        return;
    }

    const payload = {
        name: document.getElementById('edit-emp-name').value.trim(),
        email: document.getElementById('edit-emp-email').value.trim(),
        phone: document.getElementById('edit-emp-phone').value.trim(),
        departmentId: parseInt(deptVal),
        designation: document.getElementById('edit-emp-designation').value.trim(),
        status: document.getElementById('edit-emp-status').value,
        skills: document.getElementById('edit-emp-skills').value.trim()
    };

    try {
        await API.request(`/api/employees/${empId}`, {
            method: 'PUT',
            body: JSON.stringify(payload)
        });
        showToast('Employee profile and department assignment updated', 'success');
        document.getElementById('modal-edit-employee').classList.remove('active');
        await loadEmployeesList();
        loadEmployeesTable();
        loadDepartmentsTable();
    } catch (err) {
        showToast('Failed to update employee: ' + err.message, 'error');
    }
}

async function handleDeleteEmployee(empId, empName) {
    if (!confirm(`Are you sure you want to remove employee "${empName}"?`)) return;

    try {
        await API.request(`/api/employees/${empId}`, { method: 'DELETE' });
        showToast('Employee removed successfully', 'success');
        await loadEmployeesList();
        loadEmployeesTable();
        loadDepartmentsTable();
        loadDashboard();
    } catch (err) {
        showToast('Failed to delete employee: ' + err.message, 'error');
    }
}

/* ==========================================================================
   Department Management Module
   ========================================================================== */

async function loadDepartmentsList() {
    try {
        let depts = null;
        try {
            depts = await API.request('/api/departments');
        } catch {
            depts = await API.request('/api/erp/departments');
        }
        allDepartments = Array.isArray(depts) ? depts : [];

        // Populate ONLY the complaints filter dropdown with "All Departments"
        const filterSelect = document.getElementById('filter-comp-dept');
        if (filterSelect) {
            filterSelect.innerHTML = '<option value="">All Departments</option>';
            allDepartments.forEach(d => {
                filterSelect.innerHTML += `<option value="${d.id}">${escapeHtml(d.code)} - ${escapeHtml(d.name)}</option>`;
            });
        }
    } catch (err) {
        console.error('Failed to pre-fetch departments:', err);
    }
}

async function loadDepartmentsTable() {
    const tbody = document.getElementById('erp-departments-tbody');
    if (!tbody) return;

    tbody.innerHTML = '<tr><td colspan="7" style="text-align: center; padding: 24px; color: var(--text-muted);">Loading departments...</td></tr>';

    try {
        let depts = null;
        try {
            depts = await API.request('/api/departments');
        } catch {
            depts = await API.request('/api/erp/departments');
        }
        allDepartments = Array.isArray(depts) ? depts : [];

        if (allDepartments.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" style="text-align: center; padding: 32px; color: var(--text-muted);">No departments configured.</td></tr>';
            return;
        }

        tbody.innerHTML = '';
        allDepartments.forEach(dept => {
            const tr = document.createElement('tr');
            const isActive = dept.status === 'ACTIVE';
            const employeeCount = dept.employeeCount || 0;

            tr.innerHTML = `
                <td><strong style="color: var(--brand-navy); font-family: var(--erp-mono);">#${dept.id}</strong></td>
                <td>
                    <div style="font-weight: 600;">${escapeHtml(dept.name)}</div>
                    <span style="font-size: 0.75rem; color: var(--text-muted); font-family: var(--erp-mono);">${escapeHtml(dept.code)}</span>
                </td>
                <td>${escapeHtml(dept.manager ? dept.manager.name : 'System Admin')}</td>
                <td>
                    <span class="badge" style="background:#eef2ff; color:#3730a3; padding:4px 10px; border-radius:12px; font-weight:600; font-size:0.82rem;">
                        👥 ${employeeCount} staff
                    </span>
                </td>
                <td>$${Number(dept.budget || 0).toLocaleString()}</td>
                <td>
                    <span class="status-pill ${isActive ? 'resolved' : 'pending'}" style="${!isActive ? 'background:#fee2e2; color:#991b1b;' : ''}">
                        ${escapeHtml(dept.status || 'ACTIVE')}
                    </span>
                </td>
                <td>
                    <div style="display: flex; gap: 6px; flex-wrap: wrap;">
                        <button class="btn btn-secondary btn-sm" onclick="openEditDeptModal(${dept.id})">Edit</button>
                        ${isActive
                            ? `<button class="btn btn-secondary btn-sm" onclick="toggleDeptStatus(${dept.id}, 'INACTIVE')">Deactivate</button>`
                            : `<button class="btn btn-secondary btn-sm" onclick="toggleDeptStatus(${dept.id}, 'ACTIVE')">Activate</button>`
                        }
                        <button class="btn btn-danger btn-sm" onclick="handleDeleteDept(${dept.id}, '${escapeHtml(dept.name)}', ${employeeCount})">Delete</button>
                    </div>
                </td>
            `;
            tbody.appendChild(tr);
        });
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align: center; padding: 24px; color: #ef4444;">Error: ${err.message}</td></tr>`;
    }
}

function openEditDeptModal(deptId) {
    const dept = (allDepartments || []).find(d => Number(d.id) === Number(deptId));
    if (!dept) {
        showToast('Department not found', 'error');
        return;
    }

    document.getElementById('edit-dept-id').value = dept.id;
    document.getElementById('edit-dept-code').value = dept.code || '';
    document.getElementById('edit-dept-name').value = dept.name || '';
    document.getElementById('edit-dept-desc').value = dept.description || '';
    document.getElementById('edit-dept-status').value = dept.status || 'ACTIVE';
    document.getElementById('edit-dept-budget').value = dept.budget || 0;
    document.getElementById('edit-dept-email').value = dept.contactEmail || '';
    document.getElementById('edit-dept-phone').value = dept.contactPhone || '';

    const modal = document.getElementById('modal-edit-dept');
    if (modal) modal.classList.add('active');
}

async function handleUpdateDepartment(e) {
    e.preventDefault();
    const deptId = document.getElementById('edit-dept-id').value;
    const payload = {
        code: document.getElementById('edit-dept-code').value.trim().toUpperCase(),
        name: document.getElementById('edit-dept-name').value.trim(),
        description: document.getElementById('edit-dept-desc').value.trim(),
        status: document.getElementById('edit-dept-status').value,
        budget: parseFloat(document.getElementById('edit-dept-budget').value) || 0.0,
        contactEmail: document.getElementById('edit-dept-email').value.trim(),
        contactPhone: document.getElementById('edit-dept-phone').value.trim()
    };

    try {
        await API.request(`/api/departments/${deptId}`, {
            method: 'PUT',
            body: JSON.stringify(payload)
        });
        showToast('Department updated successfully', 'success');
        document.getElementById('modal-edit-dept').classList.remove('active');
        await loadDepartmentsList();
        loadDepartmentsTable();
        loadEmployeesTable();
    } catch (err) {
        showToast('Failed to update department: ' + err.message, 'error');
    }
}

async function toggleDeptStatus(deptId, targetStatus) {
    try {
        await API.request(`/api/departments/${deptId}/status?status=${encodeURIComponent(targetStatus)}`, {
            method: 'PUT'
        });
        showToast(`Department status changed to ${targetStatus}`, 'success');
        await loadDepartmentsList();
        loadDepartmentsTable();
    } catch (err) {
        showToast('Status update failed: ' + err.message, 'error');
    }
}

async function handleDeleteDept(deptId, deptName, employeeCount) {
    if (employeeCount > 0) {
        alert(`Cannot delete "${deptName}". It has ${employeeCount} assigned employees. Please reassign the employees first, or deactivate this department instead.`);
        return;
    }

    if (!confirm(`Are you sure you want to delete department "${deptName}"? This action cannot be undone.`)) {
        return;
    }

    try {
        await API.request(`/api/departments/${deptId}`, { method: 'DELETE' });
        showToast(`Department "${deptName}" deleted successfully`, 'success');
        await loadDepartmentsList();
        loadDepartmentsTable();
        loadDashboard();
    } catch (err) {
        showToast('Deletion failed: ' + err.message, 'error');
    }
}

async function handleCreateDepartment(e) {
    e.preventDefault();
    const payload = {
        code: document.getElementById('dept-code').value.trim().toUpperCase(),
        name: document.getElementById('dept-name').value.trim(),
        description: document.getElementById('dept-desc').value.trim(),
        budget: parseFloat(document.getElementById('dept-budget').value) || 0.0,
        contactEmail: document.getElementById('dept-email').value.trim(),
        contactPhone: document.getElementById('dept-phone').value.trim(),
        status: 'ACTIVE'
    };

    try {
        await API.request('/api/departments', {
            method: 'POST',
            body: JSON.stringify(payload)
        });
        showToast('Department successfully created', 'success');
        document.getElementById('modal-add-dept').classList.remove('active');
        document.getElementById('form-add-dept').reset();

        // Refresh the department list immediately after adding a new department
        await loadDepartmentsList();
        loadDepartmentsTable();
        loadDashboard();
    } catch (err) {
        showToast('Failed to create department: ' + err.message, 'error');
    }
}

/* ==========================================================================
   Workflow & Approvals Module
   ========================================================================== */

async function loadApprovalsTable() {
    const tbody = document.getElementById('erp-approvals-tbody');
    if (!tbody) return;

    tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; padding: 24px; color: var(--text-muted);">Loading pending approvals...</td></tr>';

    try {
        const approvals = await API.request('/api/erp/approvals/pending');
        if (!approvals || approvals.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; padding: 32px; color: #16a34a; font-weight: 500;">✓ All approval workflows are clear. No pending work order requests.</td></tr>';
            return;
        }

        tbody.innerHTML = '';
        approvals.forEach(app => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><strong>#${app.id}</strong></td>
                <td>Ticket #${app.complaint.id}: ${escapeHtml(app.complaint.title)}</td>
                <td><span class="status-pill pending">${escapeHtml(app.approvalType)}</span></td>
                <td><strong>$${Number(app.amount || 0).toLocaleString()}</strong></td>
                <td>${escapeHtml(app.reason || 'Work order validation')}</td>
                <td>
                    <button class="btn btn-success btn-sm" onclick="decideApproval(${app.id}, 'APPROVED')">Approve</button>
                    <button class="btn btn-danger btn-sm" onclick="decideApproval(${app.id}, 'REJECTED')" style="margin-left: 6px;">Reject</button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; padding: 24px; color: #ef4444;">Error: ${err.message}</td></tr>`;
    }
}

async function decideApproval(id, status) {
    const comments = prompt(`Enter optional remarks for ${status.toLowerCase()} this approval:`);
    try {
        await API.request(`/api/erp/approvals/${id}/decide`, {
            method: 'PUT',
            body: JSON.stringify({ status: status, comments: comments || '' })
        });
        showToast(`Approval marked as ${status}`, 'success');
        loadApprovalsTable();
        loadDashboard();
    } catch (err) {
        showToast('Decision failed: ' + err.message, 'error');
    }
}

/* ==========================================================================
   User & RBAC Management Module
   ========================================================================== */

async function loadUsersTable() {
    const tbody = document.getElementById('erp-users-tbody');
    if (!tbody) return;

    tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; padding: 24px; color: var(--text-muted);">Loading users directory...</td></tr>';

    try {
        allUsers = await API.request('/api/erp/users');
        if (!allUsers || allUsers.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; padding: 32px; color: var(--text-muted);">No users found.</td></tr>';
            return;
        }

        tbody.innerHTML = '';
        allUsers.forEach(u => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><strong>#${u.id}</strong></td>
                <td>
                    <div style="font-weight: 600;">${escapeHtml(u.name)}</div>
                    <div style="font-size: 0.8rem; color: var(--text-muted);">${escapeHtml(u.email)}</div>
                </td>
                <td>
                    <select onchange="updateUserRole(${u.id}, this.value)" style="padding: 4px 8px; font-size: 0.8rem; border-radius: var(--radius-sm); border: 1px solid var(--border-subtle); background: var(--bg-surface); color: var(--text-primary);">
                        <option value="SUPER_ADMIN" ${u.role === 'SUPER_ADMIN' ? 'selected' : ''}>SUPER_ADMIN</option>
                        <option value="ADMIN" ${u.role === 'ADMIN' ? 'selected' : ''}>ADMIN</option>
                        <option value="DEPT_MANAGER" ${u.role === 'DEPT_MANAGER' ? 'selected' : ''}>DEPT_MANAGER</option>
                        <option value="EMPLOYEE" ${u.role === 'EMPLOYEE' ? 'selected' : ''}>EMPLOYEE</option>
                        <option value="COMPLAINT_REPORTER" ${u.role === 'COMPLAINT_REPORTER' || u.role === 'STUDENT' ? 'selected' : ''}>COMPLAINT_REPORTER</option>
                    </select>
                </td>
                <td>${escapeHtml(u.department || 'Campus Admin')}</td>
                <td><span class="status-pill resolved">ACTIVE</span></td>
                <td style="font-size: 0.8rem; color: var(--text-muted);">${formatDate(u.createdAt)}</td>
            `;
            tbody.appendChild(tr);
        });
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; padding: 24px; color: #ef4444;">Error: ${err.message}</td></tr>`;
    }
}

async function updateUserRole(userId, newRole) {
    try {
        await API.request(`/api/erp/users/${userId}/role`, {
            method: 'PUT',
            body: JSON.stringify({ role: newRole })
        });
        showToast('User RBAC role successfully updated', 'success');
    } catch (err) {
        showToast('Failed to update role: ' + err.message, 'error');
        loadUsersTable();
    }
}

async function handleCreateUser(e) {
    e.preventDefault();
    const payload = {
        name: document.getElementById('user-name').value.trim(),
        email: document.getElementById('user-email').value.trim(),
        password: document.getElementById('user-password').value,
        role: document.getElementById('user-role').value,
        department: document.getElementById('user-dept').value.trim()
    };

    try {
        await API.request('/api/erp/users', {
            method: 'POST',
            body: JSON.stringify(payload)
        });
        showToast('User created with assigned role', 'success');
        document.getElementById('modal-add-user').classList.remove('active');
        document.getElementById('form-add-user').reset();
        loadUsersTable();
    } catch (err) {
        showToast('Failed to create user: ' + err.message, 'error');
    }
}

/* ==========================================================================
   Notifications Center
   ========================================================================== */

async function loadNotifications() {
    const listContainer = document.getElementById('notif-items-list');
    const badge = document.getElementById('topbar-notif-badge');
    if (!listContainer) return;

    try {
        const notifs = await API.request('/api/erp/notifications');
        const countRes = await API.request('/api/erp/notifications/unread-count');
        const unreadCount = countRes ? countRes.count : 0;

        if (badge) {
            if (unreadCount > 0) {
                badge.innerText = unreadCount;
                badge.style.display = 'flex';
            } else {
                badge.style.display = 'none';
            }
        }

        if (!notifs || notifs.length === 0) {
            listContainer.innerHTML = '<div style="padding: 24px; text-align: center; color: var(--text-muted); font-size: 0.82rem;">No notifications right now.</div>';
            return;
        }

        listContainer.innerHTML = '';
        notifs.slice(0, 15).forEach(n => {
            const item = document.createElement('div');
            item.className = `notif-item ${!n.read ? 'unread' : ''}`;
            item.onclick = async () => {
                if (!n.read) {
                    await API.request(`/api/erp/notifications/${n.id}/read`, { method: 'PUT' });
                    item.classList.remove('unread');
                    loadNotifications();
                }
                if (n.referenceId) {
                    switchModule('complaints');
                    openComplaintDetailDrawer(n.referenceId);
                }
            };

            item.innerHTML = `
                <div class="notif-title">${escapeHtml(n.title)}</div>
                <div class="notif-msg">${escapeHtml(n.message)}</div>
                <div class="notif-time">${formatDate(n.createdAt)}</div>
            `;
            listContainer.appendChild(item);
        });
    } catch (err) {
        console.error('Failed to load notifications:', err);
    }
}

async function markAllNotificationsRead() {
    try {
        await API.request('/api/erp/notifications/read-all', { method: 'PUT' });
        showToast('All notifications marked as read', 'success');
        loadNotifications();
    } catch (err) {
        showToast('Failed: ' + err.message, 'error');
    }
}

/* ==========================================================================
   Audit Logs & System Settings Module
   ========================================================================== */

async function loadAuditLogsTable() {
    const tbody = document.getElementById('erp-audit-tbody');
    if (!tbody) return;

    tbody.innerHTML = '<tr><td colspan="5" style="text-align: center; padding: 24px; color: var(--text-muted);">Loading audit logs...</td></tr>';

    try {
        const logs = await API.request('/api/erp/audit-logs');
        if (!logs || logs.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" style="text-align: center; padding: 32px; color: var(--text-muted);">No system audit entries found.</td></tr>';
            return;
        }

        tbody.innerHTML = '';
        logs.forEach(l => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><strong>#${l.id}</strong></td>
                <td><strong style="color: var(--brand-navy);">${escapeHtml(l.action)}</strong></td>
                <td>${escapeHtml(l.user ? l.user.name : 'System')}</td>
                <td style="font-size: 0.82rem; color: var(--text-secondary);">${escapeHtml(l.details || '')}</td>
                <td style="font-size: 0.8rem; color: var(--text-muted);">${formatDate(l.timestamp)}</td>
            `;
            tbody.appendChild(tr);
        });
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; padding: 24px; color: #ef4444;">Error: ${err.message}</td></tr>`;
    }
}

/* ==========================================================================
   Modals & Global Search
   ========================================================================== */

function setupModals() {
    // Backdrop close handlers
    document.querySelectorAll('.modal-backdrop').forEach(modal => {
        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                modal.classList.remove('active');
            }
        });
    });

    // Close button handlers
    document.querySelectorAll('.modal-close-btn, .btn-modal-cancel').forEach(btn => {
        btn.addEventListener('click', () => {
            const modal = btn.closest('.modal-backdrop');
            if (modal) modal.classList.remove('active');
        });
    });

    // Form Submissions
    document.getElementById('form-add-employee')?.addEventListener('submit', handleRegisterEmployee);
    document.getElementById('form-edit-employee')?.addEventListener('submit', handleUpdateEmployee);
    document.getElementById('form-add-dept')?.addEventListener('submit', handleCreateDepartment);
    document.getElementById('form-edit-dept')?.addEventListener('submit', handleUpdateDepartment);
    document.getElementById('form-assign-employee')?.addEventListener('submit', handleSaveAssignment);
    document.getElementById('form-detail-comment')?.addEventListener('submit', handleAddComment);
    document.getElementById('form-add-user')?.addEventListener('submit', handleCreateUser);

    // Filter listeners
    document.getElementById('filter-comp-category')?.addEventListener('change', applyComplaintFilters);
    document.getElementById('filter-comp-status')?.addEventListener('change', applyComplaintFilters);
    document.getElementById('filter-comp-search')?.addEventListener('input', applyComplaintFilters);
}

function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) modal.classList.add('active');
}

function setupGlobalSearch() {
    const input = document.getElementById('global-search-input');
    if (!input) return;

    window.addEventListener('keydown', (e) => {
        if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
            e.preventDefault();
            input.focus();
        }
    });

    input.addEventListener('input', (e) => {
        const val = e.target.value.toLowerCase().trim();
        if (val.length > 1) {
            switchModule('complaints');
            const searchField = document.getElementById('filter-comp-search');
            if (searchField) {
                searchField.value = val;
                applyComplaintFilters();
            }
        }
    });
}
