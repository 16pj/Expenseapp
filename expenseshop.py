from flask import Flask, request
from flask_mysqldb import MySQL
import datetime


app = Flask(__name__)
app.config['MYSQL_HOST'] = 'localhost'
app.config['MYSQL_USER'] = 'testuser'
app.config['MYSQL_PASSWORD'] = 'test123'
app.config['MYSQL_DB'] = 'testdb'

mysql = MySQL(app)



@app.route('/')
def index():
    return "WELCOME TO THE APP. USE THE API."


##############################SHOPLIST PART############################


@app.route('/shoplist/items/<string:item>', methods=['POST', 'DELETE'])
def manage_items(item):
    cur = mysql.connection.cursor()

    if request.method == 'POST':
        cur.execute('''SELECT MAX(id) from shoplist_table''')
        maxid = cur.fetchone()
        ID = maxid[0]
        if not maxid[0]:
            ID = -1
        cur.execute('''INSERT INTO shoplist_table(id, name) value (%s ,"%s")''' %(ID+1, item))
        mysql.connection.commit()
        return "Added %s!" %item


    if request.method == 'DELETE':
        cur.execute('''DELETE from shoplist_table where name = "%s"''' %item)
        mysql.connection.commit()
        return "Deleted %s!" %item

@app.route('/shoplist/items')
def get_items():
    cur = mysql.connection.cursor()
    cur.execute('''SELECT name from shoplist_table''')
    retVal = cur.fetchall()
    return str(retVal)


#############################EXPENSE PART#####################################

@app.route('/expense/items/<string:item>/<int:cost>/<string:category>', methods=['POST'])
def add_expense(item, cost, category="GROCERIES"):
        cur = mysql.connection.cursor()
        cur.execute('''SELECT MAX(id) from expense_table''')
        maxid = cur.fetchone()
        ID = maxid[0]
        if not maxid[0]:
            ID = -1
        now = datetime.datetime.now()
        month = "%d%d" %(now.year, now.month)
        month = month[2:]
        cur.execute('''INSERT INTO expense_table(id, name, cost, month, category) value (%s ,"%s", %s, %s, "%s")''' %(ID+1, item, cost, month, category))
        mysql.connection.commit()
        return "Added %s!" %item


@app.route('/expense/items')
def get_all_expenses():
    cur = mysql.connection.cursor()
    cur.execute('''SELECT name, cost, month, category from expense_table''')
    retVal = cur.fetchall()
    return str(retVal)

@app.route('/expense/total/<int:month>')
def get_month_total(month):
    cur = mysql.connection.cursor()
    cur.execute('''SELECT sum(cost) from expense_table where month = %d''' % month)
    retVal = cur.fetchone()
    return str(retVal[0])

@app.route('/expense/items/<int:month>/<string:category>')
def get_category_month_expenses(month, category):
    cur = mysql.connection.cursor()
    if month == 0:
        cur.execute('''SELECT name, cost, month from expense_table where category = "%s"''' % category)
    elif category == '0':
        cur.execute('''SELECT name, cost, category from expense_table where month = %d''' % month)
    else:
        cur.execute('''SELECT name, cost from expense_table where month = %s and category = "%s"''' % (month, category))
    retVal = cur.fetchall()
    return str(retVal)


@app.route('/expense/items/<string:item>/<int:month>', methods=['DELETE'])
def delete_item(item, month):
    cur = mysql.connection.cursor()
    cur.execute('''DELETE from expense_table where name = "%s" and month = "%s"''' %(item, month))
    mysql.connection.commit()
    return "Deleted %s!" %item


@app.route('/expense/limit_bal')
def get_month_limit():
    cur = mysql.connection.cursor()
    now = datetime.datetime.now()
    month = "%d%d" % (now.year, now.month)
    month = month[2:]
    cur.execute('''SELECT mon_lim from limit_table where name = "DEFAULT"''')
    lim_val = cur.fetchone()
    cur.execute('''SELECT sum(cost) from expense_table where month = %s''' % month)
    mon_val = cur.fetchone()
    return str(lim_val[0]-mon_val[0])

@app.route('/expense/limit/<int:mon_lim>', methods=['PUT'])
def update_limit(mon_lim):
    cur = mysql.connection.cursor()
    cur.execute('''UPDATE limit_table set mon_lim = %d where name = "DEFAULT"''' % (mon_lim))
    mysql.connection.commit()
    return "Updated monthly limit to %s!" %mon_lim


if __name__ == "__main__":
    app.run('127.0.0.1', 3000, debug=True)