/**
 * showToast('mensaje', 'error' | 'success' | 'warning' | 'info')
 */
function showToast(message, type) {
    type = type || 'error';

    var container = document.getElementById('toastContainer');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toastContainer';
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    var icons = {
        error:   'bi-x-circle-fill',
        success: 'bi-check-circle-fill',
        warning: 'bi-exclamation-triangle-fill',
        info:    'bi-info-circle-fill'
    };

    var toast = document.createElement('div');
    toast.className = 'toast toast-' + type;
    toast.innerHTML = '<i class="bi ' + (icons[type] || icons.info) + '"></i>' + message;

    container.appendChild(toast);

    setTimeout(function () {
        toast.remove();
    }, 4000);
}