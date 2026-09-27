/**
 * API Client Module - Centralizes all HTTP communication with the Java backend REST endpoints.
 * Includes automatic session token header injection and robust parameter serialization.
 */
const API = {

  getAuthHeaders(customHeaders = {}) {
    const headers = { 'Content-Type': 'application/json', ...customHeaders };
    let token = '';
    if (typeof Auth !== 'undefined' && Auth.getToken) {
      token = Auth.getToken();
    }
    if (!token && typeof localStorage !== 'undefined') {
      token = localStorage.getItem('voyage_token') || '';
    }
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
      headers['X-Session-Token'] = token;
    }
    return headers;
  },

  async fetchAuth(url, options = {}) {
    options.headers = this.getAuthHeaders(options.headers);
    return fetch(url, options);
  },

  // Authentication
  async login(email, password) {
    const res = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });
    return res.json();
  },

  async register(fullName, email, phone, password, confirmPassword, role = 'TRAVELER') {
    const res = await fetch('/api/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ fullName, email, phone, password, confirmPassword, role })
    });
    return res.json();
  },

  // Flights
  async getFlights(filters = {}) {
    const query = new URLSearchParams(filters).toString();
    const res = await this.fetchAuth(`/api/flights${query ? '?' + query : ''}`);
    return res.json();
  },

  async addFlight(flightData) {
    const res = await this.fetchAuth('/api/flights', {
      method: 'POST',
      body: JSON.stringify(flightData)
    });
    return res.json();
  },

  // Hotels
  async getHotels(filters = {}) {
    const query = new URLSearchParams(filters).toString();
    const res = await this.fetchAuth(`/api/hotels${query ? '?' + query : ''}`);
    return res.json();
  },

  async addHotel(hotelData) {
    const res = await this.fetchAuth('/api/hotels', {
      method: 'POST',
      body: JSON.stringify(hotelData)
    });
    return res.json();
  },

  // Cars
  async getCars(filters = {}) {
    const query = new URLSearchParams(filters).toString();
    const res = await this.fetchAuth(`/api/cars${query ? '?' + query : ''}`);
    return res.json();
  },

  async addCar(carData) {
    const res = await this.fetchAuth('/api/cars', {
      method: 'POST',
      body: JSON.stringify(carData)
    });
    return res.json();
  },

  // Tour Packages
  async getPackages(filters = {}) {
    const query = new URLSearchParams(filters).toString();
    const res = await this.fetchAuth(`/api/packages${query ? '?' + query : ''}`);
    return res.json();
  },

  async addPackage(pkgData) {
    const res = await this.fetchAuth('/api/packages', {
      method: 'POST',
      body: JSON.stringify(pkgData)
    });
    return res.json();
  },

  // Destinations
  async getDestinations() {
    const res = await this.fetchAuth('/api/destinations');
    return res.json();
  },

  // Bookings
  async getBookings(params = {}) {
    let queryObj = {};
    if (typeof params === 'number' || typeof params === 'string') {
      queryObj.userId = params;
    } else if (params && typeof params === 'object') {
      queryObj = { ...params };
    }

    // Attach active user ID hint if not specified
    if (!queryObj.userId && !queryObj.agentId && typeof Auth !== 'undefined' && Auth.getUser) {
      const user = Auth.getUser();
      if (user && user.id) {
        if (user.role === 'AGENT') queryObj.agentId = user.id;
        else if (user.role !== 'ADMIN') queryObj.userId = user.id;
      }
    }

    const query = new URLSearchParams(queryObj).toString();
    const res = await this.fetchAuth(`/api/bookings${query ? '?' + query : ''}`);
    return res.json();
  },

  async createBooking(bookingData) {
    const res = await this.fetchAuth('/api/bookings', {
      method: 'POST',
      body: JSON.stringify(bookingData)
    });
    return res.json();
  },

  async cancelBooking(bookingId, userId, isAdmin = false) {
    const res = await this.fetchAuth('/api/bookings/cancel', {
      method: 'POST',
      body: JSON.stringify({ bookingId, userId, isAdmin })
    });
    return res.json();
  },

  // Itinerary
  async getItinerary(userId) {
    if (!userId && typeof Auth !== 'undefined' && Auth.getUser) {
      const user = Auth.getUser();
      if (user && user.id) userId = user.id;
    }
    const res = await this.fetchAuth(`/api/itinerary${userId ? '?userId=' + userId : ''}`);
    return res.json();
  },

  // Messages
  async getMessages(params = {}) {
    let queryObj = {};
    if (typeof params === 'number' || typeof params === 'string') {
      queryObj.userId = params;
    } else if (params && typeof params === 'object') {
      queryObj = { ...params };
    }

    if (!queryObj.userId && !queryObj.agentId && typeof Auth !== 'undefined' && Auth.getUser) {
      const user = Auth.getUser();
      if (user && user.id) {
        if (user.role === 'AGENT') queryObj.agentId = user.id;
        else if (user.role !== 'ADMIN') queryObj.userId = user.id;
      }
    }

    const query = new URLSearchParams(queryObj).toString();
    const res = await this.fetchAuth(`/api/messages${query ? '?' + query : ''}`);
    return res.json();
  },

  async sendMessage(userId, agentId, subject, message) {
    const res = await this.fetchAuth('/api/messages', {
      method: 'POST',
      body: JSON.stringify({ userId, agentId, subject, message })
    });
    return res.json();
  },

  async replyMessage(messageId, reply) {
    const res = await this.fetchAuth('/api/messages/reply', {
      method: 'POST',
      body: JSON.stringify({ messageId, reply })
    });
    return res.json();
  },

  // Agent Portal
  async getAgentStats(agentId) {
    const res = await this.fetchAuth(`/api/agent/stats${agentId ? '?agentId=' + agentId : ''}`);
    return res.json();
  },

  async getAgentListings(agentId) {
    const res = await this.fetchAuth(`/api/agent/listings${agentId ? '?agentId=' + agentId : ''}`);
    return res.json();
  },

  async getAgentBookings(agentId) {
    const res = await this.fetchAuth(`/api/agent/bookings${agentId ? '?agentId=' + agentId : ''}`);
    return res.json();
  },

  async deleteAgentListing(type, id) {
    const res = await this.fetchAuth('/api/agent/listings', {
      method: 'POST',
      body: JSON.stringify({ action: 'delete', type, id })
    });
    return res.json();
  },

  // Admin Portal
  async getAdminStats() {
    const res = await this.fetchAuth('/api/admin/stats');
    return res.json();
  },

  async getAdminUsers() {
    const res = await this.fetchAuth('/api/admin/users');
    return res.json();
  },

  async toggleUserStatus(userId, status) {
    const res = await this.fetchAuth('/api/admin/toggle-user', {
      method: 'POST',
      body: JSON.stringify({ userId, status })
    });
    return res.json();
  },

  async getAdminListings() {
    const res = await this.fetchAuth('/api/admin/listings');
    return res.json();
  },

  async moderateListing(action, type, id) {
    const res = await this.fetchAuth('/api/admin/listings', {
      method: 'POST',
      body: JSON.stringify({ action, type, id })
    });
    return res.json();
  },

  async getAdminBookings() {
    const res = await this.fetchAuth('/api/admin/bookings');
    return res.json();
  },

  async updateBookingStatus(bookingId, status) {
    const res = await this.fetchAuth('/api/admin/update-booking-status', {
      method: 'POST',
      body: JSON.stringify({ bookingId, status })
    });
    return res.json();
  },

  async getAdminPayments() {
    const res = await this.fetchAuth('/api/admin/payments');
    return res.json();
  },

  async getAdminSettings() {
    const res = await this.fetchAuth('/api/admin/settings');
    return res.json();
  },

  async updateAdminSettings(settings) {
    const res = await this.fetchAuth('/api/admin/settings', {
      method: 'POST',
      body: JSON.stringify(settings)
    });
    return res.json();
  }
};

const Api = API;
if (typeof window !== 'undefined') {
  window.API = API;
  window.Api = API;
}
