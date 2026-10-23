-- MySQL 8+, UTF-8, transactional InnoDB tables. Execute once on an empty instance.
CREATE DATABASE IF NOT EXISTS restaurant_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE restaurant_db;

CREATE TABLE IF NOT EXISTS ingredient (
 id INT AUTO_INCREMENT PRIMARY KEY,
 name VARCHAR(120) NOT NULL UNIQUE,
 unit VARCHAR(30) NOT NULL,
 quantity_in_stock DOUBLE NOT NULL DEFAULT 0 CHECK (quantity_in_stock >= 0),
 unit_price DECIMAL(10,2) NOT NULL CHECK (unit_price >= 0)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS dish (
 id INT AUTO_INCREMENT PRIMARY KEY,
 name VARCHAR(120) NOT NULL UNIQUE,
 category VARCHAR(60) NOT NULL,
 price DECIMAL(10,2) NOT NULL CHECK (price >= 0)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS dish_ingredient (
 dish_id INT NOT NULL,
 ingredient_id INT NOT NULL,
 quantity_required DOUBLE NOT NULL CHECK (quantity_required > 0),
 PRIMARY KEY(dish_id,ingredient_id),
 FOREIGN KEY (dish_id) REFERENCES dish(id) ON DELETE CASCADE,
 FOREIGN KEY (ingredient_id) REFERENCES ingredient(id) ON DELETE RESTRICT
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS menu (
 id INT AUTO_INCREMENT PRIMARY KEY,
 name VARCHAR(120) NOT NULL UNIQUE
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS menu_dish (
 menu_id INT NOT NULL,
 dish_id INT NOT NULL,
 PRIMARY KEY (menu_id,dish_id),
 FOREIGN KEY (menu_id) REFERENCES menu(id) ON DELETE CASCADE,
 FOREIGN KEY (dish_id) REFERENCES dish(id) ON DELETE RESTRICT
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS employee (
 id INT AUTO_INCREMENT PRIMARY KEY,
 name VARCHAR(120) NOT NULL,
 last_name VARCHAR(120) NOT NULL,
 phone VARCHAR(40) NOT NULL,
 email VARCHAR(200) NOT NULL UNIQUE,
 role VARCHAR(70) NOT NULL,
 salary DECIMAL(12,2) NOT NULL CHECK (salary >= 0),
 hire_date DATE NOT NULL
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS customer (
 id INT AUTO_INCREMENT PRIMARY KEY,
 name VARCHAR(120) NOT NULL,
 last_name VARCHAR(120) NOT NULL,
 phone VARCHAR(40) NOT NULL,
 email VARCHAR(200) NOT NULL UNIQUE,
 registration_date DATE NOT NULL
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS subscription (
 id INT AUTO_INCREMENT PRIMARY KEY,
 customer_id INT NOT NULL,
 start_date DATE NOT NULL,
 end_date DATE NOT NULL,
 CHECK (end_date >= start_date),
 INDEX (customer_id,start_date,end_date),
 FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE
) ENGINE=InnoDB;
