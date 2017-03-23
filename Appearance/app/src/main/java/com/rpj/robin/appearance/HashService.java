package com.rpj.robin.appearance;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class HashService extends Service {
    Handler mHandler = new Handler();
    private String [] hashbrown = new String[2];
    private int retry_time = 0;
    private int retry_number = 0;
    private String myURL = myconf.global_url;
    private String login_name;
    private String login_pass;
    Sqealer2 sqealee2 = new Sqealer2(this, null, null, 1);
    Sqealer sqealee = new Sqealer(this, null, null, 1);
    private int counter = 0;


    public HashService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void sendMessage(String batch) {
        Intent intent = new Intent("SYNC-REQUIRED");
        // add data
        intent.putExtra("message", "SYNC-REQUIRED:" + batch);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void checkHash() {

        counter++;
        try {

            String month_string = new SimpleDateFormat("mmss", Locale.GERMANY).format(new Date());
            int month_int;

            try {
                month_int  = Integer.parseInt(month_string);
            }
            catch (Exception e){
                month_int = 0;
            }

            if(retry_number < 5) {
                retry_time = month_int;

                ///////   SHOPLIST CHECK
                String sURL = myURL + "/" + login_name + ":" + login_pass + "/shoplist/hashbrown";
                new Shop_Get_Service_hash_ContentTask().execute(sURL, "HASH", "CHECK_HASH");


                ///////   EXPENSE CHECK

                if(counter%3!=0) {

                    sURL = myURL + "/" + login_name + ":" + login_pass + "/expense/hashbrown/0";
                    String message = "CHECK_HASH" + ":" + String.valueOf(0);
                    new Expense_Get_Service_hash_ContentTask().execute(sURL, "HASH", message);
                }
                else checkFullHash();
            }

            if(month_int > retry_time+30) {
                retry_number = 0;
                retry_time = month_int;
            }
            else retry_number++;


        } catch (Exception e) {
            Log.e("Error", "In onStartCommand");
            e.printStackTrace();
        }
        scheduleNext();
    }

    private void checkFullHash() {

        try {

            String month_string = new SimpleDateFormat("mmss", Locale.GERMANY).format(new Date());
            int month_int;

            try {
                month_int  = Integer.parseInt(month_string);
            }
            catch (Exception e){
                month_int = 0;
            }

            if(retry_number < 5) {
                retry_time = month_int;

                ///////   EXPENSE CHECK

                String sURL = myURL + "/" + login_name + ":" + login_pass + "/expense/hashbrown";
                String message = "CHECK_HASH" + ":" + String.valueOf(-1);
                new Expense_Get_Service_hash_ContentTask().execute(sURL, "HASH", message);

            }

            if(month_int > retry_time+30) {
                retry_number = 0;
                retry_time = month_int;
            }
            else retry_number++;




        } catch (Exception e) {
            Log.e("Error", "In onStartCommand");
            e.printStackTrace();
        }
    }

    private void scheduleNext() {
        mHandler.postDelayed(new Runnable() {
            public void run() { checkHash(); }
        }, 10000);
    }


    @Override
    public int onStartCommand(Intent intent, int x, int y) {
        SharedPreferences sharedpref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        login_name = sharedpref.getString("username", "");
        login_pass = sharedpref.getString("password", "");

        mHandler = new android.os.Handler();
        if(!login_name.isEmpty())
        checkHash();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d("RPJ", "DESTROYED");
        super.onDestroy();
    }



 class Shop_Get_Service_hash_ContentTask extends AsyncTask<String, String, String> {
    private String whenceforth = "";
    protected String doInBackground(String... params) {

        whenceforth = params[2];

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

            if(whenceforth.equals("CHECK_HASH")) {

                //CHECK HASH
                String[] hashmash = sqealee2.shoplisthashbrown().split(":");

                boolean x = hashmash[0].equals(hashbrown[0]);
                boolean y = hashmash[1].equals(hashbrown[1]);

                if (!x || !y) {

                    sendMessage("0");

                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}

    private class Expense_Get_Service_hash_ContentTask extends AsyncTask<String, String, String> {
        private String whenceforth = "";
        private String batch = "0";
        protected String doInBackground(String... params) {

            whenceforth = params[2].split(":")[0];
            try {
                batch = params[2].split(":")[1];
            }
            catch (Exception e){
                e.printStackTrace();
            }
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

                if(whenceforth.equals("CHECK_HASH")) {
                    String[] hashmash;
                    //CHECK HASH
                    if(!batch.equals("-1"))
                        hashmash = sqealee.expenselisthashbrown(Integer.parseInt(batch)).split(":");
                    else hashmash = sqealee.expenselist_fullhashbrown().split(":");

                    boolean x = hashmash[0].equals(hashbrown[0]);
                    boolean y = hashmash[1].equals(hashbrown[1]);

                    if (!x || !y) {
                        sendMessage(batch);
                    }

                    //END CHECK HASH
                }

            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }


}