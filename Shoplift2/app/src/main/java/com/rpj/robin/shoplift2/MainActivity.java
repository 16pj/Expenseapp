package com.rpj.robin.shoplift2;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText editTxt;
    private ListView list;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> arrayList;
    String myURL = "http://192.168.1.25:3000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTxt = (EditText) findViewById(R.id.editText);
        list = (ListView) findViewById(R.id.listView);
        arrayList = new ArrayList<String>();

        // Adapter: You need three parameters 'the context, id of the layout (it will be where the data is shown),
        // and the array that contains the data
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, arrayList);

        // Here, you set the data in your ListView
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String som = adapter.getItem(position);
                editTxt.setText(som);
            }
        });

    }

    public void clickDelete(View view) {
        String thing = editTxt.getText().toString();
        String sURL = myURL + "/shoplist/items/" + thing;

        if (!editTxt.getText().toString().equals("")) {
            adapter.remove(thing);
            Toast.makeText(MainActivity.this, "Deleted " + thing, Toast.LENGTH_SHORT).show();
            new GetUrlContentTask().execute(sURL, "DELETE");
            editTxt.setText("");
        }
    }

    public void clickAdd(View view) {
        String thing = editTxt.getText().toString();

        if (!thing.equals("")) {
            String sURL = myURL + "/shoplist/items/" + thing;
            arrayList.add(thing);
            adapter.notifyDataSetChanged();
            new GetUrlContentTask().execute(sURL, "ADD");
            editTxt.setText("");

        } else {
            arrayList.clear();
            adapter.notifyDataSetChanged();
        }
    }


    public void clickRefresh(View view) {
        String sURL = myURL + "/shoplist/items";
        new GetUrlContentTask().execute(sURL, "SHOW");

        arrayList.clear();
        adapter.notifyDataSetChanged();
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
            arrayList.add(progress[0]);
            adapter.notifyDataSetChanged();
        }

        protected void onPostExecute(String result) {
        }
    }

}


