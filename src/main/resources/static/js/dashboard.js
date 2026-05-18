// ── Helpers ──

function getToken() {
    var match = document.cookie.match(/(?:^|; )jwt=([^;]*)/);
    return match ? match[1] : '';
}

function headers() {
    return {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + getToken()
    };
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

function isToday(dateStr) {
    if (!dateStr) return false;
    var d = new Date(dateStr);
    var today = new Date();
    return d.getFullYear() === today.getFullYear()
        && d.getMonth() === today.getMonth()
        && d.getDate() === today.getDate();
}

function formatTime(dateStr) {
    if (!dateStr) return '-';
    var d = new Date(dateStr);
    return d.toLocaleTimeString('es-EC', { hour: '2-digit', minute: '2-digit' });
}

function statusBadge(status) {
    var map = {
        PENDIENTE:  { label: 'Pendiente',  css: 'badge-pending' },
        CONFIRMADA: { label: 'Confirmada', css: 'badge-confirmed' },
        CANCELADA:  { label: 'Cancelada',  css: 'badge-cancelled' }
    };
    var s = map[status] || { label: status, css: '' };
    return '<span class="badge ' + s.css + '">' + s.label + '</span>';
}

// ── Logout ──

function logout() {
    document.cookie = 'jwt=; path=/; max-age=0';
    window.location.href = '/login';
}

// ── Load stats ──

async function loadStats() {
    try {
        var [membersRes, workspacesRes, allWorkspacesRes, bookingsRes, pendingRes] = await Promise.all([
            fetch('/api/members/active', { headers: headers() }),
            fetch('/api/workspaces/available', { headers: headers() }),
            fetch('/api/workspaces', { headers: headers() }),
            fetch('/api/bookings', { headers: headers() }),
            fetch('/api/bookings/status/PENDIENTE', { headers: headers() })
        ]);

        // Miembros activos
        if (membersRes.ok) {
            var members = await membersRes.json();
            document.getElementById('statMembers').textContent = members.length;
        }

        // Espacios disponibles
        var availableCount = 0;
        var totalWorkspaces = 0;
        if (workspacesRes.ok) {
            var available = await workspacesRes.json();
            availableCount = available.length;
            document.getElementById('statWorkspaces').textContent = availableCount;
        }

        // Total espacios activos (disponibles) para ocupación
        if (allWorkspacesRes.ok) {
            var allWs = await allWorkspacesRes.json();
            totalWorkspaces = allWs.filter(function (w) { return w.available; }).length;
        }

        // Reservas de hoy y tabla
        if (bookingsRes.ok) {
            var bookings = await bookingsRes.json();
            var todayBookings = bookings.filter(function (b) {
                return isToday(b.startDatetime) && b.status !== 'CANCELADA';
            });
            document.getElementById('statBookingsToday').textContent = todayBookings.length;
            renderTodayBookings(todayBookings);
            renderRecentActivity(bookings.slice(-5).reverse());

            // Ocupación = espacios únicos con reserva activa hoy
            var usedWorkspaces = [];
            todayBookings.forEach(function (b) {
                if (b.workspaceId && usedWorkspaces.indexOf(b.workspaceId) === -1) {
                    usedWorkspaces.push(b.workspaceId);
                }
            });
            var occupied = usedWorkspaces.length;
            var percent = totalWorkspaces > 0 ? Math.round((occupied / totalWorkspaces) * 100) : 0;
            document.getElementById('occupancyFill').style.width = percent + '%';
            document.getElementById('occupancyText').textContent = occupied + ' / ' + totalWorkspaces + ' en uso hoy';
            document.getElementById('occupancyPercent').textContent = percent + '%';
        }

        // Pendientes
        if (pendingRes.ok) {
            var pending = await pendingRes.json();
            document.getElementById('statPending').textContent = pending.length;
        }

    } catch (e) {
        console.error('Error cargando dashboard:', e);
    }
}

// ── Render today's bookings ──

function renderTodayBookings(bookings) {
    var tbody = document.getElementById('todayBookings');

    if (!bookings || bookings.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" class="empty-mini">Sin reservas para hoy</td></tr>';
        return;
    }

    tbody.innerHTML = bookings.map(function (b) {
        return '<tr>' +
            '<td>' + (b.memberFullName || '-') + '</td>' +
            '<td>' + (b.workspaceName || '-') + '</td>' +
            '<td>' + formatTime(b.startDatetime) + ' - ' + formatTime(b.endDatetime) + '</td>' +
            '<td>' + statusBadge(b.status) + '</td>' +
            '</tr>';
    }).join('');
}

// ── Render recent activity ──

function renderRecentActivity(bookings) {
    var container = document.getElementById('recentActivity');

    if (!bookings || bookings.length === 0) {
        container.innerHTML =
            '<div class="activity-item">' +
            '<div class="activity-dot dot-green"></div>' +
            '<div class="activity-text"><span class="activity-desc">Sin actividad reciente</span></div>' +
            '</div>';
        return;
    }

    var dotMap = {
        PENDIENTE: 'dot-orange',
        CONFIRMADA: 'dot-green',
        CANCELADA: 'dot-red'
    };

    var labelMap = {
        PENDIENTE: 'Nueva reserva',
        CONFIRMADA: 'Reserva confirmada',
        CANCELADA: 'Reserva cancelada'
    };

    container.innerHTML = bookings.map(function (b) {
        var dot = dotMap[b.status] || 'dot-blue';
        var label = labelMap[b.status] || 'Reserva';
        var time = formatTime(b.createdAt || b.startDatetime);

        return '<div class="activity-item">' +
            '<div class="activity-dot ' + dot + '"></div>' +
            '<div class="activity-text">' +
            '<span class="activity-desc">' + label + ' — ' + (b.memberFullName || 'Miembro') + '</span><br>' +
            '<span class="activity-time">' + time + '</span>' +
            '</div>' +
            '</div>';
    }).join('');
}

// ── Init ──

document.addEventListener('DOMContentLoaded', function () {
    var email = getEmailFromToken();
    if (email) {
        document.getElementById('adminEmail').textContent = email;
    }
    loadStats();
});