CREATE DATABASE Pho_restaurant;
USE Pho_restaurant;

DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS recipes;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS menu_items;
DROP TABLE IF EXISTS ingredients;
DROP TABLE IF EXISTS employees;

CREATE TABLE Employees (
	employee_id int AUTO_INCREMENT,
    full_name VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    salary DECIMAL(10, 0),
    PRIMARY KEY(employee_id)
);

CREATE TABLE Ingredients (
	ingredient_id int AUTO_INCREMENT,
    name VARCHAR(255) unique NOT NULL,
    stock_quantity double NOT NULL,
    unit VARCHAR(10),
    primary key (ingredient_id)
);

CREATE TABLE Menu_items (
	item_id int auto_increment,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL,
    category VARCHAR(20),
    PRIMARY KEY (item_id)
);

CREATE TABLE Recipes (
	item_id int,
    ingredient_id int,
    quantity_needed double NOT NULL,
    FOREIGN KEY (item_id) REFERENCES Menu_items(item_id),
    FOREIGN KEY (ingredient_id) REFERENCES Ingredients(ingredient_id),
    PRIMARY KEY (item_id, ingredient_id)
);

CREATE TABLE Orders (
	order_id int AUTO_INCREMENT,
    order_type VARCHAR(20),
    order_status VARCHAR(30),
    total_price DECIMAL,
    order_timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    employee_id int,
    FOREIGN KEY (employee_id) REFERENCES Employees(employee_id),
    PRIMARY KEY (order_id)
);

CREATE TABLE Order_items (
	quantity int NOT NULL,
	order_id int REFERENCES Orders(order_id),
    item_id int REFERENCES Menu_items(item_id),
    subtotal DECIMAL,
    PRIMARY KEY (order_id, item_id)
);

CREATE TABLE Transactions (
	transaction_id int AUTO_INCREMENT PRIMARY KEY,
    order_id int REFERENCES Orders(order_id),
    payment_method VARCHAR(20),
    amount_paid DECIMAL,
    transaction_timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO employees (full_name, role, salary) VALUES 
('Nguyen Van An', 'Manager', 15000000),
('Tran Thi Binh', 'Staff', 5000000),
('Le Van Cuong', 'Staff', 4500000),
('Nguyen Trong Huy', 'Manager', 30000000),
('Nguyen Xuan Anh Khoa', 'Staff', 100000);


INSERT INTO ingredients (name, stock_quantity, unit) VALUES 
('Rice Noodles (Banh Pho)', 50.0, 'kg'),
('Beef Tenderloin (Thit Bo)', 20.0, 'kg'),
('Chicken Breast (Thit Ga)', 15.0, 'kg'),
('Beef Broth', 100.0, 'liters'),
('Chicken Broth', 80.0, 'liters'),
('Green Onions', 5.0, 'kg'),
('Bean Sprouts', 10.0, 'kg'),
('Coffee Beans', 2.0, 'kg'),
('Condensed Milk', 20.0, 'cans');

INSERT INTO menu_items (name, description, price, category) VALUES 
('Pho Tai', 'Beef noodle soup with rare steak', 55000, 'Pho'),
('Pho Chin', 'Beef noodle soup with well-done brisket', 55000, 'Pho'),
('Pho Ga', 'Chicken noodle soup', 50000, 'Pho'),
('Goi Cuon', 'Fresh spring rolls with shrimp (2 pcs)', 25000, 'Side Dish'),
('Cha Gio', 'Fried spring rolls (3 pcs)', 30000, 'Side Dish'),
('Banh Quay', 'Fried bread stick (1 pc)', 3000, 'Side Dish'),
('Cafe Sua Da', 'Vietnamese iced coffee with condensed milk', 25000, 'Drink'),
('Tra Da', 'Iced tea', 5000, 'Drink');

INSERT INTO recipes (item_id, ingredient_id, quantity_needed) VALUES 
(1, 1, 0.15), (1, 2, 0.1), (1, 4, 0.4), (1, 6, 0.01), -- Pho Tai
(3, 1, 0.15), (3, 3, 0.1), (3, 5, 0.4), -- Pho Ga
(6, 8, 0.02), (6, 9, 0.1); -- Cafe Sua Da

INSERT INTO orders (order_type, order_status, total_price, order_timestamp, employee_id) VALUES 
('DineIn', 'Completed', 135000, '2023-10-01 12:30:00', 2),
('Grab', 'Completed', 60000, '2023-10-01 12:45:00', 3),
('Pickup', 'Completed', 55000, '2023-10-02 09:00:00', 2),
('DineIn', 'Pending', 80000, NOW(), 3);

INSERT INTO order_items (order_id, item_id, quantity, subtotal) VALUES 
(1, 1, 2, 110000), (1, 6, 1, 25000), -- Order 1: 2 Pho Tai, 1 Cafe
(2, 3, 1, 50000), (2, 7, 2, 10000), -- Order 2: 1 Pho Ga, 2 Tra Da
(3, 2, 1, 55000),                   -- Order 3: 1 Pho Chin
(4, 1, 1, 55000), (4, 6, 1, 25000); 

INSERT INTO transactions (order_id, payment_method, amount_paid, transaction_timestamp) VALUES 
(1, 'Cash', 135000, '2023-10-01 13:15:00'),
(2, 'GrabPay', 60000, '2023-10-01 13:00:00'),
(3, 'Momo', 55000, '2023-10-02 09:10:00');