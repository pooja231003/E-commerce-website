from flask import Flask, render_template, request, redirect, url_for, flash
from flask import request, jsonify
import sqlite3
from PIL import Image
from io import BytesIO
import base64
from flask_wtf import CSRFProtect
import re

app = Flask(__name__)
app.secret_key = 'your_secret_key'  # Add a secret key for session management
csrf = CSRFProtect(app)

HARD_CODED_USERNAME = 'admin'
HARD_CODED_PASSWORD = 'password123'

def get_db_connection():
    conn = sqlite3.connect('dummy.db')
    conn.row_factory = sqlite3.Row  # Enable dictionary-like access to rows
    return conn

def create_table():
    conn = get_db_connection()
    with conn:
        conn.execute('''
            CREATE TABLE IF NOT EXISTS products (
                id INTEGER PRIMARY KEY NOT NULL,
                name TEXT NOT NULL,
                image BLOB NOT NULL,
                cost REAL NOT NULL,
                quantity_half BOOLEAN NOT NULL,
                quantity1 BOOLEAN NOT NULL,
                quantity2 BOOLEAN NOT NULL,
                quantity3 BOOLEAN NOT NULL,
                quantity5 BOOLEAN NOT NULL,
                availability BOOLEAN NOT NULL,
                description TEXT NOT NULL
            )
        ''')
        conn.execute('''
            CREATE TABLE IF NOT EXISTS orders (
                o_id INTEGER PRIMARY KEY AUTOINCREMENT,
                c_gmail TEXT NOT NULL, 
                id INTEGER NOT NULL,
                pkg_quantity TEXT NOT NULL,
                price FLOAT NOT NULL,
                no_quantity INTEGER NOT NULL,
                date DATE NOT NULL,
                delivery BOOLEAN NOT NULL DEFAULT FALSE,
                FOREIGN KEY (c_gmail) REFERENCES customer(c_gmail),
                FOREIGN KEY (id) REFERENCES products(id)
            )
        ''')

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
        conn.execute('''
            CREATE TABLE IF NOT EXISTS stock (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                pid INTEGER UNIQUE,
                qty_500ml REAL DEFAULT 0,
                qty_1l REAL DEFAULT 0,
                qty_2l REAL DEFAULT 0,
                qty_3l REAL DEFAULT 0,
                qty_5l REAL DEFAULT 0,
                FOREIGN KEY(pid) REFERENCES products(id)
            )

        ''')


def convert_image_to_blob(image):
    img = Image.open(image)
    buffered = BytesIO()
    img.save(buffered, format="PNG" if img.format == "PNG" else "JPEG")
    return buffered.getvalue()

@app.route('/')
def home():
    return render_template('index.html')

@app.route('/signup', methods=['GET', 'POST'])
def signup():
    return "Signup functionality is disabled."

@app.route('/login', methods=['GET', 'POST'])
def login():
    if request.method == 'POST':
        username = request.form['username']
        password = request.form['password']
        if username == HARD_CODED_USERNAME and password == HARD_CODED_PASSWORD:
            return render_template('success.html', username=username)
        else:
            flash("Invalid username or password")
            return redirect(url_for('login'))
    return render_template('login.html')

@app.route('/stock', methods=['GET'])
def get_stock():
    conn = get_db_connection()
    with conn:
        cursor = conn.cursor()
        cursor.execute('''
            SELECT products.id, products.name, 
                stock.qty_500ml, stock.qty_1l, stock.qty_2l, stock.qty_3l, stock.qty_5l
            FROM stock
            JOIN products ON stock.pid = products.id
            GROUP BY products.id, products.name
        ''')
        stock_items = cursor.fetchall()
    return render_template('stock.html', stock_items=stock_items)


@app.route('/add_stock', methods=['POST'])
def add_stock():
    print(request.form)  # Debugging form data
    try:
        pid = int(request.form['pid'])
        qty_500ml = float(request.form['qty_500ml'] or 0)
        qty_1l = float(request.form['qty_1l'] or 0)
        qty_2l = float(request.form['qty_2l'] or 0)
        qty_3l = float(request.form['qty_3l'] or 0)
        qty_5l = float(request.form['qty_5l'] or 0)
    except KeyError as e:
        return f"Missing form data: {e}", 400

    conn = get_db_connection()
    with conn:
        cursor = conn.cursor()
        cursor.execute('''
            INSERT INTO stock (pid, qty_500ml, qty_1l, qty_2l, qty_3l, qty_5l) 
            VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT(pid) DO UPDATE SET 
                qty_500ml = qty_500ml + excluded.qty_500ml,
                qty_1l = qty_1l + excluded.qty_1l,
                qty_2l = qty_2l + excluded.qty_2l,
                qty_3l = qty_3l + excluded.qty_3l,
                qty_5l = qty_5l + excluded.qty_5l
        ''', (pid, qty_500ml, qty_1l, qty_2l, qty_3l, qty_5l))
        conn.commit()
    
    return redirect(url_for('get_stock'))

@app.route('/remove_stock/<int:pid>', methods=['GET','POST'])
def remove_stock(pid):
    conn = get_db_connection()
    with conn:
        conn.execute('DELETE FROM stock WHERE pid = ?', (pid,))
    return redirect(url_for('get_stock'))

@app.route('/update_stock/<int:pid>', methods=['GET', 'POST'])
def update_stock(pid):
    conn = get_db_connection()
    if request.method == 'POST':
        # Fetch the current quantities from the database
        cursor = conn.cursor()
        cursor.execute('''
            SELECT qty_500ml, qty_1l, qty_2l, qty_3l, qty_5l 
            FROM stock WHERE pid = ?
        ''', (pid,))
        current_quantities = cursor.fetchone()
        
        # Read new quantities from the form
        qty_500ml = float(request.form['qty_500ml'] or 0)
        qty_1l = float(request.form['qty_1l'] or 0)
        qty_2l = float(request.form['qty_2l'] or 0)
        qty_3l = float(request.form['qty_3l'] or 0)
        qty_5l = float(request.form['qty_5l'] or 0)
        
        # Add new quantities to current quantities
        new_qty_500ml = current_quantities['qty_500ml'] + qty_500ml
        new_qty_1l = current_quantities['qty_1l'] + qty_1l
        new_qty_2l = current_quantities['qty_2l'] + qty_2l
        new_qty_3l = current_quantities['qty_3l'] + qty_3l
        new_qty_5l = current_quantities['qty_5l'] + qty_5l
        
        with conn:
            cursor.execute('''
                UPDATE stock SET 
                    qty_500ml = ?,
                    qty_1l = ?,
                    qty_2l = ?,
                    qty_3l = ?,
                    qty_5l = ?
                WHERE pid = ?
            ''', (new_qty_500ml, new_qty_1l, new_qty_2l, new_qty_3l, new_qty_5l, pid))
            conn.commit()
        return redirect(url_for('get_stock'))

    cursor = conn.cursor()
    cursor.execute('''
        SELECT qty_500ml, qty_1l, qty_2l, qty_3l, qty_5l 
        FROM stock WHERE pid = ?
    ''', (pid,))
    current_quantities = cursor.fetchone()
    return render_template('update_stock.html', pid=pid, current_quantities=current_quantities)



@app.route('/add_product', methods=['GET', 'POST'])
def add_product():
    if request.method == 'POST':
        id = request.form['id']
        name = request.form['name']
        image_blob = convert_image_to_blob(request.files['image'])
        cost = float(request.form['cost'])
        quantity_half = request.form['quantity_half'] == 'yes'
        quantity1 = request.form['quantity1'] == 'yes'
        quantity2 = request.form['quantity2'] == 'yes'
        quantity3 = request.form['quantity3'] == 'yes'
        quantity5 = request.form['quantity5'] == 'yes'
        availability = request.form['availability'] == 'yes'
        description = request.form['description']

        # Collect pkg_quantity as a string
        pkg_quantity_list = [
            '500ml' if quantity_half else '',
            '1 litre' if quantity1 else '',
            '2 litre' if quantity2 else '',
            '3 litre' if quantity3 else '',
            '5 litre' if quantity5 else ''
        ]
        pkg_quantity = ', '.join(filter(None, pkg_quantity_list))

        conn = get_db_connection()
        with conn:
            conn.execute('''
                INSERT INTO products (id, name, image, cost, quantity_half, quantity1, quantity2, quantity3, quantity5, availability, description) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ''', (id, name, image_blob, cost, quantity_half, quantity1, quantity2, quantity3, quantity5, availability, description))
        return redirect(url_for('get_stock'))
    return render_template('add_product.html')


@app.route('/products')
def products():
    conn = get_db_connection()
    with conn:
        cursor = conn.cursor()
        cursor.execute('SELECT * FROM products')
        products = cursor.fetchall()
    
    encoded_products = [
        (
            product['id'], product['name'], base64.b64encode(product['image']).decode('utf-8'),
            product['cost'], product['quantity_half'], product['quantity1'], product['quantity2'],
            product['quantity3'], product['quantity5'], product['availability'], product['description']
        ) for product in products
    ]

    return render_template('products.html', products=encoded_products)

@app.route('/remove_product/<int:product_id>', methods=['POST'])
def remove_product(product_id):
    conn = get_db_connection()
    with conn:
        conn.execute('DELETE FROM products WHERE id = ?', (product_id,))
    return render_template('success.html')

@app.route('/edit_product/<int:product_id>', methods=['GET', 'POST'])
def edit_product(product_id):
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute('SELECT * FROM products WHERE id = ?', (product_id,))
    product = cursor.fetchone()

    if request.method == 'POST':
        name = request.form['name']
        cost = float(request.form['cost'])
        quantity_half = request.form['quantity_half'] == 'yes'
        quantity1 = request.form['quantity1'] == 'yes'
        quantity2 = request.form['quantity2'] == 'yes'
        quantity3 = request.form['quantity3'] == 'yes'
        quantity5 = request.form['quantity5'] == 'yes'
        availability = request.form['availability'] == 'yes'
        description = request.form['description']

        # Collect pkg_quantity as a string
        pkg_quantity_list = [
            '500ml' if quantity_half else '',
            '1 litre' if quantity1 else '',
            '2 litre' if quantity2 else '',
            '3 litre' if quantity3 else '',
            '5 litre' if quantity5 else ''
        ]
        pkg_quantity = ', '.join(filter(None, pkg_quantity_list))

        if 'image' in request.files and request.files['image'].filename != '':
            new_image = request.files['image']
            image_blob = convert_image_to_blob(new_image)
        else:
            image_blob = product['image']

        with conn:
            cursor.execute('''
                UPDATE products 
                SET name = ?, image = ?, cost = ?, quantity_half = ?, quantity1 = ?, quantity2 = ?, quantity3 = ?, quantity5 = ?, availability = ?, description = ?
                WHERE id = ?
            ''', (name, image_blob, cost, quantity_half, quantity1, quantity2, quantity3, quantity5, availability, description, product_id))
        return redirect(url_for('products'))
    return render_template('edit_product.html', product=product)


@app.route('/order')
def order():
    conn = get_db_connection()
    with conn:
        cursor = conn.cursor()
        cursor.execute('''
            SELECT o.o_id, c.c_name, p.name, o.pkg_quantity, o.price, o.no_quantity, o.date, o.delivery, c.c_gmail, 
                   p.id AS product_id
            FROM orders o
            JOIN products p ON o.id = p.id
            JOIN customer c ON o.c_gmail = c.c_gmail
            ORDER BY o.date DESC
        ''')
        orders = cursor.fetchall()

    encoded_orders = []
    
    for order in orders:
        product_id = order['product_id']
        pkg_quantity_str = order['pkg_quantity']
        
        # Convert pkg_quantity to liters
        try:
            pkg_quantity_liters = convert_pkg_quantity_to_liters(pkg_quantity_str)
        except ValueError as e:
            return jsonify({'success': False, 'message': str(e)}), 400
        
        # Determine the stock quantity field
        if pkg_quantity_liters <= 0.5:
            qty_field = 'qty_500ml'
        elif pkg_quantity_liters <= 1:
            qty_field = 'qty_1l'
        elif pkg_quantity_liters <= 2:
            qty_field = 'qty_2l'
        elif pkg_quantity_liters <= 3:
            qty_field = 'qty_3l'
        elif pkg_quantity_liters <= 5:
            qty_field = 'qty_5l'
        else:
            qty_field = 'qty_5l'  # Default to 5 liter if not fitting any other

        # Fetch the stock quantity for the product
        cursor.execute(f'''
            SELECT {qty_field} as stock_quantity FROM stock WHERE pid = ?
        ''', (product_id,))
        stock = cursor.fetchone()
        
        stock_quantity = stock[0] if stock else 0
        
        encoded_orders.append(
            (
                order['o_id'], order['c_name'], order['name'], order['pkg_quantity'], order['price'], order['no_quantity'],
                order['date'], order['delivery'], order['c_gmail'], float(order['no_quantity']) * float(order['price']), 
                stock_quantity
            )
        )

    return render_template('order.html', orders=encoded_orders)


def convert_pkg_quantity_to_liters(pkg_quantity_str):
    """ Convert pkg_quantity string to liters. """
    pkg_quantity_str = pkg_quantity_str.lower().strip()
    
    # Regex to match the quantity and unit
    match = re.match(r'(\d+)\s*(ml|litre|liter)', pkg_quantity_str)
    if match:
        value, unit = match.groups()
        value = int(value)
        if unit == 'ml':
            return value / 1000.0  # Convert ml to liters
        elif unit in ['litre', 'liter']:
            return value
        else:
            raise ValueError(f"Unrecognized unit: {unit}")
    else:
        raise ValueError(f"Unrecognized pkg_quantity format: {pkg_quantity_str}")

@app.route('/update_delivery/<int:order_id>', methods=['POST'])
def update_delivery(order_id):
    data = request.get_json()
    delivery_status = data.get('delivery')

    if not isinstance(delivery_status, bool):
        return jsonify({'success': False, 'message': 'Invalid delivery status'}), 400

    conn = get_db_connection()
    with conn:
        cursor = conn.cursor()

        # Fetch the order details
        cursor.execute('''
            SELECT pkg_quantity, no_quantity, id FROM orders WHERE o_id = ?
        ''', (order_id,))
        order = cursor.fetchone()

        if not order:
            return jsonify({'success': False, 'message': 'Order not found'}), 404

        pkg_quantity_str = order[0]
        no_quantity = order[1]
        product_id = order[2]

        try:
            # Convert pkg_quantity to liters
            pkg_quantity_liters = convert_pkg_quantity_to_liters(pkg_quantity_str)
        except ValueError as e:
            return jsonify({'success': False, 'message': str(e)}), 400

        if delivery_status:
            # Fetch stock quantities
            cursor.execute('''
                SELECT qty_500ml, qty_1l, qty_2l, qty_3l, qty_5l FROM stock WHERE pid = ?
            ''', (product_id,))
            stock = cursor.fetchone()
            if not stock:
                return jsonify({'success': False, 'message': 'Product not found in stock'}), 404

            # Determine the stock quantity field to use
            if pkg_quantity_liters <= 0.5:
                qty_field = 'qty_500ml'
            elif pkg_quantity_liters <= 1:
                qty_field = 'qty_1l'
            elif pkg_quantity_liters <= 2:
                qty_field = 'qty_2l'
            elif pkg_quantity_liters <= 3:
                qty_field = 'qty_3l'
            elif pkg_quantity_liters <= 5:
                qty_field = 'qty_5l'
            else:
                return jsonify({'success': False, 'message': 'Quantity exceeds available stock sizes'}), 400

            current_quantity = stock[{'qty_500ml': 0, 'qty_1l': 1, 'qty_2l': 2, 'qty_3l': 3, 'qty_5l': 4}[qty_field]]

            if current_quantity >= no_quantity:
                # Reduce the stock quantity
                cursor.execute(f'''
                    UPDATE stock
                    SET {qty_field} = {qty_field} - ?
                    WHERE pid = ?
                ''', (no_quantity, product_id))
                cursor.execute('''
                    UPDATE orders
                    SET delivery = 1
                    WHERE o_id = ?
                ''', (order_id,))
                conn.commit()
                return jsonify({'success': True, 'message': 'Delivery status updated successfully'})
            else:
                return jsonify({'success': False, 'message': 'Not enough stock'}), 400

        else:
            # Set delivery status to 0 if the update fails
            cursor.execute('''
                UPDATE orders
                SET delivery = 0
                WHERE o_id = ?
            ''', (order_id,))
            conn.commit()
            return jsonify({'success': True, 'message': 'Delivery status set to not delivered'})

    return jsonify({'success': False, 'message': 'Unknown error occurred'}), 500


@app.route('/customer_view/<string:c_gmail>', methods=['GET'])
def customer_view(c_gmail):
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute('''
        SELECT c_gmail, c_name, address, city, state, pincode, contact
        FROM customer 
        WHERE c_gmail = ?
    ''', (c_gmail,))
    customer = cursor.fetchone()
    conn.close()  # Ensure the connection is closed after use

    if customer:
        encoded_customer = (
            customer['c_gmail'], customer['c_name'],
            customer['address'], customer['city'], customer['state'], customer['pincode'],
            customer['contact']
        )
        return render_template('customer_view.html', customer=encoded_customer)
    else:
        return redirect(url_for('order'))
  # Redirect to shop page if customer not found

@app.route('/success_product', methods=['GET', 'POST'])
def success_product():
    return render_template('success_product.html')

@app.route('/stock', methods=['GET', 'POST'])
def stock():
    return render_template('stock.html')

@app.route('/success', methods=['GET', 'POST'])
def success():
    return render_template('success.html')

@app.route('/add_stock_form', methods=['GET'])
def add_stock_form():
    return render_template('add_stock.html')


if __name__ == '__main__':
    create_table()
    app.run(debug=True)
