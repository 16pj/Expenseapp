package com.rpj.robin.appearance;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

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

    private String myURL = "http://192.168.1.11:35741";
    //String myURL = "http://rojo16.pythonanywhere.com";





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

        repopulate(null);


    }

    public void repopulate(View view){
        SharedPreferences sharedpref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String name = sharedpref.getString("username", "");

        String sURL = myURL + "/" + name + "/expense/batch/0";
        mylist.clear();
        batch=1;
        new Expense_GetUrlContentTask().execute(sURL, "SHOW");

    }

    public void overpopulate(View view){

        if(mylist.get(mylist.size()-1).name.equals(""))
        mylist.remove(mylist.size()-1);
        adapter.notifyDataSetChanged();
        SharedPreferences sharedpref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String name = sharedpref.getString("username", "");

        String sURL = myURL + "/" + name + "/expense/batch/" + batch;
        new Expense_GetUrlContentTask().execute(sURL, "SHOW");
        batch +=1;
    }


    public void set_limit(View view){
        SharedPreferences sharedpref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String name = sharedpref.getString("username", "");

        String sURL = myURL +"/" + name+ "/expense/limit/" + m_Text;
        new Expense_GetUrlContentTask().execute(sURL, "LIMIT");
        Toast.makeText(this, "Limit set to " + m_Text, Toast.LENGTH_SHORT).show();
    }

    public void onAdd(View view){
        String name = editName.getText().toString();
        String num = editNum.getText().toString();
        //num = Integer.parseInt(editNum.getText().toString());

        if (!name.equals("") && !num.equals("")) {

            name = name.replace(" ", "_");
            SharedPreferences sharedpref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
            String shared_name = sharedpref.getString("username", "");

            String sURL = myURL +"/" + shared_name+ "/expense/items/" + name + "/" + num + "/DEFAULT";
            new Expense_GetUrlContentTask().execute(sURL, "ADD");
            editName.setText("");
            editNum.setText("");
            repopulate(null);
            listView.clearChoices();
        }
    }

    public void onRemove(View view){

        if(mylist.size() == 0) return;

        SharedPreferences sharedpref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String shared_name = sharedpref.getString("username", "");

        for (Expense_item item:selecteditems) {
            String thing = item.name;
            thing = thing.replace(" ", "_");
            String sURL = myURL+"/" + shared_name + "/expense/items/" + thing + "/" + item.date + "/" + item.cost.replace(" SEK","");

            new Expense_GetUrlContentTask().execute(sURL, "DELETE");
        }
        listView.clearChoices();
        selecteditems.clear();
        repopulate(null);

    }

    public void onLimit(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Limit");

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

// Set up the buttons
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
                    break;
                default:
                    break;
                }
            return null;
        }


        protected void onProgressUpdate(String... progress) {
        }


        protected void onPostExecute(String result) {

            String test="0000";
            try {
                String heading_text;
                JSONArray jsonArray = new JSONArray(result);
                limit.setText(String.format("LIMIT: %s", jsonArray.getJSONObject(jsonArray.length()-1).getString("limit")));

                try {
                    heading_text = "MON TOTAL: " + jsonArray.getJSONObject(jsonArray.length() - 1).getString("total") + " SEK";
                    heading.setText(heading_text);

                for (int i =0; i< jsonArray.length(); i++) {
                    mylist.add(new Expense_item(jsonArray.getJSONObject(i).getString("name").replace("_", " "), jsonArray.getJSONObject(i).getString("cost") + " SEK",jsonArray.getJSONObject(i).getString("date") ));
                }
                    test = jsonArray.getJSONObject(jsonArray.length() - 1).getString("date");

                }catch (Exception e){
                    e.printStackTrace();
                }
                mylist.add(new Expense_item("","",test));
                adapter.notifyDataSetChanged();
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }
}
