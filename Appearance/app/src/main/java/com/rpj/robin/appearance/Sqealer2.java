package com.rpj.robin.appearance;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;


class Sqealer2 extends SQLiteOpenHelper {


    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Spree_shoplist.db";
    //private static final String myconf.SHOPLIST_TABLE_NAME = "shoplist_table";
    private static final String COLUMN_ID = "_id";
    private static final String name_col = "name";
    private static final String modified_col = "modified";
    private static final String deleted_col = "deleted";
    private static final String priority_col = "priority";
    private static final String tag_col = "tag";
    private static final String servid_col = "server_id";
    private Shoplist_item shoplist_item = new Shoplist_item("","","","","","","");
    private int no_of_days_before_cleanup = 30;


     Sqealer2(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        String query = "CREATE TABLE " + myconf.SHOPLIST_TABLE_NAME + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT " + "," +
                name_col + " TEXT " + ","+ priority_col + " TEXT " + ","+ deleted_col + " INT " + ","+ modified_col + " INT " + ","+ tag_col + " INT " + ","+ servid_col + " INT " +
                ");";

        db.execSQL(query);

    }

    @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            db.execSQL("DROP TABLE IF EXISTS " + myconf.SHOPLIST_TABLE_NAME);
            onCreate(db);
    }


     void truncater(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + myconf.SHOPLIST_TABLE_NAME);
        onCreate(db);    }

     void addValue(Shoplist_item item) {

         ContentValues values = new ContentValues();
         SQLiteDatabase db = getWritableDatabase();

         values.put(name_col, item.name);
         values.put(priority_col, item.priority);
         values.put(deleted_col, item.deleted);
         values.put(modified_col, item.modified);
         values.put(tag_col, item.tag);
         values.put(servid_col, item.serve_id);

         db.insert(myconf.SHOPLIST_TABLE_NAME, null, values);

         db.close();
     }

     void cleanup(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + myconf.SHOPLIST_TABLE_NAME + " WHERE "+ deleted_col + " = 1 and " + modified_col + "< \"" + ((System.currentTimeMillis() / 1000) - (86500 *no_of_days_before_cleanup)) + "\" ;" );
    }

     void deleteValues(Shoplist_item item){
        SQLiteDatabase db = getWritableDatabase();
            db.execSQL("UPDATE " + myconf.SHOPLIST_TABLE_NAME + " set " + deleted_col + " = 1, " + modified_col + " = " + ((System.currentTimeMillis() / 1000)) +  " WHERE " + COLUMN_ID  + " = \"" + item.client_id + "\" ;" );
    }
/*
    public String getValues(){
        String dbString= "";
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + myconf.SHOPLIST_TABLE_NAME + " WHERE 1";

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
        String query = "SELECT * FROM " + myconf.SHOPLIST_TABLE_NAME + " order by " + priority_col +" DESC";

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
        String query = "SELECT * FROM " + myconf.SHOPLIST_TABLE_NAME + " order by " + priority_col +" DESC";

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

    ArrayList<Shoplist_item> getArray2(){

        ArrayList <Shoplist_item> myArray = new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + myconf.SHOPLIST_TABLE_NAME + " order by " + priority_col +" DESC";

        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();

        try {
            do {
                if (c.getString(c.getColumnIndex(name_col)) != null) {
                    myArray.add(new Shoplist_item(c.getString(c.getColumnIndex(COLUMN_ID)), c.getString(c.getColumnIndex(name_col)), c.getString(c.getColumnIndex(priority_col)), c.getString(c.getColumnIndex(deleted_col)), c.getString(c.getColumnIndex(modified_col)),c.getString(c.getColumnIndex(tag_col)) , c.getString(c.getColumnIndex(servid_col)) ));
                }
            } while (c.moveToNext());


        }catch (Exception e){
            e.printStackTrace();
        }
        db.close();
        c.close();
        return myArray;
    }

     void prioritize(Shoplist_item item){
        SQLiteDatabase db = getWritableDatabase();
            db.execSQL("UPDATE " + myconf.SHOPLIST_TABLE_NAME + " set " + priority_col + " = 'YES' , " + modified_col + " = " + ((System.currentTimeMillis() / 1000)) + " WHERE " + COLUMN_ID  + "= \"" + item.client_id + "\" ;" );
    }

     void unprioritize(Shoplist_item item){
        SQLiteDatabase db = getWritableDatabase();
         db.execSQL("UPDATE " + myconf.SHOPLIST_TABLE_NAME + " set " + priority_col + " = 'NO' , " + modified_col + " = " + ((System.currentTimeMillis() / 1000)) + " WHERE " + COLUMN_ID  + "= \"" + item.client_id + "\" ;" );
    }



//////////////////////////// SYNC FUNCTIONS //////////////////////////


     String shoplisthashbrown(){
        String dbString= "";
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT sum("+ tag_col + "),sum(" + modified_col+") FROM " + myconf.SHOPLIST_TABLE_NAME;
        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();

        int columnnumber = c.getColumnIndex("sum(" +tag_col +")");

        do {
            if (c.getString(columnnumber) != null) {

                dbString = c.getString(c.getColumnIndex("sum(" +tag_col +")")) + ":" + c.getString(c.getColumnIndex("sum(" +modified_col +")"));
            }
        }while (c.moveToNext());

        db.close();
        c.close();
        if (dbString.equals("") || dbString.isEmpty()){
             dbString = "0:0";
         }
        return dbString;
    }



public void update_Values(Shoplist_item item){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE " + myconf.SHOPLIST_TABLE_NAME + " set " + name_col + " =  \"" + item.name + "\" , " + deleted_col + " = " + item.deleted + " , " + priority_col + " =  \"" + item.priority+ "\" , " + modified_col + " = " + item.modified + " , " + servid_col + " = " + item.serve_id +  " WHERE " + COLUMN_ID  + " = \"" + item.client_id + "\" ;" );
    }

}

