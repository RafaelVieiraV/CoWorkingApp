// --- Helpers de UI ---

function showMsg(id, text) {
    const el = document.getElementById(id);
    el.textContent = text;
    el.style.display = 'block';
}

function hideMsg(...ids) {
    ids.forEach(id => document.getElementById(id).style.display = 'none');
}

function toggleSection(section) {
    document.getElementById('loginSection').classList.toggle('hidden', section !== 'login');
    document.getElementById('registerSection').classList.toggle('hidden', section !== 'register');
}

// --- Llamadas al API ---

async function doLogin() {
    hideMsg('loginError', 'loginSuccess');

    const email = document.getElementById('loginEmail').value;
    const password = document.getElementById('loginPassword').value;

    try {
        const res = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });

        const data = await res.json();

        if (!res.ok) {
            showMsg('loginError', data.message || 'Error al iniciar sesión');
            return;
        }

        document.cookie = `jwt=${data.token}; path=/; max-age=86400`;
        if (data.email) {
            localStorage.setItem('userEmail', data.email);
        }
        showMsg('loginSuccess', '¡Bienvenido! Redirigiendo...');
        setTimeout(() => window.location.href = '/dashboard', 1500);
    } catch {
        showMsg('loginError', 'Error de conexión');
    }
}

async function doRegister() {
    hideMsg('registerError', 'registerSuccess');

    const email = document.getElementById('regEmail').value;
    const password = document.getElementById('regPassword').value;
    const confirm = document.getElementById('regConfirm').value;

    if (password !== confirm) {
        showMsg('registerError', 'Las contraseñas no coinciden');
        return;
    }

    try {
        const res = await fetch('/api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });

        const data = await res.json();

        if (!res.ok) {
            showMsg('registerError', data.message || 'Error al registrar');
            return;
        }

        showMsg('registerSuccess', '¡Cuenta creada! Ahora inicia sesión.');
        setTimeout(() => toggleSection('login'), 2000);
    } catch {
        showMsg('registerError', 'Error de conexión');
    }
}