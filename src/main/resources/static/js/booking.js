// ── State ──

let currentPage = 0;
let totalPages = 0;

// ── Helpers ──

function getToken() {
    const match = document.cookie.match(/(?:^|; )jwt=([^;]*)/);
    return match ? match[1] : '';
}

function headers() {
    return {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + getToken()
    };
}

function statusBadge(status) {
    const map = {
        PENDIENTE:  { label: 'Pendiente',  css: 'badge-pending' },
        CONFIRMADA: { label: 'Confirmada', css: 'badge-confirmed' },
        CANCELADA:  { label: 'Cancelada',  css: 'badge-cancelled' }
    };
    const s = map[status] || { label: status, css: '' };
    return '<span class="badge ' + s.css + '">' + s.label + '</span>';
}

function formatDate(dateStr) {
    if (!dateStr) return '-';
    const d = new Date(dateStr);
    return d.toLocaleString('es-EC', {
        day: '2-digit', month: '2-digit', year: 'numeric',
        hour: '2-digit', minute: '2-digit'
    });
}

function toLocalInput(dateStr) {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    const offset = d.getTimezoneOffset();
    const local = new Date(d.getTime() - offset * 60000);
    return local.toISOString().slice(0, 16);
}

function showError(msg) {
    const el = document.getElementById('modalError');
    el.textContent = msg;
    el.style.display = 'block';
}

function hideError() {
    document.getElementById('modalError').style.display = 'none';
}

function showAlert(msg, type) {
    var el = document.getElementById('pageAlert');
    el.className = 'page-alert alert-' + (type || 'error');
    el.innerHTML = '<i class="bi ' + (type === 'success' ? 'bi-check-circle-fill' : 'bi-exclamation-circle-fill') + '"></i>' + msg;
    el.style.display = 'flex';
    setTimeout(function () { el.style.display = 'none'; }, 5000);
}

// ── Render ──

function renderTable(bookings) {
    const tbody = document.getElementById('bookingsBody');

    if (!bookings || bookings.length === 0) {
        tbody.innerHTML = '<tr><td colspan="10" class="empty-state">No hay reservas registradas</td></tr>';
        return;
    }

    tbody.innerHTML = bookings.map(function (b) {
        var safeB = JSON.stringify(b).replace(/'/g, '&#39;').replace(/"/g, '&quot;');

        var actions = '';
        if (b.status === 'PENDIENTE') {
            actions +=
                '<button class="btn-action confirm" title="Confirmar" onclick="confirmBooking(' + b.id + ')">' +
                '<i class="bi bi-check-lg"></i>' +
                '</button>' +
                '<button class="btn-action cancel" title="Cancelar" onclick="cancelBooking(' + b.id + ')">' +
                '<i class="bi bi-x-lg"></i>' +
                '</button>';
        }
        actions +=
            '<button class="btn-action edit" title="Editar" onclick=\'openEditModal(' + safeB + ')\'>' +
            '<i class="bi bi-pencil"></i>' +
            '</button>' +
            '<button class="btn-action delete" title="Eliminar" onclick="openDeleteModal(' + b.id + ')">' +
            '<i class="bi bi-trash"></i>' +
            '</button>';

        return '<tr>' +
            '<td>' + b.id + '</td>' +
            '<td>' + (b.memberFullName || b.memberId || '-') + '</td>' +
            '<td>' + (b.workspaceName || b.workspaceId || '-') + '</td>' +
            '<td>' + (b.workspaceType || '-') + '</td>' +
            '<td>' + formatDate(b.startDatetime) + '</td>' +
            '<td>' + formatDate(b.endDatetime) + '</td>' +
            '<td>' + (b.totalHours != null ? b.totalHours + 'h' : '-') + '</td>' +
            '<td>' + (b.totalPrice != null ? '$' + b.totalPrice.toFixed(2) : '-') + '</td>' +
            '<td>' + statusBadge(b.status) + '</td>' +
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

// ── Load data ──

async function loadBookings(page) {
    currentPage = page || 0;
    var searchId = document.getElementById('searchId') ? document.getElementById('searchId').value.trim() : '';
    var statusFilter = document.getElementById('filterStatus') ? document.getElementById('filterStatus').value : '';
    try {
        var url = '/api/bookings/search?page=' + currentPage + '&size=10';
        if (searchId) {
            url += '&id=' + encodeURIComponent(searchId);
        }
        var res = await fetch(url, { headers: headers() });
        if (!res.ok) throw new Error();
        var data = await res.json();
        totalPages = data.totalPages;
        var contentData = data.content;
        if (statusFilter) {
            contentData = contentData.filter(b => b.status === statusFilter);
        }
        renderTable(contentData);
        renderPagination();
    } catch (e) {
        var tb = document.getElementById('bookingsBody');
        if(tb) tb.innerHTML = '<tr><td colspan="10" class="empty-state">Error al cargar reservas</td></tr>';
    }
}

// ── Filters ──

async function applyFilters() {
    loadBookings(0);
}

function clearFilters() {
    document.getElementById('searchId').value = '';
    document.getElementById('filterStatus').value = '';
    loadBookings(0);
}

function goToPage(page) {
    loadBookings(page);
}

// ── Modal create/edit ──

function openCreateModal() {
    document.getElementById('modalTitle').textContent = 'Nueva Reserva';
    document.getElementById('editingId').value = '';
    $('#formMemberId').val('').trigger('change');
    $('#formWorkspaceId').val('').trigger('change');
    document.getElementById('formStartDatetime').value = '';
    document.getElementById('formEndDatetime').value = '';
    hideError();
    document.getElementById('bookingModal').classList.remove('hidden');
}

function openEditModal(b) {
    document.getElementById('modalTitle').textContent = 'Editar Reserva #' + b.id;
    document.getElementById('editingId').value = b.id;
    $('#formMemberId').val(b.memberId).trigger('change');
    $('#formWorkspaceId').val(b.workspaceId).trigger('change');
    document.getElementById('formStartDatetime').value = toLocalInput(b.startDatetime);
    document.getElementById('formEndDatetime').value = toLocalInput(b.endDatetime);
    hideError();
    document.getElementById('bookingModal').classList.remove('hidden');
}

function closeModal() {
    document.getElementById('bookingModal').classList.add('hidden');
}

async function saveBooking() {
    hideError();

    var id = document.getElementById('editingId').value;
    var body = {
        memberId:       parseInt(document.getElementById('formMemberId').value),
        workspaceId:    parseInt(document.getElementById('formWorkspaceId').value),
        startDatetime:  document.getElementById('formStartDatetime').value,
        endDatetime:    document.getElementById('formEndDatetime').value
    };

    if (!body.memberId || !body.workspaceId || !body.startDatetime || !body.endDatetime) {
        showError('Todos los campos son obligatorios');
        return;
    }

    try {
        var url = id ? '/api/bookings/' + id : '/api/bookings';
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
        loadBookings(currentPage);
    } catch (e) {
        showError('Error de conexión');
    }
}

// ── Confirm / Cancel status ──

async function confirmBooking(id) {
    try {
        var res = await fetch('/api/bookings/' + id + '/confirm', {
            method: 'PATCH',
            headers: headers()
        });
        if (res.ok) {
            showAlert('Reserva confirmada', 'success');
            loadBookings(currentPage);
        } else {
            var data = await res.json();
            showAlert(data.message || 'No se pudo confirmar', 'error');
        }
    } catch (e) {
        showAlert('Error de conexión', 'error');
    }
}

async function cancelBooking(id) {
    try {
        var res = await fetch('/api/bookings/' + id + '/cancel', {
            method: 'PATCH',
            headers: headers()
        });
        if (res.ok) {
            showAlert('Reserva cancelada', 'success');
            loadBookings(currentPage);
        } else {
            var data = await res.json();
            showAlert(data.message || 'No se pudo cancelar', 'error');
        }
    } catch (e) {
        showAlert('Error de conexión', 'error');
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
        var res = await fetch('/api/bookings/' + id, {
            method: 'DELETE',
            headers: headers()
        });
        if (res.ok || res.status === 204) {
            closeDeleteModal();
            showAlert('Reserva eliminada', 'success');
            loadBookings(currentPage);
        } else {
            var data = await res.json();
            closeDeleteModal();
            showAlert(data.message || 'No se pudo eliminar', 'error');
        }
    } catch (e) {
        showAlert('Error de conexión', 'error');
    }
}

// ── Select2: cargar miembros y espacios ──

async function loadMemberOptions() {
    try {
        var res = await fetch('/api/members', { headers: headers() });
        if (!res.ok) return;
        var members = await res.json();
        var select = document.getElementById('formMemberId');
        select.innerHTML = '<option value="">Buscar miembro...</option>';
        members.forEach(function (m) {
            var opt = document.createElement('option');
            opt.value = m.id;
            opt.textContent = m.fullName + ' (' + m.email + ')';
            select.appendChild(opt);
        });
    } catch (e) { /* silenciar */ }
}

async function loadWorkspaceOptions() {
    try {
        var res = await fetch('/api/workspaces/available', { headers: headers() });
        if (!res.ok) return;
        var workspaces = await res.json();
        var select = document.getElementById('formWorkspaceId');
        select.innerHTML = '<option value="">Buscar espacio...</option>';
        workspaces.forEach(function (w) {
            var opt = document.createElement('option');
            opt.value = w.id;
            opt.textContent = w.name + ' - ' + (w.type || '') + ' (Piso ' + w.floor + ')';
            select.appendChild(opt);
        });
    } catch (e) { /* silenciar */ }
}

// ── Init ──

document.addEventListener('DOMContentLoaded', async function () {
    await Promise.all([loadMemberOptions(), loadWorkspaceOptions()]);

    $('.select2-field').select2({
        placeholder: 'Buscar...',
        allowClear: true,
        language: 'es',
        dropdownParent: $('#bookingModal')
    });

    loadBookings(0);
});
