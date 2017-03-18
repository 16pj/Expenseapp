package com.rpj.robin.appearance;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Expense extends AppCompatActivity {

    private ArrayList<Expense_item> mylist;
    private ArrayList<Expense_item> selecteditems;
    private ArrayList<Expense_item> my_expenselist;

    private int batch;
    private TextView limit;

    private ArrayAdapter<Expense_item> adapter;
    private ListView listView;
    private EditText editName;
    private EditText editNum;
    private TextView heading;
    private String m_Text = "";
    private String  TOTAL_FLAG = "FALSE";
    private String CATEGOY_FLAG = "FALSE";
    private String Selected_month = "";
    private String Selected_category = "DEFAULT";
    private String Selected_category_total = "DEFAULT";
    private String login_name;
    private String login_pass;
    Sqealer sqealee;


    private String myURL = myconf.global_url;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);

        listView = (ListView) findViewById(R.id.listView);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        selecteditems = new ArrayList<>();
        my_expenselist = new ArrayList<>();
        mylist = new ArrayList<>();
        limit = (TextView) findViewById(R.id.limit);
        batch = 1;
        heading = (TextView) findViewById(R.id.heading);
        sqealee = new Sqealer(this, null, null, 1);
        Button Adder;
        Adder = (Button) findViewById(R.id.ADD);

        Adder.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(TOTAL_FLAG.equals("FALSE"))
                onFullAdd();
                return true;
            }
        });

        SharedPreferences sharedpref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        login_name = sharedpref.getString("username", "");
        login_pass = sharedpref.getString("password", "");

        editName = (EditText) findViewById(R.id.name);
        editNum = (EditText) findViewById(R.id.num);

        adapter = new CustomUsersAdapter(this, mylist);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                CheckedTextView mybox = (CheckedTextView) view.findViewById(R.id.checkedview);

                if (selecteditems.contains(mylist.get(position))){
                    selecteditems.remove(mylist.get(position));
                    if(mybox.isChecked())
                        mybox.setChecked(false);

                }
                else {
                    selecteditems.add(mylist.get(position));
                    if(!mybox.isChecked())
                        mybox.setChecked(true);
                }
            }
        });


        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                final List<String> c = Arrays.asList("GROCERIES", "LEISURE", "NEEDS", "DEFAULT");
                Expense_item selected_expense = mylist.get(position);
                int mon_pos = Integer.parseInt(selected_expense.date.substring(2))-1;
                int cat_pos = c.indexOf(selected_expense.category);

                onEdit(selected_expense.client_id, selected_expense.name, selected_expense.cost, mon_pos, cat_pos);

                return true;
            }
        });

        repopulate(null);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.expense_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.month_totals:
                if (item.isChecked())
                    item.setChecked(false);
                else item.setChecked(true);

                if (TOTAL_FLAG.equals("FALSE")) {
                    TOTAL_FLAG = "TRUE";
                    CATEGOY_FLAG = "FALSE";
                    Toast.makeText(this, "Monthly Totals", Toast.LENGTH_SHORT).show();
                    mylist.clear();
                    adapter.notifyDataSetChanged();
                    get_totals();
                }
                else {
                    TOTAL_FLAG = "FALSE";
                    repopulate(null);
                }
                return true;

            case R.id.category_wise:
                if (item.isChecked())
                    item.setChecked(false);
                else item.setChecked(true);

                if (CATEGOY_FLAG.equals("FALSE")) {
                    TOTAL_FLAG = "FALSE";
                    CATEGOY_FLAG = "TRUE";

                    final String[] c = {"GROCERIES", "LEISURE", "NEEDS", "DEFAULT"};

                    final ArrayAdapter<String> adp_c = new ArrayAdapter<>(Expense.this,
                            android.R.layout.simple_spinner_item, c);


                    final Spinner sp_c = new Spinner(Expense.this);
                    sp_c.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    sp_c.setAdapter(adp_c);

                    sp_c.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            Selected_category_total = c[position];
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });


                    AlertDialog.Builder builder = new AlertDialog.Builder(Expense.this);

                    LinearLayout ll = new LinearLayout(this);
                    ll.setOrientation(LinearLayout.VERTICAL);
                    ll.addView(sp_c);


                    builder.setView(ll);
                    builder.setPositiveButton("Show", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            mylist.clear();
                            adapter.notifyDataSetChanged();
                            get_categories(Selected_category_total, 0);
                            batch=1;
                            Toast.makeText(Expense.this, "SHOWING " + Selected_category_total + " ITEMS", Toast.LENGTH_SHORT).show();

                        }
                    });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CATEGOY_FLAG = "FALSE";
                        repopulate(null);
                        dialog.cancel();
                    }
                });

                    builder.create().show();

                }
                else {
                    CATEGOY_FLAG = "FALSE";
                    repopulate(null);
                }
                return true;

            default:
                return true;
        }
    }


    public void repopulate(View view){

        mylist.clear();
        my_expenselist.clear();
        selecteditems.clear();
        adapter.notifyDataSetChanged();
        listView.clearChoices();
        try {
            get_items(0);
        }catch (Exception e){
            e.printStackTrace();
        }
        batch=1;
        TOTAL_FLAG="FALSE";
        CATEGOY_FLAG = "FALSE";
    }

    public void overpopulate(View view){

        if(TOTAL_FLAG.equals("FALSE")) {

            if(CATEGOY_FLAG.equals("FALSE"))
               get_items(batch);
            else
               get_categories(Selected_category_total,batch);
            batch += 1;
            listView.clearChoices();
        }
    }


    public void set_limit(String limit){

        sqealee.setLimit(limit);
        Toast.makeText(this, "Limit set to " + limit, Toast.LENGTH_SHORT).show();
    }

    public void onAdd(View view){

        if(TOTAL_FLAG.equals("FALSE")) {
            String name = editName.getText().toString();
            String num = editNum.getText().toString();

            if (!name.equals("") && !num.equals("")) {
                Toast.makeText(this, name, Toast.LENGTH_SHORT).show();
                Toast.makeText(this, num, Toast.LENGTH_SHORT).show();
                name = name.replace(" ", "_");
                String month_string = new SimpleDateFormat("yyMM", Locale.GERMANY).format(new Date());
                //Toast.makeText(this, String.valueOf(System.currentTimeMillis() / 1000L), Toast.LENGTH_SHORT).show();
                sqealee.addValue(new Expense_item("", name, num, month_string, "DEFAULT", "0", String.valueOf(System.currentTimeMillis() / 1000L),  String.valueOf(System.currentTimeMillis() / 1000L), ""));

                editName.setText("");
                editNum.setText("");
                repopulate(null);
                listView.clearChoices();
            } else {
                onFullAdd();
            }
        }
    }

    public void onRemove(View view){
        if(TOTAL_FLAG.equals("FALSE")) {
            if (mylist.size() == 0) return;

            for (Expense_item item : selecteditems) {
                Toast.makeText(this, item.name, Toast.LENGTH_SHORT).show();
                sqealee.deleteValues(item);
            }

            repopulate(null);
        }
    }

    public void onLimit(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Limit");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_Text = input.getText().toString();
                try{
                    int temp = Integer.parseInt(m_Text);
                    m_Text = String.valueOf(temp);
                    set_limit(m_Text);
                    repopulate(null);
                }catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getBaseContext(), "Invalid limit", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }


    public void onFullAdd(){

        final String[] m = { "JAN", "FEB", "MAR", "APR", "MAY", "JUN",
                "JUL", "AUG", "SEP", "OCT", "NOV", "DEC" };

        final String[] c = { "GROCERIES", "LEISURE", "NEEDS", "DEFAULT"};


        final ArrayAdapter<String> adp_m = new ArrayAdapter<>(Expense.this,
                android.R.layout.simple_spinner_item, m);

        final ArrayAdapter<String> adp_c = new ArrayAdapter<>(Expense.this,
                android.R.layout.simple_spinner_item, c);

        final EditText name = new EditText(Expense.this);
        final EditText cost = new EditText(Expense.this);
        name.setHint("NAME");
        cost.setHint("COST");
        name.setInputType(InputType.TYPE_CLASS_TEXT);
        cost.setInputType(InputType.TYPE_CLASS_NUMBER);

        final Spinner sp_m = new Spinner(Expense.this);
        sp_m.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        sp_m.setAdapter(adp_m);
        int pos;
        try {
            pos = Integer.parseInt(mylist.get(0).date.substring(2)) - 1;
        }catch (Exception e){
            pos = 0;
        }
            sp_m.setSelection(pos);
        sp_m.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Selected_month = m[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final Spinner sp_c = new Spinner(Expense.this);
        sp_c.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        sp_c.setAdapter(adp_c);

        sp_c.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Selected_category = c[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        AlertDialog.Builder builder = new AlertDialog.Builder(Expense.this);

        LinearLayout ll=new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.addView(name);
        ll.addView(cost);
        ll.addView(sp_m);
        ll.addView(sp_c);


        builder.setView(ll);
        builder.setPositiveButton("Add",  new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String nam = name.getText().toString();
                String cos = cost.getText().toString();


                nam = nam.replace(" ", "_");

                sqealee.addValue(new Expense_item("",nam,  cos, date_from_monthstring(Selected_month), Selected_category, "0", String.valueOf(System.currentTimeMillis() / 1000L), String.valueOf(System.currentTimeMillis() / 1000L), ""));

                repopulate(null);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();

        repopulate(null);

    }


    public void onEdit(final String idee, String edit_name, String edit_cost, int  edit_month, int edit_category ){

        if(TOTAL_FLAG.equals("FALSE")) {

            final String[] m = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN",
                    "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};

            final String[] c = {"GROCERIES", "LEISURE", "NEEDS", "DEFAULT"};

            final ArrayAdapter<String> adp_m = new ArrayAdapter<>(Expense.this,
                    android.R.layout.simple_spinner_item, m);

            final ArrayAdapter<String> adp_c = new ArrayAdapter<>(Expense.this,
                    android.R.layout.simple_spinner_item, c);

            final EditText name = new EditText(Expense.this);
            final EditText cost = new EditText(Expense.this);
            name.setText(edit_name);
            cost.setText(edit_cost);
            name.setInputType(InputType.TYPE_CLASS_TEXT);
            cost.setInputType(InputType.TYPE_CLASS_NUMBER);

            final Spinner sp_m = new Spinner(Expense.this);
            sp_m.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            sp_m.setAdapter(adp_m);

            sp_m.setSelection(edit_month);
            sp_m.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Selected_month = m[position];
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            final Spinner sp_c = new Spinner(Expense.this);
            sp_c.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            sp_c.setAdapter(adp_c);
            sp_c.setSelection(edit_category);
            sp_c.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Selected_category = c[position];
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });


            AlertDialog.Builder builder = new AlertDialog.Builder(Expense.this);

            LinearLayout ll = new LinearLayout(this);
            ll.setOrientation(LinearLayout.VERTICAL);
            ll.addView(name);
            ll.addView(cost);
            ll.addView(sp_m);
            ll.addView(sp_c);


            builder.setView(ll);
            builder.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    String nam = name.getText().toString();
                    String cos = cost.getText().toString();


                    nam = nam.replace(" ", "_");
                    SharedPreferences sharedpref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
                    String shared_name = sharedpref.getString("username", "");
                    String passwd = sharedpref.getString("password", "");

                    String sURL = myURL + "/" + shared_name +":" + passwd+ "/expense/items/" + idee + ":" + nam + ":" + Selected_month + ":" + cos + ":" + Selected_category;



                    sqealee.update_Values(new Expense_item(idee, nam,cos,date_from_monthstring(Selected_month), Selected_category, "0", String.valueOf(System.currentTimeMillis() / 1000L), "", ""));

                    repopulate(null);
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.create().show();
            repopulate(null);
        }
    }


    private void get_totals(){
        try {
            ArrayList<Expense_item> myarray = sqealee.get_totals();

            if (!myarray.isEmpty()) {
                //Toast.makeText(this, "value found", Toast.LENGTH_SHORT).show();
                for (Expense_item value : myarray) {
                    my_expenselist.add(value);
                        value.name = value.name.replace("_", " ");
                        mylist.add(value);
                }

                adapter.notifyDataSetChanged();
                limit.setText("");
                heading.setText(String.valueOf("MONTH TOTALS"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }



    }


    private void get_categories(String selected_category, int  batch){
        try {
            ArrayList<Expense_item> myarray = sqealee.get_category(selected_category, batch);

            if (!myarray.isEmpty()) {
                //Toast.makeText(this, "value found", Toast.LENGTH_SHORT).show();
                for (Expense_item value : myarray) {
                    my_expenselist.add(value);
                    value.name = value.name.replace("_", " ");
                    mylist.add(value);
                }
                int lim = -1;
                adapter.notifyDataSetChanged();
                try {
                    lim = Integer.parseInt(sqealee.getlimit()) - Integer.parseInt(sqealee.getmonthvalue());

                }catch (Exception e){
                    e.printStackTrace();
                }
                if (lim == -1)
                    lim = Integer.parseInt(sqealee.getlimit());
                limit.setText(String.format("LIMIT: %s", lim));
                heading.setText(String.format("MONTH TOTAL: %s", sqealee.getmonthvalue()));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void get_items(int  batch) {

        try {
            ArrayList<Expense_item> myarray = sqealee.get_batch_array(batch);

            if (!myarray.isEmpty()) {
                //Toast.makeText(this, "value found", Toast.LENGTH_SHORT).show();
                for (Expense_item value : myarray) {
                    my_expenselist.add(value);
                    if (!value.deleted.equals("1")) {
                        value.name = value.name.replace("_", " ");
                        mylist.add(value);
                    }
                }

                int lim = -1;
                adapter.notifyDataSetChanged();
                try {
                    lim = Integer.parseInt(sqealee.getlimit()) - Integer.parseInt(sqealee.getmonthvalue());

                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (lim == -1)
                    lim = Integer.parseInt(sqealee.getlimit());
                limit.setText(String.format("LIMIT: %s", lim));
                heading.setText(String.format("MONTH TOTAL: %s", sqealee.getmonthvalue()));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private String date_from_monthstring(String a) {
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

        return (yearstamp + month);

    }
}
