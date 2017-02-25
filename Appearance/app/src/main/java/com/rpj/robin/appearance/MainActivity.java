package com.rpj.robin.appearance;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private String userID;
    private String userPassword;
    private String enter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
                Toast.makeText(this, "LOGIN", Toast.LENGTH_SHORT).show();

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

                final EditText input = new EditText(this);
                builder1.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ;

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

            case R.id.stuff3:
                if (item.isChecked())
                    item.setChecked(false);
                else item.setChecked(true);
                Toast.makeText(this, "RESET", Toast.LENGTH_SHORT).show();


                AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                builder2.setTitle("Are you sure you want to Reset?");
                builder2.setMessage("Enter Password");


                final EditText input_reset = new EditText(this);
                input_reset.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                builder2.setView(input_reset);

                builder2.setPositiveButton("RESET", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "Everything is Fresh now!", Toast.LENGTH_SHORT).show();

                    }
                });
                builder2.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder2.show();

                return true;
            default:
                return super.onOptionsItemSelected(item);


        }
    }

    public void onlist1(View view){
        Intent i = new Intent(MainActivity.this, Shoplist.class);
        startActivity(i);
    }

    public void onlist2(View view){
        Intent i = new Intent(MainActivity.this, Expense.class);
        startActivity(i);
    }
}
