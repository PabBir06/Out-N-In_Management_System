USE restaurant_db;
-- Optional: run once after schema.sql on a new empty database.
INSERT INTO ingredient(name,unit,quantity_in_stock,unit_price) VALUES
 ('Bun','piece',30,0.80),('Patty','piece',20,2.50),('Cheese','slice',8,0.50),('Lettuce','g',500,0.01);
INSERT INTO dish(name,category,price) VALUES ('Classic Burger','Burger',8.90),('Cheeseburger','Burger',10.50);
INSERT INTO dish_ingredient(dish_id,ingredient_id,quantity_required)
 SELECT d.id,i.id,CASE WHEN i.name='Lettuce' THEN 40 ELSE 1 END FROM dish d JOIN ingredient i
 WHERE (d.name='Classic Burger' AND i.name IN ('Bun','Patty','Lettuce'))
    OR (d.name='Cheeseburger' AND i.name IN ('Bun','Patty','Cheese'));
INSERT INTO menu(name) VALUES ('Burger Combo');
INSERT INTO menu_dish(menu_id,dish_id) SELECT m.id,d.id FROM menu m CROSS JOIN dish d WHERE m.name='Burger Combo';
INSERT INTO employee(name,last_name,phone,email,role,salary,hire_date)
 VALUES ('Alex','Martin','555-1001','alex@example.com','Cook',2200,'2025-03-01');
INSERT INTO customer(name,last_name,phone,email,registration_date)
 VALUES ('Taylor','Smith','555-1002','taylor@example.com',CURDATE());
INSERT INTO subscription(customer_id,start_date,end_date)
 SELECT id,CURDATE(),DATE_ADD(CURDATE(),INTERVAL 1 MONTH) FROM customer WHERE email='taylor@example.com';
