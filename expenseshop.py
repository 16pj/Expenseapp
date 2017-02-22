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
        items = item.split(',')
        for i, j in enumerate(items):
            cur.execute('''INSERT INTO shoplist_table(id, name) value (%s ,"%s")''' %(ID+1+i, j))
            mysql.connection.commit()
        return "Added %s!" %item


    if request.method == 'DELETE':
        items = item.split(',')
        for i, item in enumerate(items):
            cur.execute('''DELETE from shoplist_table where name = "%s"''' %item)
            mysql.connection.commit()
        return "Deleted item(s)!"


@app.route('/shoplist/prioritize/<string:item>', methods=['PUT'])
def prioritize_items(item):
    cur = mysql.connection.cursor()

    if request.method == 'PUT':

        items = item.split(',')
        for i, item in enumerate(items):
            cur.execute('''UPDATE shoplist_table set priority = "YES" where name = "%s"''' %item)
            mysql.connection.commit()
        return "Set Priorities!"


@app.route('/shoplist/unprioritize/<string:item>', methods=['PUT'])
def unprioritize_items(item):
    cur = mysql.connection.cursor()

    if request.method == 'PUT':

        items = item.split(',')
        for i, item in enumerate(items):
            cur.execute('''UPDATE shoplist_table set priority = "NO" where name = "%s"''' %item)
            mysql.connection.commit()
        return "Unset Priorities!"


@app.route('/shoplist/price/<string:item>', methods=['PUT'])
def price_items(item):
    cur = mysql.connection.cursor()

    if request.method == 'PUT':

        items = item.split(',')
        for i, item in enumerate(items):
            tag = item.split(':')
            cur.execute('''UPDATE shoplist_table set cost = "%s" where name = "%s"''' % (tag[0], tag[1]))
            mysql.connection.commit()
        return "Set Priorities!"


@app.route('/shoplist/items')
def get_items():
    cur = mysql.connection.cursor()
    cur.execute('''SELECT name from shoplist_table order by priority desc''')
    retVal = cur.fetchall()
    Val = [i[0] for i in retVal]
    sting = ""
    for i in Val:
        sting += i + "\n"

    return str(sting)


#############################EXPENSE PART#####################################


@app.route('/expense/items/<string:item>/<int:cost>/<string:category>', methods=['POST'])
def add_expense(item, cost, category):
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


@app.route('/expense/items/<string:item>/<int:month>/<int:cost>', methods=['DELETE', 'PUT'])
def delete_item(item, month, cost):
    cur = mysql.connection.cursor()

    if request.method == 'DELETE':
        if cost == 0:
            cur.execute('''DELETE from expense_table where name = "%s" and month = "%s"''' %(item, month))
        else:
            cur.execute('''DELETE from expense_table where name = "%s" and month = "%s" and cost = "%s"''' %(item, month, cost))
        mysql.connection.commit()
        return "Deleted %s!" %item
    elif request.method == 'PUT':
        cur.execute('''UPDATE expense_table set cost = "%s" where name = "%s" and month = "%s"''' % (cost, item, month))
        mysql.connection.commit()
        return "Updated %s!" %item

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
    app.run('0.0.0.0', 3000, debug=True)