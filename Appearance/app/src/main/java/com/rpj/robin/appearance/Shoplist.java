package com.rpj.robin.appearance;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class Shoplist extends AppCompatActivity {

    private ArrayList<Shoplist_item> my_shoplist;
    private ArrayList<Shoplist_item> client_sync_list;
    private ArrayList<Shoplist_item> server_sync_list;
    private ArrayList<String> mylist;
    private ArrayList<String> selecteditems;
    private ArrayAdapter<String> adapter;
    private ListView listView;
    private EditText editText;
    Sqealer2 sqealee2;
    private Shoplist_item shoplist_item;
    private String [] hashbrown = new String[2];
    private String login_name;
    private String login_pass;
    private int retry_time = 0;
    private int retry_number = 0;

    private String myURL = myconf.global_url;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shoplist);
        listView = (ListView) findViewById(R.id.listview);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        selecteditems = new ArrayList<>();
        mylist = new ArrayList<>();
        client_sync_list = new ArrayList<>();
        my_shoplist = new ArrayList<>();
        server_sync_list = new ArrayList<>();
        editText = (EditText) findViewById(R.id.editText);
        shoplist_item = new Shoplist_item("","","","","","","");

        SharedPreferences sharedpref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        login_name = sharedpref.getString("username", "");
        login_pass = sharedpref.getString("password", "");
        myconf.SHOPLIST_TABLE_NAME = login_name  + "_shoplist_table";
        sqealee2 = new Sqealer2(this, null, null, 1);

        cclean();
        adapter = new ArrayAdapter<>(this, R.layout.list_items, R.id.checkedview, mylist);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = ((TextView) view).getText().toString();

                if (selecteditems.contains(item)) {
                    selecteditems.remove(item);
                } else {
                    selecteditems.add(item);
                    //Toast.makeText(Shoplist.this, item, Toast.LENGTH_SHORT).show();
                }

            }
        });

        repopulate(null);
    }


    @Override
    public void onResume() {
        super.onResume();

        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("SYNC-REQUIRED"));
    }

    // handler for received Intents for the "my-event" event
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            String message = intent.getStringExtra("message");
            if(message.startsWith("SYNC-REQUIRED:")){

                check_hash();

            }
            Log.d("receiver", "Got message: " + message);
        }
    };

    @Override
    protected void onPause() {
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    @Override
protected void onDestroy() {
        super.onDestroy();
    }

public void repopulate(View view) {

        mylist.clear();
        my_shoplist.clear();
        client_sync_list.clear();
        selecteditems.clear();
        adapter.notifyDataSetChanged();
        listView.clearChoices();

    try {
        ArrayList<Shoplist_item> valuemash = sqealee2.getArray2();
        if(!valuemash.isEmpty()) {
            for (Shoplist_item value : valuemash) {

                client_sync_list.add(value);
                if (!value.deleted.equals("1")) {
                    value.name = value.name.replace("_", " ");

                    if (value.priority.equals("YES"))
                        mylist.add("*" + value.name);
                    else
                        mylist.add(value.name);
                    my_shoplist.add(value);
                }
            }
            adapter.notifyDataSetChanged();
        }

    }catch (Exception e){
        e.printStackTrace();
    }

           // check_hash();
           // second_stage();
       // Toast.makeText(this, hashmash[0] + "," + hashmash[1], Toast.LENGTH_SHORT).show();
    }


public void onAdd(View view) {
        String test = editText.getText().toString();
        if(!test.equals("")){
            String [] testarray = test.split(",");

            for(String value:testarray){

        shoplist_item.name = value.replace(" ", "_");
        shoplist_item.priority = "NO";
        shoplist_item.deleted = "0";
        shoplist_item.modified = String.valueOf((System.currentTimeMillis() / 1000));
        shoplist_item.tag = String.valueOf((System.currentTimeMillis() / 1000));
        shoplist_item.serve_id = "";

        sqealee2.addValue(shoplist_item);}
        editText.setText("");
        repopulate(null);}
    }

public void onRemove(View view) {
        if (mylist.size() == 0) return;

        for (String item : selecteditems) {
            sqealee2.deleteValues(my_shoplist.get(mylist.indexOf(item)));
        }
        repopulate(null);
       // selecteditems.clear();
    }


public void onPriority(View view) {

        for (String item : selecteditems) {
            if (item.startsWith("*")) {
                sqealee2.unprioritize(my_shoplist.get(mylist.indexOf(item)));
                //Toast.makeText(this, item + " UNPRIOR", Toast.LENGTH_SHORT).show();
            } else {
                sqealee2.prioritize(my_shoplist.get(mylist.indexOf(item)));
                //Toast.makeText(this, item + " PRIOR", Toast.LENGTH_SHORT).show();
            }
        }
        listView.clearChoices();
        selecteditems.clear();
        repopulate(null);

    }

public void check_hash() {

    Toast.makeText(Shoplist.this, "sync required", Toast.LENGTH_SHORT).show();
    client_sync_list.clear();
    server_sync_list.clear();

    ArrayList<Shoplist_item> valuemash = sqealee2.getArray2();

    if(!valuemash.isEmpty()) {
        for (Shoplist_item value : valuemash) {

            client_sync_list.add(value);
        }
    }
    String sURL = myURL + "/" + login_name + ":" + login_pass + "/shoplist/get_items";
    new Shop_GetUrlContentTask().execute(sURL, "SHOW", "SECOND_STAGE");

}

private class Shop_GetUrlContentTask extends AsyncTask<String, String, String> {
    private String whenceforth = "";
        protected String doInBackground(String... params) {

            whenceforth = params[2];
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

            /*if(whenceforth.equals("CHECK_HASH")) {
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
                        server_sync_list.add(new Shoplist_item(clid, jsonArray.getJSONObject(i).getString("name"), jsonArray.getJSONObject(i).getString("priority"), jsonArray.getJSONObject(i).getString("deleted"), jsonArray.getJSONObject(i).getString("modified"), jsonArray.getJSONObject(i).getString("id")));
                    }

                    if (!client_sync_list.isEmpty() || !server_sync_list.isEmpty())
                        compare_presence(client_sync_list, server_sync_list);
                    else
                        Toast.makeText(Shoplist.this, "Clientlist or serverlist is empty", Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            else */if(whenceforth.equals("SECOND_STAGE")) {

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
                            server_sync_list.add(new Shoplist_item(clid, jsonArray.getJSONObject(i).getString("name"), jsonArray.getJSONObject(i).getString("priority"), jsonArray.getJSONObject(i).getString("deleted"), jsonArray.getJSONObject(i).getString("modified"), jsonArray.getJSONObject(i).getString("tag") ,jsonArray.getJSONObject(i).getString("id")));
                    }

                    if (!client_sync_list.isEmpty() || !server_sync_list.isEmpty())
                        compare_updates(client_sync_list, server_sync_list);
                    else
                        Toast.makeText(Shoplist.this, "Clientlist or serverlist is empty", Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }
/*
public void compare_presence(ArrayList<Shoplist_item> client, ArrayList<Shoplist_item> server){

        ArrayList<String> client_stuff = new ArrayList<>();
        ArrayList<String> server_stuff = new ArrayList<>();

        for (Shoplist_item item:client) {

            if (item.serve_id.equals("")) {
               client_stuff.add(item.name);
                  //update_server_item(item, "ADD");
                Toast.makeText(this, "Server gets " + item.client_id + "," + item.name + ", " + item.priority + ", " + item.deleted + ", " + item.modified + ", " + item.serve_id, Toast.LENGTH_SHORT).show();
            }
        }

        for (Shoplist_item item:server){

            if(item.client_id.equals("-1")){
              server_stuff.add(item.name);
               // update_client_item(item, "ADD");
                Toast.makeText(this, "Client gets " + item.client_id + "," + item.name + ", " + item.priority + ", " + item.deleted + ", " + item.modified + ", " + item.serve_id, Toast.LENGTH_SHORT).show();
            }
        }
        second_stage();

        String x = String.valueOf(client_stuff.size()) + " , " +  String.valueOf(server_stuff.size());
        //Toast.makeText(this,x , Toast.LENGTH_SHORT).show();
}


public void second_stage() {
        Toast.makeText(this, "Second Stage", Toast.LENGTH_SHORT).show();
        String sURL = myURL + "/" + login_name + ":" + login_pass + "/shoplist/hashbrown";
        new Shop_Get_hash_ContentTask().execute(sURL, "HASH", "SECOND_STAGE");
    }
*/
public void compare_updates(ArrayList<Shoplist_item> client, ArrayList<Shoplist_item> server){

    //Toast.makeText(this, "compare updates", Toast.LENGTH_SHORT).show();
    String [] client_array = new String[client.size()];
    String [] server_array = new String[server.size()];

    for (int i =0; i <client.size(); i++) {
    client_array[i] = "";
    }

    for (int i =0; i <server.size(); i++) {
        server_array[i] = "";
    }

        for (int i =0; i <client.size(); i++){
            for(int j =0; j < server.size(); j++){
                if(client.get(i).name.equals(server.get(j).name) && client.get(i).tag.equals(server.get(j).tag)) {
                  //  Toast.makeText(this, "match found", Toast.LENGTH_SHORT).show();

                    if (Integer.parseInt(client.get(i).modified) > Integer.parseInt(server.get(j).modified)) {
                        Shoplist_item temp = client.get(i);
                        temp.serve_id = server.get(j).serve_id;
                        update_server_item(temp, "EDIT");
                        client_array[i] = "UPDATE";
                        server_array[j] = "UPDATE";
                       // Toast.makeText(this, "Server updates " + temp.client_id + "," + temp.name + ", " + temp.priority + ", " + temp.deleted + ", " + temp.modified + ", " + temp.serve_id, Toast.LENGTH_SHORT).show();
                    }

                    else if (Integer.parseInt(client.get(i).modified) < Integer.parseInt(server.get(j).modified)) {
                        Shoplist_item temp = server.get(j);
                        temp.client_id = client.get(i).client_id;
                       update_client_item(temp, "EDIT");
                        client_array[i] = "UPDATE";
                        server_array[j] = "UPDATE";
                     //  Toast.makeText(this, "Client updates " + temp.client_id + "," + temp.name + ", " + temp.priority + ", " + temp.deleted + ", " + temp.modified + ", " + temp.serve_id, Toast.LENGTH_SHORT).show();
                    }
                    else  {
                        client_array[i] = "EXISTS";
                        server_array[j] = "EXISTS";
                    }
                }
            }

            if (client_array[i].equals("") || client_array[i].isEmpty()){
                update_server_item(client.get(i), "ADD");
            }
        }

    for (int i = 0 ; i < server.size(); i++){
        if (server_array[i].equals("") || server_array[i].isEmpty()){
            update_client_item(server.get(i), "ADD");
        }
    }

    repopulate(null);

    }


public void update_server_item(Shoplist_item item, String type){


        if(type.equals("ADD")) {
            String sURL = myURL + "/" + login_name + ":" + login_pass + "/shoplist/add_item/" + item.name.replace(" ", "_") + "/" + item.priority + "/" + item.modified + "/" + item.deleted + "/" + item.tag + "/" +item.client_id;
            new Shop_GetUrlContentTask().execute(sURL, "ADD", "NO_RETURN");
        }
        else if(type.equals("EDIT")){
            String sURL = myURL + "/" + login_name + ":" + login_pass + "/shoplist/sync_item/" + item.serve_id + "/" + item.name.replace(" ", "_") + "/" + item.priority + "/" + item.modified + "/" + item.deleted + "/" + item.client_id;
            new Shop_GetUrlContentTask().execute(sURL, "UPDATE", "NO_RETURN");
        }
    }

public void update_client_item(Shoplist_item item, String type){
        sqealee2.update_Values(item);
        if(type.equals("ADD")) {
            sqealee2.addValue(item);

        }
        else if(type.equals("EDIT")){
            sqealee2.update_Values(item);
        }

    }


public void cclean(){
        sqealee2.cleanup();
            String sURL = myURL + "/" + login_name + ":" + login_pass + "/shoplist/cleanup";
            new Shop_GetUrlContentTask().execute(sURL, "CLEANUP", "NO_RETURN");
    }
}