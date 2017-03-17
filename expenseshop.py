from flask import Flask, request
from flask_mysqldb import MySQL
import datetime
import json
import time

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
days_before_cleanup = 0


@app.route('/')
def index():
    return json.dumps([{'result': "WELCOME TO THE APP. USE THE API."}])

# ############################SHOPLIST PART############################


@app.route('/<string:user>:<string:passwd>/shoplist/items')
def get_items(user, passwd):
    if verify_user(user, passwd):
        cur = mysql.connection.cursor()
        cur.execute('''SELECT id, name, priority, modified, deleted from %s_shoplist_table order by priority desc ''' % user)
        ret_val = cur.fetchall()
        sting = [i for i in ret_val]
        spring = []
        for i in sting:
            thing = dict(zip(["id", "name", "priority", "modified", "deleted"], i))
            spring.append(thing)
        del cur
        return json.dumps(spring)
    else:
        return json.dumps([{'name': "invalid credentials"}])


@app.route('/<string:user>:<string:passwd>/shoplist/items/<string:item>', methods=['POST', 'DELETE'])
def manage_items(user, passwd, item):
    if verify_user(user, passwd):
        if request.method == 'POST':
            cur = mysql.connection.cursor()
            cur.execute('''SELECT MAX(id) from %s_shoplist_table''' % user)
            maxid = cur.fetchone()
            idee = maxid[0]
            if not maxid[0]:
                idee = -1
            items = item.split(',')
            for i, j in enumerate(items):
                cur.execute('''INSERT INTO %s_shoplist_table(id, name, modified, deleted) value (%s ,"%s", %s, %s)''' % (user, idee+1+i, j, time.time(), 0))
                mysql.connection.commit()
            del cur
            return json.dumps([{'result': "Added %s!" % item}])

        if request.method == 'DELETE':
            cur = mysql.connection.cursor()
            items = item.split(',')
            for i, item in enumerate(items):
                cur.execute('''UPDATE %s_shoplist_table set deleted = 1, modified = %s where name = "%s"''' % (user, time.time(),item))
                mysql.connection.commit()
            del cur
            return json.dumps([{'result': "Deleted item(s)!"}])
        else:
            return json.dumps([{'name': "invalid credentials"}])


@app.route('/<string:user>:<string:passwd>/shoplist/prioritize/<string:item>', methods=['PUT'])
def prioritize_items(user, passwd, item):
    if verify_user(user, passwd):
        if request.method == 'PUT':
            cur = mysql.connection.cursor()
            items = item.split(',')
            for i, item in enumerate(items):
                cur.execute('''UPDATE %s_shoplist_table set priority = "YES", modified = %s where name = "%s"''' % (user, time.time(), item))
                mysql.connection.commit()
            del cur
            return json.dumps([{'result': "Set Priorities!"}])
    else:
        return json.dumps([{'name': "invalid credentials"}])


@app.route('/<string:user>:<string:passwd>/shoplist/unprioritize/<string:item>', methods=['PUT'])
def unprioritize_items(user, passwd, item):
    if verify_user(user, passwd):
        if request.method == 'PUT':
            cur = mysql.connection.cursor()
            items = item.split(',')
            for i, item in enumerate(items):
                cur.execute('''UPDATE %s_shoplist_table set priority = "NO", modified = %s where name = "%s"''' % (user, time.time(), item))
                mysql.connection.commit()
            del cur
            return json.dumps([{'result': "Unset Priorities!"}])
    else:
        return json.dumps([{'name': "invalid credentials"}])

@app.route('/<string:user>:<string:passwd>/shoplist/cleanup', methods=['DELETE'])
def clear_old_items(user, passwd):
        if verify_user(user, passwd):

            if request.method == 'DELETE':
                cur = mysql.connection.cursor()
                cur.execute('''delete from %s_shoplist_table where deleted = 1 and modified < "%s" ''' % (user, time.time()- (86500*days_before_cleanup)))
                mysql.connection.commit()
                del cur
                return json.dumps([{'result': "Cleaning up!"}])
            else:
                return json.dumps([{'name': "invalid credentials"}])

@app.route('/<string:user>:<string:passwd>/shoplist/hashbrown')
def get_hash(user, passwd):
                if verify_user(user, passwd):
                    cur = mysql.connection.cursor()
                    cur.execute(
                        '''SELECT sum(id), sum(modified) from %s_shoplist_table''' % user)
                    ret_val = cur.fetchall()
                    sting = [i for i in ret_val]
                    spring = []
                    for j, k in sting:
                        if j is None:
                            j = "0"
                        if k is None:
                            k = "0"
                        spring.append({"s_id": str(j), "s_modified": str(k)})
                    del cur
                    return json.dumps(spring)
                else:
                    return json.dumps([{'name': "invalid credentials"}])
#########################################SHOPLIST SYNC

@app.route('/<string:user>:<string:passwd>/shoplist/get_items')
def get_sync_items(user, passwd):
    if verify_user(user, passwd):
        cur = mysql.connection.cursor()
        cur.execute('''SELECT id, name, priority, modified, deleted, client_id from %s_shoplist_table order by priority desc ''' % user)
        ret_val = cur.fetchall()
        sting = [i for i in ret_val]
        spring = []
        for i in sting:
            thing = dict(zip(["id", "name", "priority", "modified", "deleted", "client_id"], i))
            spring.append(thing)
        del cur
        return json.dumps(spring)
    else:
        return json.dumps([{'name': "invalid credentials"}])




@app.route('/<string:user>:<string:passwd>/shoplist/sync_item/<string:idee>/<string:item>/<string:priority>/<string:modified>/<string:deleted>/<string:client_id>', methods=['PUT'])
def sync_items(idee, user, passwd, item, priority, modified, deleted, client_id):
    if verify_user(user, passwd):
        if request.method == 'PUT':
            cur = mysql.connection.cursor()
            items = item.split(',')
            for i, item in enumerate(items):
                cur.execute('''UPDATE %s_shoplist_table set name = "%s", priority = "%s", modified = "%s", deleted = "%s", client_id="%s" where id = "%s"''' % (user, item, priority, modified, deleted, client_id, idee))
                mysql.connection.commit()
            del cur
            return json.dumps([{'result': "sync edit!"}])
    else:
        return json.dumps([{'name': "invalid credentials"}])

@app.route('/<string:user>:<string:passwd>/shoplist/add_item/<string:item>/<string:priority>/<string:modified>/<string:deleted>/<string:client_id>',methods=['POST'])
def add_items(user, passwd, item, priority, modified, deleted, client_id):
        if verify_user(user, passwd):
            if request.method == 'POST':
                cur = mysql.connection.cursor()
                items = item.split(',')
                for i, item in enumerate(items):
                    cur.execute(
                        '''INSERT INTO %s_shoplist_table(name, priority, modified, deleted, client_id) value ("%s", "%s", "%s", "%s", "%s")''' % (user, item, priority, modified, deleted, client_id))
                    mysql.connection.commit()
                del cur
                return json.dumps([{'result': "sync add!"}])
        else:
            return json.dumps([{'name': "invalid credentials"}])


# ############################EXPENSE PART#####################################


@app.route('/<string:user>:<string:passwd>/expense/batch/<int:batch>')
def get_batch_expenses(user, passwd, batch):
    if verify_user(user, passwd):
        cur = mysql.connection.cursor()
        now = datetime.datetime.now()
        month = int(now.strftime("%y%m"))
        batch_start = batch*3
        start = get_sub_date(month, batch_start+3)
        end = get_sub_date(month, batch_start)
        cur.execute('''SELECT id, name, cost, month, category from %s_expense_table where month > %s and month <= %s order by month DESC''' % (user, start, end))
        ret_val = cur.fetchall()
        cur.execute('''SELECT mon_lim from %s_limit_table where name = "DEFAULT"''' % user)
        lim_val = cur.fetchone()
        cur.execute('''SELECT sum(cost) from %s_expense_table where month = %s''' % (user, month))
        mon_val = cur.fetchone()
        try:
            lim = str(lim_val[0] - mon_val[0])
        except:
            lim = str(lim_val[0])
        sting = [i for i in ret_val]
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
    else:
        return json.dumps([{'name': "invalid credentials"}])


@app.route('/<string:user>:<string:passwd>/expense/batch_cat/<string:category>/<int:batch>')
def get_cat_batch_expenses(user, passwd, category, batch):
    if verify_user(user, passwd):
        cur = mysql.connection.cursor()
        now = datetime.datetime.now()
        month = int(now.strftime("%y%m"))
        batch_start = batch*3
        start = get_sub_date(month, batch_start+3)
        end  = get_sub_date(month, batch_start)
        cur.execute('''SELECT id, name, cost, month, category from %s_expense_table where month > %s and month <= %s and category = "%s" order by month DESC''' % (user, start, end, category))
        ret_val = cur.fetchall()
        cur.execute('''SELECT mon_lim from %s_limit_table where name = "DEFAULT"''' % user)
        lim_val = cur.fetchone()
        cur.execute('''SELECT sum(cost) from %s_expense_table where month = %s''' % (user, month))
        mon_val = cur.fetchone()
        try:
            lim = str(lim_val[0] - mon_val[0])
        except:
            lim = str(lim_val[0])
        sting = [i for i in ret_val]
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
    else:
        return json.dumps([{'name': "invalid credentials"}])


@app.route('/<string:user>:<string:passwd>/expense/totals')
def get_totals_expenses(user, passwd):
    if verify_user(user, passwd):
        cur = mysql.connection.cursor()
        cur.execute('''select month, sum(cost) from %s_expense_table group by month''' % user)
        ret_val = cur.fetchall()
        sting = [i for i in ret_val]
        spring = []
        for j, k in sting:
                spring.append({"date": j, "cost": int(k)})
        del cur
        return json.dumps(spring)
    else:
        return json.dumps([{'name': "invalid credentials"}])


@app.route('/<string:user>:<string:passwd>/expense/items')
def get_all_expenses1(user, passwd):
    if verify_user(user, passwd):
        cur = mysql.connection.cursor()
        cur.execute('''SELECT name, cost, month, category from %s_expense_table''' % user)
        ret_val = cur.fetchall()
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
        sting = [i for i in ret_val]
        spring = []
        for i in sting:
            thing = dict(zip(["id", "name", "cost", "date", "category"], i))
            thing['limit'] = lim
            spring.append(thing)
        if len(sting) != 0:
            del cur
            return json.dumps(spring)
        else:
            del cur
            return json.dumps([{'limit': lim}])
    else:
        return json.dumps([{'name': "invalid credentials"}])
     
      
@app.route('/<string:user>:<string:passwd>/expense/total/<int:month>')
def get_month_total(user, passwd, month):
    if verify_user(user, passwd):
        cur = mysql.connection.cursor()
        cur.execute('''SELECT sum(cost) from %s_expense_table where month = %d''' % (user, month))
        ret_val = cur.fetchone()
        del cur
        return str(ret_val[0])
    else:
        return json.dumps([{'name': "invalid credentials"}])


@app.route('/<string:user>:<string:passwd>/expense/items/<int:month>/<string:category>')
def get_category_month_expenses(user, passwd, month, category):
    if verify_user(user, passwd):
        cur = mysql.connection.cursor()
        if month == 0:
            cur.execute('''SELECT name, cost, month from %s_expense_table where category = "%s"''' % (user, category))
        elif category == '0':
            cur.execute('''SELECT name, cost, category from %s_expense_table where month = %d''' % (user, month))
        else:
            cur.execute('''SELECT name, cost from %s_expense_table where month = %s and category = "%s"''' % (user, month, category))
        ret_val = cur.fetchall()
        del cur
        return str(ret_val)
    else:
        return json.dumps([{'name': "invalid credentials"}])


@app.route('/<string:user>:<string:passwd>/expense/items/<string:item>/<int:cost>/<string:category>', methods=['POST'])
def add_expense_to_be_deleted(user, passwd, item, cost, category):
    if verify_user(user, passwd):
        cur = mysql.connection.cursor()
        cur.execute('''SELECT MAX(id) from %s_expense_table''' % user)
        maxid = cur.fetchone()
        idee = maxid[0]
        if not maxid[0]:
            idee = -1
        now = datetime.datetime.now()
        month = int(now.strftime("%y%m"))
        cur.execute('''INSERT INTO %s_expense_table(id, name, cost, month, category) value (%s ,"%s", %s, %s, "%s")''' %(user, idee+1, item, cost, month, category))
        mysql.connection.commit()
        del cur
        return "Added %s!" % item
    else:
        return json.dumps([{'name': "invalid credentials"}])


@app.route('/<string:user>:<string:passwd>/expense/items1/<string:item>:<string:month>:<int:cost>:<string:category>', methods=['POST'])
def add_expense_new(user, passwd, item, month, cost, category):
    if verify_user(user, passwd):
        cur = mysql.connection.cursor()
        cur.execute('''SELECT MAX(id) from %s_expense_table''' % user)
        maxid = cur.fetchone()
        idee = maxid[0]
        if not maxid[0]:
            idee = -1
        month = date_from_monthstring(month)
        cur.execute('''INSERT INTO %s_expense_table(id, name, cost, month, category) value (%s ,"%s", %s, %s, "%s")''' %(user, idee+1, item, cost, month, category))
        mysql.connection.commit()
        del cur
        return "Added %s!" % item
    else:
        return json.dumps([{'name': "invalid credentials"}])


@app.route('/<string:user>:<string:passwd>/expense/limit/<int:mon_lim>', methods=['PUT'])
def update_limit(user, passwd, mon_lim):
    if verify_user(user, passwd):
        cur = mysql.connection.cursor()
        cur.execute('''UPDATE %s_limit_table set mon_lim = %d where name = "DEFAULT"''' % (user, mon_lim))
        mysql.connection.commit()
        del cur
        return "Updated monthly limit to %s!" % mon_lim
    else:
        return json.dumps([{'name': "invalid credentials"}])


@app.route('/<string:user>:<string:passwd>/expense/items/<string:item>/<int:month>/<int:cost>', methods=['DELETE'])
def delete_item(user, passwd, item, month, cost):
    if verify_user(user, passwd):
        if request.method == 'DELETE':
            cur = mysql.connection.cursor()
            if cost == 0:
                cur.execute('''DELETE from %s_expense_table where name = "%s" and month = "%s"''' % (user, item, month))
            else:
                cur.execute('''DELETE from %s_expense_table where name = "%s" and month = "%s" and cost = "%s"''' % (user, item, month, cost))
            mysql.connection.commit()
            del cur
            return "Deleted %s!" % item
        return "Done nothing!"
    else:
        return json.dumps([{'name': "invalid credentials"}])


@app.route('/<string:user>:<string:passwd>/expense/items/<string:idee>:<string:item>:<string:month>:<string:cost>:<string:category>', methods=['PUT'])
def edit_item(user, passwd, idee, item, month, cost, category):
    if verify_user(user, passwd):
        if request.method == 'PUT':
            month = date_from_monthstring(month)
            cur = mysql.connection.cursor()
            cur.execute('''UPDATE %s_expense_table set name = "%s", cost = "%s", month = "%s", category = "%s" where id = "%s"''' % (user, item, cost, month, category, idee))
            mysql.connection.commit()
            del cur
        return "Updated %s!" % item
    else:
        return json.dumps([{'name': "invalid credentials"}])


# ######################USER INFO#################################################


@app.route('/users/<string:name>/<string:passwd>', methods=['POST', 'DELETE', 'GET'])
def manage_user(name, passwd):
        if request.method == 'GET':

                    if verify_user(name, passwd):
                        return json.dumps([{'result': "LOGGED"}])
                    else:
                        return json.dumps([{'result': "WRONG"}])

        elif request.method == 'POST':
            cur = mysql.connection.cursor()
            cur.execute('''SELECT name from user_table where name = "%s"''' % name)
            if cur.fetchone():
                return json.dumps([{'result': "EXISTS"}])

            cur.execute('''SELECT MAX(id) from user_table''')
            maxid = cur.fetchone()
            idee = maxid[0]
            if not maxid[0]:
                idee = -1
            cur.execute('''INSERT INTO user_table(id, name, password) value (%s ,"%s", "%s")''' % (idee + 1, name, passwd))
            mysql.connection.commit()
            del cur
            return json.dumps([{'result': "CREATED"}])

        elif request.method == 'DELETE':
            cur = mysql.connection.cursor()
            cur.execute('''DELETE from user_table where name = "%s" and password = "%s"''' % (name, passwd))
            mysql.connection.commit()
            del cur
            return json.dumps([{'result': "DELETED %s!" % name}])

        return json.dumps([{'result': "Did nothing!"}])


@app.route('/RESET/<string:name>:<string:passwd>', methods=['PUT'])
def reset_tables(name, passwd):
    if verify_user(name, passwd):
        if request.method == 'PUT':
            cur = mysql.connection.cursor()
            cur.execute('''DROP TABLE IF EXISTS `%s`''' % (name+"_expense_table"))
            mysql.connection.commit()
            cur.execute('''DROP TABLE IF EXISTS `%s`''' % (name+"_shoplist_table"))
            mysql.connection.commit()
            cur.execute('''DROP TABLE IF EXISTS `%s`''' % (name+"_limit_table"))
            mysql.connection.commit()

            cur.execute(''' create table %s(id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, name VARCHAR(20), cost INT, month INT, category VARCHAR(20), modified INT, deleted TINYINT(1))''' % (name + "_expense_table"))
            mysql.connection.commit()

            cur.execute('''create table %s(id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, name VARCHAR(20), priority VARCHAR(10), modified INT, deleted TINYINT(1))''' % (name + "_shoplist_table"))
            mysql.connection.commit()

            cur.execute('''create table %s(name VARCHAR(20), mon_lim INT, modified INT)''' % (name + "_limit_table"))
            mysql.connection.commit()

            cur.execute('''insert into %s(name, mon_lim, modified) value ("DEFAULT", 1000, %s)''' % (name + "_limit_table", time.time()))
            mysql.connection.commit()
            del cur
            return json.dumps([{'result': "REFRESHED"}])

        return json.dumps([{'result': "FAILED"}])
    else:
        return json.dumps([{'name': "invalid credentials"}])


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
            cur.execute('''DELETE from user_table where name = "%s"''' % name)
            mysql.connection.commit()
            del cur
            return json.dumps([{'result': "DELETED %s!" % name}])
        return json.dumps([{'result': "Did nothing!"}])

# #######################OTHER FUNCTIONS ###########################


def get_sub_date(date, num):
    yy = date / 100 - num / 12
    mm = date % 100 - num % 12
    while mm <= 0:
        mm += 12
        yy -= 1
    return yy * 100 + mm


def date_from_monthstring(a):
    m = ["JAN", "FEB", "MAR", "APR", "MAY", "JUN",
         "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"]
    month = "ERROR"
    for i, j in enumerate(m):
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


def verify_user(user, passwd):
    cur = mysql.connection.cursor()
    cur.execute('''SELECT password from user_table where name = "%s"''' % user)
    ret_val = cur.fetchone()
    return ret_val[0] == passwd

# #######################MAIN #################################################3

if __name__ == "__main__":
    app.run('0.0.0.0', 35741, debug=True)
