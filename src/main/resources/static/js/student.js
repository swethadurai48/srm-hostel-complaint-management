/**
 * SRM Hostel Complaint Management System - Resident Portal Scripts
 * Recreated to match Visual Design Reference
 */

let currentUser = null;
let allComplaints = [];

document.addEventListener('DOMContentLoaded', async () => {
    // 1. Verify Authentication & Role
    try {
        currentUser = await API.getMe();
        if (!currentUser || currentUser.role !== 'STUDENT') {
            window.location.href = '/index.html';
            return;
        }

        // Header user info
        const headerName = document.getElementById('header-user-name');
        if (headerName) headerName.innerText = currentUser.name.split(' ')[0] || 'Student';

        const avatarInitials = document.getElementById('header-avatar-initials');
        if (avatarInitials && currentUser.name) {
            const parts = currentUser.name.trim().split(' ');
            avatarInitials.innerText = parts.length > 1 ? (parts[0][0] + parts[1][0]).toUpperCase() : parts[0].slice(0, 2).toUpperCase();
        }

        // Dashboard welcome & profile
        const welcomeName = document.getElementById('dash-welcome-name');
        if (welcomeName) welcomeName.innerText = currentUser.name.split(' ')[0] || 'Student';

        populateProfile(currentUser);

        // Prefill hostel location if available
        if (currentUser.roomOrHostel) {
            const locInput = document.getElementById('comp-location');
            if (locInput) locInput.value = currentUser.roomOrHostel;
        }
    } catch {
        window.location.href = '/index.html';
        return;
    }

    // 2. Setup Sidebar Navigation Listeners
    setupSidebarNavigation();

    // 3. Setup Action Listeners
    const logoutBtn = document.getElementById('btn-logout');
    if (logoutBtn) logoutBtn.addEventListener('click', () => API.logout());

    const submitForm = document.getElementById('form-submit-complaint');
    if (submitForm) submitForm.addEventListener('submit', handleSubmitComplaint);

    // Modal Close Listeners
    const modal = document.getElementById('complaint-detail-modal');
    const closeBtn = document.getElementById('btn-close-modal');
    if (closeBtn && modal) {
        closeBtn.addEventListener('click', () => modal.classList.remove('active'));
        modal.addEventListener('click', (e) => {
            if (e.target === modal) modal.classList.remove('active');
        });
    }

    // 4. Initial Load
    await loadStudentData();
});

function setupSidebarNavigation() {
    const sidebarItems = document.querySelectorAll('.sidebar-item[data-target]');
    sidebarItems.forEach(item => {
        item.addEventListener('click', () => {
            const target = item.getAttribute('data-target');
            switchTab(target);
        });
    });
}

function switchTab(targetSectionId) {
    // Update sidebar items
    document.querySelectorAll('.sidebar-item[data-target]').forEach(item => {
        if (item.getAttribute('data-target') === targetSectionId) {
            item.classList.add('active');
        } else {
            item.classList.remove('active');
        }
    });

    // Update sections
    document.querySelectorAll('.portal-section').forEach(sec => {
        if (sec.id === targetSectionId) {
            sec.classList.add('active');
        } else {
            sec.classList.remove('active');
        }
    });

    window.scrollTo({ top: 0, behavior: 'smooth' });
}

function openQuickComplaint(categoryKey) {
    switchTab('sec-submit');
    const catSelect = document.getElementById('comp-category');
    if (catSelect) {
        catSelect.value = categoryKey;
    }
}

async function loadStudentData() {
    await Promise.all([loadStats(), loadComplaints(), loadNotices()]);
}

async function loadNotices() {
    try {
        const notices = await API.getNotices();
        if (!notices || notices.length === 0) return;

        // Populate Dashboard Important Notices
        const dashNoticesList = document.querySelector('.dash-card .notices-list');
        if (dashNoticesList) {
            dashNoticesList.innerHTML = '';
            notices.slice(0, 5).forEach(n => {
                const row = document.createElement('div');
                row.className = 'notice-row';
                row.onclick = () => switchTab('sec-notices');
                row.innerHTML = `
                    <div class="notice-row-left">
                        <span class="notice-row-date">${formatSimpleDate(n.publishedAt)}</span>
                        <span class="notice-row-title">${escapeHtml(n.title)}</span>
                    </div>
                    <span class="notice-row-arrow">&rsaquo;</span>
                `;
                dashNoticesList.appendChild(row);
            });
        }

        // Populate Full Notices Section
        const fullNoticesContainer = document.querySelector('#sec-notices > div:last-child');
        if (fullNoticesContainer) {
            fullNoticesContainer.innerHTML = '';
            notices.forEach(n => {
                const card = document.createElement('div');
                card.className = 'card';
                card.innerHTML = `
                    <div style="display: flex; justify-content: space-between; margin-bottom: 6px;">
                        <span class="category-badge">${escapeHtml(n.category || 'General')}</span>
                        <span style="font-size: 0.8rem; color: var(--text-muted);">${formatSimpleDate(n.publishedAt)}</span>
                    </div>
                    <h3 style="font-size: 1.1rem; color: var(--text-heading); margin-bottom: 6px;">${escapeHtml(n.title)}</h3>
                    <p style="font-size: 0.885rem; color: var(--text-muted); line-height: 1.5;">${escapeHtml(n.content)}</p>
                `;
                fullNoticesContainer.appendChild(card);
            });
        }
    } catch (err) {
        console.error('Error loading notices:', err);
    }
}

async function loadStats() {
    try {
        const stats = await API.getMyStats();
        if (stats) {
            const totalEl = document.getElementById('stat-total');
            const resolvedEl = document.getElementById('stat-resolved');
            const progressEl = document.getElementById('stat-progress');
            const pendingEl = document.getElementById('stat-pending');

            if (totalEl) totalEl.innerText = stats.total ?? 0;
            if (resolvedEl) resolvedEl.innerText = stats.resolved ?? 0;
            if (progressEl) progressEl.innerText = stats.inProgress ?? 0;
            if (pendingEl) pendingEl.innerText = stats.pending ?? 0;
        }
    } catch (err) {
        console.error('Error fetching stats:', err);
    }
}

async function loadComplaints() {
    const dashTableBody = document.getElementById('dash-recent-table-body');
    const myTableBody = document.getElementById('my-complaints-table-body');

    try {
        allComplaints = await API.getMyComplaints();

        // 1. Render Dashboard Recent Complaints (up to 5 rows matching reference)
        if (dashTableBody) {
            dashTableBody.innerHTML = '';
            if (!allComplaints || allComplaints.length === 0) {
                dashTableBody.innerHTML = '<tr><td colspan="5" style="text-align: center; color: var(--text-muted); padding: 24px;">No complaints submitted yet. Click "Submit Complaint" to report an issue.</td></tr>';
            } else {
                const recentList = allComplaints.slice(0, 5);
                recentList.forEach((c, idx) => {
                    const tr = document.createElement('tr');
                    tr.onclick = () => openDetailModal(c.id);
                    tr.innerHTML = `
                        <td style="font-weight: 600; color: var(--text-muted);">${idx + 1}</td>
                        <td><strong style="color: var(--srm-navy);">${getCategoryLabel(c.category)}</strong></td>
                        <td style="max-width: 280px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;" title="${escapeHtml(c.description)}">${escapeHtml(c.title || c.description)}</td>
                        <td>${getStatusPillHtml(c.status)}</td>
                        <td style="color: var(--text-muted); font-size: 0.82rem;">${formatSimpleDate(c.createdAt)}</td>
                    `;
                    dashTableBody.appendChild(tr);
                });
            }
        }

        // 2. Render My Complaints Table (Full list)
        if (myTableBody) {
            myTableBody.innerHTML = '';
            if (!allComplaints || allComplaints.length === 0) {
                myTableBody.innerHTML = '<tr><td colspan="6" style="text-align: center; color: var(--text-muted); padding: 32px;">No complaints recorded yet.</td></tr>';
            } else {
                allComplaints.forEach((c, idx) => {
                    const tr = document.createElement('tr');
                    tr.onclick = () => openDetailModal(c.id);
                    tr.innerHTML = `
                        <td style="font-weight: 600; color: var(--text-muted);">${idx + 1}</td>
                        <td><strong style="color: var(--srm-navy);">${getCategoryLabel(c.category)}</strong></td>
                        <td>
                            <div style="font-weight: 600; color: var(--text-main); margin-bottom: 2px;">${escapeHtml(c.title)}</div>
                            <div style="font-size: 0.82rem; color: var(--text-muted); max-width: 320px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">${escapeHtml(c.description)}</div>
                        </td>
                        <td style="font-size: 0.85rem; color: var(--text-muted);">📍 ${escapeHtml(c.location || 'Hostel Room')}</td>
                        <td>${getStatusPillHtml(c.status)}</td>
                        <td style="color: var(--text-muted); font-size: 0.82rem;">${formatSimpleDate(c.createdAt)}</td>
                    `;
                    myTableBody.appendChild(tr);
                });
            }
        }
    } catch (err) {
        console.error('Error loading complaints:', err);
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

async function handleSubmitComplaint(e) {
    e.preventDefault();
    const form = e.target;
    const submitBtn = form.querySelector('button[type="submit"]');
    const originalHtml = submitBtn.innerHTML;
    submitBtn.innerText = 'Submitting...';
    submitBtn.disabled = true;

    const payload = {
        title: document.getElementById('comp-title').value,
        category: document.getElementById('comp-category').value,
        location: document.getElementById('comp-location').value,
        description: document.getElementById('comp-desc').value
    };

    try {
        await API.submitComplaint(payload);
        showToast('Complaint submitted successfully to Hostel Administration!', 'success');
        form.reset();
        if (currentUser && currentUser.roomOrHostel) {
            document.getElementById('comp-location').value = currentUser.roomOrHostel;
        }
        await loadStudentData();
        switchTab('sec-complaints');
    } catch (err) {
        showToast(err.message, 'error');
    } finally {
        submitBtn.innerHTML = originalHtml;
        submitBtn.disabled = false;
    }
}

async function openDetailModal(id) {
    const modal = document.getElementById('complaint-detail-modal');
    try {
        const c = await API.getComplaint(id);
        if (!c) return;

        document.getElementById('modal-detail-title').innerText = c.title;
        document.getElementById('modal-detail-category').innerText = getCategoryLabel(c.category);
        document.getElementById('modal-detail-status').innerHTML = getStatusPillHtml(c.status);
        document.getElementById('modal-detail-location').innerText = c.location || 'Hostel Room';
        document.getElementById('modal-detail-created').innerText = formatDate(c.createdAt);
        document.getElementById('modal-detail-desc').innerText = c.description;

        const staffRow = document.getElementById('modal-detail-staff-row');
        const staffEl = document.getElementById('modal-detail-staff');
        if (c.assignedStaff && c.assignedStaff.trim().length > 0) {
            if (staffRow) staffRow.style.display = 'block';
            if (staffEl) staffEl.innerText = c.assignedStaff;
        } else if (staffRow) {
            staffRow.style.display = 'none';
        }

        const responseBox = document.getElementById('modal-admin-response-box');
        const responseText = document.getElementById('modal-detail-response-text');

        if (c.adminResponse && c.adminResponse.trim().length > 0) {
            responseBox.style.display = 'block';
            responseText.innerText = c.adminResponse;
        } else {
            responseBox.style.display = 'none';
        }

        modal.classList.add('active');
    } catch (err) {
        showToast('Failed to load complaint details: ' + err.message, 'error');
    }
}

function populateProfile(user) {
    if (!user) return;
    const nameEl = document.getElementById('profile-name');
    const idEl = document.getElementById('profile-student-id');
    const deptEl = document.getElementById('profile-dept');
    const roomEl = document.getElementById('profile-room');
    const emailEl = document.getElementById('profile-email');
    const initialsEl = document.getElementById('profile-initials');

    if (nameEl) nameEl.innerText = user.name;
    if (idEl) idEl.innerText = user.studentId || 'N/A';
    if (deptEl) deptEl.innerText = user.department || 'SRM Engineering';
    if (roomEl) roomEl.innerText = user.roomOrHostel || 'SRM Hostel';
    if (emailEl) emailEl.innerText = user.email;

    if (initialsEl && user.name) {
        const parts = user.name.trim().split(' ');
        initialsEl.innerText = parts.length > 1 ? (parts[0][0] + parts[1][0]).toUpperCase() : parts[0].slice(0, 2).toUpperCase();
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

function setRating(el, starCount) {
    const parent = el.parentElement;
    const stars = parent.querySelectorAll('span');
    stars.forEach((s, idx) => {
        if (idx < starCount) {
            s.classList.add('active');
        } else {
            s.classList.remove('active');
        }
    });
}

async function handleFeedbackSubmit(e) {
    e.preventDefault();
    const commentsEl = document.getElementById('feedback-comments');
    const message = commentsEl ? commentsEl.value.trim() : '';
    if (!message) {
        showToast('Please enter your feedback comments.', 'error');
        return;
    }

    const cleanlinessStars = document.querySelectorAll('.rating-stars[data-field="cleanliness"] .active').length || 4;
    const foodStars = document.querySelectorAll('.rating-stars[data-field="food"] .active').length || 3;
    const wifiStars = document.querySelectorAll('.rating-stars[data-field="wifi"] .active').length || 4;
    const avgRating = Math.max(1, Math.min(5, Math.round((cleanlinessStars + foodStars + wifiStars) / 3)));

    try {
        await API.submitFeedback({
            rating: avgRating,
            message: message
        });
        showToast('Thank you! Your feedback has been recorded by the Hostel Administration.', 'success');
        if (commentsEl) commentsEl.value = '';
    } catch (err) {
        showToast('Failed to record feedback: ' + err.message, 'error');
    }
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
