package com.rpj.robin.appearance;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import java.util.ArrayList;


public class Shoplist extends AppCompatActivity {

    private ArrayList<Shoplist_item> my_shoplist;
    private ArrayList<Shoplist_item> client_sync_list;
    private ArrayList<Shoplist_item> server_sync_list;
    private ArrayList<Shoplist_item> final_list;
    private ArrayList<String> mylist;
    private ArrayList<String> selecteditems;
    private ArrayAdapter<String> adapter;
    private ListView listView;
    private EditText editText;
    Sqealer2 sqealee;
    private Shoplist_item shoplist_item;
    private String [] hashbrown = new String[2];

    private String myURL = myconf.global_url;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shoplist);
        listView = (ListView) findViewById(R.id.listview);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        selecteditems = new ArrayList<>();
        sqealee = new Sqealer2(this, null, null, 1);
        mylist = new ArrayList<>();
        client_sync_list = new ArrayList<>();
        my_shoplist = new ArrayList<>();
        server_sync_list = new ArrayList<>();
        editText = (EditText) findViewById(R.id.editText);
        shoplist_item = new Shoplist_item("","","","","","");

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

        ArrayList<Shoplist_item> valuemash = sqealee.getArray2();

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
           get_hash();
            //check_hash();

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
        shoplist_item.serve_id = "";

        sqealee.addValue(shoplist_item);}
        editText.setText("");
        repopulate(null);}
    }

    public void onRemove(View view) {
        if (mylist.size() == 0) return;

        for (String item : selecteditems) {
            sqealee.deleteValues(my_shoplist.get(mylist.indexOf(item)));
        }
        repopulate(null);
       // selecteditems.clear();
    }


    public void onPriority(View view) {

        for (String item : selecteditems) {
            if (item.startsWith("*")) {
                sqealee.unprioritize(my_shoplist.get(mylist.indexOf(item)));
                //Toast.makeText(this, item + " UNPRIOR", Toast.LENGTH_SHORT).show();
            } else {
                sqealee.prioritize(my_shoplist.get(mylist.indexOf(item)));
                //Toast.makeText(this, item + " PRIOR", Toast.LENGTH_SHORT).show();

            }
        }
        listView.clearChoices();
        selecteditems.clear();
        repopulate(null);

    }

public void get_hash() {
    SharedPreferences sharedpref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
    String name = sharedpref.getString("username", "");
    String passwd = sharedpref.getString("password", "");

    String sURL = myURL + "/" + name + ":" + passwd + "/shoplist/hashbrown";
    new Shop_Get_hash_ContentTask().execute(sURL, "HASH");
}

public void  check_hash() {

    server_sync_list.clear();
    String []  hashmash = sqealee.shoplisthashbrown().split(":");

        boolean x = hashmash[0].equals(hashbrown[0]);
        boolean y = hashmash[1].equals(hashbrown[1]);


        //Toast.makeText(this, String.format("my localhash is  %s , %s", hashmash[0],hashmash[1]), Toast.LENGTH_SHORT).show();

    if(!x || !y ){
            SharedPreferences sharedpref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
            String name = sharedpref.getString("username", "");
            String passwd = sharedpref.getString("password", "");

            String sURL = myURL + "/" + name + ":" + passwd + "/shoplist/get_items";
            new Shop_GetUrlContentTask().execute(sURL, "SHOW");
        }

        //Toast.makeText(this, String.format(" mash %s and brown %s", hashmash[1], hashbrown[1]), Toast.LENGTH_SHORT).show();
        //Toast.makeText(this, String.format("%s and %s", x, y), Toast.LENGTH_SHORT).show();
}

    public ArrayList<Shoplist_item> compare_lists(ArrayList<Shoplist_item> client, ArrayList<Shoplist_item> server){

        ArrayList<String> client_stuff = new ArrayList<>();
        ArrayList<String> server_stuff = new ArrayList<>();

        for (Shoplist_item item:client) {

            if (item.serve_id.equals("")) {
                client_stuff.add(item.name);
                update_server_item(item, "ADD");
                Toast.makeText(this, "Server gets " + item.client_id + "," + item.name + ", " + item.priority + ", " + item.deleted + ", " + item.modified + ", " + item.serve_id, Toast.LENGTH_SHORT).show();

            }
        }

        for (Shoplist_item item:server){

            if(item.client_id.equals("-1")){
                server_stuff.add(item.name);
                sqealee.addValue(item);
                Toast.makeText(this, "Client gets " + item.client_id + "," + item.name + ", " + item.priority + ", " + item.deleted + ", " + item.modified + ", " + item.serve_id, Toast.LENGTH_SHORT).show();

            }
        }


        second_stage();

        //String x = String.valueOf(client_stuff.size()) + " , " +  String.valueOf(server_stuff.size());
        //Toast.makeText(this,x , Toast.LENGTH_SHORT).show();


        return null;
    }


    public void second_stage() {

        //Toast.makeText(this, "Second Stage", Toast.LENGTH_SHORT).show();

        SharedPreferences sharedpref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String name = sharedpref.getString("username", "");
        String passwd = sharedpref.getString("password", "");

        String sURL = myURL + "/" + name + ":" + passwd + "/shoplist/hashbrown";
        new Shop_Get_hash_ContentTask2().execute(sURL, "HASH");
    }

    public void third_stage(){

        //Toast.makeText(this, "Third Stage", Toast.LENGTH_SHORT).show();


        client_sync_list.clear();
        server_sync_list.clear();

        ArrayList<Shoplist_item> valuemash = sqealee.getArray2();

        if(!valuemash.isEmpty()) {
            for (Shoplist_item value : valuemash) {

                client_sync_list.add(value);
            }


            SharedPreferences sharedpref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
            String name = sharedpref.getString("username", "");
            String passwd = sharedpref.getString("password", "");

            String sURL = myURL + "/" + name + ":" + passwd + "/shoplist/get_items";
            new Shop_GetUrlContentTask3().execute(sURL, "SHOW");

        }
        //Toast.makeText(this, String.format(" mash %s and brown %s", hashmash[1], hashbrown[1]), Toast.LENGTH_SHORT).show();
        //Toast.makeText(this, String.format("%s and %s", x, y), Toast.LENGTH_SHORT).show();

    }

    public ArrayList<Shoplist_item> fourth_stage(ArrayList<Shoplist_item> client, ArrayList<Shoplist_item> server){

        Toast.makeText(this, "Fourth Stage", Toast.LENGTH_SHORT).show();
        for (int i =0; i <client.size(); i++){
            for(int j =0; j < server.size(); j++){
                if(client.get(i).name.equals(server.get(j).name)) {
                   // Toast.makeText(this, "name equal", Toast.LENGTH_SHORT).show();

                    if(client.get(i).serve_id.equals("")) {
                        Toast.makeText(this, "client serve_id blank", Toast.LENGTH_SHORT).show();
                        Shoplist_item temp = client.get(i);
                        temp.serve_id = server.get(j).serve_id;
                        sqealee.update_Values(temp);
                    }
                    else if (server.get(j).client_id.equals("-1")){
                        Toast.makeText(this, "server client_id blank", Toast.LENGTH_SHORT).show();
                        Shoplist_item temp = server.get(j);
                        temp.client_id = client.get(i).client_id;
                        update_server_item(temp, "EDIT");
                    }

                    else if (Integer.parseInt(client.get(i).modified) > Integer.parseInt(server.get(j).modified)) {
                        Shoplist_item temp = client.get(i);
                        temp.serve_id = server.get(j).serve_id;
                        update_server_item(temp, "EDIT");
                        Toast.makeText(this, "Server updates " + temp.client_id + "," + temp.name + ", " + temp.priority + ", " + temp.deleted + ", " + temp.modified + ", " + temp.serve_id, Toast.LENGTH_SHORT).show();
                    }

                    else if (Integer.parseInt(client.get(i).modified) < Integer.parseInt(server.get(j).modified)) {
                        Shoplist_item temp = server.get(j);
                        temp.client_id = client.get(i).client_id;
                        sqealee.update_Values(temp);
                        Toast.makeText(this, "Client updates " + temp.client_id + "," + temp.name + ", " + temp.priority + ", " + temp.deleted + ", " + temp.modified + ", " + temp.serve_id, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

        repopulate(null);
        return null;

    }


    public void update_server_item(Shoplist_item item, String type){

        SharedPreferences sharedpref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String name = sharedpref.getString("username", "");
        String passwd = sharedpref.getString("password", "");

        if(type.equals("ADD")) {
            String sURL = myURL + "/" + name + ":" + passwd + "/shoplist/add_item/" + item.name + "/" + item.priority + "/" + item.modified + "/" + item.deleted + "/" + item.client_id;
            new Shop_GetUrlContentTask2().execute(sURL, "ADD");
        }
        else if(type.equals("EDIT")){
            String sURL = myURL + "/" + name + ":" + passwd + "/shoplist/sync_item/" + item.serve_id + "/" + item.name + "/" + item.priority + "/" + item.modified + "/" + item.deleted + "/" + item.client_id;
            new Shop_GetUrlContentTask2().execute(sURL, "PRIORITY");
        }
    }

    public void update_client_item(Shoplist_item item){

    }


    public void cclean(){
        sqealee.cleanup();
        SharedPreferences sharedpref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String name = sharedpref.getString("username", "");
        String passwd = sharedpref.getString("password", "");

            String sURL = myURL + "/" + name + ":" + passwd + "/shoplist/cleanup";
            new Shop_GetUrlContentTask2().execute(sURL, "DELETE");
    }


   /* public ArrayList<Shoplist_item> get_server_list() {
        SharedPreferences sharedpref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String name = sharedpref.getString("username", "");

        String sURL = myURL + "/" + name + "/shoplist/items";
        mylist.clear();
        adapter.notifyDataSetChanged();
        new Shop_GetUrlContentTask().execute(sURL, "SHOW");
        adapter.notifyDataSetChanged();
        return null;
    }
*/
    private class Shop_GetUrlContentTask extends AsyncTask<String, String, String> {
        protected String doInBackground(String... params) {

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
                case ("PRIORITY"):

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
            try {
                String clid = "";
                JSONArray jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        clid = jsonArray.getJSONObject(i).getString("client_id");
                       // Toast.makeText(Shoplist.this, "Client id is " + clid, Toast.LENGTH_SHORT).show();
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                    if (clid.isEmpty() || clid.equals("") || clid.equals("null"))
                        clid = "-1";
                    //Toast.makeText(Shoplist.this, "final Client id is " + clid, Toast.LENGTH_SHORT).show();
                    server_sync_list.add(new Shoplist_item(clid,jsonArray.getJSONObject(i).getString("name"), jsonArray.getJSONObject(i).getString("priority"), jsonArray.getJSONObject(i).getString("deleted"),jsonArray.getJSONObject(i).getString("modified"), jsonArray.getJSONObject(i).getString("id")));
                }

                if(!client_sync_list.isEmpty() || !server_sync_list.isEmpty())
               final_list = compare_lists(client_sync_list, server_sync_list);
                else Toast.makeText(Shoplist.this, "Clientlist or serverlist is empty", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


    private class Shop_GetUrlContentTask2 extends AsyncTask<String, String, String> {
        protected String doInBackground(String... params) {

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
                case ("PRIORITY"):

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

        }
    }


    private class Shop_GetUrlContentTask3 extends AsyncTask<String, String, String> {
        protected String doInBackground(String... params) {

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
                case ("PRIORITY"):

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
            try {
                String clid = "";
                JSONArray jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        clid = jsonArray.getJSONObject(i).getString("client_id");
                        // Toast.makeText(Shoplist.this, "Client id is " + clid, Toast.LENGTH_SHORT).show();
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                    if (clid.isEmpty() || clid.equals("") || clid.equals("null"))
                        clid = "-1";
                    //Toast.makeText(Shoplist.this, "final Client id is " + clid, Toast.LENGTH_SHORT).show();
                    server_sync_list.add(new Shoplist_item(clid,jsonArray.getJSONObject(i).getString("name"), jsonArray.getJSONObject(i).getString("priority"), jsonArray.getJSONObject(i).getString("deleted"),jsonArray.getJSONObject(i).getString("modified"), jsonArray.getJSONObject(i).getString("id")));
                }

                if(!client_sync_list.isEmpty() || !server_sync_list.isEmpty())
                    final_list = fourth_stage(client_sync_list, server_sync_list);
                else Toast.makeText(Shoplist.this, "Clientlist or serverlist is empty", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

        private class Shop_Get_hash_ContentTask extends AsyncTask<String, String, String> {
            protected String doInBackground(String... params) {

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

                    //Toast.makeText(Shoplist.this, "My hash is " + hashbrown[0] + ", " + hashbrown[1], Toast.LENGTH_SHORT).show();
                    check_hash();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
    }


    private class Shop_Get_hash_ContentTask2 extends AsyncTask<String, String, String> {
        protected String doInBackground(String... params) {

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

               // Toast.makeText(Shoplist.this, "My hash is " + hashbrown[0] + ", " + hashbrown[1], Toast.LENGTH_SHORT).show();
                third_stage();
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }


}