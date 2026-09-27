/**
 * Authentication and Session Management Module
 */
const Auth = {
  getUser() {
    const data = localStorage.getItem('voyage_user');
    if (!data) return null;
    try {
      return JSON.parse(data);
    } catch (e) {
      return null;
    }
  },

  getToken() {
    const u = this.getUser();
    if (u && u.token) return u.token;
    return localStorage.getItem('voyage_token') || '';
  },

  setUser(user, token = null) {
    if (user) {
      if (token) user.token = token;
      else if (!user.token && localStorage.getItem('voyage_token')) {
        user.token = localStorage.getItem('voyage_token');
      }
      localStorage.setItem('voyage_user', JSON.stringify(user));
      if (user.token) {
        localStorage.setItem('voyage_token', user.token);
        document.cookie = `session_token=${user.token}; path=/; max-age=86400; SameSite=Lax`;
      }
    } else {
      localStorage.removeItem('voyage_user');
      localStorage.removeItem('voyage_token');
      document.cookie = 'session_token=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT';
    }
  },

  logout() {
    this.setUser(null);
    window.location.href = 'login.html';
  },

  isLoggedIn() {
    return this.getUser() !== null;
  },

  isAdmin() {
    const u = this.getUser();
    return u && u.role === 'ADMIN';
  },

  isAgent() {
    const u = this.getUser();
    return u && u.role === 'AGENT';
  },

  isTraveler() {
    const u = this.getUser();
    return u && (u.role === 'TRAVELER' || u.role === 'USER');
  },

  requireAuth(allowedRoles = []) {
    const user = this.getUser();
    if (!user) {
      alert('Please log in to access this page.');
      window.location.href = 'login.html';
      return false;
    }
    if (allowedRoles.length > 0 && !allowedRoles.includes(user.role)) {
      alert(`Access denied. This page is restricted to ${allowedRoles.join('/')} accounts.`);
      if (user.role === 'ADMIN') window.location.href = 'admin-dashboard.html';
      else if (user.role === 'AGENT') window.location.href = 'agent-dashboard.html';
      else window.location.href = 'traveler-dashboard.html';
      return false;
    }
    return true;
  },

  renderNavbar() {
    const navAuth = document.getElementById('navAuth');
    const demoUserText = document.getElementById('demoUserText');
    const user = this.getUser();

    if (demoUserText) {
      if (user) {
        demoUserText.innerHTML = `Logged in as: <b>${user.fullName}</b> (${user.role})`;
      } else {
        demoUserText.innerHTML = `Not logged in (Guest). Select demo role to test:`;
      }
    }

    if (!navAuth) return;

    if (user) {
      let dashboardUrl = 'traveler-dashboard.html';
      let dashboardLabel = 'Traveler Hub';
      let badgeClass = 'badge-primary';

      if (user.role === 'ADMIN') {
        dashboardUrl = 'admin-dashboard.html';
        dashboardLabel = 'Admin Control';
        badgeClass = 'badge-danger';
      } else if (user.role === 'AGENT') {
        dashboardUrl = 'agent-dashboard.html';
        dashboardLabel = 'Agent Hub';
        badgeClass = 'badge-warning';
      }

      navAuth.innerHTML = `
        <span class="badge ${badgeClass}">${user.role}</span>
        <a href="${dashboardUrl}" class="btn btn-sm btn-outline">⚡ ${dashboardLabel}</a>
        <button class="btn btn-sm btn-danger" onclick="Auth.logout()">Logout</button>
      `;
    } else {
      navAuth.innerHTML = `
        <a href="login.html" class="btn btn-sm btn-outline">Login</a>
        <a href="register.html" class="btn btn-sm btn-primary">Register</a>
      `;
    }
  },

  // One-click demo login for viva presentation
  async quickDemoLogin(role) {
    let email = 'traveler@example.com';
    let pass = 'traveler123';
    let target = 'traveler-dashboard.html';

    if (role === 'admin') {
      email = 'admin@example.com';
      pass = 'admin123';
      target = 'admin-dashboard.html';
    } else if (role === 'agent') {
      email = 'agent@example.com';
      pass = 'agent123';
      target = 'agent-dashboard.html';
    }

    try {
      const res = await API.login(email, pass);
      if (res && res.success && res.user) {
        if (res.token) res.user.token = res.token;
        this.setUser(res.user);
        window.location.href = target;
      } else {
        const errorMsg = (res && (res.message || res.error)) || 'Authentication error';
        alert('Demo login failed: ' + errorMsg);
      }
    } catch (e) {
      alert('Unable to connect to server: ' + (e.message || 'Network error'));
    }
  }
};

document.addEventListener('DOMContentLoaded', () => {
  Auth.renderNavbar();
});
