// Table reservation form

function isoDate(date) {
  // Local date as yyyy-MM-dd (toISOString would shift to UTC)
  const offset = date.getTimezoneOffset() * 60000;
  return new Date(date.getTime() - offset).toISOString().slice(0, 10);
}

function fillOptions() {
  const times = [];
  for (let minutes = 11 * 60; minutes <= 22 * 60 + 30; minutes += 30) {
    const hh = String(Math.floor(minutes / 60)).padStart(2, '0');
    const mm = String(minutes % 60).padStart(2, '0');
    times.push(`${hh}:${mm}`);
  }
  const timeSelect = document.getElementById('r-time');
  timeSelect.innerHTML = times.map(t => `<option value="${t}" ${t === '19:30' ? 'selected' : ''}>${t}</option>`).join('');

  document.getElementById('r-party').innerHTML = Array.from({ length: 12 }, (_, i) => i + 1)
    .map(n => `<option value="${n}" ${n === 2 ? 'selected' : ''}>${n} guest${n === 1 ? '' : 's'}</option>`).join('');

  const today = new Date();
  const max = new Date();
  max.setDate(today.getDate() + 60);
  const dateInput = document.getElementById('r-date');
  dateInput.min = isoDate(today);
  dateInput.max = isoDate(max);
  dateInput.value = isoDate(today);
}

function showError(message) {
  const box = document.getElementById('reserve-error');
  box.textContent = message;
  box.classList.toggle('hidden', !message);
}

async function submit(event) {
  event.preventDefault();
  showError('');
  const body = {
    customerName: document.getElementById('r-name').value.trim(),
    phone: document.getElementById('r-phone').value.trim(),
    partySize: Number(document.getElementById('r-party').value),
    date: document.getElementById('r-date').value,
    time: document.getElementById('r-time').value,
    notes: document.getElementById('r-notes').value.trim()
  };
  if (!body.customerName || !body.phone || !body.date) {
    showError('Please fill in your name, mobile number and date.');
    return;
  }

  const button = document.getElementById('reserve-btn');
  button.disabled = true;
  try {
    const r = await api('/api/reservations', { method: 'POST', body });
    const when = new Date(`${r.date}T${r.time}`).toLocaleString('en-IN',
      { weekday: 'long', day: 'numeric', month: 'long', hour: '2-digit', minute: '2-digit' });
    document.getElementById('done-text').innerHTML =
      `Booking <strong>#${r.id}</strong> for <strong>${r.partySize}</strong> on <strong>${escapeHtml(when)}</strong>.<br>` +
      `We look forward to seeing you, ${escapeHtml(r.customerName)}!`;
    document.getElementById('reserve-form').classList.add('hidden');
    document.getElementById('reserve-done').classList.remove('hidden');
  } catch (e) {
    showError(e.message);
  } finally {
    button.disabled = false;
  }
}

document.addEventListener('DOMContentLoaded', () => {
  fillOptions();
  document.getElementById('reserve-form').addEventListener('submit', submit);
  document.getElementById('book-another').addEventListener('click', () => {
    document.getElementById('reserve-form').reset();
    fillOptions();
    document.getElementById('reserve-done').classList.add('hidden');
    document.getElementById('reserve-form').classList.remove('hidden');
  });
});
