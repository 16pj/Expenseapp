import time
from datetime import datetime

def date_from_stamp(a):
    return datetime.fromtimestamp(a).strftime("%y%m")
a = time.time()

print (date_from_stamp(a))