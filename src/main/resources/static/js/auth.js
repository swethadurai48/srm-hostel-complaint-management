/**
 * Authentication and Landing Page Scripts
 */

document.addEventListener('DOMContentLoaded', async () => {
    // Check if user is already logged in
    try {
        const user = await API.getMe();
        if (user) {
            if (user.role === 'SUPER_ADMIN' || user.role === 'DEPT_MANAGER' || user.role === 'EMPLOYEE') {
                window.location.href = '/erp.html';
                return;
            } else if (user.role === 'ADMIN') {
                window.location.href = '/admin.html';
                return;
            } else if (user.role === 'STUDENT' || user.role === 'COMPLAINT_REPORTER') {
                window.location.href = '/student.html';
                return;
            }
        }
    } catch {
        // Not logged in, stay on landing page
    }

    const loginTabBtn = document.getElementById('tab-login');
    const registerTabBtn = document.getElementById('tab-register');
    const loginForm = document.getElementById('form-login');
    const registerForm = document.getElementById('form-register');

    if (loginTabBtn && registerTabBtn) {
        loginTabBtn.addEventListener('click', () => {
            loginTabBtn.classList.add('active');
            registerTabBtn.classList.remove('active');
            loginForm.style.display = 'block';
            registerForm.style.display = 'none';
        });

        registerTabBtn.addEventListener('click', () => {
            registerTabBtn.classList.add('active');
            loginTabBtn.classList.remove('active');
            registerForm.style.display = 'block';
            loginForm.style.display = 'none';
        });
    }

    // Handle Login
    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const submitBtn = loginForm.querySelector('button[type="submit"]');
            const originalText = submitBtn.innerText;
            submitBtn.innerText = 'Signing In...';
            submitBtn.disabled = true;

            const email = document.getElementById('login-email').value;
            const password = document.getElementById('login-password').value;

            try {
                const user = await API.login(email, password);
                showToast(`Welcome back, ${user.name}!`, 'success');
                setTimeout(() => {
                    if (user.role === 'SUPER_ADMIN' || user.role === 'DEPT_MANAGER' || user.role === 'EMPLOYEE') {
                        window.location.href = '/erp.html';
                    } else if (user.role === 'ADMIN') {
                        window.location.href = '/admin.html';
                    } else {
                        window.location.href = '/student.html';
                    }
                }, 600);
            } catch (err) {
                showToast(err.message, 'error');
                submitBtn.innerText = originalText;
                submitBtn.disabled = false;
            }
        });
    }

    // Handle Register
    if (registerForm) {
        registerForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const submitBtn = registerForm.querySelector('button[type="submit"]');
            const originalText = submitBtn.innerText;
            submitBtn.innerText = 'Creating Account...';
            submitBtn.disabled = true;

            const data = {
                name: document.getElementById('reg-name').value,
                email: document.getElementById('reg-email').value,
                password: document.getElementById('reg-password').value,
                studentId: document.getElementById('reg-studentid').value,
                department: document.getElementById('reg-dept').value,
                roomOrHostel: document.getElementById('reg-room').value
            };

            try {
                const user = await API.register(data);
                showToast('Registration successful! Redirecting...', 'success');
                setTimeout(() => {
                    window.location.href = '/student.html';
                }, 800);
            } catch (err) {
                showToast(err.message, 'error');
                submitBtn.innerText = originalText;
                submitBtn.disabled = false;
            }
        });
    }
});
