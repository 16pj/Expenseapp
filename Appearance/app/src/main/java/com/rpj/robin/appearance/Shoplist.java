package com.rpj.robin.appearance;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class Shoplist extends AppCompatActivity {

    private ArrayList<String> mylist;
    private ArrayList<String> selecteditems;
    private ArrayAdapter<String> adapter;
    private int i;
    private ListView listView;
    private EditText editText;
    Sqealer sqealee;
    String myURL = "http://192.168.1.11:35741";

   // String myURL = "http://rojo16.pythonanywhere.com";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shoplist);

        listView = (ListView) findViewById(R.id.listview);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        selecteditems = new ArrayList<>();
        sqealee = new Sqealer(this, null, null, 1);
        mylist = new ArrayList<>();
        editText = (EditText) findViewById(R.id.editText);

        adapter = new ArrayAdapter<String>(this, R.layout.list_items, R.id.checkedview, mylist);
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

            }
        });
        repopulate(null);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(!mylist.isEmpty()){
            sqealee.truncater();
            sqealee.addValues(mylist);
        }

    }

    public void repopulate(View view){
        SharedPreferences sharedpref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String name = sharedpref.getString("username", "");

        String sURL = myURL + "/" + name+ "/shoplist/items";
        mylist.clear();
        adapter.notifyDataSetChanged();
        new Shop_GetUrlContentTask().execute(sURL, "SHOW");
        adapter.notifyDataSetChanged();
    }


    public void onAdd(View view){
        String thing = editText.getText().toString();

        if (!thing.equals("")) {
            SharedPreferences sharedpref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
            String name = sharedpref.getString("username", "");
            thing = thing.replace(" ", "_");
            String sURL = myURL + "/" + name+ "/shoplist/items/" + "_"+thing;
            //mylist.add(thing);
            //adapter.notifyDataSetChanged();
            new Shop_GetUrlContentTask().execute(sURL, "ADD");
            editText.setText("");
            repopulate(null);
            listView.clearChoices();
        }
    }

    public void onRemove(View view){

        if(mylist.size() == 0) return;
        SharedPreferences sharedpref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String name = sharedpref.getString("username", "");
        for (String item:selecteditems) {
            if (item.startsWith("*"))
                item = item.substring(1);
            String thing = item.replace(" ", "_");
            //mylist.remove(item);
            String sURL = myURL + "/" + name+ "/shoplist/items/" + thing;
            new Shop_GetUrlContentTask().execute(sURL, "DELETE");
        }
        repopulate(null);
        listView.clearChoices();
        selecteditems.clear();

    }

    public void onPriority(View view){

        SharedPreferences sharedpref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String name = sharedpref.getString("username", "");

        for (String item:selecteditems){
            if (item.startsWith("*")) {
                mylist.remove(item);
                mylist.add(item.substring(1));
                String thing = item.replace(" ", "_");
                String sURL = myURL + "/" + name + "/shoplist/unprioritize/" + thing.substring(1);
                new Shop_GetUrlContentTask().execute(sURL, "PRIORITY");
            }
            else {
                mylist.remove(item);
                mylist.add("*" + item);
                String thing = item.replace(" ", "_");
                String sURL = myURL + "/" + name + "/shoplist/prioritize/" + thing;
                new Shop_GetUrlContentTask().execute(sURL, "PRIORITY");
            }}
        listView.clearChoices();
        selecteditems.clear();
        repopulate(null);

    }

    public void onCost(View view) {
        ;
    }


    private class Shop_GetUrlContentTask extends AsyncTask<String, String, String> {
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

            else if (params[1].equals("PRIORITY")){

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
            /*progress[0] = progress[0].replace("_", " ");
            mylist.add(progress[0]);
            adapter.notifyDataSetChanged();
        */}

        protected void onPostExecute(String result) {
            String item;
            try {
                JSONArray jsonArray = new JSONArray(result);
                for (i =0; i< jsonArray.length(); i++) {
                    if (jsonArray.getJSONObject(i).getString("priority").equals("YES")) {
                        item = "*" + jsonArray.getJSONObject(i).getString("name");
                        item = item.replace("_", " ");
                        mylist.add(item);
                    } else {
                        item = jsonArray.getJSONObject(i).getString("name");
                        item = item.replace("_", " ");
                        mylist.add(item);

                    }
                }
                adapter.notifyDataSetChanged();
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

}
