from flask import Flask, request
from flask_mysqldb import MySQL
import datetime, json, time

# SERVER ON  rojo16.pythonanywhere.com

app = Flask(__name__)

'''
app.config['MYSQL_HOST'] = 'rojo16.mysql.pythonanywhere-services.com'
app.config['MYSQL_USER'] = 'rojo16'
app.config['MYSQL_PASSWORD'] = 'hello123'
app.config['MYSQL_DB'] = 'rojo16$testdb'

'''
app.config['MYSQL_HOST'] = 'localhost'
app.config['MYSQL_USER'] = 'testuser'
app.config['MYSQL_PASSWORD'] = 'test123'
app.config['MYSQL_DB'] = 'testdb'


mysql = MySQL(app)


@app.route('/')
def index():
    return "WELCOME TO THE APP. USE THE API."

#############################SHOPLIST PART############################


@app.route('/<string:user>/shoplist/items')
def get_items(user):
    cur = mysql.connection.cursor()
    cur.execute('''SELECT name, priority from %s_shoplist_table order by priority desc''' % user)
    retVal = cur.fetchall()
    sting = [i for i in retVal]
    spring = []
    for i in sting:
        thing = dict(zip(["name", "priority"], i))
        spring.append(thing)
    del cur
    return json.dumps(spring)


@app.route('/<string:user>/shoplist/items/<string:item>', methods=['POST', 'DELETE'])
def manage_items(user, item):
    if request.method == 'POST':
        cur = mysql.connection.cursor()
        cur.execute('''SELECT MAX(id) from %s_shoplist_table''' % user)
        maxid = cur.fetchone()
        ID = maxid[0]
        if not maxid[0]:
            ID = -1
        items = item.split(',')
        for i, j in enumerate(items):
            cur.execute('''INSERT INTO %s_shoplist_table(id, name) value (%s ,"%s")''' %(user, ID+1+i, j))
            mysql.connection.commit()
        del cur
        return "Added %s!" % item

    if request.method == 'DELETE':
        cur = mysql.connection.cursor()
        items = item.split(',')
        for i, item in enumerate(items):
            cur.execute('''DELETE from %s_shoplist_table where name = "%s"''' %(user, item))
            mysql.connection.commit()
        del cur
        return "Deleted item(s)!"


@app.route('/<string:user>/shoplist/prioritize/<string:item>', methods=['PUT'])
def prioritize_items(user, item):
    if request.method == 'PUT':
        cur = mysql.connection.cursor()
        items = item.split(',')
        for i, item in enumerate(items):
            cur.execute('''UPDATE %s_shoplist_table set priority = "YES" where name = "%s"''' %(user, item))
            mysql.connection.commit()
        del cur
        return "Set Priorities!"


@app.route('/<string:user>/shoplist/unprioritize/<string:item>', methods=['PUT'])
def unprioritize_items(user, item):
    if request.method == 'PUT':
        cur = mysql.connection.cursor()
        items = item.split(',')
        for i, item in enumerate(items):
            cur.execute('''UPDATE %s_shoplist_table set priority = "NO" where name = "%s"''' % (user, item))
            mysql.connection.commit()
        del cur
        return "Unset Priorities!"

"""
@app.route('/<string:user>/shoplist/price/<string:item>', methods=['PUT'])
def price_items(user, item):
    if request.method == 'PUT':
        cur = mysql.connection.cursor()
        items = item.split(',')
        for i, item in enumerate(items):
            tag = item.split(':')
            cur.execute('''UPDATE %s_shoplist_table set cost = "%s" where name = "%s"''' % (user, tag[0], tag[1]))
            mysql.connection.commit()
        del cur
        return "Set Priorities!"
"""

#############################EXPENSE PART#####################################


@app.route('/<string:user>/expense/batch/<int:batch>')
def get_batch_expenses(user, batch):
    cur = mysql.connection.cursor()
    now = datetime.datetime.now()
    month = int(now.strftime("%y%m"))
    batch_start = batch*3
    start = get_sub_date(month, batch_start+3)
    end  = get_sub_date(month, batch_start)
    cur.execute('''SELECT id, name, cost, month, category from %s_expense_table where month > %s and month <= %s order by month DESC''' % (user, start, end))
    retVal = cur.fetchall()
    cur.execute('''SELECT mon_lim from %s_limit_table where name = "DEFAULT"''' %user)
    lim_val = cur.fetchone()
    cur.execute('''SELECT sum(cost) from %s_expense_table where month = %s''' % (user, month))
    mon_val = cur.fetchone()
    try:
        lim = str(lim_val[0] - mon_val[0])
    except:
        lim = str(lim_val[0])
    sting = [i for i in retVal]
    spring = []
    for i in sting:
        thing = dict(zip(["id", "name", "cost", "date", "category"], i))
        thing['limit'] = lim
        thing['total'] = str(mon_val[0])
        spring.append(thing)
    if len(sting) != 0:
        del cur
        return json.dumps(spring)
    else:
        del cur
        return json.dumps([{'limit': lim}])


@app.route('/<string:user>/expense/totals')
def get_totals_expenses(user):
    cur = mysql.connection.cursor()
    cur.execute('''select month, sum(cost) from %s_expense_table group by month''' % user)
    retVal = cur.fetchall()
    sting = [i for i in retVal]
    spring = []
    for j,k in sting:
            spring.append({"date":j, "cost":int(k)})
    del cur
    return json.dumps(spring)


@app.route('/<string:user>/expense/items')
def get_all_expenses1(user):
    cur = mysql.connection.cursor()
    cur.execute('''SELECT name, cost, month, category from %s_expense_table''' % user)
    retVal = cur.fetchall()
    now = datetime.datetime.now()
    month = now.strftime("%y%m")
    cur.execute('''SELECT mon_lim from %s_limit_table where name = "DEFAULT"''' % user)
    lim_val = cur.fetchone()
    cur.execute('''SELECT sum(cost) from %s_expense_table where month = %s''' % (user, month))
    mon_val = cur.fetchone()
    try:
        lim = str(lim_val[0] - mon_val[0])
    except:
        lim = str(lim_val[0])
    sting = [i for i in retVal]
    spring = []
    for i in sting:
        thing =dict(zip(["id","name", "cost", "date", "category"], i))
        thing['limit'] = lim
        spring.append(thing)
    if len(sting) != 0:
        del cur
        return json.dumps(spring)
    else:
        del cur
        return json.dumps([{'limit':lim}])
      
      
@app.route('/<string:user>/expense/total/<int:month>')
def get_month_total(user, month):
    cur = mysql.connection.cursor()
    cur.execute('''SELECT sum(cost) from %s_expense_table where month = %d''' % (user, month))
    retVal = cur.fetchone()
    del cur
    return str(retVal[0])


@app.route('/<string:user>/expense/items/<int:month>/<string:category>')
def get_category_month_expenses(user, month, category):
    cur = mysql.connection.cursor()
    if month == 0:
        cur.execute('''SELECT name, cost, month from %s_expense_table where category = "%s"''' % (user, category))
    elif category == '0':
        cur.execute('''SELECT name, cost, category from %s_expense_table where month = %d''' % (user, month))
    else:
        cur.execute('''SELECT name, cost from %s_expense_table where month = %s and category = "%s"''' % (user, month, category))
    retVal = cur.fetchall()
    del cur
    return str(retVal)

"""
@app.route('/<string:user>/expense/limit_bal')
def get_month_limit(user):
    cur = mysql.connection.cursor()
    now = datetime.datetime.now()

    month = int(now.strftime("%y%m"))
    cur.execute('''SELECT mon_lim from %s_limit_table where name = "DEFAULT"''' % user)
    lim_val = cur.fetchone()
    cur.execute('''SELECT sum(cost) from %s_expense_table where month = %s''' % (user, month))
    mon_val = cur.fetchone()
    return str(lim_val[0]-mon_val[0])
"""


@app.route('/<string:user>/expense/items/<string:item>/<int:cost>/<string:category>', methods=['POST'])
def add_expense(user, item, cost, category):
        cur = mysql.connection.cursor()
        cur.execute('''SELECT MAX(id) from %s_expense_table'''%user)
        maxid = cur.fetchone()
        ID = maxid[0]
        if not maxid[0]:
            ID = -1
        now = datetime.datetime.now()
        month = int(now.strftime("%y%m"))
        cur.execute('''INSERT INTO %s_expense_table(id, name, cost, month, category) value (%s ,"%s", %s, %s, "%s")''' %(user, ID+1, item, cost, month, category))
        mysql.connection.commit()
        del cur
        return "Added %s!" % item


@app.route('/<string:user>/expense/limit/<int:mon_lim>', methods=['PUT'])
def update_limit(user, mon_lim):
    cur = mysql.connection.cursor()
    cur.execute('''UPDATE %s_limit_table set mon_lim = %d where name = "DEFAULT"''' % (user, mon_lim))
    mysql.connection.commit()
    del cur
    return "Updated monthly limit to %s!" % mon_lim


@app.route('/<string:user>/expense/items/<string:item>/<int:month>/<int:cost>', methods=['DELETE'])
def delete_item(user, item, month, cost):
    if request.method == 'DELETE':
        cur = mysql.connection.cursor()
        if cost == 0:
            cur.execute('''DELETE from %s_expense_table where name = "%s" and month = "%s"''' %(user, item, month))
        else:
            cur.execute('''DELETE from %s_expense_table where name = "%s" and month = "%s" and cost = "%s"''' %(user, item, month, cost))
        mysql.connection.commit()
        del cur
        return "Deleted %s!" % item
    return "Done nothing!"


@app.route('/<string:user>/expense/items/<string:idee>:<string:item>:<string:month>:<string:cost>:<string:category>', methods=['PUT'])
def edit_item(user, idee, item, month, cost, category):
        if request.method == 'PUT':
            month = date_from_monthstring(month)
            cur = mysql.connection.cursor()
            cur.execute('''UPDATE %s_expense_table set name = "%s", cost = "%s", month = "%s", category = "%s" where id = "%s"''' % (user, item, cost, month, category, idee))
            mysql.connection.commit()
            del cur
        return "Updated %s!" % item


#######################USER INFO#################################################


@app.route('/users/<string:name>/<string:passwd>', methods=['POST', 'DELETE', 'GET'])
def manage_user(name, passwd):
        if request.method == 'GET':
                cur = mysql.connection.cursor()
                cur.execute('''SELECT password from user_table where name = "%s"''' % name)
                retVal = cur.fetchone()
                if retVal:
                    if retVal[0]==passwd:
                        return "LOGGED"
                    else:
                        return "WRONG"
                    #return str(json.dumps({"output":output}))
                else:
                    return "FAILED"
                    #return str(json.dumps({"fail":"User Doesn't Exist"}))

        elif request.method == 'POST':
            cur = mysql.connection.cursor()
            cur.execute('''SELECT name from user_table where name = "%s"''' % name)
            if cur.fetchone():
                return "EXISTS"

            cur.execute('''SELECT MAX(id) from user_table''')
            maxid = cur.fetchone()
            ID = maxid[0]
            if not maxid[0]:
                ID = -1
            cur.execute('''INSERT INTO user_table(id, name, password) value (%s ,"%s", "%s")''' % (ID + 1, name, passwd))
            mysql.connection.commit()
            del cur
            return "CREATED"

        elif request.method == 'DELETE':
            cur = mysql.connection.cursor()
            cur.execute('''DELETE from user_table where name = "%s" and password = "%s"''' % (name, passwd))
            mysql.connection.commit()
            del cur
            return "DELETED %s!" % name

        return "Did nothing!"


"""@app.route('/users/<string:name>/<string:passwd>', methods=['POST', 'DELETE', 'GET'])
def manage_user(name, passwd):
        cur = mysql.connection.cursor()

        if request.method == 'GET':
                cur.execute('''SELECT password from user_table where name = "%s"''' % name)
                retVal = cur.fetchone()
                output = retVal[0]==passwd
                return str(json.dumps({"output":output}))

        elif request.method == 'POST':

            cur.execute('''SELECT MAX(id) from user_table''')
            maxid = cur.fetchone()
            ID = maxid[0]
            if not maxid[0]:
                ID = -1
            cur.execute('''INSERT INTO user_table(id, name, password) value (%s ,"%s", "%s")''' % (ID + 1, name, passwd))
            mysql.connection.commit()
            return "CREATED %s!" % name

        elif request.method == 'DELETE':
            cur.execute('''DELETE from user_table where name = "%s" and password = "%s"''' % (name, passwd))
            mysql.connection.commit()
            return "DELETED %s!" % name

        return "Did nothing!"
"""


@app.route('/RESET/<string:name>', methods=['PUT'])
def reset_tables(name):
        if request.method == 'PUT':
            cur = mysql.connection.cursor()
            cur.execute('''DROP TABLE IF EXISTS `%s`''' % (name+"_expense_table"))
            mysql.connection.commit()
            cur.execute('''DROP TABLE IF EXISTS `%s`''' % (name+"_shoplist_table"))
            mysql.connection.commit()
            cur.execute('''DROP TABLE IF EXISTS `%s`''' % (name+"_limit_table"))
            mysql.connection.commit()

            cur.execute(''' create table %s(id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, name VARCHAR(20), cost INT, month INT, category VARCHAR(20), modified INT)''' % (name + "_expense_table"))
            mysql.connection.commit()

            cur.execute('''create table %s(id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, name VARCHAR(20), priority VARCHAR(10))''' % (name + "_shoplist_table"))
            mysql.connection.commit()

            cur.execute('''create table %s(name VARCHAR(20), mon_lim INT)''' % (name + "_limit_table"))
            mysql.connection.commit()

            cur.execute('''insert into %s(name, mon_lim) value ("DEFAULT", 1000)''' % (name + "_limit_table"))
            mysql.connection.commit()
            del cur
            return "REFRESHED"

        return "FAILED"


@app.route('/DELETE/<string:name>', methods=['DELETE'])
def delete_user(name):
        if request.method == 'DELETE':
            cur = mysql.connection.cursor()
            cur.execute('''DROP TABLE IF EXISTS `%s`''' % (name+"_expense_table"))
            mysql.connection.commit()
            cur.execute('''DROP TABLE IF EXISTS `%s`''' % (name+"_shoplist_table"))
            mysql.connection.commit()
            cur.execute('''DROP TABLE IF EXISTS `%s`''' % (name+"_limit_table"))
            mysql.connection.commit()
            cur.execute('''DELETE from user_table where name = "%s"''' % (name))
            mysql.connection.commit()
            del cur
            return "DELETED %s!" % name
        return "Did nothing!"

########################OTHER FUNCTIONS ###########################


def get_sub_date(date, num):
    YY = date / 100 - num / 12
    MM = date % 100 - num % 12
    while MM <= 0:
        MM += 12
        YY -= 1
    return YY * 100 + MM


def date_from_monthstring(a):
    m = ["JAN", "FEB", "MAR", "APR", "MAY", "JUN",
         "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"]
    month = "ERROR"
    for i,j in enumerate(m):
        if j == a:
            if i >= 10:
                month = str(i+1)
            else:
                month = "0%d" % (i+1)
    return "%s%s" % (datetime.datetime.fromtimestamp(time.time()).strftime("%y"), month)


'''
def date_from_timestamp(a):
    return datetime.fromtimestamp(a).strftime("%y%m")
'''


########################MAIN #################################################3

if __name__ == "__main__":
    app.run('0.0.0.0', 35741, debug=True)
