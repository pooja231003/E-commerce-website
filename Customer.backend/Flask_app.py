from flask import Flask, render_template, request, redirect, url_for, session, jsonify, abort, flash
import sqlite3
from PIL import Image
from io import BytesIO
import base64
from flask_wtf import CSRFProtect
from datetime import datetime

app = Flask(__name__)
app.config['SECRET_KEY'] = 'your_secret_key'
csrf = CSRFProtect(app)

def get_db_connection():
    conn = sqlite3.connect('dummy.db')
    conn.row_factory = sqlite3.Row  # To access columns by name
    return conn

def create_table():
    conn = get_db_connection()
    with conn:
        conn.execute('''
            CREATE TABLE IF NOT EXISTS customer (
                c_gmail TEXT PRIMARY KEY, 
                password TEXT NOT NULL,
                c_name TEXT NOT NULL,
                address TEXT NOT NULL,
                city TEXT NOT NULL,
                state TEXT NOT NULL,
                pincode INT NOT NULL,
                contact INT NOT NULL
            )
        ''')
        conn.execute('''
            CREATE TABLE IF NOT EXISTS cart (
                cart_id INTEGER PRIMARY KEY AUTOINCREMENT,
                c_gmail TEXT NOT NULL,
                id INTEGER NOT NULL,
                pkg_quantity INTEGER NOT NULL,
                price FLOAT NOT NULL,
                no_quantity INTEGER NOT NULL,
                FOREIGN KEY (c_gmail) REFERENCES customer(c_gmail),
                FOREIGN KEY (id) REFERENCES products(id)
            )
        ''')

def convert_image_to_blob(image):
    img = Image.open(image)
    buffered = BytesIO()
    img.save(buffered, format="PNG" if img.format == "PNG" else "JPEG")
    return buffered.getvalue()


@app.route('/')
def home():
    return render_template('home.html')


@app.route('/billing', methods=['GET', 'POST'])
def billing():
    if request.method == 'POST':
        c_gmail = request.form['c_gmail']
        password = request.form['password']
        c_name = request.form['c_name']
        address = request.form['address']
        city = request.form['city']
        state = request.form['state']
        pincode = request.form['pincode']
        contact = request.form['contact']
        conn = get_db_connection()
        with conn:
            cursor = conn.cursor()
            cursor.execute('INSERT INTO customer (c_gmail, password, c_name, address, city, state, pincode, contact) VALUES (?, ?, ?, ?, ?, ?, ?, ?)', (c_gmail, password, c_name, address, city, state, pincode, contact))
            conn.commit()
        return redirect(url_for('userlogin'))
    return render_template('billing.html')


@app.route('/userlogin', methods=['GET', 'POST'])
def userlogin():
    if request.method == 'POST':
        c_gmail = request.form['c_gmail']
        password = request.form['password']
        conn = get_db_connection()
        with conn:
            cursor = conn.cursor()
            cursor.execute('SELECT * FROM customer WHERE c_gmail = ?', (c_gmail,))
            user = cursor.fetchone()
            if user and user['password'] == password:  # Check if user exists and password matches
                session['user_id'] = c_gmail
                return redirect(url_for('home'))  # Redirect to home page after successful login
            else:
                flash('Invalid email or password')  # Display an error message
                return redirect(url_for('userlogin'))
    return render_template('userlogin.html')



@app.route('/logout', methods=['POST'])
def logout():
    session.pop('user_id', None)  # Remove user ID from session
    return redirect(url_for('home'))



# new urls
@app.route('/shop')
def shop():
    conn = get_db_connection()
    with conn:
        cursor = conn.cursor()
        cursor.execute('SELECT id, name, image, cost FROM products')
        products = cursor.fetchall()
    
    encoded_products = [
        (
            product['id'], product['name'], base64.b64encode(product['image']).decode('utf-8'),
            product['cost']
        ) for product in products
    ]

    return render_template('shop.html', products=encoded_products)


@app.route('/get_prices/<int:product_id>', methods=['GET'])
def get_prices(product_id):
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute('''
        SELECT cost, quantity_half, quantity1, quantity2, quantity3, quantity5
        FROM products WHERE id = ?
    ''', (product_id,))
    row = cursor.fetchone()
    conn.close()
    
    if row is None:
        return jsonify({'error': 'Product not found'}), 404
    
    cost, quantity_half, quantity1, quantity2, quantity3, quantity5 = row
    
    prices = {}
    if quantity_half:
        prices['500 ml'] = cost * 0.5
    if quantity1:
        prices['1 litre'] = cost
    if quantity2:
        prices['2 litre'] = cost * 2
    if quantity3:
        prices['3 litre'] = cost * 3
    if quantity5:
        prices['5 litre'] = cost * 5
    
    return jsonify(prices)


@app.route('/add_to_cart', methods=['POST'])
def add_to_cart():
    if 'user_id' not in session:
        return jsonify({'success': False, 'message': 'You must be logged in to add items to the cart.'}), 401

    data = request.get_json()
    product_id = data.get('product_id')
    pkg_quantity = data.get('pkg_quantity')
    price = data.get('price')
    no_quantity = data.get('no_quantity')

    if not all([product_id, pkg_quantity, price, no_quantity]):
        return jsonify({'success': False, 'message': 'Missing data'}), 400

    user_email = session['user_id']

    conn = get_db_connection()
    cursor = conn.cursor()

    cursor.execute('''
        INSERT INTO cart (c_gmail, id, pkg_quantity, price, no_quantity)
        VALUES (?, ?, ?, ?, ?)
    ''', (user_email, product_id, pkg_quantity, price, no_quantity))

    conn.commit()
    conn.close()

    return jsonify({'success': True, 'message': 'Item added to cart'}), 200



@app.route('/get_cart_items', methods=['GET'])
def get_cart_items():
    if 'user_id' not in session:
        return jsonify({'success': False, 'message': 'Please log in to view cart items.'}), 401

    user_id = session['user_id']
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute('''
        SELECT cart.cart_id, cart.id, products.name, products.image, cart.pkg_quantity, cart.price, cart.no_quantity 
        FROM cart 
        JOIN products ON cart.id = products.id 
        WHERE cart.c_gmail = ?
    ''', (user_id,))
    cart_items = cursor.fetchall()
    conn.close()

    cart = []
    for item in cart_items:
        cart.append({
            'cart_id': item[0],
            'product_id': item[1],
            'name': item[2],
            'image': base64.b64encode(item[3]).decode('utf-8'),
            'pkg_quantity': item[4],
            'price': item[5],
            'no_quantity': item[6]
        })

    return jsonify({'success': True, 'cart': cart}), 200



@app.route('/update_cart_item', methods=['POST'])
def update_cart_item():
    data = request.json
    cart_id = data.get('cart_id')
    quantity = data.get('quantity')

    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute('UPDATE cart SET no_quantity = ? WHERE cart_id = ?', (quantity, cart_id))
        conn.commit()
        conn.close()

        return jsonify({'success': True, 'message': 'Cart item updated.'})
    except Exception as e:
        print("Error updating cart item:", e)
        return jsonify({'success': False, 'message': 'Failed to update cart item.'})
    

@app.route('/remove_from_cart', methods=['POST'])
def remove_from_cart():
    data = request.json
    cart_id = data.get('cart_id')

    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute('DELETE FROM cart WHERE cart_id = ?', (cart_id,))
        conn.commit()
        conn.close()

        return jsonify({'success': True, 'message': 'Item removed from cart.'})
    except Exception as e:
        print("Error removing cart item:", e)
        return jsonify({'success': False, 'message': 'Failed to remove item from cart.'})


@app.route('/cart', methods=['GET', 'POST'])
def cart():
    return render_template('cart.html')


@app.route('/profile', methods=['GET'])
def profile():
    conn = get_db_connection()
    with conn:
        user_id = session.get('user_id')
        cursor = conn.cursor()
        cursor.execute('SELECT c_gmail, c_name, address, city, state, pincode, contact FROM customer WHERE c_gmail = ?', (user_id,))
        customer = cursor.fetchone()

    customer_dict = {
        'c_gmail': customer['c_gmail'], 
        'c_name': customer['c_name'], 
        'address': customer['address'], 
        'city': customer['city'], 
        'state': customer['state'], 
        'pincode': customer['pincode'], 
        'contact': customer['contact']
    } if customer else None

    return render_template('profile.html', customer=customer_dict)

@app.route('/update_profile', methods=['POST'])
def update_profile():
    data = request.get_json()
    email = session.get('user_id')
    name = data.get('name')
    address = data.get('address')
    city = data.get('city')
    state = data.get('state')
    pincode = data.get('pin_code')
    contact = data.get('contact')

    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute('''
        UPDATE customer SET c_name = ?, address = ?, city = ?, state = ?, pincode = ?, contact = ? WHERE c_gmail = ?
    ''', ( name, address, city, state, pincode, contact, email))
    conn.commit()
    conn.close()

    return jsonify({'success': True})

@app.route('/your_profile')
def your_profile():
    if 'user_id' not in session:
        return redirect(url_for('userlogin'))
    
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute('SELECT c_gmail, c_name, address, city, state, pincode, contact FROM customer WHERE c_gmail = ?', (session['user_id'],))
    customer = cursor.fetchone()
    conn.close()

    if customer is None:
        return redirect(url_for('userlogin'))
    
    customer_dict = {
        'c_gmail': customer[0],
        'c_name': customer[1],
        'address': customer[2],
        'city': customer[3],
        'state': customer[4],
        'pincode': customer[5],
        'contact': customer[6]
    }
    
    return render_template('your_profile.html', customer=customer_dict)


@app.route('/place_order', methods=['POST'])
def place_order():
    data = request.json
    c_gmail = data.get('c_gmail')
    if not c_gmail:
        return jsonify({'success': False, 'message': 'Customer email is required'}), 400
    conn = get_db_connection()
    cursor = conn.cursor()
    try:
        cursor.execute('SELECT * FROM cart WHERE c_gmail = ?', (c_gmail,))
        cart_items = cursor.fetchall()
        if not cart_items:
            return jsonify({'success': False, 'message': 'No items in the cart'}), 400
        order_date = datetime.now().date()
        for item in cart_items:
            cursor.execute('''
                INSERT INTO orders (c_gmail, id, pkg_quantity, price, no_quantity, date)
                VALUES (?, ?, ?, ?, ?, ?)
            ''', (
                c_gmail, item['id'], item['pkg_quantity'], item['price'], item['no_quantity'], order_date
            ))
        cursor.execute('DELETE FROM cart WHERE c_gmail = ?', (c_gmail,))
        conn.commit()
        return jsonify({'success': True, 'message': 'Order placed successfully'})
    except Exception as e:
        conn.rollback()
        return jsonify({'success': False, 'message': str(e)}), 500
    finally:
        conn.close()


@app.route('/view_orders')
def view_orders():
    conn = get_db_connection()
    user_id = session.get('user_id')
    
    if not user_id:
        return redirect(url_for('login'))  # Redirect to login if user_id is not in session
    
    cursor = conn.cursor()
    cursor.execute('''
        SELECT p.name, p.image, o.pkg_quantity, o.price, o.no_quantity, o.date, o.delivery, c.c_gmail
        FROM orders o
        JOIN products p ON o.id = p.id
        JOIN customer c ON o.c_gmail = c.c_gmail
        WHERE o.c_gmail = ?
        ORDER BY o.date DESC  -- Sort by date in descending order
    ''', (user_id,))
    orders = cursor.fetchall()
    conn.close()
    
    encoded_orders = [
        (
            order[0], base64.b64encode(order[1]).decode('utf-8'), order[2], 
            order[3], order[4], order[5], order[6], order[7],
            order[3] * order[4]  # Total Price calculation
        ) for order in orders
    ]

    return render_template('view_orders.html', orders=encoded_orders)


@app.route('/about', methods=['GET', 'POST'])
def about():
    return render_template('about.html')

@app.route('/description1/<int:product_id>', methods=['GET'])
def description1(product_id):
    conn = get_db_connection()
    cursor = conn.cursor()

    # Fetch product details and stock quantities
    cursor.execute('''
        SELECT p.id, p.name, p.image, p.cost, s.qty_500ml, s.qty_1l, s.qty_2l, s.qty_3l, s.qty_5l, p.availability, p.description
        FROM products p
        LEFT JOIN stock s ON p.id = s.pid
        WHERE p.id = ?
    ''', (product_id,))
    product = cursor.fetchone()

    conn.close()  # Ensure the connection is closed after use

    if product:
        # If no stock data found, handle it here
        if product['qty_500ml'] is None:
            # Handle missing stock data (e.g., set default quantities or a flag)
            product = dict(product)  # Convert to dict if necessary
            product['qty_500ml'] = 0
            product['qty_1l'] = 0
            product['qty_2l'] = 0
            product['qty_3l'] = 0
            product['qty_5l'] = 0

        encoded_product = (
            product['id'], product['name'], base64.b64encode(product['image']).decode('utf-8'),
            product['cost'], product['qty_500ml'], product['qty_1l'], product['qty_2l'],
            product['qty_3l'], product['qty_5l'], product['availability'], product['description']
        )

        # Check availability
        in_stock = any([
            product['qty_500ml'] > 5,
            product['qty_1l'] > 5,
            product['qty_2l'] > 5,
            product['qty_3l'] > 5,
            product['qty_5l'] > 5,
            product['availability'] is True
        ])

        return render_template('description1.html', product=encoded_product, in_stock=in_stock)
    else:
        # Product not found
        return render_template('not_found.html', message='Product not found.')


@app.route('/check_cart/<int:product_id>/<pkg_quantity>', methods=['GET'])
def check_cart(product_id, pkg_quantity):
    conn = get_db_connection()
    cursor = conn.cursor()

    is_in_cart = False
    if 'user_id' in session:
        user_email = session['user_id']
        cursor.execute('''
            SELECT * FROM cart 
            WHERE c_gmail = ? AND id = ? AND pkg_quantity = ?
        ''', (user_email, product_id, pkg_quantity))
        is_in_cart = cursor.fetchone() is not None

    conn.close()

    return jsonify({'in_cart': is_in_cart})

@app.route('/get_cart_count', methods=['GET'])
def get_cart_count():
    if 'user_id' not in session:
        return jsonify({'success': False, 'message': 'Please log in to view cart count.'}), 401

    user_id = session['user_id']
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute('''
        SELECT COUNT(*) FROM cart WHERE c_gmail = ?
    ''', (user_id,))
    count = cursor.fetchone()[0]
    conn.close()

    return jsonify({'success': True, 'count': count}), 200

@app.route('/get_stock/<int:product_id>/<string:pkg_quantity>', methods=['GET'])
def get_stock(product_id, pkg_quantity):
    conn = get_db_connection()
    cursor = conn.cursor()

    pkg_quantity_mapping = {
        '500 ml': 'qty_500ml',
        '1 litre': 'qty_1l',
        '2 litre': 'qty_2l',
        '3 litre': 'qty_3l',
        '5 litre': 'qty_5l'
    }
    
    stock_column = pkg_quantity_mapping.get(pkg_quantity)
    if not stock_column:
        return jsonify({'error': 'Invalid package quantity'}), 400

    cursor.execute(f'''
        SELECT {stock_column}
        FROM stock
        WHERE pid = ?
    ''', (product_id,))
    stock = cursor.fetchone()

    conn.close()

    if stock:
        return jsonify({'max_stock': stock[0]})
    else:
        return jsonify({'max_stock': 0})


@app.route('/get_all_stock/<int:product_id>', methods=['GET'])
def get_all_stock(product_id):
    conn = get_db_connection()
    cursor = conn.cursor()

    cursor.execute('''
        SELECT qty_500ml, qty_1l, qty_2l, qty_3l, qty_5l
        FROM stock
        WHERE pid = ?
    ''', (product_id,))
    stock = cursor.fetchone()

    conn.close()

    if stock:
        stock_data = {
            '500 ml': stock[0],
            '1 litre': stock[1],
            '2 litre': stock[2],
            '3 litre': stock[3],
            '5 litre': stock[4]
        }
        return jsonify({'stock': stock_data})
    else:
        return jsonify({'stock': {}})


@app.route('/success', methods=['GET', 'POST'])
def success():
    return render_template('success.html')

if __name__ == '__main__':
    create_table()
    app.run(debug=True)
