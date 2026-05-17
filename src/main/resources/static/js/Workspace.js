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

function typeLabel(type) {
    var map = { ESCRITORIO: 'Escritorio', SALA_PRIVADA: 'Sala Privada', SALA_REUNION: 'Sala de Reunión', CABINA: 'Cabina' };
    return map[type] || type || '-';
}

function availableBadge(available) {
    if (available) return '<span class="badge badge-confirmed">Disponible</span>';
    return '<span class="badge badge-cancelled">No disponible</span>';
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

function renderTable(workspaces) {
    var tbody = document.getElementById('workspacesBody');

    if (!workspaces || workspaces.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" class="empty-state">No hay espacios registrados</td></tr>';
        return;
    }

    tbody.innerHTML = workspaces.map(function (w) {
        var safeW = JSON.stringify(w).replace(/'/g, '&#39;').replace(/"/g, '&quot;');

        var actions =
            '<button class="btn-action edit" title="Editar" onclick=\'openEditModal(' + safeW + ')\'>' +
            '<i class="bi bi-pencil"></i>' +
            '</button>';

        if (w.available) {
            actions +=
                '<button class="btn-action cancel" title="Deshabilitar" onclick="disableWorkspace(' + w.id + ')">' +
                '<i class="bi bi-slash-circle"></i>' +
                '</button>';
        }

        actions +=
            '<button class="btn-action delete" title="Eliminar" onclick="openDeleteModal(' + w.id + ')">' +
            '<i class="bi bi-trash"></i>' +
            '</button>';

        return '<tr>' +
            '<td>' + w.id + '</td>' +
            '<td>' + (w.name || '-') + '</td>' +
            '<td>' + typeLabel(w.type) + '</td>' +
            '<td>' + (w.capacity || '-') + '</td>' +
            '<td>$' + (w.pricePerHour != null ? w.pricePerHour.toFixed(2) : '0.00') + '</td>' +
            '<td>' + (w.floor != null ? w.floor : '-') + '</td>' +
            '<td>' + availableBadge(w.available) + '</td>' +
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

async function loadWorkspaces(page) {
    currentPage = page || 0;
    try {
        var res = await fetch('/api/workspaces/search?page=' + currentPage + '&size=10', { headers: headers() });
        if (!res.ok) throw new Error();
        var data = await res.json();
        totalPages = data.totalPages;
        renderTable(data.content);
        renderPagination();
    } catch (e) {
        document.getElementById('workspacesBody').innerHTML =
            '<tr><td colspan="8" class="empty-state">Error al cargar espacios</td></tr>';
    }
}

// ── Filters ──

async function applyFilters() {
    var name = document.getElementById('searchName').value.trim();
    var type = document.getElementById('filterType').value;
    var availableFilter = document.getElementById('filterAvailable').value;

    if (availableFilter === 'available') {
        try {
            var res = await fetch('/api/workspaces/available', { headers: headers() });
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

    if (type) {
        try {
            var res = await fetch('/api/workspaces/type/' + type, { headers: headers() });
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
            var res = await fetch('/api/workspaces/search?name=' + encodeURIComponent(name) + '&page=0&size=10', { headers: headers() });
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

    loadWorkspaces(0);
}

function clearFilters() {
    document.getElementById('searchName').value = '';
    document.getElementById('filterType').value = '';
    document.getElementById('filterAvailable').value = '';
    loadWorkspaces(0);
}

function goToPage(page) {
    loadWorkspaces(page);
}

// ── Modal ──

function openCreateModal() {
    document.getElementById('modalTitle').textContent = 'Nuevo Espacio';
    document.getElementById('editingId').value = '';
    document.getElementById('formName').value = '';
    document.getElementById('formType').value = '';
    document.getElementById('formCapacity').value = '';
    document.getElementById('formPrice').value = '';
    document.getElementById('formFloor').value = '';
    document.getElementById('formDescription').value = '';
    hideError();
    document.getElementById('wsModal').classList.remove('hidden');
}

function openEditModal(w) {
    document.getElementById('modalTitle').textContent = 'Editar Espacio #' + w.id;
    document.getElementById('editingId').value = w.id;
    document.getElementById('formName').value = w.name || '';
    document.getElementById('formType').value = w.type || '';
    document.getElementById('formCapacity').value = w.capacity || '';
    document.getElementById('formPrice').value = w.pricePerHour || '';
    document.getElementById('formFloor').value = w.floor != null ? w.floor : '';
    document.getElementById('formDescription').value = w.description || '';
    hideError();
    document.getElementById('wsModal').classList.remove('hidden');
}

function closeModal() {
    document.getElementById('wsModal').classList.add('hidden');
}

async function saveWorkspace() {
    hideError();

    var id = document.getElementById('editingId').value;
    var body = {
        name:         document.getElementById('formName').value,
        type:         document.getElementById('formType').value,
        capacity:     parseInt(document.getElementById('formCapacity').value),
        pricePerHour: parseFloat(document.getElementById('formPrice').value),
        floor:        parseInt(document.getElementById('formFloor').value),
        description:  document.getElementById('formDescription').value || null
    };

    if (!body.name || !body.type || !body.capacity || isNaN(body.pricePerHour) || isNaN(body.floor)) {
        showError('Complete todos los campos obligatorios');
        return;
    }

    try {
        var url = id ? '/api/workspaces/' + id : '/api/workspaces';
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
        loadWorkspaces(currentPage);
    } catch (e) {
        showError('Error de conexión');
    }
}

// ── Disable ──

async function disableWorkspace(id) {
    try {
        var res = await fetch('/api/workspaces/' + id + '/disable', {
            method: 'PATCH',
            headers: headers()
        });
        if (res.ok || res.status === 204) {
            loadWorkspaces(currentPage);
        } else {
            var data = await res.json();
            alert(data.message || 'No se pudo deshabilitar el espacio');
        }
    } catch (e) {
        alert('Error de conexión');
    }
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
        var res = await fetch('/api/workspaces/' + id, {
            method: 'DELETE',
            headers: headers()
        });
        if (res.ok || res.status === 204) {
            closeDeleteModal();
            loadWorkspaces(currentPage);
        }
    } catch (e) { /* silenciar */ }
}

// ── Init ──

document.addEventListener('DOMContentLoaded', function () {
    loadWorkspaces(0);
});