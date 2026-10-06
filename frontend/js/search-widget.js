/**
 * Tripzy Smart Search & Recommendation Engine
 * Real-time autocomplete, live route recommendations, keyboard navigation,
 * and rating-based sorting using existing APIs.
 */

const SearchWidget = (() => {
  let cache = {
    flights: [],
    hotels: [],
    cars: [],
    packages: [],
    loaded: false
  };

  // Debounce helper
  function debounce(fn, delay = 160) {
    let timer = null;
    return function (...args) {
      clearTimeout(timer);
      timer = setTimeout(() => fn.apply(this, args), delay);
    };
  }

  // Safe text escape helper
  function escapeHtml(str) {
    if (str === null || str === undefined) return '';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }

  // Substring match highlighter
  function highlightMatch(text, query) {
    if (!text) return '';
    if (!query) return escapeHtml(text);
    const safeText = escapeHtml(text);
    const q = query.trim().replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    if (!q) return safeText;
    const regex = new RegExp(`(${q})`, 'gi');
    return safeText.replace(regex, '<mark class="autocomplete-match">$1</mark>');
  }

  // Currency formatter
  function formatCurrency(amount) {
    return '₹' + Number(amount || 0).toLocaleString('en-IN', {
      maximumFractionDigits: 0
    });
  }

  // Fetch and cache catalog data
  async function loadData() {
    try {
      const [flightsRes, hotelsRes, carsRes, packagesRes] = await Promise.all([
        API.getFlights().catch(() => []),
        API.getHotels().catch(() => []),
        API.getCars().catch(() => []),
        API.getPackages().catch(() => [])
      ]);

      const rawFlights = Array.isArray(flightsRes) ? flightsRes : (flightsRes.flights || flightsRes.data || []);
      const rawHotels = Array.isArray(hotelsRes) ? hotelsRes : (hotelsRes.hotels || hotelsRes.data || []);
      const rawCars = Array.isArray(carsRes) ? carsRes : (carsRes.cars || carsRes.data || []);
      const rawPkgs = Array.isArray(packagesRes) ? packagesRes : (packagesRes.packages || packagesRes.data || []);

      // Filter availability: seats > 0, rooms > 0, units > 0
      cache.flights = rawFlights.filter(f => (f.availableSeats || 0) > 0);
      cache.hotels = rawHotels.filter(h => (h.availableRooms || 0) > 0);
      cache.cars = rawCars.filter(c => (c.availableUnits || 0) > 0);
      cache.packages = rawPkgs;
      cache.loaded = true;
    } catch (e) {
      console.warn('Tripzy SearchWidget data load error:', e);
    }
  }

  // Close all open dropdowns
  function closeAllDropdowns() {
    document.querySelectorAll('.autocomplete-dropdown').forEach(d => {
      d.classList.remove('open');
      d.innerHTML = '';
    });
  }

  // Setup wrapper, dropdown, and keyboard navigation for an input element
  function setupInput(inputEl) {
    if (!inputEl) return null;
    let wrapper = inputEl.parentElement;
    if (!wrapper.classList.contains('autocomplete-wrapper')) {
      const newWrapper = document.createElement('div');
      newWrapper.className = 'autocomplete-wrapper';
      wrapper.replaceChild(newWrapper, inputEl);
      newWrapper.appendChild(inputEl);
      wrapper = newWrapper;
    }

    let dropdown = wrapper.querySelector('.autocomplete-dropdown');
    if (!dropdown) {
      dropdown = document.createElement('div');
      dropdown.className = 'autocomplete-dropdown';
      dropdown.setAttribute('role', 'listbox');
      wrapper.appendChild(dropdown);
    }

    // Keyboard navigation (ArrowDown, ArrowUp, Enter, Escape)
    inputEl.removeEventListener('keydown', handleKeyNavigation);
    inputEl.addEventListener('keydown', (e) => handleKeyNavigation(e, dropdown));

    return dropdown;
  }

  function handleKeyNavigation(e, dropdown) {
    if (!dropdown.classList.contains('open')) return;

    const items = dropdown.querySelectorAll('.autocomplete-item');
    if (items.length === 0) return;

    let activeIndex = -1;
    items.forEach((item, index) => {
      if (item.classList.contains('active')) {
        activeIndex = index;
      }
    });

    if (e.key === 'ArrowDown') {
      e.preventDefault();
      if (activeIndex >= 0) items[activeIndex].classList.remove('active');
      const nextIndex = (activeIndex + 1) % items.length;
      items[nextIndex].classList.add('active');
      items[nextIndex].scrollIntoView({ block: 'nearest' });
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      if (activeIndex >= 0) items[activeIndex].classList.remove('active');
      const prevIndex = activeIndex <= 0 ? items.length - 1 : activeIndex - 1;
      items[prevIndex].classList.add('active');
      items[prevIndex].scrollIntoView({ block: 'nearest' });
    } else if (e.key === 'Enter') {
      if (activeIndex >= 0) {
        e.preventDefault();
        items[activeIndex].click();
      }
    } else if (e.key === 'Escape') {
      dropdown.classList.remove('open');
      dropdown.innerHTML = '';
    }
  }

  // Display smart recommendation preview below tab form
  function showRecommendation(tabId, item, type) {
    const form = document.getElementById(tabId);
    if (!form) return;

    let box = form.parentElement.querySelector('.search-recommendation-box');
    if (!box) {
      box = document.createElement('div');
      box.className = 'search-recommendation-box';
      form.parentElement.appendChild(box);
    }

    if (!item) {
      box.remove();
      return;
    }

    if (type === 'flight') {
      box.innerHTML = `
        <div class="recommendation-info">
          <span class="recommendation-badge">⭐ Recommended Flight</span>
          <div class="recommendation-details">
            <h4 style="margin:0 0 4px; font-size:1rem; font-weight:700;">✈ ${escapeHtml(item.airline)} (${escapeHtml(item.flightNumber)}) • ${escapeHtml(item.origin)} → ${escapeHtml(item.destination)}</h4>
            <p style="margin:0; font-size:0.84rem; color:var(--text-muted);">⏰ ${escapeHtml(item.departureTime)} – ${escapeHtml(item.arrivalTime)} | 🗓 ${escapeHtml(item.departureDate)} | 💺 ${escapeHtml(item.availableSeats)} seats available</p>
          </div>
        </div>
        <div class="recommendation-actions" style="display:flex; align-items:center; gap:12px;">
          <span class="price-tag" style="font-size:1.15rem;">${formatCurrency(item.price)} <small style="font-size:0.75rem; font-weight:normal; color:var(--text-muted);">/ seat</small></span>
          <a href="booking.html?type=flight&id=${item.id}" class="btn btn-sm btn-primary">Book Flight →</a>
        </div>
      `;
    } else if (type === 'hotel') {
      box.innerHTML = `
        <div class="recommendation-info">
          <span class="recommendation-badge">⭐ Top-Rated Hotel</span>
          <div class="recommendation-details">
            <h4 style="margin:0 0 4px; font-size:1rem; font-weight:700;">🏨 ${escapeHtml(item.hotelName)} • ⭐ ${escapeHtml(item.rating || '4.5')} / 5.0</h4>
            <p style="margin:0; font-size:0.84rem; color:var(--text-muted);">📍 ${escapeHtml(item.location || item.address)} | 🏨 ${escapeHtml(item.roomType || 'Deluxe')} | 🛏 ${escapeHtml(item.availableRooms)} rooms left</p>
          </div>
        </div>
        <div class="recommendation-actions" style="display:flex; align-items:center; gap:12px;">
          <span class="price-tag" style="font-size:1.15rem;">${formatCurrency(item.pricePerNight)} <small style="font-size:0.75rem; font-weight:normal; color:var(--text-muted);">/ night</small></span>
          <a href="booking.html?type=hotel&id=${item.id}" class="btn btn-sm btn-primary">Book Room →</a>
        </div>
      `;
    } else if (type === 'car') {
      box.innerHTML = `
        <div class="recommendation-info">
          <span class="recommendation-badge">🚗 Available Fleet</span>
          <div class="recommendation-details">
            <h4 style="margin:0 0 4px; font-size:1rem; font-weight:700;">${escapeHtml(item.carName)} (${escapeHtml(item.brand)} ${escapeHtml(item.carType)})</h4>
            <p style="margin:0; font-size:0.84rem; color:var(--text-muted);">📍 Pickup: ${escapeHtml(item.location)} | 🚗 ${escapeHtml(item.availableUnits)} units available</p>
          </div>
        </div>
        <div class="recommendation-actions" style="display:flex; align-items:center; gap:12px;">
          <span class="price-tag" style="font-size:1.15rem;">${formatCurrency(item.pricePerDay)} <small style="font-size:0.75rem; font-weight:normal; color:var(--text-muted);">/ day</small></span>
          <a href="booking.html?type=car&id=${item.id}" class="btn btn-sm btn-primary">Rent Car →</a>
        </div>
      `;
    } else if (type === 'package') {
      box.innerHTML = `
        <div class="recommendation-info">
          <span class="recommendation-badge">🎒 Curated Tour</span>
          <div class="recommendation-details">
            <h4 style="margin:0 0 4px; font-size:1rem; font-weight:700;">${escapeHtml(item.packageName)} • ${escapeHtml(item.durationDays)}D / ${escapeHtml(item.durationNights)}N</h4>
            <p style="margin:0; font-size:0.84rem; color:var(--text-muted);">📍 ${escapeHtml(item.placesCovered || 'Scenic Highlights')}</p>
          </div>
        </div>
        <div class="recommendation-actions" style="display:flex; align-items:center; gap:12px;">
          <span class="price-tag" style="font-size:1.15rem;">${formatCurrency(item.pricePerPerson)} <small style="font-size:0.75rem; font-weight:normal; color:var(--text-muted);">/ person</small></span>
          <a href="booking.html?type=package&id=${item.id}" class="btn btn-sm btn-primary">Book Package →</a>
        </div>
      `;
    }
  }

  // --- 1. Flights Autocomplete ---
  function initFlightsAutocomplete() {
    const originInput = document.getElementById('flightOrigin');
    const destInput = document.getElementById('flightDest');
    const dateInput = document.getElementById('flightDate');
    if (!originInput || !destInput) return;

    const originDrop = setupInput(originInput);
    const destDrop = setupInput(destInput);

    // Render Origin Options
    function renderOriginDropdown(query = '') {
      if (!cache.loaded) {
        originDrop.innerHTML = `<div class="autocomplete-loading">Loading available flight origins...</div>`;
        originDrop.classList.add('open');
        return;
      }

      const q = query.trim().toLowerCase();
      const availableFlights = cache.flights;

      // Distinct departure origins
      const allOrigins = [...new Set(availableFlights.map(f => f.origin).filter(Boolean))].sort();
      const filteredOrigins = q ? allOrigins.filter(o => o.toLowerCase().includes(q)) : allOrigins;

      // Matching direct routes
      const matchingRoutes = availableFlights.filter(f =>
        f.origin && (q ? f.origin.toLowerCase().includes(q) || f.destination.toLowerCase().includes(q) : true)
      );

      if (filteredOrigins.length === 0 && matchingRoutes.length === 0) {
        originDrop.innerHTML = `<div class="autocomplete-empty">No available flight origin found for "${escapeHtml(query)}"</div>`;
        originDrop.classList.add('open');
        return;
      }

      let html = '';

      if (filteredOrigins.length > 0) {
        html += `<div class="autocomplete-group-title">Available Departure Cities</div>`;
        filteredOrigins.forEach(orig => {
          const count = availableFlights.filter(f => f.origin.toLowerCase() === orig.toLowerCase()).length;
          html += `
            <div class="autocomplete-item" data-action="select-origin" data-value="${escapeHtml(orig)}">
              <div class="autocomplete-item-main">
                <span class="autocomplete-item-title">🛫 ${highlightMatch(orig, query)}</span>
                <span class="autocomplete-item-sub">${count} scheduled departures with open seats</span>
              </div>
              <span class="autocomplete-item-badge">${count} flights</span>
            </div>
          `;
        });
      }

      // Popular direct routes
      if (matchingRoutes.length > 0) {
        html += `<div class="autocomplete-group-title">Available Direct Routes</div>`;
        const seenRoutes = new Set();
        matchingRoutes.slice(0, 6).forEach(f => {
          const routeKey = `${f.origin} → ${f.destination}`.toLowerCase();
          if (!seenRoutes.has(routeKey)) {
            seenRoutes.add(routeKey);
            html += `
              <div class="autocomplete-item" data-action="select-route" data-origin="${escapeHtml(f.origin)}" data-dest="${escapeHtml(f.destination)}" data-flight-id="${f.id}">
                <div class="autocomplete-item-main">
                  <span class="autocomplete-item-title">✈ ${highlightMatch(f.origin, query)} → ${escapeHtml(f.destination)}</span>
                  <span class="autocomplete-item-sub">${escapeHtml(f.airline)} (${escapeHtml(f.flightNumber)}) • ⏰ ${escapeHtml(f.departureTime)} • 💺 ${f.availableSeats} seats left</span>
                </div>
                <span class="autocomplete-item-badge">${formatCurrency(f.price)}</span>
              </div>
            `;
          }
        });
      }

      originDrop.innerHTML = html;
      originDrop.classList.add('open');
    }

    // Origin Event Listeners (focus, click, input)
    originInput.addEventListener('focus', () => renderOriginDropdown(originInput.value));
    originInput.addEventListener('click', () => renderOriginDropdown(originInput.value));
    originInput.addEventListener('input', debounce(() => renderOriginDropdown(originInput.value)));

    // Origin Click Selection
    originDrop.addEventListener('click', (e) => {
      const item = e.target.closest('.autocomplete-item');
      if (!item) return;

      const action = item.getAttribute('data-action');
      if (action === 'select-origin') {
        const val = item.getAttribute('data-value');
        originInput.value = val;
        originDrop.classList.remove('open');
        // Immediately focus and trigger Destination dropdown
        destInput.focus();
        renderDestinationDropdown('');
      } else if (action === 'select-route') {
        const orig = item.getAttribute('data-origin');
        const dest = item.getAttribute('data-dest');
        const flightId = item.getAttribute('data-flight-id');
        originInput.value = orig;
        destInput.value = dest;
        originDrop.classList.remove('open');

        const matchedFlight = cache.flights.find(f => String(f.id) === String(flightId));
        if (matchedFlight) {
          if (dateInput && matchedFlight.departureDate) {
            dateInput.value = matchedFlight.departureDate;
          }
          showRecommendation('searchFormFlights', matchedFlight, 'flight');
        }
      }
    });

    // Render Destination Options
    function renderDestinationDropdown(query = '') {
      if (!cache.loaded) {
        destDrop.innerHTML = `<div class="autocomplete-loading">Loading available flight destinations...</div>`;
        destDrop.classList.add('open');
        return;
      }

      const q = query.trim().toLowerCase();
      const currentOrigin = originInput.value.trim().toLowerCase();

      // Filter eligible flights strictly based on selected origin if present
      let eligibleFlights = cache.flights;
      if (currentOrigin) {
        eligibleFlights = cache.flights.filter(f =>
          f.origin && f.origin.toLowerCase() === currentOrigin
        );
      }

      // Distinct available destinations from these flights
      const allDests = [...new Set(eligibleFlights.map(f => f.destination).filter(Boolean))].sort();
      const matchingDests = q ? allDests.filter(d => d.toLowerCase().includes(q)) : allDests;

      // Matching flight route listings
      const matchingFlightItems = eligibleFlights.filter(f =>
        q ? (f.destination && f.destination.toLowerCase().includes(q)) || (f.airline && f.airline.toLowerCase().includes(q)) : true
      );

      if (matchingDests.length === 0 && matchingFlightItems.length === 0) {
        const emptyMsg = currentOrigin
          ? `No available flights departing from ${escapeHtml(originInput.value.trim())} to "${escapeHtml(query)}"`
          : `No matching flight destination found for "${escapeHtml(query)}"`;
        destDrop.innerHTML = `<div class="autocomplete-empty">${emptyMsg}</div>`;
        destDrop.classList.add('open');
        return;
      }

      let html = '';

      // Destinations Group
      const titlePrefix = currentOrigin
        ? `Available Destinations from ${escapeHtml(originInput.value.trim())}`
        : `Available Destinations (Tip: Select an origin for direct routes)`;
      html += `<div class="autocomplete-group-title">${titlePrefix}</div>`;

      matchingDests.forEach(dest => {
        const routeFlights = eligibleFlights.filter(f => f.destination.toLowerCase() === dest.toLowerCase());
        const bestFlight = routeFlights[0];
        const minPrice = routeFlights.reduce((min, f) => (f.price < min ? f.price : min), routeFlights[0]?.price || 0);

        html += `
          <div class="autocomplete-item" data-action="select-dest" data-value="${escapeHtml(dest)}" data-flight-id="${bestFlight ? bestFlight.id : ''}">
            <div class="autocomplete-item-main">
              <span class="autocomplete-item-title">🛬 ${highlightMatch(dest, query)}</span>
              <span class="autocomplete-item-sub">${routeFlights.length} scheduled flight${routeFlights.length > 1 ? 's' : ''} • Starting from ${formatCurrency(minPrice)}</span>
            </div>
            <span class="autocomplete-item-badge">${formatCurrency(minPrice)}</span>
          </div>
        `;
      });

      // Direct Flight Details Group
      if (matchingFlightItems.length > 0) {
        html += `<div class="autocomplete-group-title">Available Scheduled Flights</div>`;
        matchingFlightItems.slice(0, 5).forEach(f => {
          html += `
            <div class="autocomplete-item" data-action="select-flight" data-origin="${escapeHtml(f.origin)}" data-dest="${escapeHtml(f.destination)}" data-flight-id="${f.id}">
              <div class="autocomplete-item-main">
                <span class="autocomplete-item-title">✈ ${highlightMatch(f.airline, query)} (${escapeHtml(f.flightNumber)})</span>
                <span class="autocomplete-item-sub">Route: ${escapeHtml(f.origin)} → ${highlightMatch(f.destination, query)} | ⏰ ${escapeHtml(f.departureTime)} – ${escapeHtml(f.arrivalTime)} | 💺 ${f.availableSeats} seats</span>
              </div>
              <span class="autocomplete-item-badge">${formatCurrency(f.price)}</span>
            </div>
          `;
        });
      }

      destDrop.innerHTML = html;
      destDrop.classList.add('open');
    }

    // Destination Event Listeners (focus, click, input)
    destInput.addEventListener('focus', () => renderDestinationDropdown(destInput.value));
    destInput.addEventListener('click', () => renderDestinationDropdown(destInput.value));
    destInput.addEventListener('input', debounce(() => renderDestinationDropdown(destInput.value)));

    // Destination Click Selection
    destDrop.addEventListener('click', (e) => {
      const item = e.target.closest('.autocomplete-item');
      if (!item) return;

      const action = item.getAttribute('data-action');
      if (action === 'select-dest') {
        const val = item.getAttribute('data-value');
        const flightId = item.getAttribute('data-flight-id');
        destInput.value = val;
        destDrop.classList.remove('open');

        if (flightId) {
          const matchedFlight = cache.flights.find(f => String(f.id) === String(flightId));
          if (matchedFlight) {
            if (dateInput && matchedFlight.departureDate) {
              dateInput.value = matchedFlight.departureDate;
            }
            showRecommendation('searchFormFlights', matchedFlight, 'flight');
          }
        }
      } else if (action === 'select-flight') {
        const orig = item.getAttribute('data-origin');
        const dest = item.getAttribute('data-dest');
        const flightId = item.getAttribute('data-flight-id');
        originInput.value = orig;
        destInput.value = dest;
        destDrop.classList.remove('open');

        const matchedFlight = cache.flights.find(f => String(f.id) === String(flightId));
        if (matchedFlight) {
          if (dateInput && matchedFlight.departureDate) {
            dateInput.value = matchedFlight.departureDate;
          }
          showRecommendation('searchFormFlights', matchedFlight, 'flight');
        }
      }
    });
  }

  // --- 2. Hotels Autocomplete (Strictly sorted by existing rating DESC) ---
  function initHotelsAutocomplete() {
    const locInput = document.getElementById('hotelLocation');
    if (!locInput) return;
    const drop = setupInput(locInput);

    function renderHotelDropdown(query = '') {
      if (!cache.loaded) {
        drop.innerHTML = `<div class="autocomplete-loading">Loading verified accommodations...</div>`;
        drop.classList.add('open');
        return;
      }

      const q = query.trim().toLowerCase();
      // Strictly available rooms > 0 and sorted by rating DESC
      const availableHotels = [...cache.hotels]
        .filter(h => (h.availableRooms || 0) > 0)
        .sort((a, b) => (parseFloat(b.rating) || 0) - (parseFloat(a.rating) || 0));

      // Distinct locations/cities
      const allLocs = [...new Set(availableHotels.map(h => h.location || h.address).filter(Boolean))].sort();
      const matchingLocs = q ? allLocs.filter(l => l.toLowerCase().includes(q)) : allLocs;

      // Matching hotels (by hotelName or location)
      const matchingHotels = availableHotels.filter(h =>
        q ? (h.hotelName && h.hotelName.toLowerCase().includes(q)) || (h.location && h.location.toLowerCase().includes(q)) : true
      );

      if (matchingLocs.length === 0 && matchingHotels.length === 0) {
        drop.innerHTML = `<div class="autocomplete-empty">No available hotels found in "${escapeHtml(query)}"</div>`;
        drop.classList.add('open');
        return;
      }

      let html = '';

      if (matchingLocs.length > 0) {
        html += `<div class="autocomplete-group-title">Cities & Tourist Regions</div>`;
        matchingLocs.slice(0, 5).forEach(loc => {
          const count = availableHotels.filter(h => (h.location || h.address || '').toLowerCase().includes(loc.toLowerCase())).length;
          html += `
            <div class="autocomplete-item" data-action="select-loc" data-value="${escapeHtml(loc)}">
              <div class="autocomplete-item-main">
                <span class="autocomplete-item-title">📍 ${highlightMatch(loc, query)}</span>
                <span class="autocomplete-item-sub">${count} verified hotel${count > 1 ? 's' : ''} with open rooms</span>
              </div>
              <span class="autocomplete-item-badge">${count} available</span>
            </div>
          `;
        });
      }

      if (matchingHotels.length > 0) {
        html += `<div class="autocomplete-group-title">Top-Rated Available Hotels (⭐ Highest First)</div>`;
        matchingHotels.slice(0, 5).forEach(h => {
          const ratingVal = h.rating ? `${h.rating} / 5.0` : '4.5';
          html += `
            <div class="autocomplete-item" data-action="select-hotel" data-name="${escapeHtml(h.hotelName)}" data-loc="${escapeHtml(h.location || h.address)}" data-id="${h.id}">
              <div class="autocomplete-item-main">
                <span class="autocomplete-item-title">🏨 ${highlightMatch(h.hotelName, query)}</span>
                <span class="autocomplete-item-sub">⭐ <b>${ratingVal}</b> • ${escapeHtml(h.roomType || 'Deluxe')} • 📍 ${escapeHtml(h.location || h.address)} • 🛏 ${h.availableRooms} rooms left</span>
              </div>
              <span class="autocomplete-item-badge">${formatCurrency(h.pricePerNight)}/nt</span>
            </div>
          `;
        });
      }

      drop.innerHTML = html;
      drop.classList.add('open');
    }

    locInput.addEventListener('focus', () => renderHotelDropdown(locInput.value));
    locInput.addEventListener('click', () => renderHotelDropdown(locInput.value));
    locInput.addEventListener('input', debounce(() => renderHotelDropdown(locInput.value)));

    drop.addEventListener('click', (e) => {
      const item = e.target.closest('.autocomplete-item');
      if (!item) return;

      const action = item.getAttribute('data-action');
      if (action === 'select-loc') {
        const loc = item.getAttribute('data-value');
        locInput.value = loc;
        drop.classList.remove('open');

        // Show top rated hotel for this location in preview
        const topHotelForLoc = cache.hotels
          .filter(h => (h.location || h.address || '').toLowerCase().includes(loc.toLowerCase()) && (h.availableRooms || 0) > 0)
          .sort((a, b) => (parseFloat(b.rating) || 0) - (parseFloat(a.rating) || 0))[0];
        if (topHotelForLoc) {
          showRecommendation('searchFormHotels', topHotelForLoc, 'hotel');
        }
      } else if (action === 'select-hotel') {
        const loc = item.getAttribute('data-loc');
        const hotelId = item.getAttribute('data-id');
        locInput.value = loc;
        drop.classList.remove('open');

        const matchedHotel = cache.hotels.find(h => String(h.id) === String(hotelId));
        if (matchedHotel) {
          showRecommendation('searchFormHotels', matchedHotel, 'hotel');
        }
      }
    });
  }

  // --- 3. Cars Autocomplete ---
  function initCarsAutocomplete() {
    const cityInput = document.getElementById('carCity');
    const typeSelect = document.getElementById('carTypeSelect');
    if (!cityInput) return;
    const drop = setupInput(cityInput);

    function renderCarDropdown(query = '') {
      if (!cache.loaded) {
        drop.innerHTML = `<div class="autocomplete-loading">Loading rental fleet...</div>`;
        drop.classList.add('open');
        return;
      }

      const q = query.trim().toLowerCase();
      // Strictly available units > 0, sorted by price ASC
      const availableCars = [...cache.cars]
        .filter(c => (c.availableUnits || 0) > 0)
        .sort((a, b) => (a.pricePerDay || 0) - (b.pricePerDay || 0));

      // Distinct pickup cities
      const allCities = [...new Set(availableCars.map(c => c.location).filter(Boolean))].sort();
      const matchingCities = q ? allCities.filter(c => c.toLowerCase().includes(q)) : allCities;

      // Matching vehicles
      const matchingCars = availableCars.filter(c =>
        q ? (c.carName && c.carName.toLowerCase().includes(q)) ||
            (c.brand && c.brand.toLowerCase().includes(q)) ||
            (c.location && c.location.toLowerCase().includes(q)) ||
            (c.carType && c.carType.toLowerCase().includes(q)) : true
      );

      if (matchingCities.length === 0 && matchingCars.length === 0) {
        drop.innerHTML = `<div class="autocomplete-empty">No available rental cars found in "${escapeHtml(query)}"</div>`;
        drop.classList.add('open');
        return;
      }

      let html = '';

      if (matchingCities.length > 0) {
        html += `<div class="autocomplete-group-title">Pickup Locations</div>`;
        matchingCities.forEach(city => {
          const count = availableCars.filter(c => (c.location || '').toLowerCase().includes(city.toLowerCase())).length;
          html += `
            <div class="autocomplete-item" data-action="select-city" data-value="${escapeHtml(city)}">
              <div class="autocomplete-item-main">
                <span class="autocomplete-item-title">📍 ${highlightMatch(city, query)}</span>
                <span class="autocomplete-item-sub">${count} vehicle${count > 1 ? 's' : ''} ready for dispatch</span>
              </div>
              <span class="autocomplete-item-badge">${count} available</span>
            </div>
          `;
        });
      }

      if (matchingCars.length > 0) {
        html += `<div class="autocomplete-group-title">Available Rental Vehicles</div>`;
        matchingCars.slice(0, 5).forEach(c => {
          html += `
            <div class="autocomplete-item" data-action="select-car" data-city="${escapeHtml(c.location)}" data-type="${escapeHtml(c.carType)}" data-id="${c.id}">
              <div class="autocomplete-item-main">
                <span class="autocomplete-item-title">🚗 ${highlightMatch(c.carName, query)} (${escapeHtml(c.brand)})</span>
                <span class="autocomplete-item-sub">📍 ${escapeHtml(c.location)} • ${escapeHtml(c.carType)} • 🚗 ${c.availableUnits} units available</span>
              </div>
              <span class="autocomplete-item-badge">${formatCurrency(c.pricePerDay)}/day</span>
            </div>
          `;
        });
      }

      drop.innerHTML = html;
      drop.classList.add('open');
    }

    cityInput.addEventListener('focus', () => renderCarDropdown(cityInput.value));
    cityInput.addEventListener('click', () => renderCarDropdown(cityInput.value));
    cityInput.addEventListener('input', debounce(() => renderCarDropdown(cityInput.value)));

    drop.addEventListener('click', (e) => {
      const item = e.target.closest('.autocomplete-item');
      if (!item) return;

      const action = item.getAttribute('data-action');
      if (action === 'select-city') {
        const city = item.getAttribute('data-value');
        cityInput.value = city;
        drop.classList.remove('open');

        const topCarForCity = cache.cars.find(c => (c.location || '').toLowerCase() === city.toLowerCase() && (c.availableUnits || 0) > 0);
        if (topCarForCity) {
          if (typeSelect && topCarForCity.carType) {
            typeSelect.value = topCarForCity.carType;
          }
          showRecommendation('searchFormCars', topCarForCity, 'car');
        }
      } else if (action === 'select-car') {
        const city = item.getAttribute('data-city');
        const type = item.getAttribute('data-type');
        const carId = item.getAttribute('data-id');
        cityInput.value = city;
        if (typeSelect && type) {
          typeSelect.value = type;
        }
        drop.classList.remove('open');

        const matchedCar = cache.cars.find(c => String(c.id) === String(carId));
        if (matchedCar) {
          showRecommendation('searchFormCars', matchedCar, 'car');
        }
      }
    });
  }

  // --- 4. Tour Packages Autocomplete ---
  function initPackagesAutocomplete() {
    const pkgInput = document.getElementById('packageKeyword');
    if (!pkgInput) return;
    const drop = setupInput(pkgInput);

    function renderPackageDropdown(query = '') {
      if (!cache.loaded) {
        drop.innerHTML = `<div class="autocomplete-loading">Loading curated packages...</div>`;
        drop.classList.add('open');
        return;
      }

      const q = query.trim().toLowerCase();
      const allPkgs = [...cache.packages].sort((a, b) => (a.pricePerPerson || 0) - (b.pricePerPerson || 0));

      const matchingPkgs = allPkgs.filter(p =>
        q ? (p.packageName && p.packageName.toLowerCase().includes(q)) ||
            (p.placesCovered && p.placesCovered.toLowerCase().includes(q)) ||
            (p.description && p.description.toLowerCase().includes(q)) : true
      );

      if (matchingPkgs.length === 0) {
        drop.innerHTML = `<div class="autocomplete-empty">No tour packages found matching "${escapeHtml(query)}"</div>`;
        drop.classList.add('open');
        return;
      }

      let html = `<div class="autocomplete-group-title">Recommended Holiday Packages</div>`;
      matchingPkgs.slice(0, 5).forEach(p => {
        html += `
          <div class="autocomplete-item" data-action="select-pkg" data-name="${escapeHtml(p.packageName)}" data-id="${p.id}">
            <div class="autocomplete-item-main">
              <span class="autocomplete-item-title">🎒 ${highlightMatch(p.packageName, query)}</span>
              <span class="autocomplete-item-sub">⏱ ${p.durationDays}D / ${p.durationNights}N • 📍 ${highlightMatch(p.placesCovered || '', query)}</span>
            </div>
            <span class="autocomplete-item-badge">${formatCurrency(p.pricePerPerson)}</span>
          </div>
        `;
      });

      drop.innerHTML = html;
      drop.classList.add('open');
    }

    pkgInput.addEventListener('focus', () => renderPackageDropdown(pkgInput.value));
    pkgInput.addEventListener('click', () => renderPackageDropdown(pkgInput.value));
    pkgInput.addEventListener('input', debounce(() => renderPackageDropdown(pkgInput.value)));

    drop.addEventListener('click', (e) => {
      const item = e.target.closest('.autocomplete-item');
      if (!item) return;

      const name = item.getAttribute('data-name');
      const pkgId = item.getAttribute('data-id');
      pkgInput.value = name;
      drop.classList.remove('open');

      const matchedPkg = cache.packages.find(p => String(p.id) === String(pkgId));
      if (matchedPkg) {
        showRecommendation('searchFormPackages', matchedPkg, 'package');
      }
    });
  }

  // Global listeners (Click outside and escape)
  function initGlobalListeners() {
    document.addEventListener('click', (e) => {
      if (!e.target.closest('.autocomplete-wrapper')) {
        closeAllDropdowns();
      }
    });

    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape') {
        closeAllDropdowns();
      }
    });
  }

  // Public Initialization Method
  async function init() {
    await loadData();
    initFlightsAutocomplete();
    initHotelsAutocomplete();
    initCarsAutocomplete();
    initPackagesAutocomplete();
    initGlobalListeners();
  }

  return {
    init,
    getCache: () => cache,
    clearRecommendation: (tabId) => {
      const form = document.getElementById(tabId);
      if (form && form.parentElement) {
        const box = form.parentElement.querySelector('.search-recommendation-box');
        if (box) box.remove();
      }
    }
  };
})();

// Auto-initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
  SearchWidget.init();
});
