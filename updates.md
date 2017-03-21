# Expenseapp
An Android App with a Python REST server backend that tracks expenses and also makes shopping lists



testlist2 needs to be worked on and renamed as Shoplift2. 
testlist2 currently has ArrayAdapter working and hence can manipulate multiple items at a time.
testlist2 added priority and json
testlist2 works fine except for cost

testlist1 (Modified from testlist2) is for expense tracking and needs to merge into Shoplift2
testlist1 currently has add, remove, limit and show functions implemented. Fixed json output.
testlist1 fixed app crash when deleting more than 1 option, added limit changing option


appearance added which is the main user interface
appearance only allows selecting an option if logged in. it has menu with
1. login which goes to another page to login
2. logout which clears shared preference
3. Reset which deletes old tables (if existing) and creates new tables (with default values)

appearance has the core functionalities
appearance is merged with testlist1 and testlist2
appearance needs code optimization, bug fixing and additional features
appearance beta version is ready.

Spree is the name of the App
Spree now has a SNAP mode, which saves the last retrieved list
Spree now has edit option and monthly totals along with option to add more details
Spree needs work on security of password, operations and refinemet in code
Spree now has a secure md5 on password, json responses and refine code
Spree now has edit, month totals, category wise items
Spree needs a new offline sync feature and test for bugs, refinements.

Spree has offline mode of shoplist, need to make it sync with server
Spree now supports synchronization with server. Internet is not a requirement except for initial login
Spree only needs cleaner code and proper table exporting