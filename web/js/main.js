/**
 * Main Frontend Utilities & Layout Ingestion
 */

// Toast notifications
function showToast(message, type = 'success') {
  let container = document.getElementById('toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toast-container';
    document.body.appendChild(container);
  }

  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.textContent = message;
  container.appendChild(toast);

  setTimeout(() => {
    toast.remove();
  }, 4000);
}

// Modal handling
function openModal(id) {
  const m = document.getElementById(id);
  if (m) {
    m.style.display = 'flex';
    m.classList.add('open');
  }
}

function closeModal(id) {
  const m = document.getElementById(id);
  if (m) {
    m.style.display = 'none';
    m.classList.remove('open');
  }
}

// Format Currency
function formatCurrency(val) {
  const num = parseFloat(val) || 0;
  return '₹' + num.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

// URL param helper
function getQueryParam(name) {
  const urlParams = new URLSearchParams(window.location.search);
  return urlParams.get(name);
}

// HTML escape helper
function escapeHtml(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

// Fallback image helper with contextual categories
function handleImageError(img, type = 'travel') {
  if (!img) return;
  img.onerror = null;
  const fallbacks = {
    car: 'https://images.unsplash.com/photo-1549399542-7e3f8b79c341?w=800&auto=format&fit=crop',
    hotel: 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&auto=format&fit=crop',
    destination: 'https://images.unsplash.com/photo-1512343879784-a960bf40e7f2?w=800&auto=format&fit=crop',
    package: 'https://images.unsplash.com/photo-1512343879784-a960bf40e7f2?w=800&auto=format&fit=crop',
    travel: 'https://images.unsplash.com/photo-1488646953014-85cb44e25828?w=800&auto=format&fit=crop'
  };
  img.src = fallbacks[type] || fallbacks.travel;
}

// ---------------------------------------------------------------------------
// Contextual Image Mapping Helpers (Hotels, Cars, Destinations, Packages)
// ---------------------------------------------------------------------------

// 1. Car Image Mapping - matches car name, model, brand, and type
function getCarImageUrl(car) {
  if (!car) return 'https://images.unsplash.com/photo-1549399542-7e3f8b79c341?w=800&auto=format&fit=crop';

  const name = (car.carName || '').toLowerCase();
  const brand = (car.brand || '').toLowerCase();
  const model = (car.model || '').toLowerCase();
  const type = (car.carType || '').toLowerCase();
  const text = `${name} ${brand} ${model} ${type}`;

  // 1. Mahindra Thar - ONLY when "thar" is explicitly present in name or model
  if (text.includes('thar')) {
    return 'https://images.unsplash.com/photo-1506015391300-4802dc74de2e?w=800&auto=format&fit=crop';
  }

  // 2. Toyota Fortuner
  if (text.includes('fortuner')) {
    return 'https://images.unsplash.com/photo-1519641471654-76ce0107ad1b?w=800&auto=format&fit=crop';
  }

  // 3. Toyota Innova / Crysta (MPV / Family Tourer)
  if (text.includes('innova') || text.includes('crysta') || text.includes('ertiga')) {
    return 'https://images.unsplash.com/photo-1559416523-140ddc3d238c?w=800&auto=format&fit=crop';
  }

  // 4. Hyundai Creta
  if (text.includes('creta')) {
    return 'https://images.unsplash.com/photo-1508974239320-0a029497e820?w=800&auto=format&fit=crop';
  }

  // 5. Honda City / Midsize Sedan
  if (text.includes('city') || text.includes('verna') || text.includes('civic') || text.includes('ciaz') || text.includes('dzire')) {
    return 'https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=800&auto=format&fit=crop';
  }

  // 6. Maruti Suzuki Swift / Hatchback
  if (text.includes('swift') || text.includes('i20') || text.includes('baleno') || text.includes('polo') || text.includes('altroz') || text.includes('tiago')) {
    return 'https://images.unsplash.com/photo-1541899481282-d53bffe3c35d?w=800&auto=format&fit=crop';
  }

  // 7. Mercedes-Benz / Luxury Sedans
  if (text.includes('mercedes') || text.includes('c-class') || text.includes('c200') || text.includes('bmw') || text.includes('audi') || text.includes('benz')) {
    return 'https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?w=800&auto=format&fit=crop';
  }

  // 8. Tata Nexon EV / Electric Crossover
  if (text.includes('nexon') || text.includes('ev') || text.includes('electric')) {
    return 'https://images.unsplash.com/photo-1563720223185-11003d516935?w=800&auto=format&fit=crop';
  }

  // 9. Kia Seltos / Contemporary Crossover SUV
  if (text.includes('seltos') || text.includes('taigun') || text.includes('kushaq') || text.includes('grand vitara')) {
    return 'https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7?w=800&auto=format&fit=crop';
  }

  // 10. Mahindra Scorpio / Safari / Harrier (Rugged Mid-size SUV)
  if (text.includes('scorpio') || text.includes('harrier') || text.includes('safari') || text.includes('xuv') || text.includes('endeavour')) {
    return 'https://images.unsplash.com/photo-1533106497176-45ae19e68ba2?w=800&auto=format&fit=crop';
  }

  // 11. If car has a valid custom image URL that is NOT one of the old stock duplicates or Thar:
  if (car.imageUrl &&
      !car.imageUrl.includes('photo-1533473359331-0135ef1b58bf') &&
      !car.imageUrl.includes('photo-1502877338535-766e1452684a') &&
      !car.imageUrl.includes('photo-1503376780353-7e6692767b70') &&
      !car.imageUrl.includes('photo-1506015391300-4802dc74de2e') &&
      car.imageUrl.startsWith('http')) {
    return car.imageUrl;
  }

  // 12. General vehicle-type fallbacks (NEVER Thar!)
  if (type.includes('suv') || text.includes('suv') || text.includes('4x4')) {
    return 'https://images.unsplash.com/photo-1549399542-7e3f8b79c341?w=800&auto=format&fit=crop';
  }
  if (type.includes('sedan') || text.includes('sedan')) {
    return 'https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=800&auto=format&fit=crop';
  }
  if (type.includes('hatchback') || text.includes('hatchback')) {
    return 'https://images.unsplash.com/photo-1541899481282-d53bffe3c35d?w=800&auto=format&fit=crop';
  }
  if (type.includes('muv') || type.includes('van') || text.includes('van') || text.includes('muv')) {
    return 'https://images.unsplash.com/photo-1559416523-140ddc3d238c?w=800&auto=format&fit=crop';
  }
  if (type.includes('luxury') || text.includes('luxury')) {
    return 'https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?w=800&auto=format&fit=crop';
  }

  return 'https://images.unsplash.com/photo-1549399542-7e3f8b79c341?w=800&auto=format&fit=crop';
}

// 2. Destination Image Mapping - matches destination name and state
function getDestinationImageUrl(dest) {
  if (!dest) return 'https://images.unsplash.com/photo-1512343879784-a960bf40e7f2?w=800&auto=format&fit=crop';

  const name = typeof dest === 'string' ? dest : (dest.name || dest.destinationName || dest.location || dest.state || '');
  const text = name.toLowerCase();

  if (text.includes('goa')) {
    return 'https://images.unsplash.com/photo-1512343879784-a960bf40e7f2?w=800&auto=format&fit=crop';
  }
  if (text.includes('manali') || text.includes('himachal') || text.includes('solang') || text.includes('rohtang')) {
    return 'https://images.unsplash.com/photo-1626621341517-bbf3d9990a23?w=800&auto=format&fit=crop';
  }
  if (text.includes('jaipur') || text.includes('hawa mahal') || text.includes('amer')) {
    return 'https://images.unsplash.com/photo-1477587458883-47145ed94245?w=800&auto=format&fit=crop';
  }
  if (text.includes('kashmir') || text.includes('srinagar') || text.includes('dal lake') || text.includes('gulmarg') || text.includes('pahalgam')) {
    return 'https://images.unsplash.com/photo-1595846519845-68e298c2edd8?w=800&auto=format&fit=crop';
  }
  if (text.includes('delhi')) {
    return 'https://images.unsplash.com/photo-1587474260584-136574528ed5?w=800&auto=format&fit=crop';
  }
  if (text.includes('kerala') || text.includes('alleppey') || text.includes('munnar') || text.includes('kochi')) {
    return 'https://images.unsplash.com/photo-1602216056096-3b40cc0c9944?w=800&auto=format&fit=crop';
  }
  if (text.includes('rishikesh') || text.includes('haridwar') || text.includes('ganga') || text.includes('uttarakhand')) {
    return 'https://images.unsplash.com/photo-1544735716-392fe2489ffa?w=800&auto=format&fit=crop';
  }
  if (text.includes('udaipur') || text.includes('pichola')) {
    return 'https://images.unsplash.com/photo-1566837945700-30057527ade0?w=800&auto=format&fit=crop';
  }
  if (text.includes('agra') || text.includes('taj')) {
    return 'https://images.unsplash.com/photo-1564507592333-c60657eea523?w=800&auto=format&fit=crop';
  }
  if (text.includes('mumbai') || text.includes('bombay')) {
    return 'https://images.unsplash.com/photo-1570168007204-dfb528c6958f?w=800&auto=format&fit=crop';
  }
  if (text.includes('bengaluru') || text.includes('bangalore')) {
    return 'https://images.unsplash.com/photo-1596176530529-78163a4f7af2?w=800&auto=format&fit=crop';
  }

  if (typeof dest === 'object' && dest.image_url && dest.image_url.startsWith('http')) {
    return dest.image_url;
  }

  return 'https://images.unsplash.com/photo-1512343879784-a960bf40e7f2?w=800&auto=format&fit=crop';
}

// 3. Hotel Image Mapping - unique, visually matching images for each hotel
const HOTEL_PALETTE = [
  'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&auto=format&fit=crop', // Taj Cidade Goa beach resort
  'https://images.unsplash.com/photo-1582719508461-905c673771fd?w=800&auto=format&fit=crop', // Goa Palms tropical resort pool
  'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800&auto=format&fit=crop', // Himalayan stone & pine spa resort
  'https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=800&auto=format&fit=crop', // Snow Valley mountain lodge
  'https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=800&auto=format&fit=crop', // ITC Rajputana palace haveli
  'https://images.unsplash.com/photo-1564501049412-61c2a3083791?w=800&auto=format&fit=crop', // Hotel Pearl Palace heritage
  'https://images.unsplash.com/photo-1596394516093-501ba68a0ba6?w=800&auto=format&fit=crop', // Wangnoo luxury Dal Lake houseboat
  'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800&auto=format&fit=crop', // The Lalit Grand Palace Srinagar
  'https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=800&auto=format&fit=crop', // The Imperial New Delhi art-deco
  'https://images.unsplash.com/photo-1584132967334-10e028bd69f7?w=800&auto=format&fit=crop', // Lake Song Backwater Resort Kerala
  'https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?w=800&auto=format&fit=crop', // Grand Azure resort / seaside villa
  'https://images.unsplash.com/photo-1540541338287-41700207dee6?w=800&auto=format&fit=crop', // Seaside Palm resort pool
  'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800&auto=format&fit=crop', // Modern boutique hotel suite
  'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800&auto=format&fit=crop', // Elegant luxury bedroom
  'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=800&auto=format&fit=crop'  // Executive hotel suite
];

function getHotelImageUrl(hotel) {
  if (!hotel) return HOTEL_PALETTE[0];

  const name = (hotel.hotelName || '').toLowerCase();
  const loc = (hotel.location || hotel.address || hotel.destinationName || '').toLowerCase();
  const desc = (hotel.description || '').toLowerCase();
  const text = `${name} ${loc} ${desc}`;

  // Specific hotel names:
  if (text.includes('cidade') || (text.includes('taj') && text.includes('goa'))) {
    return HOTEL_PALETTE[0]; // Taj Cidade de Goa
  }
  if (text.includes('goa palms') || text.includes('calangute')) {
    return HOTEL_PALETTE[1]; // Goa Palms Beach Resort
  }
  if (text.includes('himalayan') || text.includes('hadimba')) {
    return HOTEL_PALETTE[2]; // The Himalayan Spa Resort
  }
  if (text.includes('snow valley') || text.includes('log hut')) {
    return HOTEL_PALETTE[3]; // Snow Valley Mountain View Hotel
  }
  if (text.includes('rajputana') || (text.includes('itc') && text.includes('jaipur'))) {
    return HOTEL_PALETTE[4]; // ITC Rajputana Palace
  }
  if (text.includes('pearl palace') || text.includes('hathroi')) {
    return HOTEL_PALETTE[5]; // Hotel Pearl Palace Heritage
  }
  if (text.includes('wangnoo') || text.includes('houseboat')) {
    return HOTEL_PALETTE[6]; // Wangnoo Luxury Houseboats
  }
  if (text.includes('lalit') || (text.includes('grand palace') && text.includes('srinagar'))) {
    return HOTEL_PALETTE[7]; // The Lalit Grand Palace Srinagar
  }
  if (text.includes('imperial') || (text.includes('delhi') && (text.includes('heritage') || text.includes('janpath')))) {
    return HOTEL_PALETTE[8]; // The Imperial New Delhi
  }
  if (text.includes('lake song') || text.includes('vembanad') || text.includes('kumarakom')) {
    return HOTEL_PALETTE[9]; // Lake Song Backwater Resort
  }
  if (text.includes('azure')) {
    return HOTEL_PALETTE[10]; // Grand Azure Resort
  }
  if (text.includes('seaside palm') || text.includes('palm villa')) {
    return HOTEL_PALETTE[11]; // Seaside Palm Villa
  }

  // Location-based themes:
  if (loc.includes('manali') || text.includes('snow') || text.includes('mountain') || text.includes('alpine')) {
    return HOTEL_PALETTE[3];
  }
  if (loc.includes('kashmir') || loc.includes('srinagar') || text.includes('dal lake')) {
    return HOTEL_PALETTE[6];
  }
  if (loc.includes('jaipur') || loc.includes('udaipur') || text.includes('palace') || text.includes('haveli')) {
    return HOTEL_PALETTE[4];
  }
  if (loc.includes('kerala') || text.includes('backwater') || text.includes('ayurveda')) {
    return HOTEL_PALETTE[9];
  }
  if (loc.includes('goa') || text.includes('beach') || text.includes('sea view') || text.includes('coastal')) {
    return HOTEL_PALETTE[1];
  }
  if (loc.includes('delhi') || text.includes('business') || text.includes('city center')) {
    return HOTEL_PALETTE[8];
  }

  // If a valid custom image URL was provided and isn't the single generic fallback
  if (hotel.imageUrl && !hotel.imageUrl.includes('photo-1566073771259-6a8506099945') && hotel.imageUrl.startsWith('http')) {
    return hotel.imageUrl;
  }

  // Deterministic rotation based on ID or name hash to ensure uniqueness
  const idNum = parseInt(hotel.id, 10);
  if (!isNaN(idNum) && idNum > 0) {
    return HOTEL_PALETTE[idNum % HOTEL_PALETTE.length];
  }
  let hash = 0;
  for (let i = 0; i < name.length; i++) {
    hash = (hash * 31 + name.charCodeAt(i)) % HOTEL_PALETTE.length;
  }
  return HOTEL_PALETTE[Math.abs(hash) % HOTEL_PALETTE.length];
}

// 4. Tour Package Image Mapping
function getPackageImageUrl(pkg) {
  if (!pkg) return 'https://images.unsplash.com/photo-1512343879784-a960bf40e7f2?w=800&auto=format&fit=crop';

  const text = `${pkg.packageName || ''} ${pkg.placesCovered || ''} ${pkg.description || ''}`.toLowerCase();

  if (text.includes('goa') || text.includes('beach') || text.includes('carnival')) {
    return 'https://images.unsplash.com/photo-1512343879784-a960bf40e7f2?w=800&auto=format&fit=crop';
  }
  if (text.includes('manali') || text.includes('snow') || text.includes('adventure') || text.includes('solang')) {
    return 'https://images.unsplash.com/photo-1626621341517-bbf3d9990a23?w=800&auto=format&fit=crop';
  }
  if (text.includes('jaipur') || text.includes('palace') || text.includes('rajasthan') || text.includes('heritage')) {
    return 'https://images.unsplash.com/photo-1477587458883-47145ed94245?w=800&auto=format&fit=crop';
  }
  if (text.includes('kashmir') || text.includes('honeymoon') || text.includes('srinagar') || text.includes('shikara')) {
    return 'https://images.unsplash.com/photo-1595846519845-68e298c2edd8?w=800&auto=format&fit=crop';
  }
  if (text.includes('delhi') || text.includes('cultural trail') || text.includes('monuments')) {
    return 'https://images.unsplash.com/photo-1587474260584-136574528ed5?w=800&auto=format&fit=crop';
  }
  if (text.includes('kerala') || text.includes('backwater') || text.includes('munnar')) {
    return 'https://images.unsplash.com/photo-1602216056096-3b40cc0c9944?w=800&auto=format&fit=crop';
  }
  if (text.includes('rishikesh') || text.includes('ganga') || text.includes('rafting')) {
    return 'https://images.unsplash.com/photo-1544735716-392fe2489ffa?w=800&auto=format&fit=crop';
  }
  if (text.includes('udaipur') || text.includes('lakes')) {
    return 'https://images.unsplash.com/photo-1566837945700-30057527ade0?w=800&auto=format&fit=crop';
  }

  if (pkg.imageUrl && !pkg.imageUrl.includes('photo-1512343879784-a960bf40e7f2') && pkg.imageUrl.startsWith('http')) {
    return pkg.imageUrl;
  }

  return getDestinationImageUrl(pkg.placesCovered || pkg.packageName);
}

// Auto-inject standard header and footer if placeholder exists
document.addEventListener('DOMContentLoaded', () => {
  const mainHeader = document.getElementById('main-header');
  if (mainHeader && !mainHeader.innerHTML.trim()) {
    const curPath = window.location.pathname;
    mainHeader.innerHTML = `
      <!-- Demo Bar -->
      <div class="demo-bar">
        <span>🎓 <b>College Project Demo Mode:</b> <span id="demoUserText">Not logged in</span></span>
        <div>
          <span>Quick Switch:</span>
          <button onclick="Auth.quickDemoLogin('traveler')">Traveler (John)</button>
          <button onclick="Auth.quickDemoLogin('agent')">Travel Agent (Skyline)</button>
          <button onclick="Auth.quickDemoLogin('admin')">Admin</button>
        </div>
      </div>

      <!-- Navbar -->
      <header class="navbar">
        <div class="nav-container">
          <a href="index.html" class="nav-brand">✈ Voyage<span>Quest</span></a>
          <ul class="nav-links">
            <li><a href="index.html" class="${curPath.includes('index.html') || curPath.endsWith('/') ? 'active' : ''}">Home</a></li>
            <li><a href="flights.html" class="${curPath.includes('flights.html') ? 'active' : ''}">Flights</a></li>
            <li><a href="hotels.html" class="${curPath.includes('hotels.html') ? 'active' : ''}">Hotels</a></li>
            <li><a href="cars.html" class="${curPath.includes('cars.html') ? 'active' : ''}">Rental Cars</a></li>
            <li><a href="packages.html" class="${curPath.includes('packages.html') ? 'active' : ''}">Packages</a></li>
            <li><a href="my-bookings.html" class="${curPath.includes('my-bookings.html') ? 'active' : ''}">My Bookings</a></li>
            <li><a href="itinerary.html" class="${curPath.includes('itinerary.html') ? 'active' : ''}">Itinerary</a></li>
          </ul>
          <div class="nav-auth" id="navAuth"></div>
        </div>
      </header>
    `;
    if (typeof Auth !== 'undefined' && Auth.renderNavbar) {
      Auth.renderNavbar();
    }
  }

  const mainFooter = document.getElementById('main-footer');
  if (mainFooter && !mainFooter.innerHTML.trim()) {
    mainFooter.innerHTML = `
      <footer class="footer">
        <div class="container footer-content">
          <div>
            <div class="footer-brand">✈ Voyage<span>Quest</span></div>
            <p>Comprehensive Online Travel Booking & Tourism Management Platform. Built with core Java JDK, JDBC, and MySQL database.</p>
          </div>
          <div>
            <h4>Travel Services</h4>
            <ul class="footer-links">
              <li><a href="flights.html">Airline Flights</a></li>
              <li><a href="hotels.html">Hotels & Resorts</a></li>
              <li><a href="cars.html">Self-Drive & Rental Cars</a></li>
              <li><a href="packages.html">Holiday Packages</a></li>
            </ul>
          </div>
          <div>
            <h4>Member Portals</h4>
            <ul class="footer-links">
              <li><a href="traveler-dashboard.html">Traveler Dashboard</a></li>
              <li><a href="agent-dashboard.html">Travel Agent Hub</a></li>
              <li><a href="admin-dashboard.html">Admin Console</a></li>
              <li><a href="itinerary.html">Trip Itinerary</a></li>
            </ul>
          </div>
          <div>
            <h4>College Project Specs</h4>
            <ul class="footer-links">
              <li><span>Architecture: MVC / Clean DAO</span></li>
              <li><span>Backend: Java HttpServer & JDBC</span></li>
              <li><span>Security: SHA-256 Hashing</span></li>
              <li><span>Database: MySQL 8.0</span></li>
            </ul>
          </div>
        </div>
        <div class="footer-bottom">
          &copy; ${new Date().getFullYear()} VoyageQuest Online Travel Platform. Designed for B.Tech CSE Final / Minor Project Demonstration.
        </div>
      </footer>
    `;
  }
});
