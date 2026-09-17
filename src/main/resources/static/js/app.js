const API_BASE = '/api';
const MAX_RECENT = 5;
let authToken = null;
const recentUrls = [];

window.localStorage.removeItem('zaplink_recent_urls');
window.localStorage.removeItem('zaplink_jwt_token');

// ============================================
// DOM Elements
// ============================================
const shortenForm = document.getElementById('shortenForm');
const originalUrlInput = document.getElementById('originalUrl');
const customCodeInput = document.getElementById('customCode');
const loadingDiv = document.getElementById('loading');
const resultCard = document.getElementById('resultCard');
const errorCard = document.getElementById('errorCard');
const errorMessage = document.getElementById('errorMessage');
const recentList = document.getElementById('recentList');

// Auth elements
const toggleAuthBtn = document.getElementById('toggleAuthBtn');
const authModal = document.getElementById('authModal');
const loginForm = document.getElementById('loginForm');
const registerForm = document.getElementById('registerForm');
const loginFormElement = document.getElementById('loginFormElement');
const registerFormElement = document.getElementById('registerFormElement');
const userInfo = document.getElementById('userInfo');
const username = document.getElementById('username');
const logoutBtn = document.getElementById('logoutBtn');
const userUrlsSection = document.getElementById('userUrlsSection');
const userUrlsList = document.getElementById('userUrlsList');

// ============================================
// Event Listeners
// ============================================
shortenForm.addEventListener('submit', handleShortenUrl);
toggleAuthBtn.addEventListener('click', openAuthModal);
logoutBtn.addEventListener('click', handleLogout);
loginFormElement.addEventListener('submit', handleLogin);
registerFormElement.addEventListener('submit', handleRegister);

// ============================================
// Authentication Functions
// ============================================

function openAuthModal() {
    authModal.style.display = 'block';
    loginForm.classList.add('active');
    registerForm.classList.remove('active');
}

function closeAuthModal() {
    authModal.style.display = 'none';
    loginFormElement.reset();
    registerFormElement.reset();
}

function toggleAuthForms(e) {
    e.preventDefault();
    loginForm.classList.toggle('active');
    registerForm.classList.toggle('active');
}

async function handleLogin(e) {
    e.preventDefault();

    const username = document.getElementById('loginUsername').value;
    const password = document.getElementById('loginPassword').value;

    try {
        showLoading(true);

        const response = await fetch(`${API_BASE}/v1/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ username, password })
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Login failed');
        }

        const data = await response.json();

        authToken = data.token;

        // Update UI
        updateAuthUI(true, data.username);

        closeAuthModal();
        showError('Login successful!');

        // Load user URLs
        loadUserUrls();

    } catch (error) {
        showError(error.message);
    } finally {
        showLoading(false);
    }
}

async function handleRegister(e) {
    e.preventDefault();

    const username = document.getElementById('regUsername').value;
    const email = document.getElementById('regEmail').value;
    const password = document.getElementById('regPassword').value;

    try {
        showLoading(true);

        const response = await fetch(`${API_BASE}/v1/auth/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ username, email, password })
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Registration failed');
        }

        const data = await response.json();

        authToken = data.token;

        // Update UI
        updateAuthUI(true, data.username);

        closeAuthModal();
        showError('Registration successful!');

        // Load user URLs
        loadUserUrls();

    } catch (error) {
        showError(error.message);
    } finally {
        showLoading(false);
    }
}

function handleLogout() {
    authToken = null;
    updateAuthUI(false);
    userUrlsSection.style.display = 'none';
    resetForm();
}

function updateAuthUI(isAuthenticated, user = null) {
    if (isAuthenticated) {
        toggleAuthBtn.style.display = 'none';
        userInfo.style.display = 'flex';
        document.getElementById('username').textContent = user;
    } else {
        toggleAuthBtn.style.display = 'block';
        userInfo.style.display = 'none';
    }
}

function isLoggedIn() {
    return !!authToken;
}

function getAuthHeaders() {
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${authToken}`
    };
}

async function loadUserUrls() {
    if (!isLoggedIn()) return;

    try {
        const response = await fetch(`${API_BASE}/v1/user/urls`, {
            headers: getAuthHeaders()
        });

        if (!response.ok) return;

        const urls = await response.json();

        userUrlsSection.style.display = 'block';

        if (urls.length === 0) {
            userUrlsList.innerHTML = '<p class="text-muted">No URLs created yet</p>';
            return;
        }

        userUrlsList.innerHTML = urls.map(item => `
            <div class="recent-item">
                <div>
                    <div class="recent-item-code">${item.shortCode}</div>
                    <div class="recent-item-original" title="${item.originalUrl}">
                        ${item.originalUrl}
                    </div>
                </div>
                <div class="recent-actions">
                    <button
                        type="button"
                        class="btn-copy"
                        onclick="copyRecentUrl('${item.shortUrl}', event)"
                    >
                        📋
                    </button>
                    <a
                        href="${item.shortUrl}"
                        target="_blank"
                        class="btn-copy"
                        style="text-decoration: none;"
                    >
                        🔗
                    </a>
                    <button
                        type="button"
                        class="btn-copy"
                        style="background-color: var(--danger-color);"
                        onclick="deleteUserUrl('${item.shortCode}', event)"
                    >
                        🗑️
                    </button>
                </div>
            </div>
        `).join('');

    } catch (error) {
        console.error('Error loading user URLs:', error);
    }
}

async function deleteUserUrl(shortCode, event) {
    event.preventDefault();

    if (!confirm('Are you sure you want to delete this URL?')) return;

    try {
        const response = await fetch(`${API_BASE}/v1/${shortCode}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });

        if (response.ok) {
            loadUserUrls();
            showError('URL deleted successfully');
        } else {
            showError('Failed to delete URL');
        }

    } catch (error) {
        showError(error.message);
    }
}

// ============================================
// URL Shortening Functions
// ============================================

async function handleShortenUrl(e) {
    e.preventDefault();

    const originalUrl = originalUrlInput.value.trim();
    const customCode = customCodeInput.value.trim() || null;

    if (!originalUrl) {
        showError('Please enter a URL');
        return;
    }

    try {
        showLoading(true);
        closeError();

        const payload = {
            originalUrl: originalUrl,
            customCode: customCode
        };

        const headers = isLoggedIn() ? getAuthHeaders() : {'Content-Type': 'application/json'};

        const response = await fetch(`${API_BASE}/v1/shorten`, {
            method: 'POST',
            headers: headers,
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const error = await response.json();
            console.log(error.message);
            throw new Error(error.message || 'Failed to shorten URL');
        }

        const data = await response.json();

        displayResult(data);
        addToRecentUrls(data);

        // Reload user URLs if authenticated
        if (isLoggedIn()) {
            loadUserUrls();
        }

    } catch (error) {
        console.error('Error:', error);
        showError(error.message);
    } finally {
        showLoading(false);
    }
}

function displayResult(data) {
    document.getElementById('shortCode').textContent = data.shortCode;
    document.getElementById('shortUrl').value = data.shortUrl;
    document.getElementById('originalUrlDisplay').value = data.originalUrl;
    document.getElementById('createdAt').textContent = formatDate(data.createdAt);

    const testBtn = document.getElementById('testRedirectBtn');
    testBtn.href = data.shortUrl;

    resultCard.style.display = 'block';
    resultCard.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

function showLoading(show) {
    loadingDiv.style.display = show ? 'block' : 'none';
}

function showError(message) {
    errorMessage.textContent = message;
    errorCard.style.display = 'block';
    errorCard.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

function closeError() {
    errorCard.style.display = 'none';
}

function resetForm() {
    shortenForm.reset();
    resultCard.style.display = 'none';
    closeError();
    originalUrlInput.focus();
}

function copyToClipboard(elementId) {
    const element = document.getElementById(elementId);
    const text = element.tagName === 'INPUT' ? element.value : element.textContent;

    navigator.clipboard.writeText(text).then(() => {
        const button = event.target;
        const originalText = button.textContent;
        button.textContent = '✓ Copied!';

        setTimeout(() => {
            button.textContent = originalText;
        }, 2000);
    }).catch(err => {
        showError('Failed to copy to clipboard');
    });
}

function formatDate(dateString) {
    if (!dateString) return 'Never';

    const date = new Date(dateString);
    return date.toLocaleString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// ============================================
// Recent URLs
// ============================================

function addToRecentUrls(urlData) {
    recentUrls.unshift({
        shortCode: urlData.shortCode,
        shortUrl: urlData.shortUrl,
        originalUrl: urlData.originalUrl,
        createdAt: urlData.createdAt
    });

    recentUrls.splice(MAX_RECENT);

    displayRecentUrls();
}

function displayRecentUrls() {
    if (recentUrls.length === 0) {
        recentList.innerHTML = '<p class="text-muted">No recent URLs yet</p>';
        return;
    }

    recentList.innerHTML = recentUrls.map(item => `
        <div class="recent-item">
            <div>
                <div class="recent-item-code">${item.shortCode}</div>
                <div class="recent-item-original" title="${item.originalUrl}">
                    ${item.originalUrl}
                </div>
            </div>
            <div class="recent-actions">
                <button
                    type="button"
                    class="btn-copy"
                    onclick="copyRecentUrl('${item.shortUrl}', event)"
                >
                    📋
                </button>
                <a
                    href="${item.shortUrl}"
                    target="_blank"
                    class="btn-copy"
                    style="text-decoration: none;"
                >
                    🔗
                </a>
            </div>
        </div>
    `).join('');
}

function copyRecentUrl(url, event) {
    event.preventDefault();
    navigator.clipboard.writeText(url).then(() => {
        const button = event.target;
        const originalText = button.textContent;
        button.textContent = '✓';

        setTimeout(() => {
            button.textContent = originalText;
        }, 1500);
    });
}

// ============================================
// Initialize
// ============================================

document.addEventListener('DOMContentLoaded', () => {
    console.log('ZapLink App Loaded');
    displayRecentUrls();
    originalUrlInput.focus();

    // Check if user is already logged in
    if (isLoggedIn()) {
        // Decode token to get username (simple approach)
        const payload = JSON.parse(atob(authToken.split('.')[1]));
        updateAuthUI(true, payload.username);
        loadUserUrls();
    }
});

// Close modal when clicking outside
window.onclick = function(event) {
    if (event.target === authModal) {
        closeAuthModal();
    }
}