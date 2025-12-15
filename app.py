from flask import Flask, render_template, request, jsonify, redirect, url_for, session, flash
from flask_pymongo import PyMongo
from bson.objectid import ObjectId
from werkzeug.security import generate_password_hash, check_password_hash
import jwt
import datetime
from functools import wraps
import os
from werkzeug.utils import secure_filename
from flask import send_from_directory
import qrcode
from io import BytesIO
import base64
from pymongo import MongoClient






app = Flask(__name__)
app.config["MONGO_URI"] = "mongodb://localhost:27017/cinema_db"
app.config['SECRET_KEY'] = ''
mongo = PyMongo(app)





mongo.db.users.update_one(
    {'email': 'admin@cinema.com'},
    {'$set': {
        'name': 'System Admin',
        'email': 'admin@cinema.com',
        'password': generate_password_hash('admin123'),
        'is_admin': True,
        'is_system_admin': True,     
        'can_manage_admins': True, 
        'is_deleted': False,
        'bookings': []
    }},
    upsert=True
)

# mongo.db.users.update_one(
#     {'email': 'fresh1763504960027@test.com'},
#     {'$set': {
#         'name': 'Fresh User',
#         'email': 'fresh1763504960027@test.com',
#         'password': generate_password_hash('X123456x', method='scrypt'),
#         'is_admin': False,
#           }},
#     upsert=True
# )

mongo.db.users.update_one(
    {'email': 'ibrahim@gmail.com'},
    {'$set': {
        'name': 'Ibrahim',
        'email': 'ibrahim@gmail.com',
        'password': generate_password_hash('X123456x', method='scrypt'),
        'is_admin': False,
 }},
    upsert=True
)

test_movie_id = ObjectId("691e0177e3b8780cdac512a9")

mongo.db.movies.update_one(
    {"_id": test_movie_id},
    {
        "$set": {
            "title": "Inception Clone - Test Add",
            "description": "A movie about dreams and nested test scenarios."
        }
    },
    upsert=True
)





if mongo.db.cinemas.count_documents({}) == 0:
    cinemas_data = [
        
        {'name': 'Stars Cinema (Nasr City)', 'city': 'Cairo', 'country': 'Egypt', 'address': 'Omar Ibn Al-Khattab, Nasr City'},
        {'name': 'Point 90 Cinema (Tagammu)', 'city': 'Cairo', 'country': 'Egypt', 'address': 'Fifth Settlement'},
        {'name': 'Mall of Egypt Cinema (6 October)', 'city': 'Cairo', 'country': 'Egypt', 'address': 'Al-Wahat Road, 6 October City'},
        
        {'name': 'Green Plaza Cinema', 'city': 'Alexandria', 'country': 'Egypt', 'address': 'Smouha, Alexandria'},
        {'name': 'City Center Cinema', 'city': 'Alexandria', 'country': 'Egypt', 'address': 'Alexandria Desert Road'},
        {'name': 'San Stefano Cinema', 'city': 'Alexandria', 'country': 'Egypt', 'address': 'Army Road, San Stefano'},
        
        {'name': 'Dandy Mall Cinema', 'city': 'Giza', 'country': 'Egypt', 'address': 'Km 28, Cairo-Alexandria Desert Road'},
        {'name': 'Americana Plaza Cinema', 'city': 'Giza', 'country': 'Egypt', 'address': 'Sheikh Zayed'},
        {'name': 'Arab Mall Cinema', 'city': 'Giza', 'country': 'Egypt', 'address': 'Gihina Square'},
    ]
    
    mongo.db.cinemas.insert_many(cinemas_data)



app.config['UPLOAD_FOLDER'] = 'static/uploads/profile_pics'
app.config['MAX_CONTENT_LENGTH'] = 2 * 1024 * 1024  
ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg', 'gif'}

os.makedirs(app.config['UPLOAD_FOLDER'], exist_ok=True)

def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS


def token_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        token = None
        if 'token' in session:
            token = session['token']
        
        if not token:
            return redirect(url_for('login'))
        
        try:
            data = jwt.decode(token, app.config['SECRET_KEY'], algorithms=["HS256"])
            current_user = mongo.db.users.find_one({'_id': ObjectId(data['user_id'])})
        except:
            return redirect(url_for('login'))
        
        return f(current_user, *args, **kwargs)
    return decorated

def admin_required(f):
    @wraps(f)
    def wrapper(*args, **kwargs):
        token = session.get('token')
        if not token:
            return redirect(url_for('login'))

        user = mongo.db.users.find_one({'_id': ObjectId(session['user_id'])})

        if not user or not user.get("is_admin"):
            session['bounced_from_admin'] = True
            return redirect(url_for('home'))

        return f(user, *args, **kwargs)
    return wrapper




@app.route('/select-cinema', methods=['GET', 'POST'])
def select_cinema():
    if request.method == 'POST':
        cinema_id = request.form.get('cinema_id')
        if cinema_id:
            cinema = mongo.db.cinemas.find_one({'_id': ObjectId(cinema_id)})
            if cinema:
                session['cinema_id'] = str(cinema['_id'])
                session['cinema_name'] = cinema['name']
                session['cinema_city'] = cinema['city']
                return redirect(url_for('home'))
    
    
    cinemas_by_city = {}
    all_cinemas = list(mongo.db.cinemas.find().sort('city', 1))
    for cinema in all_cinemas:
        city = cinema['city']
        if city not in cinemas_by_city:
            cinemas_by_city[city] = []
        cinemas_by_city[city].append(cinema)
        
    return render_template('select_cinema.html', cinemas_by_city=cinemas_by_city)


@app.route('/clear-cinema')
def clear_cinema():
    session.pop('cinema_id', None)
    session.pop('cinema_name', None)
    session.pop('cinema_city', None)
    return redirect(url_for('select_cinema'))






@app.route('/')
def home():

    if session.get("bounced_from_admin"):
        session.pop("bounced_from_admin", None)
        return render_template('index.html', now_showing=[], coming_soon=[])

    if 'cinema_id' not in session:
        return redirect(url_for('select_cinema'))

    cinema_id = ObjectId(session['cinema_id'])

    now_showing = list(mongo.db.movies.find({
        'status': 'now_showing',
        'cinema_ids': cinema_id
    }))

    coming_soon = list(mongo.db.movies.find({
        'status': 'coming_soon',
        'cinema_ids': cinema_id
    }))

    return render_template('index.html', now_showing=now_showing, coming_soon=coming_soon)




def admin_manager_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        token = None
        if 'token' in session:
            token = session['token']
        
        if not token:
            return redirect(url_for('login'))
        
        try:
            data = jwt.decode(token, app.config['SECRET_KEY'], algorithms=["HS256"])
            current_user = mongo.db.users.find_one({'_id': ObjectId(data['user_id'])})
            
            
            if not current_user or not current_user.get('is_admin'):
                flash('You do not have permission to access this page.', 'danger')
                return redirect(url_for('home'))
            
            
            if not current_user.get('is_system_admin') and not current_user.get('can_manage_admins'):
                flash('You do not have permission to manage admin accounts.', 'danger')
                return redirect(url_for('admin_dashboard'))

        except Exception as e:
            print(f"Admin manager decorator error: {e}")
            session.clear()
            return redirect(url_for('login'))
        
        return f(current_user, *args, **kwargs)
    return decorated

@app.route('/login', methods=['GET', 'POST'])
def login():
    if request.method == 'POST':
        email = request.form.get('email', '').strip()
        password = request.form.get('password', '').strip()

        if not email or not password:
            return render_template('login.html', error='Invalid credentials')

        user = mongo.db.users.find_one({'email': email})
        if not user:
            return render_template('login.html', error='Invalid credentials')

        if not check_password_hash(user['password'], password):
            return render_template('login.html', error='Invalid credentials')

        token = jwt.encode({
            'user_id': str(user['_id']),
            'exp': datetime.datetime.utcnow() + datetime.timedelta(hours=24)
        }, app.config['SECRET_KEY'])

        session['token'] = token
        session['user_id'] = str(user['_id'])
        session['name'] = user['name']
        session['is_admin'] = user.get('is_admin', False)
        session['is_system_admin'] = user.get('is_system_admin', False)
        session['can_manage_admins'] = user.get('can_manage_admins', False)

        if session.get('is_admin'):
            return redirect(url_for('admin_dashboard'))

        if 'cinema_id' not in session:
            return redirect(url_for('select_cinema'))

        return redirect(url_for('home'))

    return render_template('login.html')




@app.route('/register', methods=['GET', 'POST'])
def register():
    if request.method == 'POST':
        name = request.form.get('name')
        email = request.form.get('email')
        password = request.form.get('password')
        confirm = request.form.get('confirmPassword')

        
        
        

        
        if not name or not email or not password:
            return render_template('register.html', error='All fields are required.')

        
        if mongo.db.users.find_one({'email': email}):
            return render_template('register.html', error='Email already exists.')

        
        if len(password) < 8:
            return render_template('register.html', error='Password must be at least 8 characters long.')

        
        if not any(c.isupper() for c in password):
            return render_template('register.html', error='Password must contain at least one uppercase letter.')

        
        if not any(c.islower() for c in password):
            return render_template('register.html', error='Password must contain at least one lowercase letter.')

        
        if not any(c.isdigit() for c in password):
            return render_template('register.html', error='Password must contain at least one number.')

        
        
        
        
        

        
        hashed_password = generate_password_hash(password)
        user_id = mongo.db.users.insert_one({
            'name': name,
            'email': email,
            'password': hashed_password,
            'is_admin': False,
            'bookings': [],
            'created_at': datetime.datetime.utcnow(),
            'phone': '',
            'address': '',
            'city': '',
            'country': '',
            'dob': None,
            'gender': None,
            'profile_pic': '',
            'updated_at': datetime.datetime.utcnow()
        }).inserted_id

        
        session['name'] = name
        session['user_id'] = str(user_id)

        return redirect(url_for('select_cinema'))

    return render_template('register.html')


@app.route('/logout')
def logout():
    session.clear()

    session['logout_success'] = True

    return redirect(url_for('logout_cleanup'))


@app.route('/logout-cleanup')
def logout_cleanup():
    if request.headers.get("X-Test-Mode") == "true":
        success = session.pop('logout_success', False)
        return jsonify(success)

    return render_template("logout_cleanup.html")




@app.route('/movie/<movie_id>')
def movie_details(movie_id):
    
    if 'cinema_id' not in session:
        return redirect(url_for('select_cinema'))

    movie = mongo.db.movies.find_one({'_id': ObjectId(movie_id)})
    if not movie:
        return redirect(url_for('home'))
    
    
    screenings = list(mongo.db.screenings.find({
        'movie_id': ObjectId(movie_id),
        'cinema_id': ObjectId(session['cinema_id'])
    }))
    
    
    
    promotions = list(mongo.db.promotions.find({
        'movie_id': ObjectId(movie_id),
        'is_active': True
    }))
    
    
    return render_template('movie_details.html', movie=movie, screenings=screenings, promotions=promotions)


@app.route('/book/<screening_id>', methods=['GET', 'POST'])
@token_required
def book_screening(current_user, screening_id):
    screening = mongo.db.screenings.find_one({'_id': ObjectId(screening_id)})
    if not screening:
        return redirect(url_for('home'))
    
    movie = mongo.db.movies.find_one({'_id': screening['movie_id']})
    
    
    bookings = list(mongo.db.bookings.find({'screening_id': ObjectId(screening_id)}))
    booked_seats = [seat for booking in bookings for seat in booking['seats']]
    
    
    
    
    promotion = mongo.db.promotions.find_one({
        'movie_id': screening['movie_id'],
        'is_active': True
    })
    

    if request.method == 'POST':
        selected_seats = request.form.getlist('seats')
        
        if not selected_seats:
            return render_template('book_screening.html', 
                                screening=screening, 
                                movie=movie, 
                                booked_seats=booked_seats,
                                promotion=promotion, 
                                error='Please select at least one seat')
        
        for seat in selected_seats:
            if seat in booked_seats:
                return render_template('book_screening.html', 
                                    screening=screening, 
                                    movie=movie, 
                                    booked_seats=booked_seats,
                                    promotion=promotion, 
                                    error=f'Seat {seat} is already booked')

        
        
        base_ticket_price = 10 
        num_seats = len(selected_seats)
        total_price = num_seats * base_ticket_price
        promotion_applied = "None"
        
        if promotion:
            if promotion['type'] == 'B1G1F' and num_seats >= 2:
                
                free_tickets = num_seats // 2
                total_price = (num_seats - free_tickets) * base_ticket_price
                promotion_applied = promotion['name']
            elif promotion['type'] == 'B2G1F' and num_seats >= 3:
                
                free_tickets = num_seats // 3
                total_price = (num_seats - free_tickets) * base_ticket_price
                promotion_applied = promotion['name']
            
            
        

        booking_data = {
            'user_id': ObjectId(session['user_id']),
            'screening_id': ObjectId(screening_id),
            'movie_id': screening['movie_id'],
            'cinema_id': screening['cinema_id'], 
            'seats': selected_seats,
            'total_price': total_price, 
            'promotion_applied': promotion_applied, 
            'booking_date': datetime.datetime.utcnow(),
            'status': 'confirmed'
        }
        
        booking_id = mongo.db.bookings.insert_one(booking_data).inserted_id
        
        mongo.db.users.update_one(
            {'_id': ObjectId(session['user_id'])},
            {'$push': {'bookings': booking_id}}
        )
        
        qr = qrcode.QRCode(
            version=1,
            error_correction=qrcode.constants.ERROR_CORRECT_L,
            box_size=10,
            border=4,
        )
        
        cinema_name = mongo.db.cinemas.find_one({'_id': screening['cinema_id']})['name']
        booking_qr_data = f"Booking ID: {booking_id}\nCinema: {cinema_name}\nMovie: {movie['title']}\nDate: {screening['date']}\nTime: {screening['time']}\nSeats: {', '.join(selected_seats)}\nHall: {screening['hall']}"
        qr.add_data(booking_qr_data)
        qr.make(fit=True)
        
        img = qr.make_image(fill_color="black", back_color="white")
        buffered = BytesIO()
        img.save(buffered)
        qr_code_img = base64.b64encode(buffered.getvalue()).decode()
        
        return render_template('booking_confirmation.html', 
                            booking=booking_data, 
                            movie=movie,
                            screening=screening,
                            seats=selected_seats,
                            cinema_name=cinema_name, 
                            qr_code=qr_code_img)
    
    return render_template('book_screening.html', 
                         screening=screening, 
                         movie=movie, 
                         booked_seats=booked_seats,
                         promotion=promotion) 


@app.route('/my-bookings')
@token_required
def my_bookings(current_user):
    bookings = []
    for booking_id in current_user.get('bookings', []):
        booking = mongo.db.bookings.find_one({'_id': ObjectId(booking_id)})
        if booking:
            movie = mongo.db.movies.find_one({'_id': booking['movie_id']})
            screening = mongo.db.screenings.find_one({'_id': booking['screening_id']})
            
            cinema = None
            if 'cinema_id' in booking:
                cinema = mongo.db.cinemas.find_one({'_id': booking['cinema_id']})
                
            bookings.append({
                'booking': booking,
                'movie': movie,
                'screening': screening,
                'cinema': cinema 
            })
    
    return render_template('my_bookings.html', bookings=bookings)

@app.route('/profile')
@token_required
def profile(current_user):
    created_at = current_user.get('created_at', datetime.datetime.utcnow())
    days_member = (datetime.datetime.utcnow() - created_at).days
    
    recent_bookings = []
    for booking_id in current_user.get('bookings', [])[-5:]:
        booking = mongo.db.bookings.find_one({'_id': ObjectId(booking_id)})
        if booking:
            movie = mongo.db.movies.find_one({'_id': booking['movie_id']})
            screening = mongo.db.screenings.find_one({'_id': booking['screening_id']})
            
            cinema = None
            if 'cinema_id' in booking:
                cinema = mongo.db.cinemas.find_one({'_id': booking['cinema_id']})
            
            booking_data = {
                '_id': booking.get('_id'),
                'status': booking.get('status', 'unknown'),
                'seats': booking.get('seats', []),
                'total_price': booking.get('total_price', 0),
                'booking_date': booking.get('booking_date')
            }
            
            if screening:
                if 'date' in screening and isinstance(screening['date'], str):
                    try:
                        screening['date'] = datetime.datetime.strptime(screening['date'], '%Y-%m-%d')
                    except ValueError:
                        screening['date'] = 'Unknown'
                
                if 'time' in screening and isinstance(screening['time'], str):
                    try:
                        screening['time'] = datetime.datetime.strptime(screening['time'], '%H:%M')
                    except ValueError:
                        screening['time'] = 'Unknown'
                
                screening['hall'] = screening.get('hall', 'Unknown')
            else:
                screening = {'date': 'Unknown', 'time': 'Unknown', 'hall': 'Unknown'}
            
            recent_bookings.append({
                'booking': booking_data,
                'movie': movie if movie else {'title': 'Unknown', 'image_url': 'https://via.placeholder.com/40x60'},
                'screening': screening,
                'cinema': cinema if cinema else {'name': 'Unknown'} 
            })
    
    
    profile_pic_url = None
    filename = current_user.get('profile_pic')
    if filename:
        
        
        
        try:
            
            profile_pic_url = url_for('static', filename=os.path.join('uploads/profile_pics', filename))
        except Exception as e:
            print(f"Error creating URL for profile pic: {e}")
            profile_pic_url = None 
    

    return render_template('profile.html',
                           user=current_user,
                           recent_bookings=recent_bookings,
                           days_member=days_member,
                           bookings_count=len(current_user.get('bookings', [])),
                           profile_pic=profile_pic_url) 
@app.route('/profile/edit', methods=['GET', 'POST'])
@token_required
def edit_profile(current_user):
    if request.method == 'POST':
        name = request.form.get('name')
    
        phone = request.form.get('phone')
        address = request.form.get('address')
        city = request.form.get('city')
        country = request.form.get('country')
        dob = request.form.get('dob')
        gender = request.form.get('gender')
      

        profile_pic = current_user.get('profile_pic', '')
        if 'profile_pic' in request.files:
            file = request.files['profile_pic']
            if file.filename != '' and allowed_file(file.filename):
                if profile_pic:
                    try:
                        os.remove(os.path.join(app.config['UPLOAD_FOLDER'], profile_pic))
                    except:
                        pass
                
                filename = secure_filename(f"{current_user['_id']}_{file.filename}")
                file.save(os.path.join(app.config['UPLOAD_FOLDER'], filename))
                profile_pic = filename

        update_data = {
            'name': name,
            'email': current_user['email'], 
            'phone': phone,
            'address': address,
            'city': city,
            'country': country,
            'dob': datetime.datetime.strptime(dob, '%Y-%m-%d') if dob else None,
            'gender': gender,
            'profile_pic': profile_pic,
            'updated_at': datetime.datetime.utcnow()
        }
        
        mongo.db.users.update_one(
            {'_id': ObjectId(current_user['_id'])},
            {'$set': update_data}
        )
        
        session['name'] = name
        if profile_pic:
            session['profile_pic'] = profile_pic
        
        return redirect(url_for('profile'))
    
    return render_template('edit_profile.html', user=current_user)

@app.route('/profile/change-password', methods=['GET', 'POST'])
@token_required
def change_password(current_user):
    if request.method == 'POST':
        current_password = request.form.get('current_password')
        new_password = request.form.get('new_password')
        confirm_password = request.form.get('confirm_password')
        
        if not check_password_hash(current_user['password'], current_password):
            return render_template('change_password.html', 
                                error='Current password is incorrect')
        
        if new_password != confirm_password:
            return render_template('change_password.html', 
                                error='New passwords do not match')
        
        if len(new_password) < 8:
            return render_template('change_password.html', 
                                error='Password must be at least 8 characters')
        
        if check_password_hash(current_user['password'], new_password):
            return render_template('change_password.html',
                                error='New password must be different from current password')
        
        mongo.db.users.update_one(
            {'_id': ObjectId(current_user['_id'])},
            {'$set': {
                'password': generate_password_hash(new_password),
                'updated_at': datetime.datetime.utcnow()
            }}
        )
        
        flash('Password changed successfully!', 'success')
        return redirect(url_for('profile'))
    
    return render_template('change_password.html')

@app.route('/update-profile-pic', methods=['POST'])
@token_required
def update_profile_pic(current_user):
    if 'profile_pic' not in request.files:
        flash('No file selected', 'danger')
        return redirect(url_for('profile'))

    file = request.files['profile_pic']
    if file.filename == '':
        flash('No file selected', 'danger')
        return redirect(url_for('profile'))

    if file and allowed_file(file.filename):
        old_pic = current_user.get('profile_pic')
        if old_pic:
            try:
                os.remove(os.path.join(app.config['UPLOAD_FOLDER'], old_pic))
            except:
                pass

        filename = secure_filename(f"{current_user['_id']}_{file.filename}")
        file.save(os.path.join(app.config['UPLOAD_FOLDER'], filename))

        mongo.db.users.update_one(
            {'_id': ObjectId(current_user['_id'])},
            {'$set': {'profile_pic': filename}}
        )

        # session['profile_pic'] = filename
        flash('Profile picture updated successfully!', 'success')
    else:
        flash('Allowed file types are: png, jpg, jpeg, gif', 'danger')

    return redirect(url_for('profile'))


@app.route('/admin')
@admin_required
def admin_dashboard(current_user):
    movies = list(mongo.db.movies.find())
    screenings = list(mongo.db.screenings.find())
    bookings = list(mongo.db.bookings.find())
    
    
    cinemas = list(mongo.db.cinemas.find())
    promotions = list(mongo.db.promotions.find())
    
    
    return render_template('admin/dashboard.html', 
                           movies=movies, 
                           screenings=screenings, 
                           bookings=bookings,
                           cinemas=cinemas, 
                           promotions=promotions) 





@app.route('/admin/movies/add', methods=['GET', 'POST'])
@admin_required
def add_movie(current_user):
    if request.method == 'POST':
        title = request.form.get('title')
        description = request.form.get('description')
        duration = request.form.get('duration')
        genre = request.form.get('genre')
        status = request.form.get('status')
        image_url = request.form.get('image_url')
        
        
        cinema_ids_str = request.form.getlist('cinema_ids')
        
        cinema_ids_obj = [ObjectId(cid) for cid in cinema_ids_str]
        
        mongo.db.movies.insert_one({
            'title': title,
            'description': description,
            'duration': duration,
            'genre': genre,
            'status': status,
            'image_url': image_url,
            'cinema_ids': cinema_ids_obj, 
            'created_at': datetime.datetime.utcnow()
        })
        
        return redirect(url_for('admin_dashboard'))

    
    cinemas_by_city = {}
    all_cinemas = list(mongo.db.cinemas.find().sort('city', 1))
    for cinema in all_cinemas:
        city = cinema['city']
        if city not in cinemas_by_city:
            cinemas_by_city[city] = []
        cinemas_by_city[city].append(cinema)

    return render_template('admin/add_movie.html', cinemas_by_city=cinemas_by_city)


@app.route('/admin/movies/view/<movie_id>')
@admin_required
def view_movie(current_user, movie_id):
 
    movie = mongo.db.movies.find_one({'_id': ObjectId(movie_id)})
    
    if not movie:
        flash('Movie not found!', 'danger')
        return redirect(url_for('manage_movies_list'))

  
    linked_cinemas = []
    if 'cinema_ids' in movie and movie['cinema_ids']:
        linked_cinemas = list(mongo.db.cinemas.find({
            '_id': {'$in': movie['cinema_ids']}
        }))

    return render_template('admin/view_movie.html', movie=movie, cinemas=linked_cinemas)

@app.route('/admin/movies')
@admin_required
def manage_movies_list(current_user):
    """
    هذه هي الدالة التي كان يبحث عنها الزر ولم يجدها.
    وظيفتها: جلب كل الأفلام وعرضها في صفحة الجدول.
    """
    movies = list(mongo.db.movies.find().sort('created_at', -1))
    
    
    
    return render_template('admin/manage_movies.html', movies=movies)

@app.route('/admin/movies/edit/<movie_id>', methods=['GET', 'POST'])
@admin_required
def edit_movie(current_user, movie_id):
    movie = mongo.db.movies.find_one({'_id': ObjectId(movie_id)})
    if not movie:
        return redirect(url_for('admin_dashboard'))
    
    if request.method == 'POST':
        title = request.form.get('title')
        description = request.form.get('description')
        duration = int(request.form.get('duration'))
        genre = request.form.get('genre')
        status = request.form.get('status')
        image_url = request.form.get('image_url')

        
        cinema_ids_str = request.form.getlist('cinema_ids')
        cinema_ids_obj = [ObjectId(cid) for cid in cinema_ids_str]
        
        if 'poster_file' in request.files:
            file = request.files['poster_file']
            if file.filename != '':
                pass 
        
        mongo.db.movies.update_one(
            {'_id': ObjectId(movie_id)},
            {'$set': {
                'title': title,
                'description': description,
                'duration': duration,
                'genre': genre,
                'status': status,
                'image_url': image_url,
                'cinema_ids': cinema_ids_obj, 
                'updated_at': datetime.datetime.utcnow()
            }}
        )
        
        return redirect(url_for('admin_dashboard'))
    
    
    cinemas_by_city = {}
    all_cinemas = list(mongo.db.cinemas.find().sort('city', 1))
    for cinema in all_cinemas:
        city = cinema['city']
        if city not in cinemas_by_city:
            cinemas_by_city[city] = []
        cinemas_by_city[city].append(cinema)

    return render_template('admin/edit_movie.html', movie=movie, cinemas_by_city=cinemas_by_city)


@app.route('/admin/movies/delete/<movie_id>')
@admin_required
def delete_movie(current_user, movie_id):
    mongo.db.movies.delete_one({'_id': ObjectId(movie_id)})
    
    mongo.db.screenings.delete_many({'movie_id': ObjectId(movie_id)})
    mongo.db.promotions.delete_many({'movie_id': ObjectId(movie_id)})
    return redirect(url_for('admin_dashboard'))



@app.route('/admin/screenings/add', methods=['GET', 'POST'])
@admin_required
def add_screening(current_user):
    if request.method == 'POST':
        movie_id = request.form.get('movie_id')
        cinema_id = request.form.get('cinema_id') 
        date = request.form.get('date')
        time = request.form.get('time')
        hall = request.form.get('hall')
        
        mongo.db.screenings.insert_one({
            'movie_id': ObjectId(movie_id),
            'cinema_id': ObjectId(cinema_id), 
            'date': date,
            'time': time,
            'hall': hall,
            'created_at': datetime.datetime.utcnow()
        })
        
        return redirect(url_for('admin_dashboard'))
    
    movies = list(mongo.db.movies.find())
    cinemas = list(mongo.db.cinemas.find()) 
    return render_template('admin/add_screening.html', movies=movies, cinemas=cinemas)



@app.route('/admin/screenings')
@admin_required
def screenings_list(current_user):
    screenings = list(mongo.db.screenings.find())
    movies = {m["_id"]: m["title"] for m in mongo.db.movies.find()}
    cinemas = {c["_id"]: c["name"] for c in mongo.db.cinemas.find()}
    return render_template("admin/screenings_list.html",
                           screenings=screenings,
                           movies=movies,
                           cinemas=cinemas)



@app.route('/admin/screenings/edit/<screening_id>', methods=['GET', 'POST'])
@admin_required
def edit_screening(current_user, screening_id):
    screening = mongo.db.screenings.find_one({'_id': ObjectId(screening_id)})
    if not screening:
        flash('Screening not found!', 'danger')
        return redirect(url_for('admin_dashboard'))

    if request.method == 'POST':
        movie_id = request.form.get('movie_id')
        cinema_id = request.form.get('cinema_id')
        date = request.form.get('date')
        time = request.form.get('time')
        hall = request.form.get('hall')

        mongo.db.screenings.update_one(
            {'_id': ObjectId(screening_id)},
            {'$set': {
                'movie_id': ObjectId(movie_id),
                'cinema_id': ObjectId(cinema_id),
                'date': date,
                'time': time,
                'hall': hall,
                'updated_at': datetime.datetime.utcnow()
            }}
        )
        flash('Screening updated successfully!', 'success')
        return redirect(url_for('admin_dashboard'))

    
    movies = list(mongo.db.movies.find())
    cinemas = list(mongo.db.cinemas.find())
    return render_template('admin/edit_screening.html', 
                           screening=screening, 
                           movies=movies, 
                           cinemas=cinemas)



@app.route('/admin/screenings/delete/<screening_id>')
@admin_required
def delete_screening(current_user, screening_id):
    mongo.db.screenings.delete_one({'_id': ObjectId(screening_id)})
    return redirect(url_for('admin_dashboard'))





@app.route('/profile/delete', methods=['POST'])
@token_required
def delete_account(current_user):
    current_password = request.form.get('current_password')

    
    if not check_password_hash(current_user['password'], current_password):
        flash('Incorrect password. Account deletion cancelled.', 'danger')
        return redirect(url_for('profile'))

    try:
        user_id = current_user['_id']
        
        
        mongo.db.bookings.delete_many({'user_id': user_id})
        mongo.db.food_orders.delete_many({'user_id': user_id})
        
        
        profile_pic = current_user.get('profile_pic')
        if profile_pic:
            try:
                
                os.remove(os.path.join(app.config['UPLOAD_FOLDER'], profile_pic))
            except OSError as e:
                print(f"Error deleting profile picture file: {e}")
                pass 

        
        mongo.db.users.delete_one({'_id': user_id})

        
        session.clear()
        flash('Your account and all associated data have been permanently deleted.', 'success')
        return redirect(url_for('home')) 

    except Exception as e:
        print(f"An error occurred during account deletion: {e}")
        flash('An error occurred. Please try again.', 'danger')
        return redirect(url_for('profile'))



@app.route('/admin/cinemas')
@admin_required
def admin_cinemas(current_user):
    cinemas = list(mongo.db.cinemas.find().sort('city', 1))
    return render_template('admin/cinemas.html', cinemas=cinemas)

@app.route('/admin/cinemas/add', methods=['GET', 'POST'])
@admin_required
def add_cinema(current_user):
    if request.method == 'POST':
        mongo.db.cinemas.insert_one({
            'name': request.form.get('name'),
            'city': request.form.get('city'),
            'country': request.form.get('country'),
            'address': request.form.get('address'),
            'created_at': datetime.datetime.utcnow()
        })
        flash('Cinema branch added successfully!', 'success')
        return redirect(url_for('admin_cinemas'))
    return render_template('admin/add_cinema.html')

@app.route('/admin/cinemas/edit/<cinema_id>', methods=['GET', 'POST'])
@admin_required
def edit_cinema(current_user, cinema_id):
    cinema = mongo.db.cinemas.find_one({'_id': ObjectId(cinema_id)})
    if not cinema:
        return redirect(url_for('admin_cinemas'))
        
    if request.method == 'POST':
        mongo.db.cinemas.update_one(
            {'_id': ObjectId(cinema_id)},
            {'$set': {
                'name': request.form.get('name'),
                'city': request.form.get('city'),
                'country': request.form.get('country'),
                'address': request.form.get('address')
            }}
        )
        flash('Cinema branch updated successfully!', 'success')
        return redirect(url_for('admin_cinemas'))
        
    return render_template('admin/edit_cinema.html', cinema=cinema)

@app.route('/admin/cinemas/delete/<cinema_id>')
@admin_required
def delete_cinema(current_user, cinema_id):
    mongo.db.cinemas.delete_one({'_id': ObjectId(cinema_id)})
    
    mongo.db.screenings.delete_many({'cinema_id': ObjectId(cinema_id)})
    flash('Cinema branch and its screenings deleted!', 'success')
    return redirect(url_for('admin_cinemas'))



@app.route('/admin/promotions')
@admin_required
def admin_promotions(current_user):
    promotions = []
    for promo in mongo.db.promotions.find():
        movie = mongo.db.movies.find_one({'_id': promo['movie_id']})
        promo['movie_title'] = movie['title'] if movie else 'Unknown Movie'
        promotions.append(promo)
    return render_template('admin/promotions.html', promotions=promotions)

@app.route('/admin/promotions/add', methods=['GET', 'POST'])
@admin_required
def add_promotion(current_user):
    if request.method == 'POST':
        mongo.db.promotions.insert_one({
            'name': request.form.get('name'),
            'description': request.form.get('description'),
            'type': request.form.get('type'), 
            'movie_id': ObjectId(request.form.get('movie_id')),
            'is_active': request.form.get('is_active') == 'on',
            'created_at': datetime.datetime.utcnow()
        })
        flash('Promotion added successfully!', 'success')
        return redirect(url_for('admin_promotions'))
    
    movies = list(mongo.db.movies.find({'status': 'now_showing'}))
    return render_template('admin/add_promotion.html', movies=movies)


@app.route('/admin/promotions/edit/<promo_id>', methods=['GET', 'POST'])
@admin_required
def edit_promotion(current_user, promo_id):
    
    promotion = mongo.db.promotions.find_one({'_id': ObjectId(promo_id)})
    if not promotion:
        flash('Promotion not found!', 'danger')
        return redirect(url_for('admin_promotions'))

    if request.method == 'POST':
        
        name = request.form.get('name')
        description = request.form.get('description')
        promo_type = request.form.get('type')
        movie_id = request.form.get('movie_id')
        is_active = request.form.get('is_active') == 'on'
        
        
        mongo.db.promotions.update_one(
            {'_id': ObjectId(promo_id)},
            {'$set': {
                'name': name,
                'description': description,
                'type': promo_type,
                'movie_id': ObjectId(movie_id),
                'is_active': is_active,
                'updated_at': datetime.datetime.utcnow()
            }}
        )
        
        flash('Promotion updated successfully!', 'success')
        return redirect(url_for('admin_promotions'))

    
    
    movies = list(mongo.db.movies.find({'status': 'now_showing'}))
    return render_template('admin/edit_promotion.html', 
                          promotion=promotion, 
                          movies=movies)

@app.route('/admin/promotions/delete/<promo_id>')
@admin_required
def delete_promotion(current_user, promo_id):
    mongo.db.promotions.delete_one({'_id': ObjectId(promo_id)})
    flash('Promotion deleted successfully!', 'success')
    return redirect(url_for('admin_promotions'))







@app.route('/admin/food')
@admin_required
def admin_food(current_user):
    food_items = list(mongo.db.food_items.find())
    return render_template('admin/food_items.html', food_items=food_items)

@app.route('/admin/food/add', methods=['GET', 'POST'])
@admin_required
def add_food_item(current_user):
    if request.method == 'POST':
        name = request.form.get('name')
        description = request.form.get('description')
        price = float(request.form.get('price'))
        category = request.form.get('category')
        image_url = request.form.get('image_url')
        
        mongo.db.food_items.insert_one({
            'name': name,
            'description': description,
            'price': price,
            'category': category,
            'image_url': image_url,
            'is_available': True,
            'created_at': datetime.datetime.utcnow()
        })
        
        return redirect(url_for('admin_food'))
    
    return render_template('admin/add_food_item.html')

@app.route('/admin/food/edit/<food_id>', methods=['GET', 'POST'])
@admin_required
def edit_food_item(current_user, food_id):
    food_item = mongo.db.food_items.find_one({'_id': ObjectId(food_id)})
    if not food_item:
        return redirect(url_for('admin_food'))
    
    if request.method == 'POST':
        name = request.form.get('name')
        description = request.form.get('description')
        price = float(request.form.get('price'))
        category = request.form.get('category')
        image_url = request.form.get('image_url')
        is_available = request.form.get('is_available') == 'on'
        
        mongo.db.food_items.update_one(
            {'_id': ObjectId(food_id)},
            {'$set': {
                'name': name,
                'description': description,
                'price': price,
                'category': category,
                'image_url': image_url,
                'is_available': is_available,
                'updated_at': datetime.datetime.utcnow()
            }}
        )
        
        return redirect(url_for('admin_food'))
    
    return render_template('admin/edit_food_item.html', food_item=food_item)

@app.route('/admin/food/delete/<food_id>', methods=['POST'])  
@admin_required
def delete_food_item(current_user, food_id):
    try:
        mongo.db.food_items.delete_one({'_id': ObjectId(food_id)})
        flash('Item deleted successfully!', 'success')
    except Exception as e:
        flash('Error deleting item: ' + str(e), 'danger')
    return redirect(url_for('admin_food'))

@app.route('/food')
@token_required
def food_menu(current_user):
    food_items = list(mongo.db.food_items.find({'is_available': True}))
    return render_template('food_menu.html', food_items=food_items)

@app.route('/food/order', methods=['POST'])
@token_required
def order_food(current_user):
    food_items = request.form.getlist('food_items')
    quantities = request.form.getlist('quantities')
    
    
    order_items = []
    total_price = 0
    for i, food_id in enumerate(food_items):
        quantity = int(quantities[i])
        if quantity > 0:
            food_item = mongo.db.food_items.find_one({'_id': ObjectId(food_id)})
            if food_item and food_item.get('is_available'):
                order_items.append({
                    'food_id': ObjectId(food_id),
                    'name': food_item['name'],
                    'price': food_item['price'],
                    'quantity': quantity
                })
                total_price += food_item['price'] * quantity
    
    if not order_items:
        return jsonify({'success': False, 'message': 'No items selected'})
    
    
    order_data = {
        'user_id': ObjectId(session['user_id']),
        'items': order_items,
        'total_price': total_price,
        'status': 'pending',
        'order_date': datetime.datetime.utcnow(),
        'delivery_time': None
    }
    
    order_id = mongo.db.food_orders.insert_one(order_data).inserted_id
    
    return jsonify({
        'success': True,
        'message': 'Order placed successfully',
        'order_id': str(order_id)
    })

@app.route('/my-food-orders')
@token_required
def my_food_orders(current_user):
    orders = []
    
    user_orders = mongo.db.food_orders.find({'user_id': ObjectId(current_user['_id'])})
    for order in user_orders:
        orders.append(order)
    
    
    orders.sort(key=lambda x: x['order_date'], reverse=True)
    
    return render_template('my_food_orders.html', orders=orders)

@app.route('/checkout/food', methods=['GET', 'POST'])
@token_required
def checkout_food(current_user):
    if request.method == 'POST':
        
        selected_items = []
        total = 0
        for key, value in request.form.items():
            if key.startswith('quantities_') and int(value) > 0:
                item_id = key.replace('quantities_', '')
                quantity = int(value)
                food_item = mongo.db.food_items.find_one({'_id': ObjectId(item_id)})
                if food_item:
                    selected_items.append({
                        'item_id': item_id,
                        'name': food_item['name'],
                        'price': food_item['price'],
                        'quantity': quantity
                    })
                    total += food_item['price'] * quantity
        
        if not selected_items:
            return redirect(url_for('food_menu'))
        
        
        session['food_order'] = {
            'items': selected_items,
            'total': total,
            'order_date': datetime.datetime.utcnow().isoformat()
        }
        
        return render_template('food_checkout.html', 
                            items=selected_items, 
                            total=total)
    
    return redirect(url_for('food_menu'))

@app.route('/process-payment', methods=['POST'])
@token_required
def process_payment(current_user):
    if 'food_order' not in session:
        flash('No order found', 'danger')
        return redirect(url_for('food_menu'))
    
    try:
        order_data = {
            'user_id': ObjectId(current_user['_id']),
            'items': session['food_order']['items'],
            'total': session['food_order']['total'],
            'order_date': datetime.datetime.utcnow(),
            'status': 'completed',
            'payment_method': 'card'
        }
        
        order_id = mongo.db.food_orders.insert_one(order_data).inserted_id
        
        
        session.pop('food_order', None)
        
        flash('Your order has been placed successfully!', 'success')
        return redirect(url_for('my_food_orders'))
    
    except Exception as e:
        print(f"Error: {str(e)}")
        flash('Payment failed. Please try again.', 'danger')
        return redirect(url_for('checkout_food'))
    

    



@app.route('/admin/users')
@admin_required
def admin_users(current_user):
   
    
    users = list(mongo.db.users.find({'is_admin': {'$ne': True}}).sort('created_at', -1))
    
    return render_template('admin/users.html', users=users)


@app.route('/admin/users/edit/<user_id>', methods=['GET', 'POST'])
@admin_required
def edit_user(current_user, user_id):
    
    try:
        user_to_edit = mongo.db.users.find_one({'_id': ObjectId(user_id)})
    except:
        flash('Invalid user ID.', 'danger')
        return redirect(url_for('admin_users'))

    if not user_to_edit:
        flash('User not found.', 'danger')
        return redirect(url_for('admin_users'))

    
    if user_to_edit.get('is_admin'):
        flash('Admin accounts cannot be edited from this interface.', 'danger')
        return redirect(url_for('admin_users'))

    if request.method == 'POST':
        name = request.form.get('name')
        email = request.form.get('email')
        phone = request.form.get('phone')
        address = request.form.get('address')
        city = request.form.get('city')
        country = request.form.get('country')
        dob_str = request.form.get('dob')
        gender = request.form.get('gender')
        
        
        if email != user_to_edit['email'] and mongo.db.users.find_one({'email': email}):
            flash('Email already exists for another user.', 'danger')
            return render_template('admin/edit_user.html', user=user_to_edit)
        
        try:
            dob = datetime.datetime.strptime(dob_str, '%Y-%m-%d') if dob_str else None
        except ValueError:
            dob = user_to_edit.get('dob') 
            flash('Invalid date format for Date of Birth. Kept original value.', 'warning')

        update_data = {
            'name': name,
            'email': email,
            'phone': phone,
            'address': address,
            'city': city,
            'country': country,
            'dob': dob,
            'gender': gender,
            'updated_at': datetime.datetime.utcnow()
        }

        mongo.db.users.update_one(
            {'_id': ObjectId(user_id)},
            {'$set': update_data}
        )
        
        flash('User profile updated successfully.', 'success')
        return redirect(url_for('admin_users'))

    
    return render_template('admin/edit_user.html', user=user_to_edit)


@app.route('/admin/users/delete/<user_id>', methods=['POST'])
@admin_required
def delete_user(current_user, user_id):
   
    try:
        user_to_delete = mongo.db.users.find_one({'_id': ObjectId(user_id)})

        if not user_to_delete:
            flash('User not found.', 'danger')
            return redirect(url_for('admin_users'))

        
        if user_to_delete.get('is_admin'):
            flash('Cannot delete an admin account.', 'danger')
            return redirect(url_for('admin_users'))

        user_id_obj = user_to_delete['_id']

        
        mongo.db.bookings.delete_many({'user_id': user_id_obj})
        mongo.db.food_orders.delete_many({'user_id': user_id_obj})
        
        
        profile_pic = user_to_delete.get('profile_pic')
        if profile_pic:
            try:
                os.remove(os.path.join(app.config['UPLOAD_FOLDER'], profile_pic))
            except OSError as e:
                print(f"Error deleting profile picture file for user {user_id_obj}: {e}")
                pass 

        
        mongo.db.users.delete_one({'_id': user_id_obj})

        flash('User account and all associated data have been permanently deleted.', 'success')

    except Exception as e:
        print(f"An error occurred during user deletion by admin: {e}")
        flash('An error occurred. Please try again.', 'danger')

    return redirect(url_for('admin_users'))





@app.route('/admin/manage-admins')
@admin_manager_required
def admin_manage_list(current_user):
    
    admins = list(mongo.db.users.find({
        'is_admin': True,
        'is_deleted': {'$ne': True}
    }).sort('name', 1))
    
    
    return render_template('admin/manage_admins.html', admins=admins, current_admin_id=str(current_user['_id']))


@app.route('/admin/manage-admins/add', methods=['GET', 'POST'])
@admin_manager_required
def admin_manage_add(current_user):
    
    if request.method == 'POST':
        name = request.form.get('name')
        email = request.form.get('email')
        password = request.form.get('password')
        can_manage = request.form.get('can_manage_admins') == 'on'

        if mongo.db.users.find_one({'email': email}):
            flash('Email already exists.', 'danger')
            return render_template('admin/add_admin.html')
        
        if len(password) < 8:
            flash('Password must be at least 8 characters.', 'danger')
            return render_template('admin/add_admin.html')

        mongo.db.users.insert_one({
            'name': name,
            'email': email,
            'password': generate_password_hash(password),
            'is_admin': True,
            'is_system_admin': False, 
            'can_manage_admins': can_manage,
            'is_deleted': False,
            'created_at': datetime.datetime.utcnow(),
            'bookings': []
        })
        
        flash(f'Admin "{name}" created successfully.', 'success')
        return redirect(url_for('admin_manage_list'))
        
    return render_template('admin/add_admin.html')


@app.route('/admin/manage-admins/edit/<admin_id>', methods=['GET', 'POST'])
@admin_manager_required
def admin_manage_edit(current_user, admin_id):
   
    try:
        admin_to_edit = mongo.db.users.find_one({'_id': ObjectId(admin_id)})
    except:
        flash('Invalid admin ID.', 'danger')
        return redirect(url_for('admin_manage_list'))

    if not admin_to_edit or not admin_to_edit.get('is_admin'):
        flash('Admin not found.', 'danger')
        return redirect(url_for('admin_manage_list'))

    
    if admin_to_edit.get('is_system_admin'):
        flash('The System Admin account cannot be edited.', 'danger')
        return redirect(url_for('admin_manage_list'))

    if request.method == 'POST':
        name = request.form.get('name')
        email = request.form.get('email')
        can_manage = request.form.get('can_manage_admins') == 'on'
        
        
        if email != admin_to_edit['email'] and mongo.db.users.find_one({'email': email}):
            flash('Email already exists for another user.', 'danger')
            return render_template('admin/edit_admin.html', admin=admin_to_edit)

        update_data = {
            'name': name,
            'email': email,
            'can_manage_admins': can_manage
        }

        
        password = request.form.get('password')
        if password:
            if len(password) < 8:
                flash('Password must be at least 8 characters.', 'danger')
                return render_template('admin/edit_admin.html', admin=admin_to_edit)
            update_data['password'] = generate_password_hash(password)

        mongo.db.users.update_one(
            {'_id': ObjectId(admin_id)},
            {'$set': update_data}
        )
        
        flash(f'Admin "{name}" updated successfully.', 'success')
        return redirect(url_for('admin_manage_list'))

    return render_template('admin/edit_admin.html', admin=admin_to_edit)


@app.route('/admin/manage-admins/delete/<admin_id>', methods=['POST'])
@admin_manager_required
def admin_manage_delete(current_user, admin_id):
   
    try:
        admin_to_delete = mongo.db.users.find_one({'_id': ObjectId(admin_id)})
    except:
        flash('Invalid admin ID.', 'danger')
        return redirect(url_for('admin_manage_list'))

    if not admin_to_delete or not admin_to_delete.get('is_admin'):
        flash('Admin not found.', 'danger')
        return redirect(url_for('admin_manage_list'))

    
    if admin_to_delete.get('is_system_admin'):
        flash('The System Admin account cannot be deleted.', 'danger')
        return redirect(url_for('admin_manage_list'))
    
    if str(admin_to_delete['_id']) == str(current_user['_id']):
        flash('You cannot delete your own account.', 'danger')
        return redirect(url_for('admin_manage_list'))

    
    mongo.db.users.update_one(
        {'_id': ObjectId(admin_id)},
        {'$set': {
            'is_deleted': True,
            'password': generate_password_hash(str(datetime.datetime.utcnow())),
            'email': f"DELETED_{admin_id}_{admin_to_delete['email']}"
        }}
    )
    
    flash(f'Admin "{admin_to_delete["name"]}" has been deleted.', 'success')
    return redirect(url_for('admin_manage_list'))



@app.route('/admin/all-bookings')
@admin_required
def admin_all_bookings(current_user):
    
    
    pipeline = [
        {
            '$lookup': {
                'from': 'users',        
                'localField': 'user_id',
                'foreignField': '_id',
                'as': 'userDetails'     
            }
        },
        {
            '$lookup': {
                'from': 'movies',
                'localField': 'movie_id',
                'foreignField': '_id',
                'as': 'movieDetails'
            }
        },
        {
            '$lookup': {
                'from': 'screenings',
                'localField': 'screening_id',
                'foreignField': '_id',
                'as': 'screeningDetails'
            }
        },
        {
            '$lookup': {
                'from': 'cinemas',
                'localField': 'cinema_id',
                'foreignField': '_id',
                'as': 'cinemaDetails'
            }
        },
        {
            '$unwind': {'path': '$userDetails', 'preserveNullAndEmptyArrays': True}
        },
        {
            '$unwind': {'path': '$movieDetails', 'preserveNullAndEmptyArrays': True}
        },
        {
            '$unwind': {'path': '$screeningDetails', 'preserveNullAndEmptyArrays': True}
        },
        {
            '$unwind': {'path': '$cinemaDetails', 'preserveNullAndEmptyArrays': True}
        },
        {
            '$sort': {'booking_date': -1}
        }
    ]
    
    all_bookings = list(mongo.db.bookings.aggregate(pipeline))
    
    return render_template('admin/all_bookings.html', bookings=all_bookings)


if __name__ == '__main__':
    app.run(debug=True)