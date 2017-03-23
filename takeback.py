import urllib2
import json


response = urllib2.urlopen('http://rojo16.pythonanywhere.com/robin/expense/items')
html = response.read()
hj = json.loads(html)
for i in hj:
    print i
    print "\n"