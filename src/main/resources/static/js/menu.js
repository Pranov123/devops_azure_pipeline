// Menu browsing, cart and checkout

const GST_RATE = 0.05;
const CART_KEY = 'spice-route-cart';

let menu = [];
let categories = [];
let activeCategory = 'ALL';
let cart = loadCart();   // { menuItemId: quantity }

function loadCart() {
  try {
    return JSON.parse(localStorage.getItem(CART_KEY)) || {};
  } catch (e) {
    return {};
  }
}

function saveCart() {
  try {
    localStorage.setItem(CART_KEY, JSON.stringify(cart));
  } catch (e) {
    // storage unavailable (private mode) - cart still works for this page view
  }
}

function setQuantity(id, quantity) {
  if (quantity <= 0) {
    delete cart[id];
  } else {
    cart[id] = Math.min(quantity, 20);
  }
  saveCart();
  renderMenu();
  renderCart();
}

function renderChips() {
  const chips = [{ code: 'ALL', label: 'All' }, ...categories];
  document.getElementById('category-chips').innerHTML = chips.map(c =>
    `<button class="chip ${c.code === activeCategory ? 'active' : ''}" data-category="${c.code}" type="button">
       ${escapeHtml(c.label)}</button>`).join('');
}

function dishCard(item) {
  const quantity = cart[item.id] || 0;
  const control = !item.available
    ? '<span class="tag">Sold out</span>'
    : quantity === 0
      ? `<button class="btn btn-sm" data-add="${item.id}" type="button">Add</button>`
      : `<span class="stepper">
           <button data-dec="${item.id}" type="button" aria-label="Remove one">−</button>
           <span>${quantity}</span>
           <button data-inc="${item.id}" type="button" aria-label="Add one">+</button>
         </span>`;
  return `
    <article class="dish ${item.available ? '' : 'unavailable'}">
      <div class="dish-art">${CATEGORY_ICONS[item.category] || '🍽️'}</div>
      <div class="dish-body">
        <div class="dish-name">
          <span class="diet ${item.vegetarian ? '' : 'nonveg'}" title="${item.vegetarian ? 'Vegetarian' : 'Non-vegetarian'}"></span>
          <span>${escapeHtml(item.name)} ${item.spicy ? '🌶️' : ''}</span>
        </div>
        <div class="dish-desc">${escapeHtml(item.description)}</div>
        <div class="dish-foot">
          <span class="price">${money(item.price)}</span>
          ${control}
        </div>
      </div>
    </article>`;
}

function renderMenu() {
  const query = document.getElementById('search').value.trim().toLowerCase();
  const vegOnly = document.getElementById('veg-only').checked;
  const visible = menu.filter(item =>
    (activeCategory === 'ALL' || item.category === activeCategory) &&
    (!vegOnly || item.vegetarian) &&
    (!query || item.name.toLowerCase().includes(query) || item.description.toLowerCase().includes(query)));

  const list = document.getElementById('menu-list');
  if (visible.length === 0) {
    list.innerHTML = '<p class="empty">No dishes match your filters.</p>';
    return;
  }
  list.innerHTML = categories
    .map(c => ({ category: c, items: visible.filter(i => i.category === c.code) }))
    .filter(group => group.items.length > 0)
    .map(group => `
      <h2 class="category-title">${escapeHtml(group.category.label)}</h2>
      <div class="dish-grid">${group.items.map(dishCard).join('')}</div>`)
    .join('');
}

function cartLines() {
  return Object.entries(cart)
    .map(([id, quantity]) => ({ item: menu.find(m => m.id === Number(id)), quantity }))
    .filter(line => line.item);
}

function renderCart() {
  const lines = cartLines();
  const empty = lines.length === 0;
  const subtotal = lines.reduce((sum, l) => sum + l.item.price * l.quantity, 0);
  const tax = Math.round(subtotal * GST_RATE * 100) / 100;
  const count = lines.reduce((sum, l) => sum + l.quantity, 0);

  document.getElementById('cart-lines').innerHTML = lines.map(l => `
    <li>
      <div>
        <div class="name">${escapeHtml(l.item.name)}</div>
        <div class="sub">${money(l.item.price)} × ${l.quantity}</div>
      </div>
      <span class="stepper">
        <button data-dec="${l.item.id}" type="button" aria-label="Remove one">−</button>
        <span>${l.quantity}</span>
        <button data-inc="${l.item.id}" type="button" aria-label="Add one">+</button>
      </span>
    </li>`).join('');

  document.getElementById('cart-empty').classList.toggle('hidden', !empty);
  document.getElementById('bill').classList.toggle('hidden', empty);
  document.getElementById('checkout').classList.toggle('hidden', empty);
  document.getElementById('bill-subtotal').textContent = money(subtotal);
  document.getElementById('bill-tax').textContent = money(tax);
  document.getElementById('bill-total').textContent = money(subtotal + tax);
  document.getElementById('cart-bar').classList.toggle('hidden', empty);
  document.getElementById('cart-bar-text').textContent =
    `${count} item${count === 1 ? '' : 's'} · ${money(subtotal + tax)}`;
}

function showCheckoutError(message) {
  const box = document.getElementById('checkout-error');
  box.textContent = message;
  box.classList.toggle('hidden', !message);
}

async function placeOrder(event) {
  event.preventDefault();
  showCheckoutError('');
  const orderType = document.querySelector('input[name="order-type"]:checked').value;
  const tableValue = document.getElementById('table-number').value;
  const body = {
    customerName: document.getElementById('customer-name').value.trim(),
    orderType,
    tableNumber: orderType === 'DINE_IN' && tableValue ? Number(tableValue) : null,
    items: cartLines().map(l => ({ menuItemId: l.item.id, quantity: l.quantity }))
  };
  if (!body.customerName) {
    showCheckoutError('Please enter your name.');
    return;
  }

  const button = document.getElementById('place-order');
  button.disabled = true;
  try {
    const order = await api('/api/orders', { method: 'POST', body });
    cart = {};
    saveCart();
    renderMenu();
    renderCart();
    document.getElementById('modal-order-id').textContent = '#' + order.id;
    document.getElementById('modal-total').textContent = money(order.total);
    document.getElementById('modal-track').href = 'track.html?id=' + order.id;
    document.getElementById('order-modal').classList.remove('hidden');
  } catch (e) {
    showCheckoutError(e.message);
    // Availability may have changed while the page was open
    menu = await api('/api/menu');
    renderMenu();
  } finally {
    button.disabled = false;
  }
}

function handleClick(event) {
  const target = event.target.closest('button');
  if (!target) return;
  if (target.dataset.category) {
    activeCategory = target.dataset.category;
    renderChips();
    renderMenu();
  } else if (target.dataset.add) {
    setQuantity(target.dataset.add, 1);
  } else if (target.dataset.inc) {
    setQuantity(target.dataset.inc, (cart[target.dataset.inc] || 0) + 1);
  } else if (target.dataset.dec) {
    setQuantity(target.dataset.dec, (cart[target.dataset.dec] || 0) - 1);
  }
}

async function init() {
  document.addEventListener('click', handleClick);
  document.getElementById('search').addEventListener('input', renderMenu);
  document.getElementById('veg-only').addEventListener('change', renderMenu);
  document.getElementById('checkout').addEventListener('submit', placeOrder);
  document.getElementById('modal-close').addEventListener('click',
    () => document.getElementById('order-modal').classList.add('hidden'));
  document.querySelectorAll('input[name="order-type"]').forEach(radio =>
    radio.addEventListener('change', () => document.getElementById('table-field')
      .classList.toggle('hidden', radio.value === 'TAKEAWAY' && radio.checked)));

  try {
    [categories, menu] = await Promise.all([api('/api/menu/categories'), api('/api/menu')]);
    // Drop cart entries for dishes that no longer exist
    Object.keys(cart).forEach(id => { if (!menu.some(m => m.id === Number(id))) delete cart[id]; });
    renderChips();
    renderMenu();
    renderCart();
  } catch (e) {
    document.getElementById('menu-list').innerHTML =
      `<p class="error-box">Could not load the menu: ${escapeHtml(e.message)}</p>`;
  }
}

document.addEventListener('DOMContentLoaded', init);
