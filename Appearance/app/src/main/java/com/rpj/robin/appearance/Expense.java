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
    private ArrayList<Expense_item> client_sync_list;
    private ArrayList<Expense_item> server_sync_list;
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
    private String [] hashbrown = new String[2];


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
        client_sync_list = new ArrayList<>();
        server_sync_list = new ArrayList<>();
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
        cclean();
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

                return true;

            default:
                return true;
        }
    }


    public void repopulate(View view){

        mylist.clear();
        my_expenselist.clear();
        selecteditems.clear();
        client_sync_list.clear();
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
        check_hash(0);
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
               // Toast.makeText(this, "value found", Toast.LENGTH_SHORT).show();
                for (Expense_item value : myarray) {
                    my_expenselist.add(value);
                    client_sync_list.add(value);
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
    
    
    
    
    //////////////////////////////////SYNC




    public void check_hash(int batch) {

        String sURL = myURL + "/" + login_name + ":" + login_pass + "/expense/hashbrown/" + String.valueOf(batch);
        String message = "CHECK_HASH" + ":" +  String.valueOf(batch);
        new Expense_Get_hash_ContentTask().execute(sURL,"HASH", message);
    }


    private class Expense_Get_hash_ContentTask extends AsyncTask<String, String, String> {
        private String whenceforth = "";
        private String batch = "0";
        protected String doInBackground(String... params) {

            whenceforth = params[2].split(":")[0];
            try {
                batch = params[2].split(":")[1];
            }
            catch (Exception e){
                e.printStackTrace();
            }
            switch (params[1]) {

                case ("HASH") :
                    try {
                        URL url = new URL(params[0]);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setConnectTimeout(5000);
                        connection.setReadTimeout(5000);
                        connection.connect();
                        BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String content = "", line;
                        while ((line = rd.readLine()) != null) {
                            content += line + "\n";
                        }
                        return content;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;

            }

            return null;
        }

        protected void onProgressUpdate(String... progress) {}

        protected void onPostExecute(String  result) {

            try {
                JSONArray jsonArray = new JSONArray(result);
                for (int i =0; i< jsonArray.length(); i++) {
                    hashbrown[0] = jsonArray.getJSONObject(i).getString("s_id");
                    hashbrown[1] = jsonArray.getJSONObject(i).getString("s_modified");
                }
                Toast.makeText(Expense.this, "Server hash is " + hashbrown[0] + ", " + hashbrown[1] + " for batch: " + batch, Toast.LENGTH_SHORT).show();

                if(whenceforth.equals("CHECK_HASH")) {

                    //CHECK HASH
                    server_sync_list.clear();
                    String[] hashmash = sqealee.expenselisthashbrown(Integer.parseInt(batch)).split(":");

                    boolean x = hashmash[0].equals(hashbrown[0]);
                    boolean y = hashmash[1].equals(hashbrown[1]);

                    Toast.makeText(Expense.this, String.format("local hash is  %s , %s for batch: %s", hashmash[0],hashmash[1], batch), Toast.LENGTH_SHORT).show();

                    if (!x || !y) {

                        Toast.makeText(Expense.this, "Sync required", Toast.LENGTH_SHORT).show();
                        String sURL = myURL + "/" + login_name + ":" + login_pass + "/expense/get_batch/" + batch ;
                        String message = "CHECK_HASH" + ":" +  batch;
                        new Expense_GetUrlContentTask().execute(sURL, "SHOW", message);
                    }

                    //END CHECK HASH
                }
                else if(whenceforth.equals("SECOND_STAGE")) {

                    client_sync_list.clear();
                    server_sync_list.clear();

                    ArrayList<Expense_item> valuemash = sqealee.getArray2();

                    if(!valuemash.isEmpty()) {
                        for (Expense_item value : valuemash) {

                            client_sync_list.add(value);
                        }
                    }
                        String message = "SECOND_STAGE" + ":" +  batch;
                        String sURL = myURL + "/" + login_name + ":" + login_pass + "/expense/get_batch/" + batch ;
                        new Expense_GetUrlContentTask().execute(sURL, "SHOW", message);


                }

            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }


    private class Expense_GetUrlContentTask extends AsyncTask<String, String, String> {
        private String whenceforth = "";
        private String batch = "0";
        protected String doInBackground(String... params) {

            whenceforth = params[2].split(":")[0];
            try {
                batch = params[2].split(":")[1];
            }catch (Exception e){
                e.printStackTrace();
            }
            switch (params[1]) {

                case ("ADD"):
                    try {
                        URL url = new URL(params[0]);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("POST");
                        connection.setDoOutput(true);
                        connection.setConnectTimeout(5000);
                        connection.setReadTimeout(5000);
                        connection.connect();
                        BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String content = "", line;
                        while ((line = rd.readLine()) != null) {
                            content += line + "\n";
                        }
                        return content;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case ("CLEANUP"):
                    try {
                        URL url = new URL(params[0]);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("DELETE");
                        connection.setDoOutput(true);
                        connection.setConnectTimeout(5000);
                        connection.setReadTimeout(5000);
                        connection.connect();
                        BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String content = "", line;
                        while ((line = rd.readLine()) != null) {
                            content += line + "\n";
                        }
                        return content;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case ("UPDATE"):

                    try {
                        URL url = new URL(params[0]);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("PUT");
                        connection.setDoOutput(true);
                        connection.setConnectTimeout(5000);
                        connection.setReadTimeout(5000);
                        connection.connect();
                        BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String content = "", line;
                        while ((line = rd.readLine()) != null) {
                            content += line + "\n";
                        }
                        return content;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case ("SHOW"):
                    try {
                        URL url = new URL(params[0]);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        //connection.setDoOutput(true);
                        connection.setConnectTimeout(5000);
                        connection.setReadTimeout(5000);
                        connection.connect();
                        BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String content = "", line;
                        while ((line = rd.readLine()) != null) {
                            content += line + "\n";
                            publishProgress(line);
                        }
                        return content;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }

            return null;
        }

        protected void onProgressUpdate(String... progress) {
        }

        protected void onPostExecute(String result) {

            if(whenceforth.equals("CHECK_HASH")) {
                try {
                    String clid = "";
                    JSONArray jsonArray = new JSONArray(result);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            clid = jsonArray.getJSONObject(i).getString("client_id");
                            // Toast.makeText(Shoplist.this, "Client id is " + clid, Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (clid.isEmpty() || clid.equals("") || clid.equals("null"))
                            clid = "-1";
                        //Toast.makeText(Expense.this, "final Client id is " + clid, Toast.LENGTH_SHORT).show();
                        if(!jsonArray.getJSONObject(i).getString("name").equals("Server Empty"))
                        server_sync_list.add(new Expense_item(clid, jsonArray.getJSONObject(i).getString("name"), jsonArray.getJSONObject(i).getString("cost"), jsonArray.getJSONObject(i).getString("date"), jsonArray.getJSONObject(i).getString("category"),jsonArray.getJSONObject(i).getString("deleted"),jsonArray.getJSONObject(i).getString("modified"),jsonArray.getJSONObject(i).getString("tag"), jsonArray.getJSONObject(i).getString("id")));
                    }

                   //Toast.makeText(Expense.this, client_sync_list.get(0).name, Toast.LENGTH_SHORT).show();
                    if (client_sync_list.isEmpty() && server_sync_list.isEmpty())
                        Toast.makeText(Expense.this, "Clientlist and serverlist are empty", Toast.LENGTH_SHORT).show();


                    else
                        compare_presence(client_sync_list, server_sync_list, Integer.parseInt(batch));
                    //Toast.makeText(Expense.this, "Compare presence", Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            else if(whenceforth.equals("SECOND_STAGE")) {

                try {
                    String clid = "";
                    JSONArray jsonArray = new JSONArray(result);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            clid = jsonArray.getJSONObject(i).getString("client_id");
                            // Toast.makeText(Shoplist.this, "Client id is " + clid, Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (clid.isEmpty() || clid.equals("") || clid.equals("null"))
                            clid = "-1";
                        //Toast.makeText(Shoplist.this, "final Client id is " + clid, Toast.LENGTH_SHORT).show();
                        server_sync_list.add(new Expense_item(clid, jsonArray.getJSONObject(i).getString("name"), jsonArray.getJSONObject(i).getString("cost"), jsonArray.getJSONObject(i).getString("date"), jsonArray.getJSONObject(i).getString("category"),jsonArray.getJSONObject(i).getString("deleted"),jsonArray.getJSONObject(i).getString("modified"),jsonArray.getJSONObject(i).getString("tag"), jsonArray.getJSONObject(i).getString("id")));
                    }

                    if (!client_sync_list.isEmpty() && !server_sync_list.isEmpty())
                        compare_updates(client_sync_list, server_sync_list);
                    else
                        Toast.makeText(Expense.this, "Clientlist or serverlist is empty", Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    public void compare_presence(ArrayList<Expense_item> client, ArrayList<Expense_item> server, int batch){

        ArrayList<String> client_stuff = new ArrayList<>();
        ArrayList<String> server_stuff = new ArrayList<>();

        if(!client.isEmpty()){
        for (Expense_item item:client) {

            if (item.serve_id.equals("")) {
                    client_stuff.add(item.name);
                update_server_item(item, "ADD");
                Toast.makeText(this, "Server gets " + item.client_id + "," + item.name + ", " + item.cost + ", " + item.deleted + ", " + item.modified + ", " + item.serve_id, Toast.LENGTH_SHORT).show();
            }
        }}
        if(!server.isEmpty()){
        for (Expense_item item:server){

            if(item.client_id.equals("-1")){
                    server_stuff.add(item.name);
                update_client_item(item, "ADD");
                Toast.makeText(this, "Client gets " + item.serve_id + "," + item.name + ", " + item.cost + ", " + item.deleted + ", " + item.modified + ", " + item.serve_id, Toast.LENGTH_SHORT).show();
            }
        }}

        second_stage(batch);

        String x = String.valueOf(client_stuff.size()) + " , " +  String.valueOf(server_stuff.size());
        //Toast.makeText(this,x , Toast.LENGTH_SHORT).show();
    }


    public void second_stage(int batch) {
        Toast.makeText(this, "Second Stage", Toast.LENGTH_SHORT).show();
        String sURL = myURL + "/" + login_name + ":" + login_pass + "/expense/hashbrown/" + String.valueOf(batch);
        String message = "SECOND_STAGE" + ":" +  String.valueOf(batch);
        new Expense_Get_hash_ContentTask().execute(sURL, "HASH", message);
    }

    public void compare_updates(ArrayList<Expense_item> client, ArrayList<Expense_item> server){

        Toast.makeText(this, "compare updates", Toast.LENGTH_SHORT).show();
        for (int i =0; i <client.size(); i++){
            for(int j =0; j < server.size(); j++){
                if(client.get(i).name.equals(server.get(j).name) && client.get(i).tag.equals(server.get(j).tag)) {
                     Toast.makeText(this, "match found", Toast.LENGTH_SHORT).show();

                    if(client.get(i).serve_id.equals("")) {
                        Toast.makeText(this, "client serve_id blank", Toast.LENGTH_SHORT).show();
                        Expense_item temp = client.get(i);
                        temp.serve_id = server.get(j).serve_id;
                        update_client_item(temp,"EDIT");
                    }
                    else if (server.get(j).client_id.equals("-1")){
                        Toast.makeText(this, "server client_id blank", Toast.LENGTH_SHORT).show();
                        Expense_item temp = server.get(j);
                        temp.client_id = client.get(i).client_id;
                        update_server_item(temp, "EDIT");
                    }

                    else if (Integer.parseInt(client.get(i).modified) > Integer.parseInt(server.get(j).modified)) {
                        Expense_item temp = client.get(i);
                        temp.serve_id = server.get(j).serve_id;
                        update_server_item(temp, "EDIT");
                        Toast.makeText(this, "Server updates " + temp.client_id + "," + temp.name + ", " + temp.cost + ", " + temp.deleted + ", " + temp.modified + ", " + temp.serve_id, Toast.LENGTH_SHORT).show();
                    }

                    else if (Integer.parseInt(client.get(i).modified) < Integer.parseInt(server.get(j).modified)) {
                        Expense_item temp = server.get(j);
                        temp.client_id = client.get(i).client_id;
                        update_client_item(temp, "EDIT");
                        Toast.makeText(this, "Client updates " + temp.client_id + "," + temp.name + ", " + temp.cost + ", " + temp.deleted + ", " + temp.modified + ", " + temp.serve_id, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

    }


    public void update_server_item(Expense_item item, String type){


        if(type.equals("ADD")) {
            String sURL = myURL + "/" + login_name + ":" + login_pass + "/expense/add_item/" + item.name + "/" + item.cost + "/" + item.category  +"/" + item.date + "/" + item.deleted + "/" + item.modified  + "/"+ item.tag + "/" + item.client_id;
            new Expense_GetUrlContentTask().execute(sURL, "ADD", "NO_RETURN");
        }
        else if(type.equals("EDIT")){
            String sURL = myURL + "/" + login_name + ":" + login_pass + "/expense/sync_item/" + item.serve_id + "/" + item.name + "/" + item.cost + "/" + item.category  + "/" + item.date + "/" + item.deleted + "/" + item.modified + "/" + item.client_id;
            new Expense_GetUrlContentTask().execute(sURL, "UPDATE", "NO_RETURN");
        }
    }

    public void update_client_item(Expense_item item, String type){
        sqealee.update_Values(item);
        if(type.equals("ADD")) {
            sqealee.addValue(item);

        }
        else if(type.equals("EDIT")){
            sqealee.sync_Values(item);
        }
    }


    public void cclean(){
        sqealee.cleanup();
        String sURL = myURL + "/" + login_name + ":" + login_pass + "/expense/cleanup";
        new Expense_GetUrlContentTask().execute(sURL, "CLEANUP", "NO_RETURN:");
    }
}
