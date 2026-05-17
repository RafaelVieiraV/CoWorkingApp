// ── Helpers ──

function getToken() {
    var match = document.cookie.match(/(?:^|; )jwt=([^;]*)/);
    return match ? match[1] : '';
}

function getEmailFromToken() {
    var token = getToken();
    if (!token) return null;
    try {
        var payload = JSON.parse(atob(token.split('.')[1]));
        return payload.sub || null;
    } catch (e) {
        return null;
    }
}

// ── Logout ──

function logout() {
    document.cookie = 'jwt=; path=/; max-age=0';
    window.location.href = '/login';
}

// ── Init ──

document.addEventListener('DOMContentLoaded', function () {
    // Mostrar email del admin autenticado
    var email = getEmailFromToken();
    if (email) {
        document.getElementById('adminEmail').textContent = email;
    }
});