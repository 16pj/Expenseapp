package com.rpj.robin.testlist;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> mylist;
    private ArrayList<String> selecteditems;
    private ArrayAdapter<String> adapter;
    private ListView listView;
    private EditText editText;
    String myURL = "http://192.168.1.25:3000";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.listview);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        selecteditems = new ArrayList<>();
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

    public void repopulate(View view){
        String sURL = myURL + "/shoplist/items";
        mylist.clear();
        adapter.notifyDataSetChanged();
        new GetUrlContentTask().execute(sURL, "SHOW");
        adapter.notifyDataSetChanged();
    }


    public void onAdd(View view){
        String thing = editText.getText().toString();

        if (!thing.equals("")) {
            thing = thing.replace(" ", "_");
            String sURL = myURL + "/shoplist/items/" + thing;
            //mylist.add(thing);
            //adapter.notifyDataSetChanged();
            new GetUrlContentTask().execute(sURL, "ADD");
            editText.setText("");
            repopulate(null);
            listView.clearChoices();
    }
    }

    public void onRemove(View view){

        if(mylist.size() == 0) return;
        for (String item:selecteditems) {
            String thing = item.replace(" ", "_");
            //mylist.remove(item);
            String sURL = myURL + "/shoplist/items/" + thing;
            new GetUrlContentTask().execute(sURL, "DELETE");
            editText.setText("");
        }
        repopulate(null);
        listView.clearChoices();
        selecteditems.clear();

    }

    public void onPriority(View view){
        for (String item:selecteditems){
            if (item.startsWith("*")){
                mylist.remove(item);
                item = item.replace("*","");
                mylist.add(item);
            }
            else {
            mylist.remove(item);
            item = "*" + item;
            mylist.add(item);}
            adapter.notifyDataSetChanged();
        }
        selecteditems.clear();
        listView.clearChoices();

    }

    public void onCost(View view) {
        ;
    }


    private class GetUrlContentTask extends AsyncTask<String, String, String> {
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

            else if (params[1].equals("SHOW")) {
                try {
                    URL url = new URL(params[0]);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    // connection.setRequestMethod("GET");
                    connection.setDoOutput(true);
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
            progress[0] = progress[0].replace("_", " ");
            mylist.add(progress[0]);
            adapter.notifyDataSetChanged();
        }

        protected void onPostExecute(String result) {
        }
    }



}
