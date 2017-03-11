# Expenseapp
An Android App with a Python REST server backend that tracks expenses and also makes shopping lists




I. FRONTEND: Android App

Home page:

The main page has two main buttons on the first page: 1) Shopping list and 2) Expense tracker
On the first page, the drop down options available are 
1) Settings - Which has options to login, signup and reset
2) SNAP mode - An option to view the list offline (no edit/add option)
3) Signout - To sign out of the account

Shoplist:

The shoplist page has options to add, delete items
There is also option to star/ unstar items using the same button. The starred items go up on the list.
Multiple items can be quickly added by typing them with commas. eg: eggs,apple,orange

Expense list:

The expense list page has option to quickly add an expense by typing item and cost and clicking the ADD button.
But pressing the ADD buttong without typing the item name,cost displays a detailed window to add item. (Pressing and holding ADD leads to the same).
Clicking on the limit text displays a window to change limit value.
The added items can be edited by pressing and holding the individual items.
There is a drop down for displaying month totals and category wise items


II. BACKEND: Python REST Server
	
	Requires Python flask, flask_mysqldb modules and MySQL database

	FIRST CREATE A TABLE TO STORE USER DATA

	*create table user_table(id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, name VARCHAR(20), password VARCHAR(50));


	

	Create 3 tables in a database:
	1) expense_table
	2) shoplist_table
	3) limit_table
 
	By using :

	1) create table expense_table(id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, name VARCHAR(20), cost INT, month INT, category VARCHAR(20), modified VARCHAR(20));

	2) create table shoplist_table(id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, name VARCHAR(20), priority VARCHAR(10));

	3) create table limit_table(name VARCHAR(20), mon_lim INT);

	 
III. REST DOUMENTATION:

	SHOPLIST:

	GET /shoplist/items : Get all items from list.	

	POST /shoplist/items/{item1, item2, item3} : Insert items into list. One or multiple separated by comma.

	PUT/shoplist/prioritize/{item1, item2, item3} : Prioritize items from list. One or multiple separated by comma.
	
	PUT/shoplist/unprioritize/{item1, item2, item3} : Unprioritize items from list. One or multiple separated by comma.

	PUT/shoplist/price/{item1:price1, item2:price2, item3:price3} : Add price to items in list. One or multiple separated by comma.
	
	DELETE /expense/items/{item1, item2, item3} : Delete items from list. One or multiple separated by comma.	



	EXPENSE TRACKER:

	GET /expense/items : Get list of all expenses.

	GET /expense/limit_bal : Get the available balance of the monthly limit.

	GET /expense/total/{yym} : Get total of expenses in month. Format example for April 1994 => 944 

	GET /expense/items/{yym}/{category} : Get expenses of month and of category. 
					      Set month=0 to get expenses of a category, set category=0 to get expenses of a month.


	POST /expense/items/{item}/{cost}/{category} : Insert expense into table.

	PUT /expense/limit/{value} : Set the monthly limit value.

	PUT /expense/items/{item}/{yym}/{cost} : Update cost of expense. Provide name, month and cost.

	DELETE /expense/items/{item}/{yym}/{cost} : Delete expense from table. Provide name, month and cost. Set cost to 0 if not required.




	