// Shared helpers used by every page

const CATEGORY_ICONS = {
  STARTERS: '🍢', MAINS: '🍛', BIRYANI: '🍚', BREADS: '🫓', DESSERTS: '🍮', BEVERAGES: '🥤'
};

/** Calls the REST API and throws an Error carrying the server's message on failure. */
async function api(path, options = {}) {
  const init = { ...options, headers: { 'Content-Type': 'application/json', ...(options.headers || {}) } };
  if (init.body && typeof init.body !== 'string') {
    init.body = JSON.stringify(init.body);
  }
  const response = await fetch(path, init);
  const text = await response.text();
  const data = text ? JSON.parse(text) : null;
  if (!response.ok) {
    throw new Error((data && data.error) || `Request failed (${response.status})`);
  }
  return data;
}

function escapeHtml(value) {
  return String(value ?? '').replace(/[&<>"']/g, c => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
  }[c]));
}

function money(value) {
  return '₹' + Number(value).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function formatTime(isoDateTime) {
  return new Date(isoDateTime).toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' });
}

function minutesAgo(isoDateTime) {
  const minutes = Math.max(0, Math.round((Date.now() - new Date(isoDateTime).getTime()) / 60000));
  return minutes === 0 ? 'just now' : `${minutes} min ago`;
}

function toast(message) {
  const el = document.createElement('div');
  el.className = 'toast';
  el.textContent = message;
  document.body.appendChild(el);
  setTimeout(() => el.remove(), 2500);
}

/** Header navigation, highlighting the current page. */
function renderHeader() {
  const page = document.body.dataset.page;
  const links = [
    ['menu', 'index.html', 'Menu'],
    ['reserve', 'reserve.html', 'Book a table'],
    ['track', 'track.html', 'Track order'],
    ['kitchen', 'kitchen.html', 'Kitchen']
  ];
  document.getElementById('site-header').innerHTML = `
    <div class="inner">
      <a class="logo" href="index.html">Spice <span>Route</span></a>
      <nav class="site-nav">
        ${links.map(([key, href, label]) =>
          `<a href="${href}" class="${key === page ? 'active' : ''}">${label}</a>`).join('')}
      </nav>
    </div>`;
}

/** Footer shows which version, Jenkins build and pod served the page. */
async function renderFooter() {
  const footer = document.getElementById('site-footer');
  footer.innerHTML = `
    <div class="inner">
      <span>© Spice Route · Open daily 11:00 – 23:00</span>
      <span class="build-info" id="build-info">Loading deployment info…</span>
    </div>`;
  try {
    const info = await api('/api/info');
    document.getElementById('build-info').innerHTML =
      `Version <code>${escapeHtml(info.version)}</code> · Jenkins build <code>${escapeHtml(info.build)}</code>` +
      ` · Pod <code>${escapeHtml(info.hostname)}</code>`;
  } catch (e) {
    document.getElementById('build-info').textContent = 'Deployment info unavailable';
  }
}

document.addEventListener('DOMContentLoaded', () => {
  renderHeader();
  renderFooter();
});
