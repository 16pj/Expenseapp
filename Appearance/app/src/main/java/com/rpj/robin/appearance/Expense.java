package com.rpj.robin.appearance;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

    private ArrayList<String> mylist;
    private ArrayList<String> selecteditems;

    private ArrayList<String> itemnames;
    private ArrayList<String> itemdates;
    private ArrayList<String> itemcosts;
    private ArrayList<String> itemcategory;

    private TextView limit;


    private ArrayAdapter<String> adapter;
    private ListView listView;
    private EditText editName;
    private EditText editNum;
    private static String lim;
    private static String Str1;
    private static String Str2;
    private static String temp;
    private static int i;
    private static int j;
    private String m_Text = "";

    //private String myURL = "http://192.168.1.25:35741";
    String myURL = "http://rojo16.pythonanywhere.com";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);

        listView = (ListView) findViewById(R.id.listView);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        selecteditems = new ArrayList<>();
        mylist = new ArrayList<>();
        itemnames = new ArrayList<>();
        itemdates = new ArrayList<>();
        itemcosts = new ArrayList<>();
        itemcategory = new ArrayList<>();
        limit = (TextView) findViewById(R.id.limit);


        editName = (EditText) findViewById(R.id.name);
        editNum = (EditText) findViewById(R.id.num);

        adapter = new ArrayAdapter<String>(this, R.layout.expense_items, R.id.checkedview, mylist);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = ((TextView) view).getText().toString();

                if (selecteditems.contains(item)){
                    selecteditems.remove(item);
                }
                else {
                    selecteditems.add(item);
                }
                // }catch (Exception e){
                //   e.printStackTrace();
                //}

            }
        });


        repopulate(null);

    }

    public void repopulate(View view){
        SharedPreferences sharedpref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String name = sharedpref.getString("username", "");

        String sURL = myURL + "/" + name + "/expense/items";
        mylist.clear();
        itemnames.clear();
        itemcosts.clear();
        itemdates.clear();
        adapter.notifyDataSetChanged();
        new Expense_GetUrlContentTask().execute(sURL, "SHOW");
        adapter.notifyDataSetChanged();

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
        String num = "";
        num = editNum.getText().toString();
        //num = Integer.parseInt(editNum.getText().toString());

        if (!name.equals("") && !num.equals("")) {

            // String newstring = new SimpleDateFormat("yyyy-M").format(new Date());
            //   System.out.println(newstring);
            //  newstring = newstring.replace("-","");
            // itemnames.add(name);
            // itemdates.add(Integer.parseInt(newstring));
            //itemcosts.add(num);

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

        for (String item:selecteditems) {
            String thing = itemnames.get(selecteditems.indexOf(item));
            //int date = itemdates.get(item);
            thing = thing.replace(" ", "_");
            String sURL = myURL+"/" + shared_name + "/expense/items/" + thing + "/" + itemdates.get(selecteditems.indexOf(item)) + "/0";
            new Expense_GetUrlContentTask().execute(sURL, "DELETE");
            mylist.remove(item);
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

    public void onCost(View view) {
        ;
    }



    private class Expense_GetUrlContentTask extends AsyncTask<String, String, String> {
        protected String doInBackground(String... params) {

            if(params[1].equals("ADD")) {

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
                }}

            else if (params[1].equals("DELETE")){

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
                }}

            else if (params[1].equals("LIMIT")){

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
                }}

            else if (params[1].equals("SHOW")) {
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


            /*    try {
                    JSONArray jsonArray = new JSONArray(progress[0]);

                    for (i =0; i< jsonArray.length(); i++) {
                        Str1 = jsonArray.getJSONObject(i).getString("name");
                        Str2 = jsonArray.getJSONObject(i).getString("cost");

                        String temp = Str1 + "";
                        for (j = Str2.length(); i < 50 - Str1.length(); i++) {
                            temp += " ";
                        }

                        mylist.add(temp + Str2 + " SEK");
                        itemnames.add(Str1);
                        itemcosts.add(Str2);
                        itemdates.add(jsonArray.getJSONObject(i).getString("date"));
                        itemcategory.add(jsonArray.getJSONObject(i).getString("category"));
                        lim = jsonArray.getJSONObject(i).getString("limit");
                    }
                    limit.setText(lim);
                    adapter.notifyDataSetChanged();
                }catch (Exception e){
                    e.printStackTrace();
                }

*/





           /*     progress[0] = progress[0].replace("_", " ");
                splitted = progress[0].split(":");
                String temp = splitted[0] + "";
                for (int i = splitted[1].length(); i < 50 - splitted[0].length(); i++) {
                    temp += " ";
                }

                mylist.add(temp + splitted[1] + " SEK");
                adapter.notifyDataSetChanged();
                itemnames.add(splitted[0]);
                itemcosts.add(splitted[1]);
                itemdates.add(splitted[2]);
                String lim = "MONTH LIMIT: " + splitted[4];
                limit.setText(lim);
*/
        }


        protected void onPostExecute(String result) {

            try {
                JSONArray jsonArray = new JSONArray(result);
                limit.setText(String.format("MONTH LIMIT: %s", jsonArray.getJSONObject(jsonArray.length()-1).getString("limit")));

                for (i =0; i< jsonArray.length(); i++) {
                    temp = "";
                    for (int k =jsonArray.getJSONObject(i).getString("cost").length(); k <55 - jsonArray.getJSONObject(i).getString("name").length();k++){
                        temp = temp + " ";
                    }

                    mylist.add(jsonArray.getJSONObject(i).getString("name") + temp + jsonArray.getJSONObject(i).getString("cost") + " SEK");
                    itemnames.add(jsonArray.getJSONObject(i).getString("name"));
                    itemcosts.add(jsonArray.getJSONObject(i).getString("cost"));
                    itemdates.add(jsonArray.getJSONObject(i).getString("date"));
                    itemcategory.add(jsonArray.getJSONObject(i).getString("category"));
                }
                //limit.setText(String.format("MONTH LIMIT: %s", jsonArray.getJSONObject(0).getString("limit")));
                adapter.notifyDataSetChanged();
            }catch (Exception e){
                e.printStackTrace();
            }


        }
    }



}
