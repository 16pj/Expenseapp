# Expenseapp
An Android App with a Python REST server backend that tracks expenses and also makes shopping lists


BACKEND:
	
	Requires Python flask, flask_mysqldb modules and MySQL database

	Create 3 tables in a database:
	1) expense_table
	2) shoplist_table
	3) limit_table
 
	By using :

	1) create table expense_table(id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, name VARCHAR(20), cost INT, month INT, category VARCHAR(20));

	2) create table shoplist_table(id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, name VARCHAR(20), priority VARCHAR(10));

	3) create table limit_table(name VARCHAR(20), mon_lim INT);

