// Kitchen dashboard: order board, reservations and menu availability

const ACTION_LABELS = {
  PREPARING: 'Start cooking',
  READY: 'Mark ready',
  SERVED: 'Mark served',
  CANCELLED: 'Cancel'
};

let activeTab = 'orders';

function showError(message) {
  const box = document.getElementById('kitchen-error');
  box.textContent = message;
  box.classList.toggle('hidden', !message);
}

function ticket(order) {
  const where = order.orderType === 'DINE_IN' ? `Table ${order.tableNumber}` : 'Takeaway';
  const actions = ['PREPARING', 'READY', 'SERVED', 'CANCELLED']
    .filter(status => order.nextStatuses.includes(status))
    .map(status => `<button class="btn btn-sm ${status === 'CANCELLED' ? 'btn-outline' : ''}"
                      data-order="${order.id}" data-status="${status}" type="button">${ACTION_LABELS[status]}</button>`)
    .join('');
  return `
    <div class="ticket ${order.status}">
      <div class="ticket-head"><span>#${order.id} · ${escapeHtml(where)}</span><span>${money(order.total)}</span></div>
      <div class="ticket-meta">${escapeHtml(order.customerName)} · ${minutesAgo(order.createdAt)}</div>
      <ul>${order.items.map(i => `<li>${i.quantity} × ${escapeHtml(i.name)}</li>`).join('')}</ul>
      <div class="ticket-actions">${actions}</div>
    </div>`;
}

async function loadOrders() {
  const orders = await api('/api/orders');
  ['PLACED', 'PREPARING', 'READY'].forEach(status => {
    const group = orders.filter(o => o.status === status);
    document.getElementById('count-' + status).textContent = group.length;
    document.getElementById('col-' + status).innerHTML =
      group.length ? group.map(ticket).join('') : '<p class="empty">No orders</p>';
  });
}

async function loadReservations() {
  const reservations = await api('/api/reservations');
  document.getElementById('reservation-rows').innerHTML = reservations.length === 0
    ? '<tr><td colspan="7" class="empty">No reservations today</td></tr>'
    : reservations.map(r => `
        <tr>
          <td>${r.time.slice(0, 5)}</td>
          <td>${escapeHtml(r.customerName)}</td>
          <td>${r.partySize}</td>
          <td>${escapeHtml(r.phone)}</td>
          <td>${escapeHtml(r.notes || '')}</td>
          <td><span class="status-pill ${r.status}">${r.status}</span></td>
          <td>${r.status === 'CONFIRMED'
            ? `<button class="btn btn-sm btn-outline" data-cancel-reservation="${r.id}" type="button">Cancel</button>` : ''}</td>
        </tr>`).join('');
}

async function loadMenu() {
  const menu = await api('/api/menu');
  document.getElementById('menu-rows').innerHTML = menu.map(item => `
    <tr>
      <td><span class="diet ${item.vegetarian ? '' : 'nonveg'}"></span> ${escapeHtml(item.name)}</td>
      <td>${escapeHtml(item.categoryLabel)}</td>
      <td>${money(item.price)}</td>
      <td><label class="switch">
            <input type="checkbox" data-menu-item="${item.id}" ${item.available ? 'checked' : ''}>
            <span class="slider"></span>
          </label></td>
    </tr>`).join('');
}

async function refresh() {
  try {
    if (activeTab === 'orders') await loadOrders();
    if (activeTab === 'reservations') await loadReservations();
    showError('');
  } catch (e) {
    showError('Could not refresh: ' + e.message);
  }
}

async function handleClick(event) {
  const button = event.target.closest('button');
  if (!button) return;
  try {
    if (button.dataset.tab) {
      activeTab = button.dataset.tab;
      document.querySelectorAll('[data-tab]').forEach(b => b.classList.toggle('active', b === button));
      ['orders', 'reservations', 'menu'].forEach(tab =>
        document.getElementById('tab-' + tab).classList.toggle('hidden', tab !== activeTab));
      if (activeTab === 'menu') await loadMenu(); else await refresh();
    } else if (button.dataset.order) {
      button.disabled = true;
      await api(`/api/orders/${button.dataset.order}/status`,
        { method: 'PATCH', body: { status: button.dataset.status } });
      toast(`Order #${button.dataset.order}: ${ACTION_LABELS[button.dataset.status]}`);
      await loadOrders();
    } else if (button.dataset.cancelReservation) {
      await api(`/api/reservations/${button.dataset.cancelReservation}/cancel`, { method: 'PATCH' });
      toast('Reservation cancelled');
      await loadReservations();
    }
  } catch (e) {
    showError(e.message);
    button.disabled = false;
  }
}

async function handleToggle(event) {
  const input = event.target;
  if (!input.dataset.menuItem) return;
  try {
    const item = await api(`/api/menu/${input.dataset.menuItem}/availability`,
      { method: 'PATCH', body: { available: input.checked } });
    toast(`${item.name} is now ${item.available ? 'available' : 'sold out'}`);
  } catch (e) {
    input.checked = !input.checked;
    showError(e.message);
  }
}

document.addEventListener('DOMContentLoaded', () => {
  document.addEventListener('click', handleClick);
  document.addEventListener('change', handleToggle);
  refresh();
  setInterval(refresh, 5000);
});
