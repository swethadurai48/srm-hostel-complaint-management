/**
 * Smart Complaint Management System - Core API Client & Utilities
 */

const API = {
    // Authentication
    async register(data) {
        return this.request('/api/auth/register', {
            method: 'POST',
            body: JSON.stringify(data)
        });
    },

    async login(email, password) {
        return this.request('/api/auth/login', {
            method: 'POST',
            body: JSON.stringify({ email, password })
        });
    },

    async logout() {
        try {
            await this.request('/api/auth/logout', { method: 'POST' });
        } finally {
            window.location.href = '/index.html';
        }
    },

    async getMe() {
        return this.request('/api/auth/me');
    },

    // Student Complaint APIs
    async submitComplaint(data) {
        return this.request('/api/complaints', {
            method: 'POST',
            body: JSON.stringify(data)
        });
    },

    async getMyComplaints() {
        return this.request('/api/complaints/my');
    },

    async getMyStats() {
        return this.request('/api/complaints/my/stats');
    },

    async getComplaint(id) {
        return this.request(`/api/complaints/${id}`);
    },

    // Admin Complaint APIs
    async getAdminComplaints(category = '', status = '') {
        const params = new URLSearchParams();
        if (category) params.append('category', category);
        if (status) params.append('status', status);
        const query = params.toString() ? `?${params.toString()}` : '';
        return this.request(`/api/admin/complaints${query}`);
    },

    async getAdminStats() {
        return this.request('/api/admin/complaints/stats');
    },

    async updateComplaintStatus(id, status, assignedStaff = '') {
        return this.request(`/api/admin/complaints/${id}/status`, {
            method: 'PUT',
            body: JSON.stringify({ status, assignedStaff })
        });
    },

    async updateAdminResponse(id, response) {
        return this.request(`/api/admin/complaints/${id}/response`, {
            method: 'PUT',
            body: JSON.stringify({ response })
        });
    },

    // Notices and Feedback APIs
    async getNotices() {
        return this.request('/api/notices');
    },

    async submitFeedback(data) {
        return this.request('/api/feedback', {
            method: 'POST',
            body: JSON.stringify(data)
        });
    },

    // Base Fetch Handler
    async request(url, options = {}) {
        const headers = {
            'Content-Type': 'application/json',
            ...(options.headers || {})
        };

        try {
            const res = await fetch(url, { ...options, headers });
            const json = await res.json().catch(() => null);

            if (!res.ok) {
                const message = (json && json.message) ? json.message : `Request failed with status ${res.status}`;
                throw new Error(message);
            }

            return json ? json.data : null;
        } catch (err) {
            console.error('API Error:', err);
            throw err;
        }
    }
};

// UI Toast Notification helper
function showToast(message, type = 'success') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `
        <span>${type === 'success' ? '✓' : '⚠'}</span>
        <span>${message}</span>
    `;

    container.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(10px)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

// Format Date helper
function formatDate(isoString) {
    if (!isoString) return 'N/A';
    const date = new Date(isoString);
    return date.toLocaleDateString(undefined, {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// Category Human Label helper for SRM Hostel
function getCategoryLabel(categoryKey) {
    const labels = {
        WATER: 'Water & Plumbing',
        ELECTRICITY: 'Electrical',
        WIFI: 'Internet / Wi-Fi',
        CLEANING: 'Housekeeping',
        MAINTENANCE: 'Room Maintenance',
        HOSTEL: 'Mess & Food',
        TRANSPORT: 'Security',
        CLASSROOM: 'Common Areas',
        OTHER: 'Other'
    };
    return labels[categoryKey] || categoryKey;
}

// Status Human Label helper
function getStatusBadgeHtml(statusKey) {
    const labels = {
        PENDING: 'Pending',
        IN_PROGRESS: 'In Progress',
        RESOLVED: 'Resolved'
    };
    const label = labels[statusKey] || statusKey;
    return `<span class="status-badge ${statusKey}">${label}</span>`;
}
