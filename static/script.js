let currentUser = null;
let currentMovie = null;
let selectedSeats = [];

document.addEventListener('DOMContentLoaded', () => {
    loadMovies();
    setupEventListeners();
    checkAuthStatus();
});

function setupEventListeners() {
    document.getElementById('login-form').addEventListener('submit', handleLogin);
    document.getElementById('register-form').addEventListener('submit', handleRegister);
}

async function loadMovies() {
    try {
        const response = await fetch('http://localhost:5000/movies');
        const movies = await response.json();
        renderMovies(movies);
    } catch (error) {
        console.error('Error loading movies:', error);
    }
}

function renderMovies(movies) {
    const container = document.getElementById('movies-container');
    container.innerHTML = '';
    
    movies.forEach(movie => {
        const card = document.createElement('div');
        card.className = 'col-md-4 movie-card';
        card.innerHTML = `
            <div class="card">
                <img src="${movie.poster}" class="card-img-top" alt="${movie.title}">
                <div class="card-body">
                    <h5 class="card-title">${movie.title}</h5>
                    <p class="card-text">${movie.genre} | ${movie.duration}</p>
                    <button class="btn btn-primary" onclick="showBookingModal('${movie._id}', '${movie.title}', ${JSON.stringify(movie.times)})">Book Now</button>
                </div>
            </div>
        `;
        container.appendChild(card);
    });
}

function showBookingModal(movieId, title, times) {
    if (!currentUser) {
        showModal('login-modal');
        return;
    }
    
    currentMovie = { _id: movieId, title, times };
    document.getElementById('booking-movie-title').textContent = title;
    
    const timeSelect = document.getElementById('booking-time');
    timeSelect.innerHTML = '<option value="">Select time</option>';
    times.forEach(time => {
        const option = document.createElement('option');
        option.value = time;
        option.textContent = time;
        timeSelect.appendChild(option);
    });
    
    // Set default date to today
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('booking-date').value = today;
    
    generateSeatMap();
    showModal('booking-modal');
}

function generateSeatMap() {
    const seatMap = document.getElementById('seat-map');
    seatMap.innerHTML = '';
    selectedSeats = [];
    updateSelectedSeatsDisplay();
    
    // Generate premium seats (first 2 rows)
    for (let row = 1; row <= 2; row++) {
        const rowDiv = document.createElement('div');
        rowDiv.className = 'seat-row';
        for (let seat = 1; seat <= 10; seat++) {
            const seatId = `P-${row}-${seat}`;
            const seatElement = document.createElement('div');
            seatElement.className = 'seat seat-premium';
            seatElement.textContent = seatId;
            seatElement.onclick = () => toggleSeatSelection(seatId, 15);
            rowDiv.appendChild(seatElement);
        }
        seatMap.appendChild(rowDiv);
    }
    
    // Generate standard seats (remaining rows)
    for (let row = 3; row <= 8; row++) {
        const rowDiv = document.createElement('div');
        rowDiv.className = 'seat-row';
        for (let seat = 1; seat <= 10; seat++) {
            const seatId = `S-${row}-${seat}`;
            const seatElement = document.createElement('div');
            seatElement.className = 'seat seat-standard';
            seatElement.textContent = seatId;
            seatElement.onclick = () => toggleSeatSelection(seatId, 10);
            rowDiv.appendChild(seatElement);
        }
        seatMap.appendChild(rowDiv);
    }
    
    // TODO: Mark booked seats (would require checking with backend)
}

function toggleSeatSelection(seatId, price) {
    const index = selectedSeats.findIndex(s => s.id === seatId);
    
    if (index === -1) {
        selectedSeats.push({ id: seatId, price });
    } else {
        selectedSeats.splice(index, 1);
    }
    
    updateSelectedSeatsDisplay();
}

function updateSelectedSeatsDisplay() {
    const display = document.getElementById('selected-seats-display');
    const totalElement = document.getElementById('total-price');
    
    if (selectedSeats.length === 0) {
        display.textContent = 'None';
        totalElement.textContent = '0';
        return;
    }
    
    display.textContent = selectedSeats.map(s => s.id).join(', ');
    const total = selectedSeats.reduce((sum, seat) => sum + seat.price, 0);
    totalElement.textContent = total;
}

async function confirmBooking() {
    if (selectedSeats.length === 0) {
        alert('Please select at least one seat');
        return;
    }
    
    const date = document.getElementById('booking-date').value;
    const time = document.getElementById('booking-time').value;
    
    if (!date || !time) {
        alert('Please select date and time');
        return;
    }
    
    try {
        const response = await fetch('http://localhost:5000/bookings', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            },
            body: JSON.stringify({
                movie_id: currentMovie._id,
                seats: selectedSeats.map(s => s.id),
                date,
                time,
                total_price: selectedSeats.reduce((sum, seat) => sum + seat.price, 0)
            })
        });
        
        if (response.ok) {
            alert('Booking confirmed!');
            hideModal('booking-modal');
            loadUserBookings();
        } else {
            const error = await response.json();
            alert(error.message || 'Booking failed');
        }
    } catch (error) {
        console.error('Booking error:', error);
        alert('Booking failed');
    }
}

async function loadUserBookings() {
    if (!currentUser) return;
    
    try {
        const response = await fetch('http://localhost:5000/bookings/user', {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
        });
        const bookings = await response.json();
        renderBookings(bookings);
    } catch (error) {
        console.error('Error loading bookings:', error);
    }
}

function renderBookings(bookings) {
    const container = document.getElementById('bookings-container');
    container.innerHTML = '';
    
    if (bookings.length === 0) {
        container.innerHTML = '<p>You have no bookings yet.</p>';
        return;
    }
    
    bookings.forEach(booking => {
        const card = document.createElement('div');
        card.className = 'col-md-12 booking-card';
        card.innerHTML = `
            <h5>${booking.movie_id.title}</h5>
            <p>Date: ${booking.date} | Time: ${booking.time}</p>
            <p>Seats: ${booking.seats.join(', ')}</p>
            <p>Total: $${booking.total_price}</p>
        `;
        container.appendChild(card);
    });
}

// Authentication functions
async function handleLogin(e) {
    e.preventDefault();
    const email = document.getElementById('login-email').value;
    const password = document.getElementById('login-password').value;
    
    try {
        const response = await fetch('http://localhost:5000/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });
        
        if (response.ok) {
            const data = await response.json();
            localStorage.setItem('token', data.token);
            currentUser = { id: data.user_id, name: data.name };
            updateAuthUI();
            hideModal('login-modal');
            loadUserBookings();
        } else {
            const error = await response.json();
            alert(error.message || 'Login failed');
        }
    } catch (error) {
        console.error('Login error:', error);
        alert('Login failed');
    }
}

async function handleRegister(e) {
    e.preventDefault();
    const name = document.getElementById('register-name').value;
    const email = document.getElementById('register-email').value;
    const password = document.getElementById('register-password').value;
    
    try {
        const response = await fetch('http://localhost:5000/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, email, password })
        });
        
        if (response.ok) {
            alert('Registration successful! Please login.');
            hideModal('register-modal');
            showModal('login-modal');
        } else {
            const error = await response.json();
            alert(error.message || 'Registration failed');
        }
    } catch (error) {
        console.error('Registration error:', error);
        alert('Registration failed');
    }
}

function logout() {
    localStorage.removeItem('token');
    currentUser = null;
    updateAuthUI();
    document.getElementById('bookings-container').innerHTML = '';
}

function checkAuthStatus() {
    const token = localStorage.getItem('token');
    if (token) {
        // In a real app, you would verify the token and get user info
        currentUser = { id: 'user-id', name: 'User Name' }; // Placeholder
        updateAuthUI();
        loadUserBookings();
    }
}

function updateAuthUI() {
    const authButtons = document.getElementById('auth-buttons');
    const userInfo = document.getElementById('user-info');
    const usernameDisplay = document.getElementById('username-display');
    
    if (currentUser) {
        authButtons.style.display = 'none';
        userInfo.style.display = 'block';
        usernameDisplay.textContent = currentUser.name;
    } else {
        authButtons.style.display = 'block';
        userInfo.style.display = 'none';
    }
}

// Modal functions
function showModal(id) {
    document.getElementById(id).style.display = 'block';
}

function hideModal(id) {
    document.getElementById(id).style.display = 'none';
}

// Close modals when clicking outside
window.onclick = function(event) {
    const modals = document.getElementsByClassName('modal');
    for (let modal of modals) {
        if (event.target == modal) {
            modal.style.display = 'none';
        }
    }
}