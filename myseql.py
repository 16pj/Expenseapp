from flask import Flask, request
from flask_mysqldb import MySQL
import datetime
import json
import time
import urllib2

app = Flask(__name__)


app.config['MYSQL_HOST'] = 'localhost'
app.config['MYSQL_USER'] = 'testuser'
app.config['MYSQL_PASSWORD'] = 'test123'
app.config['MYSQL_DB'] = 'testdb'

mysql = MySQL(app)

@app.route('/')
def index():
    return json.dumps([{'result': "WELCOME TO THE APP. USE THE API."}])

@app.route('/master/shoplist/transport/<string:from_table>:<string:to_table>', methods=['PUT'])
def master_transport_shoplist_items(from_table, to_table):
   if request.method == 'PUT':
    cur = mysql.connection.cursor()
    cur.execute('''SELECT id, name, priority, modified, deleted, tag, client_id from %s order by priority desc ''' % from_table)
    ret_val = cur.fetchall()
    sting = [i for i in ret_val]
    spring = []

    strin = ""
    cur.execute('''truncate table %s ''' % to_table)
    mysql.connection.commit()
    for i in sting:

            strin += "id " + str(i[0]) + ", name " + str(i[1]) + ", priority " + str(i[2]) + ", modified " + str(i[3]) + ", deleted " + str(i[4]) + ", tag " + str(i[5]) + ", client_id" + str(i[6]) + "</br>\n"
            cur.execute('''INSERT INTO %s(id, name, priority, modified, deleted, tag, client_id) value ("%s", "%s", "%s", "%s", "%s", "%s", "%s")''' % (to_table, str(i[0]), str(i[1]), str(i[2]), str(i[3]), str(i[4]), str(i[5]),str(i[6])))
            mysql.connection.commit()
    del cur
    return str(strin)

@app.route('/master/expense/transport/<string:from_table>:<string:to_table>', methods=['PUT'])
def master_transport_expense_items(from_table, to_table):
   if request.method == 'PUT':
    cur = mysql.connection.cursor()
    cur.execute('''SELECT id, name, cost, category, month, modified, deleted, tag, client_id from %s ''' % from_table)
    ret_val = cur.fetchall()
    sting = [i for i in ret_val]
    spring = []

    strin = ""
    cur.execute('''truncate table %s ''' % to_table)
    mysql.connection.commit()
    for i in sting:

            strin += "id " + str(i[0]) + ", name " + str(i[1]) + ", cost " + str(i[2]) + ", category " + str(i[3]) + ", month " + str(i[4])  + ", modified " + str(i[5]) + ", deleted " + str(i[6]) + ", tag " + str(i[7]) + ", client_id" + str(i[8]) + "</br>\n"
            cur.execute('''INSERT INTO %s(id, name, cost, category, month, modified, deleted, tag, client_id) value ("%s", "%s", "%s", "%s", "%s", "%s", "%s", "%s", "%s")''' % (to_table, str(i[0]), str(i[1]), str(i[2]), str(i[3]), str(i[4]), str(i[5]),str(i[6]), str(i[7]), str(i[8])))
            mysql.connection.commit()
    del cur
    return str(strin)


@app.route('/master/go/<string:to_table>')
def master_go(to_table):
    cur = mysql.connection.cursor()

    response = urllib2.urlopen('http://rojo16.pythonanywhere.com/robin/expense/items')
    html = response.read()
    hj = json.loads(html)
    for i in hj:
        cur.execute('''INSERT INTO %s(name, cost, category, month) value ("%s", "%s", "%s", "%s")''' % (
        to_table, str(i['name']), str(i['cost']), str(i['category']), str(i['date'])))
        mysql.connection.commit()
    return "done"

if __name__ == "__main__":
    app.run('0.0.0.0', 35741, debug=True)
