// Live order tracking

const STEPS = [
  ['PLACED', '🧾', 'Placed'],
  ['PREPARING', '👨‍🍳', 'Preparing'],
  ['READY', '🔔', 'Ready'],
  ['SERVED', '✅', 'Served']
];

let refreshTimer = null;

function renderOrder(order) {
  document.getElementById('track-error').classList.add('hidden');
  document.getElementById('order-card').classList.remove('hidden');
  document.getElementById('o-id').textContent = '#' + order.id;

  const pill = document.getElementById('o-status');
  pill.textContent = order.status;
  pill.className = 'status-pill ' + order.status;

  const where = order.orderType === 'DINE_IN' ? `Table ${order.tableNumber}` : 'Takeaway';
  document.getElementById('o-meta').textContent =
    `${order.customerName} · ${where} · placed at ${formatTime(order.createdAt)}`;

  const cancelled = order.status === 'CANCELLED';
  const currentIndex = STEPS.findIndex(([code]) => code === order.status);
  document.getElementById('timeline').classList.toggle('hidden', cancelled);
  document.getElementById('o-cancelled').classList.toggle('hidden', !cancelled);
  document.getElementById('timeline').innerHTML = STEPS.map(([code, icon, label], i) => {
    const state = i < currentIndex || code === 'SERVED' && i === currentIndex ? 'done'
      : i === currentIndex ? 'current' : '';
    return `<div class="step ${state}"><div class="dot">${icon}</div>${label}</div>`;
  }).join('');

  document.getElementById('o-items').innerHTML =
    order.items.map(i => `<tr><td>${escapeHtml(i.name)} × ${i.quantity}</td><td>${money(i.lineTotal)}</td></tr>`).join('') +
    `<tr><td>Subtotal</td><td>${money(order.subtotal)}</td></tr>
     <tr><td>GST (5%)</td><td>${money(order.tax)}</td></tr>
     <tr class="grand"><td>Total</td><td>${money(order.total)}</td></tr>`;

  return order.status === 'SERVED' || cancelled;
}

async function track(id) {
  clearTimeout(refreshTimer);
  try {
    const order = await api('/api/orders/' + encodeURIComponent(id));
    const finished = renderOrder(order);
    if (!finished) {
      refreshTimer = setTimeout(() => track(id), 5000);
    }
  } catch (e) {
    document.getElementById('order-card').classList.add('hidden');
    const box = document.getElementById('track-error');
    box.textContent = e.message;
    box.classList.remove('hidden');
  }
}

document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('track-form');
  const input = document.getElementById('order-id');
  form.addEventListener('submit', event => {
    event.preventDefault();
    history.replaceState(null, '', '?id=' + input.value);
    track(input.value);
  });

  const id = new URLSearchParams(location.search).get('id');
  if (id) {
    input.value = id;
    track(id);
  }
});
