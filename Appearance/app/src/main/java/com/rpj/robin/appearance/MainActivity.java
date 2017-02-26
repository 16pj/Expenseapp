package com.rpj.robin.appearance;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;



public class MainActivity extends AppCompatActivity {


    private String enter;
    //private String myURL = "http://192.168.1.25:35741";

    String myURL = "http://rojo16.pythonanywhere.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sharedpref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String name =   sharedpref.getString("username", "");
        if (name.isEmpty()) {
                saveInfo("","");
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.stuff:
                if (item.isChecked())
                    item.setChecked(false);
                else item.setChecked(true);
                Toast.makeText(this, "SETTINGS", Toast.LENGTH_SHORT).show();

                Intent i = new Intent(MainActivity.this, Login.class);
                startActivity(i);

                return true;

            case R.id.stuff2:
                if (item.isChecked())
                    item.setChecked(false);
                else item.setChecked(true);
                Toast.makeText(this, "LOGOUT", Toast.LENGTH_SHORT).show();


                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setTitle("Are You Sure?");

                builder1.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveInfo("", "");

                    }
                });
                builder1.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder1.show();

                return true;

            default:
                return false;
        }
    }

    public void onlist1(View view) {

        SharedPreferences sharedpref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String name = sharedpref.getString("username", "");
        String passwd = sharedpref.getString("password", "");

        if(name.equals("") && passwd.equals("")){
            Toast.makeText(this, "Please Login with Correct Credentials", Toast.LENGTH_SHORT).show();
        }
        else {

        Intent i = new Intent(MainActivity.this, Shoplist.class);
        startActivity(i);
    }
    }


    public void onlist2(View view) {

        SharedPreferences sharedpref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String name = sharedpref.getString("username", "");
        String passwd = sharedpref.getString("password", "");

        if(name.equals("") && passwd.equals("")){
            Toast.makeText(this, "Please Login with Correct Credentials", Toast.LENGTH_SHORT).show();
        }
        else {

            Intent i = new Intent(MainActivity.this, Expense.class);
            startActivity(i);
        }
    }


    public void saveInfo(String userName, String userPass){

        SharedPreferences sharedpref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpref.edit();
        editor.putString("username", userName);
        editor.putString("password", userPass);
        editor.apply();
    }

    public boolean getInfo(String userName, String userPass){
        SharedPreferences sharedpref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String name = sharedpref.getString("username", "");
        String passwd = sharedpref.getString("password", "");

        return userName.equals(name) && userPass.equals(passwd);
    }

}
