package com.rpj.robin.appearance;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;


class Sqealer extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Spree.db";
    private static final String TABLE_NAME = "shoplist_table";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_ITEM = "name";


     Sqealer(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        String query = "CREATE TABLE " + TABLE_NAME + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT " + ","+
                COLUMN_ITEM + " TEXT " +
                ");";

        db.execSQL(query);

    }

    @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
    }


     void truncater(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
        db.close();
     }


     void addValues (ArrayList<String> items){

        ContentValues values = new ContentValues();
        SQLiteDatabase db = getWritableDatabase();

        for (String item:items)
        {values.put(COLUMN_ITEM, item);
        db.insert(TABLE_NAME, null, values);}
        db.close();
    }

    public void deleteValues(String[] items){
        SQLiteDatabase db = getWritableDatabase();
        for (String item:items)
        db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_ITEM + "= \"" + item + "\" ;" );
        db.close();
    }

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
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE 1";


        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();

        int columnnumber = c.getColumnIndex("name");

        do {
            if (c.getString(c.getColumnIndex("name")) != null) {

                myArray.add(c.getString(columnnumber));
            }
        }while (c.moveToNext());

        db.close();
        c.close();
        return myArray;
    }



}

