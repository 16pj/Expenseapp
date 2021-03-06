package com.rpj.robin.appearance;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;



public class MainActivity extends AppCompatActivity {

    private String myURL = myconf.global_url;
    private Intent hashservice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sharedpref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String name =   sharedpref.getString("username", "");

        SharedPreferences snappref = getSharedPreferences("SNAP", Context.MODE_PRIVATE);
        String output = snappref.getString("STATUS", "");


        if (output.isEmpty()) {
            SharedPreferences.Editor editor = snappref.edit();
            editor.putString("STATUS", "OFF");
            editor.apply();
        }

        if (name.isEmpty()) {
            saveInfo("","");
        }

        if (output.equals("ON")) {
            Toast.makeText(this, "Welcome To Spree.\n SNAP mode is ON. You are OFFLINE!", Toast.LENGTH_LONG).show();
        }
        else if(output.equals("OFF")) {
            Toast.makeText(this, "Welcome To Spree!", Toast.LENGTH_SHORT).show();
        }
       hashservice = new Intent(this, HashService.class);
        startService(hashservice);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
                return true;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopService(hashservice);


    }

    public void onlist1(View view) {


        SharedPreferences sharedpref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String name = sharedpref.getString("username", "");
        String passwd = sharedpref.getString("password", "");

        if(name.equals("") && passwd.equals("")){
            Toast.makeText(this, "Please Login with Correct Credentials", Toast.LENGTH_SHORT).show();
        }
        else {

            SharedPreferences snappref = getSharedPreferences("SNAP", Context.MODE_PRIVATE);
            String output = snappref.getString("STATUS", "");

            if (output.equals("OFF")){
            Intent i = new Intent(MainActivity.this, Shoplist.class);
            startActivity(i);
            }

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
            SharedPreferences snappref = getSharedPreferences("SNAP", Context.MODE_PRIVATE);
            String output = snappref.getString("STATUS", "");

            if (output.equals("OFF")){
                Intent i = new Intent(MainActivity.this, Expense.class);
                startActivity(i);
            }
            else if (output.equals("ON")){
                Toast.makeText(this, "SNAP MODE for Expense needs working.\nTry Going Online for now :)", Toast.LENGTH_LONG).show();
            }
        }
    }


    public void saveInfo(String userName, String userPass){

        SharedPreferences sharedpref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpref.edit();
        editor.putString("username", userName);
        editor.putString("password", userPass);
        editor.apply();
    }

}

