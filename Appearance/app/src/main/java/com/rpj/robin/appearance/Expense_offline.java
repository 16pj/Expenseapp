package com.rpj.robin.appearance;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class Expense_offline extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_offline);
        Toast.makeText(this, "SHOWING SNAPSHOT OF LAST SAVED LIST", Toast.LENGTH_SHORT).show();
    }
}
