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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Expense extends AppCompatActivity {

    private ArrayList<Expense_item> mylist;
    private ArrayList<Expense_item> selecteditems;
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


    private String myURL = myconf.global_url;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);

        listView = (ListView) findViewById(R.id.listView);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        selecteditems = new ArrayList<>();
        mylist = new ArrayList<>();
        limit = (TextView) findViewById(R.id.limit);
        batch = 1;
        heading = (TextView) findViewById(R.id.heading);
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

                onEdit(selected_expense.id, selected_expense.name, selected_expense.cost.substring(0, selected_expense.cost.length()-4), mon_pos, cat_pos);

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
                    Toast.makeText(this, "Monthly Totals", Toast.LENGTH_SHORT).show();

                    SharedPreferences sharedpref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
                    String name = sharedpref.getString("username", "");
                    String passwd = sharedpref.getString("password", "");

                    String sURL = myURL + "/" + name +":" + passwd+ "/expense/totals";
                    mylist.clear();
                    adapter.notifyDataSetChanged();
                    new Expense_GetUrlContentTask().execute(sURL, "SHOW");
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

                            SharedPreferences sharedpref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
                            String name = sharedpref.getString("username", "");
                            String passwd = sharedpref.getString("password", "");

                            String sURL = myURL + "/" + name +":" + passwd+ "/expense/batch_cat/" + Selected_category_total +  "/0";
                            mylist.clear();
                            adapter.notifyDataSetChanged();
                            batch=1;
                            new Expense_GetUrlContentTask().execute(sURL, "SHOW");

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
        SharedPreferences sharedpref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String name = sharedpref.getString("username", "");
        String passwd = sharedpref.getString("password", "");

        String sURL = myURL + "/" + name +":" + passwd+  "/expense/batch/0";
        mylist.clear();
        adapter.notifyDataSetChanged();
        listView.clearChoices();
        batch=1;
        TOTAL_FLAG="FALSE";
        CATEGOY_FLAG = "FALSE";
        new Expense_GetUrlContentTask().execute(sURL, "SHOW");

    }

    public void overpopulate(View view){

        if(TOTAL_FLAG.equals("FALSE")) {
            SharedPreferences sharedpref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
            String name = sharedpref.getString("username", "");
            String passwd = sharedpref.getString("password", "");

            String sURL;
            if(CATEGOY_FLAG.equals("FALSE"))
                sURL = myURL + "/" + name +":" + passwd+ "/expense/batch/" + batch;
            else
                sURL = myURL + "/" + name +":" + passwd+ "/expense/batch_cat/" + Selected_category_total+ "/" + batch;

            new Expense_GetUrlContentTask().execute(sURL, "SHOW");
            batch += 1;
            listView.clearChoices();
        }
    }


    public void set_limit(View view){
        SharedPreferences sharedpref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String name = sharedpref.getString("username", "");
        String passwd = sharedpref.getString("password", "");


        String sURL = myURL +"/" + name+":" + passwd+ "/expense/limit/" + m_Text;
        new Expense_GetUrlContentTask().execute(sURL, "LIMIT");
        Toast.makeText(this, "Limit set to " + m_Text, Toast.LENGTH_SHORT).show();
    }

    public void onAdd(View view){

        if(TOTAL_FLAG.equals("FALSE")) {
            String name = editName.getText().toString();
            String num = editNum.getText().toString();

            if (!name.equals("") && !num.equals("")) {
                Toast.makeText(this, name, Toast.LENGTH_SHORT).show();
                Toast.makeText(this, num, Toast.LENGTH_SHORT).show();
                name = name.replace(" ", "_");
                SharedPreferences sharedpref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
                String shared_name = sharedpref.getString("username", "");
                String passwd = sharedpref.getString("password", "");


                String sURL = myURL + "/" + shared_name + ":" + passwd+ "/expense/items/" + name + "/" + num + "/DEFAULT";
                new Expense_GetUrlContentTask().execute(sURL, "ADD");
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

            SharedPreferences sharedpref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
            String shared_name = sharedpref.getString("username", "");
            String passwd = sharedpref.getString("password", "");

            for (Expense_item item : selecteditems) {
                String thing = item.name;
                thing = thing.replace(" ", "_");
                String sURL = myURL + "/" + shared_name +":" + passwd+ "/expense/items/" + thing + "/" + item.date + "/" + item.cost.replace(" SEK", "");

                new Expense_GetUrlContentTask().execute(sURL, "DELETE");
            }
            listView.clearChoices();
            selecteditems.clear();
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
                    set_limit(null);
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
                SharedPreferences sharedpref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
                String shared_name = sharedpref.getString("username", "");
                String passwd = sharedpref.getString("password", "");


                String sURL = myURL +"/" + shared_name+":" + passwd+ "/expense/items1/" + nam + ":" + Selected_month + ":" + cos + ":"+ Selected_category;
                new Expense_GetUrlContentTask().execute(sURL, "ADD");

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
                    new Expense_GetUrlContentTask().execute(sURL, "EDIT");

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


    private class Expense_GetUrlContentTask extends AsyncTask<String, String, String> {
        protected String doInBackground(String... params) {

            switch (params[1]) {

                case ("ADD") :
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
                case ("EDIT") :
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
                case ("DELETE"):
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
                case ("LIMIT"):
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
                case("SHOW"):

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
                            publishProgress(line);
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


        protected void onProgressUpdate(String... progress) {
        }


        protected void onPostExecute(String result) {
            if (result == null)
                return;

            if(TOTAL_FLAG.equals("FALSE")) {
                try {
                    String heading_text;
                    JSONArray jsonArray = new JSONArray(result);

                    if(CATEGOY_FLAG.equals("FALSE"))
                    limit.setText(String.format("LIMIT: %s", jsonArray.getJSONObject(jsonArray.length() - 1).getString("limit")));
                    else
                        limit.setText("");


                    try {
                        if(CATEGOY_FLAG.equals("FALSE"))
                            heading_text = "MON TOTAL: " + jsonArray.getJSONObject(jsonArray.length() - 1).getString("total") + " SEK";
                        else
                            heading_text = Selected_category + " TOTALS";

                        heading.setText(heading_text);

                        for (int i = 0; i < jsonArray.length(); i++) {
                            mylist.add(new Expense_item(jsonArray.getJSONObject(i).getString("id"), jsonArray.getJSONObject(i).getString("name").replace("_", " "), jsonArray.getJSONObject(i).getString("cost") + " SEK", jsonArray.getJSONObject(i).getString("date"), jsonArray.getJSONObject(i).getString("category")));
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    adapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {

                try{
                    limit.setText("");
                    heading.setText(R.string.month_totals);
                    JSONArray jsonArray = new JSONArray(result);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        mylist.add(new Expense_item("", "", jsonArray.getJSONObject(i).getString("cost") + " SEK", jsonArray.getJSONObject(i).getString("date"),""));
                    }

                    adapter.notifyDataSetChanged();

                }catch (Exception e){
                    e.printStackTrace();
                }

            }

        }
    }
}
