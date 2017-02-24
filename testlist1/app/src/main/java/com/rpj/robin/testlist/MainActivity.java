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
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class MainActivity extends AppCompatActivity {

    private ArrayList<String> mylist;
    private ArrayList<Integer> selecteditems;

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

    String myURL = "http://192.168.1.25:3000";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        adapter = new ArrayAdapter<String>(this, R.layout.list_items, R.id.checkedview, mylist);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //try {
                    if (selecteditems.contains(position)) {
                        selecteditems.remove(position);
                    } else {
                        selecteditems.add(position);
                    }
               // }catch (Exception e){
                 //   e.printStackTrace();
                //}

            }
        });


        repopulate(null);

    }

    public void repopulate(View view){
        String sURL = myURL + "/expense/items";
        mylist.clear();
        itemnames.clear();
        itemcosts.clear();
        itemdates.clear();
        adapter.notifyDataSetChanged();
        new GetUrlContentTask().execute(sURL, "SHOW");
        adapter.notifyDataSetChanged();

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
            String sURL = myURL + "/expense/items/" + name + "/" + num + "/DEFAULT";
            new GetUrlContentTask().execute(sURL, "ADD");
            editName.setText("");
            editNum.setText("");
             repopulate(null);
             listView.clearChoices();
    }
    }

    public void onRemove(View view){

        if(mylist.size() == 0) return;

        for (int item:selecteditems) {
            String thing = itemnames.get(item);
            //int date = itemdates.get(item);
            thing = thing.replace(" ", "_");
            String sURL = myURL + "/expense/items/" + thing + "/" + itemdates.get(item) + "/0";
            new GetUrlContentTask().execute(sURL, "DELETE");
            mylist.remove(item);
        }
        repopulate(null);
        listView.clearChoices();
        selecteditems.clear();

    }

    public void onPriority(View view){
       ;
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
                    limit.setText(String.format("MONTH LIMIT: %s", jsonArray.getJSONObject(i-1).getString("limit")));
                    adapter.notifyDataSetChanged();
                }catch (Exception e){
                    e.printStackTrace();
                }


        }
    }



}
