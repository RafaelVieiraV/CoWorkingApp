// ── State ──

var currentPage = 0;
var totalPages = 0;

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

function planLabel(plan) {
    var map = { BASIC: 'Básico', PREMIUM: 'Premium', ENTERPRISE: 'Empresarial' };
    return map[plan] || plan || '-';
}

function activeBadge(active) {
    if (active) return '<span class="badge badge-confirmed">Activo</span>';
    return '<span class="badge badge-cancelled">Inactivo</span>';
}

function showError(msg) {
    var el = document.getElementById('modalError');
    el.textContent = msg;
    el.style.display = 'block';
}

function hideError() {
    document.getElementById('modalError').style.display = 'none';
}

// ── Render ──

function renderTable(members) {
    var tbody = document.getElementById('membersBody');

    if (!members || members.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" class="empty-state">No hay miembros registrados</td></tr>';
        return;
    }

    tbody.innerHTML = members.map(function (m) {
        var safeM = JSON.stringify(m).replace(/'/g, '&#39;').replace(/"/g, '&quot;');

        var actions =
            '<button class="btn-action edit" title="Editar" onclick=\'openEditModal(' + safeM + ')\'>' +
            '<i class="bi bi-pencil"></i>' +
            '</button>';

        if (m.active) {
            actions +=
                '<button class="btn-action cancel" title="Desactivar" onclick="deactivateMember(' + m.id + ')">' +
                '<i class="bi bi-person-slash"></i>' +
                '</button>';
        }

        actions +=
            '<button class="btn-action delete" title="Eliminar" onclick="openDeleteModal(' + m.id + ')">' +
            '<i class="bi bi-trash"></i>' +
            '</button>';

        return '<tr>' +
            '<td>' + m.id + '</td>' +
            '<td>' + (m.fullName || '-') + '</td>' +
            '<td>' + (m.email || '-') + '</td>' +
            '<td>' + (m.phone || '-') + '</td>' +
            '<td>' + planLabel(m.planType) + '</td>' +
            '<td>' + (m.monthlyHoursQuota || '-') + '</td>' +
            '<td>' + (m.usedHoursThisMonth != null ? m.usedHoursThisMonth : '-') + '</td>' +
            '<td>' + activeBadge(m.active) + '</td>' +
            '<td><div class="actions-cell">' + actions + '</div></td>' +
            '</tr>';
    }).join('');
}

function renderPagination() {
    var container = document.getElementById('pagination');
    if (totalPages <= 1) { container.innerHTML = ''; return; }

    var html = '';
    for (var i = 0; i < totalPages; i++) {
        html += '<button class="page-btn ' + (i === currentPage ? 'active' : '') + '" onclick="goToPage(' + i + ')">' + (i + 1) + '</button>';
    }
    container.innerHTML = html;
}

// ── Load ──

async function loadMembers(page) {
    currentPage = page || 0;
    try {
        var res = await fetch('/api/members/search?page=' + currentPage + '&size=10', { headers: headers() });
        if (!res.ok) throw new Error();
        var data = await res.json();
        totalPages = data.totalPages;
        renderTable(data.content);
        renderPagination();
    } catch (e) {
        document.getElementById('membersBody').innerHTML =
            '<tr><td colspan="9" class="empty-state">Error al cargar miembros</td></tr>';
    }
}

// ── Filters ──

async function applyFilters() {
    var name = document.getElementById('searchName').value.trim();
    var activeFilter = document.getElementById('filterActive').value;

    if (activeFilter === 'active') {
        try {
            var res = await fetch('/api/members/active', { headers: headers() });
            if (!res.ok) throw new Error();
            var data = await res.json();
            totalPages = 1;
            currentPage = 0;
            renderTable(data);
            document.getElementById('pagination').innerHTML = '';
        } catch (e) {
            renderTable([]);
        }
        return;
    }

    if (name) {
        try {
            var res = await fetch('/api/members/search?name=' + encodeURIComponent(name) + '&page=0&size=10', { headers: headers() });
            if (!res.ok) throw new Error();
            var data = await res.json();
            totalPages = data.totalPages;
            currentPage = 0;
            renderTable(data.content);
            renderPagination();
        } catch (e) {
            renderTable([]);
        }
        return;
    }

    loadMembers(0);
}

function clearFilters() {
    document.getElementById('searchName').value = '';
    document.getElementById('filterActive').value = '';
    loadMembers(0);
}

function goToPage(page) {
    loadMembers(page);
}

// ── Modal ──

function openCreateModal() {
    document.getElementById('modalTitle').textContent = 'Nuevo Miembro';
    document.getElementById('editingId').value = '';
    document.getElementById('formFullName').value = '';
    document.getElementById('formEmail').value = '';
    document.getElementById('formPhone').value = '';
    document.getElementById('formPlanType').value = '';
    document.getElementById('formQuota').value = '';
    hideError();
    document.getElementById('memberModal').classList.remove('hidden');
}

function openEditModal(m) {
    document.getElementById('modalTitle').textContent = 'Editar Miembro #' + m.id;
    document.getElementById('editingId').value = m.id;
    document.getElementById('formFullName').value = m.fullName || '';
    document.getElementById('formEmail').value = m.email || '';
    document.getElementById('formPhone').value = m.phone || '';
    document.getElementById('formPlanType').value = m.planType || '';
    document.getElementById('formQuota').value = m.monthlyHoursQuota || '';
    hideError();
    document.getElementById('memberModal').classList.remove('hidden');
}

function closeModal() {
    document.getElementById('memberModal').classList.add('hidden');
}

async function saveMember() {
    hideError();

    var id = document.getElementById('editingId').value;
    var body = {
        fullName:          document.getElementById('formFullName').value,
        email:             document.getElementById('formEmail').value,
        phone:             document.getElementById('formPhone').value || null,
        planType:          document.getElementById('formPlanType').value,
        monthlyHoursQuota: parseInt(document.getElementById('formQuota').value)
    };

    if (!body.fullName || !body.email || !body.planType || !body.monthlyHoursQuota) {
        showError('Complete todos los campos obligatorios');
        return;
    }

    try {
        var url = id ? '/api/members/' + id : '/api/members';
        var method = id ? 'PUT' : 'POST';

        var res = await fetch(url, {
            method: method,
            headers: headers(),
            body: JSON.stringify(body)
        });

        var data = await res.json();

        if (!res.ok) {
            showError(data.message || data.error || 'Error al guardar');
            return;
        }

        closeModal();
        loadMembers(currentPage);
    } catch (e) {
        showError('Error de conexión');
    }
}

// ── Deactivate ──

async function deactivateMember(id) {
    try {
        var res = await fetch('/api/members/' + id + '/deactivate', {
            method: 'PATCH',
            headers: headers()
        });
        if (res.ok || res.status === 204) loadMembers(currentPage);
    } catch (e) { /* silenciar */ }
}

// ── Delete ──

function openDeleteModal(id) {
    document.getElementById('deleteId').value = id;
    document.getElementById('deleteModal').classList.remove('hidden');
}

function closeDeleteModal() {
    document.getElementById('deleteModal').classList.add('hidden');
}

async function confirmDelete() {
    var id = document.getElementById('deleteId').value;
    try {
        var res = await fetch('/api/members/' + id, {
            method: 'DELETE',
            headers: headers()
        });
        if (res.ok || res.status === 204) {
            closeDeleteModal();
            loadMembers(currentPage);
        }
    } catch (e) { /* silenciar */ }
}

// ── Init ──

document.addEventListener('DOMContentLoaded', function () {
    loadMembers(0);
});