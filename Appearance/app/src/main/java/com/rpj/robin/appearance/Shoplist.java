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
    private ArrayList<String> mylist;
    private ArrayList<String> selecteditems;
    private ArrayAdapter<String> adapter;
    private ListView listView;
    private EditText editText;
    Sqealer2 sqealee;
    private Shoplist_item shoplist_item;

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
        editText = (EditText) findViewById(R.id.editText);
        shoplist_item = new Shoplist_item(0,"","","","","");

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
        sqealee.cleanup();
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

        for (Shoplist_item value : valuemash) {

            client_sync_list.add(value);
            if(!value.deleted.equals("1")){
                value.name = value.name.replace("_", " ");

            if (value.priority.equals("YES"))
                mylist.add("*" + value.name);
            else
                mylist.add(value.name);
            my_shoplist.add(value);
        }
        }

        adapter.notifyDataSetChanged();
      String [] hashmash = sqealee.shoplisthashbrown().split(":");
        Toast.makeText(this, hashmash[0] + "," + hashmash[1], Toast.LENGTH_SHORT).show();
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
}