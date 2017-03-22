package com.rpj.robin.appearance;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


class Sqealer extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Spree_expense.db";
    private static final String TABLE_NAME = "expense_table";
    private static final String LIMIT_TABLE_NAME = "limit_table";
    private static final String COLUMN_ID = "_id";
    private static final String name_col = "name";
    private static final String modified_col = "modified";
    private static final String deleted_col = "deleted";
    private static final String cost_column = "cost";
    private static final String date_column = "date";
    private static final String category_column = "category";
    private static final String tag = "tag";
    private static final String servid_col = "server_id";
    private Expense_item expense_item = new Expense_item("","","","","","","","","");
    private int no_of_days_before_cleanup = 30;


     Sqealer(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }



    @Override
    public void onCreate(SQLiteDatabase db) {

        String query = "CREATE TABLE " + TABLE_NAME + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT " + "," +
                name_col + " TEXT " + ","+ cost_column + " INT " +  ","+ date_column + " INT " + ","+ category_column + " TEXT " + ","+ deleted_col + " INT " + ","+ modified_col + " INT " + ","+ tag + " TEXT " + ","+ servid_col + " INT " +
                ");";

        db.execSQL(query);

        query = "CREATE TABLE " + LIMIT_TABLE_NAME + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT " + "," +
                name_col + " TEXT " + ","+ cost_column + " INT " + ","+ modified_col + " INT " + ","+ servid_col + " INT " +
                ");";

        db.execSQL(query);
        ContentValues values = new ContentValues();
        values.put(name_col, "DEFAULT");
        values.put(cost_column, 1500);
        values.put(modified_col,(System.currentTimeMillis() / 1000) );
        db.insert(LIMIT_TABLE_NAME, null, values);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + LIMIT_TABLE_NAME);
        onCreate(db);
    }


    void truncater(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + LIMIT_TABLE_NAME);
        onCreate(db);    }

    void addValue(Expense_item item) {

        ContentValues values = new ContentValues();
        SQLiteDatabase db = getWritableDatabase();

        values.put(name_col, item.name);
        values.put(cost_column, item.cost);
        values.put(category_column, item.category);
        values.put(date_column, item.date);
        values.put(tag, item.tag);
        values.put(deleted_col, item.deleted);
        values.put(modified_col, item.modified);
        values.put(servid_col, item.serve_id);

        db.insert(TABLE_NAME, null, values);

        db.close();
    }

    void cleanup(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE "+ deleted_col + " = 1 and " + modified_col + "< \"" + ((System.currentTimeMillis() / 1000) - (86500 *no_of_days_before_cleanup)) + "\" ;" );
    }

    void deleteValues(Expense_item item){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_NAME + " set " + deleted_col + " = 1, " + modified_col + " = " + ((System.currentTimeMillis() / 1000)) +  " WHERE " + COLUMN_ID  + " = \"" + item.client_id + "\" ;" );
    }

    void setLimit(String limit){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE " + LIMIT_TABLE_NAME + " set " + cost_column + " = " + limit + " , " + modified_col + " = " + ((System.currentTimeMillis() / 1000)) +  " WHERE " + name_col + " = 'DEFAULT'" );
    }

     String getlimit(){
        String dbString= "";
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + LIMIT_TABLE_NAME + " WHERE " + name_col + " = 'DEFAULT';";

        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();

        int columnnumber = c.getColumnIndex(cost_column);

        do {
            if (c.getString(columnnumber) != null) {
                dbString += c.getString(columnnumber);
            }
        }while (c.moveToNext());

         c.close();
         db.close();

        return dbString;
    }


    String getmonthvalue(){
        String dbString= "";
        SQLiteDatabase db = getReadableDatabase();
        String month_string = new SimpleDateFormat("yyMM", Locale.GERMANY).format(new Date());
        String query = "SELECT sum(" + cost_column + ") , " + date_column +  " FROM " + TABLE_NAME + " WHERE " + date_column + " = " + month_string + " and " + deleted_col + "!= 1" +" ;";

        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();

        int columnnumber = c.getColumnIndex("sum(" + cost_column + ")");

        do {
            if (c.getString(columnnumber) != null) {
                dbString += c.getString(columnnumber);
            }
        }while (c.moveToNext());

        c.close();
        db.close();

        return dbString;
    }


/*
    public String getValues(){
        String dbString= "";
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE 1";

        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();

        int columnnumber = c.getColumnIndex("name");

        do {
            if (c.getString(c.getColumnIndex("name")) != null) {

                dbString += c.getString(columnnumber);
                dbString += "\n";
            }
        }while (c.moveToNext());

        db.close();
        c.close();
        return dbString;
    }

     ArrayList<String> getArray(){
        ArrayList <String> myArray = new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " order by " + priority_col +" DESC";

        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();

        int columnnumber = c.getColumnIndex("name");
        String result ="";

         try {
             do {
                 if (c.getString(c.getColumnIndex("name")) != null) {
                     result = c.getString(c.getColumnIndex(servid_col)) + ":" +
                             c.getString(c.getColumnIndex(name_col)) + ":" +
                             c.getString(c.getColumnIndex(priority_col)) + ":" +
                             c.getString(c.getColumnIndex(deleted_col)) + ":" +
                             c.getString(c.getColumnIndex(modified_col));

                     myArray.add(result);
                 }
             } while (c.moveToNext());
         }catch (Exception e){
             e.printStackTrace();
         }
        db.close();
        c.close();
        return myArray;
    }


  ArrayList<Shoplist_item> getArray1(){
        ArrayList <Shoplist_item> myArray = new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " order by " + priority_col +" DESC";

        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();

        try {
            do {
                if (c.getString(c.getColumnIndex(name_col)) != null) {
                    shoplist_item.serve_id =  c.getString(c.getColumnIndex(servid_col));
                    shoplist_item.name =  c.getString(c.getColumnIndex(name_col));
                    shoplist_item.priority = c.getString(c.getColumnIndex(priority_col));
                    shoplist_item.deleted =   c.getString(c.getColumnIndex(deleted_col));
                    shoplist_item.modified = c.getString(c.getColumnIndex(modified_col));
                    myArray.add(shoplist_item);
                }
            } while (c.moveToNext());


        }catch (Exception e){
            e.printStackTrace();
        }
        db.close();
        c.close();
        return myArray;
    }
*/

    ArrayList<Expense_item> getArray2(){
        ArrayList <Expense_item> myArray = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " order by " + date_column +" DESC";

        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();

        try {
            do {
                if (c.getString(c.getColumnIndex(name_col)) != null) {
                    myArray.add(new Expense_item(c.getString(c.getColumnIndex(COLUMN_ID)), c.getString(c.getColumnIndex(name_col)), c.getString(c.getColumnIndex(cost_column)), c.getString(c.getColumnIndex(date_column)), c.getString(c.getColumnIndex(category_column)),  c.getString(c.getColumnIndex(deleted_col)),c.getString(c.getColumnIndex(modified_col)), c.getString(c.getColumnIndex(tag)), c.getString(c.getColumnIndex(servid_col)) ));
                }
            } while (c.moveToNext());


        }catch (Exception e){
            e.printStackTrace();
        }

        c.close();
        db.close();
        return myArray;
    }

    ArrayList<Expense_item> get_batch_array(int batch){

        String month_string = new SimpleDateFormat("yyMM", Locale.GERMANY).format(new Date());
        int month = Integer.parseInt(month_string);
        int batch_start = batch*3;
        String start = get_sub_date(month, batch_start+3);
        String end = get_sub_date(month, batch_start);

        ArrayList <Expense_item> myArray = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + date_column  + " > " + start + " and " + date_column + " <= " + end + " order by " + date_column +" DESC";

        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();

        try {
            do {
                if (c.getString(c.getColumnIndex(name_col)) != null) {
                    myArray.add(new Expense_item(c.getString(c.getColumnIndex(COLUMN_ID)), c.getString(c.getColumnIndex(name_col)), c.getString(c.getColumnIndex(cost_column)), c.getString(c.getColumnIndex(date_column)), c.getString(c.getColumnIndex(category_column)), c.getString(c.getColumnIndex(deleted_col)), c.getString(c.getColumnIndex(modified_col)), c.getString(c.getColumnIndex(tag)), c.getString(c.getColumnIndex(servid_col)) ));
                }
            } while (c.moveToNext());

        }catch (Exception e){
            e.printStackTrace();
        }

        c.close();
        db.close();

        return myArray;
    }

    ArrayList<Expense_item> get_category(String category, int batch){

        String month_string = new SimpleDateFormat("yyMM", Locale.GERMANY).format(new Date());
        int month = Integer.parseInt(month_string);
        int batch_start = batch*3;
        String start = get_sub_date(month, batch_start+3);
        String end = get_sub_date(month, batch_start);

        ArrayList <Expense_item> myArray = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + date_column  + " > " + start + " and " + date_column + " <= " + end + " and " + category_column + " = " + "\"" + category + "\"" +" and deleted != 1 " + " order by " + date_column +" DESC";

        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();

        try {
            do {
                if (c.getString(c.getColumnIndex(name_col)) != null) {
                    myArray.add(new Expense_item(c.getString(c.getColumnIndex(COLUMN_ID)), c.getString(c.getColumnIndex(name_col)), c.getString(c.getColumnIndex(cost_column)), c.getString(c.getColumnIndex(date_column)), c.getString(c.getColumnIndex(category_column)), c.getString(c.getColumnIndex(deleted_col)), c.getString(c.getColumnIndex(modified_col)), c.getString(c.getColumnIndex(tag)), c.getString(c.getColumnIndex(servid_col)) ));
                }
            } while (c.moveToNext());

        }catch (Exception e){
            e.printStackTrace();
        }

        c.close();
        db.close();

        return myArray;
    }


    ArrayList<Expense_item> get_totals(){

        ArrayList <Expense_item> myArray = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT " + date_column + " , " + "SUM(" + cost_column + ") FROM " + TABLE_NAME + " where deleted != 1" + " group by " + date_column;

        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();

        try {
            do {
                if (c.getString(c.getColumnIndex(date_column)) != null) {
                    myArray.add(new Expense_item("", "", c.getString(c.getColumnIndex("SUM(" + cost_column + ")")), c.getString(c.getColumnIndex(date_column)), "", "", "", "", ""));
                }
            } while (c.moveToNext());

        }catch (Exception e){
            e.printStackTrace();
        }

        c.close();
        db.close();

        return myArray;
    }




    private String  get_sub_date(int date, int num){

        int yy = date / 100 - num / 12;
        int mm = date % 100 - num % 12;
        while (mm <= 0){
        mm += 12;
        yy -= 1;}
        return (String.valueOf(yy * 100 + mm));
    }


    private int date_from_monthstring(String a) {
        String[] m = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN",
                "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
        String month = "ERROR";
        for (int i = 0; i < 12; i++) {
            if (m[i].equals(a)) {
                if (i >= 10)
                    month = String.valueOf(i + 1);
                else
                    month = "0" + String.valueOf(i + 1);
            }
        }

        String yearstamp = new SimpleDateFormat("yy", Locale.GERMANY).format(new Date());
        try {

            return (Integer.parseInt(yearstamp + month));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
//////////////////////////// SYNC FUNCTIONS //////////////////////////


    String expenselist_fullhashbrown(){
        String dbString= "";

        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT sum("+ tag + ")%100000,sum(" + modified_col+")%100000 FROM " + TABLE_NAME;
        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();

        int columnnumber = c.getColumnIndex("sum(" +tag +")%100000");

        do {
            if (c.getString(columnnumber) != null) {

                dbString = c.getInt(c.getColumnIndex("sum(" +tag +")%100000")) + ":" + c.getInt(c.getColumnIndex("sum(" +modified_col +")%100000"));
            }
        }while (c.moveToNext());

        db.close();
        c.close();
        if (dbString.equals("") || dbString.isEmpty()){
            dbString = "0:0";
        }
        db.close();
        c.close();
        return dbString;
    }


    ArrayList<Expense_item> get_full_array(){

        ArrayList <Expense_item> myArray = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;

        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();

        try {
            do {
                if (c.getString(c.getColumnIndex(name_col)) != null) {
                    myArray.add(new Expense_item(c.getString(c.getColumnIndex(COLUMN_ID)), c.getString(c.getColumnIndex(name_col)), c.getString(c.getColumnIndex(cost_column)), c.getString(c.getColumnIndex(date_column)), c.getString(c.getColumnIndex(category_column)), c.getString(c.getColumnIndex(deleted_col)), c.getString(c.getColumnIndex(modified_col)), c.getString(c.getColumnIndex(tag)), c.getString(c.getColumnIndex(servid_col)) ));
                }
            } while (c.moveToNext());

        }catch (Exception e){
            e.printStackTrace();
        }

        c.close();
        db.close();

        return myArray;
    }


    String expenselisthashbrown(int batch){
        String dbString= "";
        String month_string = new SimpleDateFormat("yyMM", Locale.GERMANY).format(new Date());
        int month = Integer.parseInt(month_string);
        int batch_start = batch*3;
        String start = get_sub_date(month, batch_start+3);
        String end = get_sub_date(month, batch_start);

        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT sum("+ tag + ")%100000,sum(" + modified_col+")%100000 FROM " + TABLE_NAME + " WHERE " + date_column  + " > " + start + " and " + date_column + " <= " + end;
        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();

        int columnnumber = c.getColumnIndex("sum(" +tag +")%100000");

        do {
            if (c.getString(columnnumber) != null) {

                dbString = c.getInt(c.getColumnIndex("sum(" +tag +")%100000")) + ":" + c.getInt(c.getColumnIndex("sum(" +modified_col +")%100000"));
            }
        }while (c.moveToNext());

        db.close();
        c.close();
        if (dbString.equals("") || dbString.isEmpty()){
            dbString = "0:0";
        }
        db.close();
        c.close();
        return dbString;
    }



    public void update_Values(Expense_item item){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_NAME + " set " + name_col + " =  \"" + item.name + "\" , " + cost_column + " = " + item.cost + " , " +  date_column + " = " + item.date  + ", "+ category_column + " = " + "\"" + item.category + "\"" + " , " + modified_col + " = " + item.modified + " WHERE " + COLUMN_ID  + " = \"" + item.client_id + "\" ;" );
    }

    public void sync_Values(Expense_item item){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_NAME + " set " + name_col + " =  \"" + item.name + "\" , " + cost_column + " = " + item.cost + " , " +  date_column + " = " + item.date  + ", "+ category_column + " = " + "\"" + item.category + "\"" + " , " + modified_col + " = " + item.modified + " , " + deleted_col + " = " + item.deleted + " , " + servid_col + " = " + item.serve_id + " WHERE " + COLUMN_ID  + " = \"" + item.client_id + "\" ;" );
    }

}
