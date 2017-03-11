package com.rpj.robin.appearance;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Login extends AppCompatActivity {

    private EditText editname;
    private EditText editpass;
    private String uname;
    private String upass;

    private String myURL = myconf.global_url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        editname = (EditText) findViewById(R.id.editId);
        editpass = (EditText) findViewById(R.id.editPass);
    }

    public void onReg(View view) {

        String id = editname.getText().toString();
        String pass = editpass.getText().toString();

        pass = get_md5_from_string(pass);

        if (id.equals("") || pass.equals("")){
            Toast.makeText(this, "Invalid ID/PASS", Toast.LENGTH_SHORT).show();
        }
        else {
            String sURL = myURL + "/users/" + id + "/" + pass;
            new GetUrlContentTask().execute(sURL, "ADD_USER");
        }
    }


    public void onSign(View view) {

        uname = editname.getText().toString();
        upass = editpass.getText().toString();
        upass = get_md5_from_string(upass);

        if (uname.equals("") || upass.equals("")){
            Toast.makeText(this, "Invalid ID/PASS", Toast.LENGTH_SHORT).show();
        }
        else {
            String sURL = myURL + "/users/" + uname + "/" + upass;
            new GetUrlContentTask().execute(sURL, "VERIFY_USER");
        }

    }

    public void onCreateTables(){
        uname = editname.getText().toString();
        upass = editpass.getText().toString();
        upass = get_md5_from_string(upass);
        String sURL = myURL + "/RESET/" + uname+ ":" + upass;
        new GetUrlContentTask().execute(sURL, "CREATE_TABLES");
    }

    public void onBack(View view) {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    public void onRESET(View view) {

        uname = editname.getText().toString();
        upass = editpass.getText().toString();
        upass = get_md5_from_string(upass);

        if (uname.equals("") || upass.equals("")){
            Toast.makeText(this, "Invalid ID/PASS", Toast.LENGTH_SHORT).show();
        }
        else {
            String sURL = myURL + "/RESET/" + uname + ":" + upass;
            new GetUrlContentTask().execute(sURL, "RESET");
        }

    }

    public void saveInfo(String userName, String userPass){

        SharedPreferences sharedpref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpref.edit();
        editor.putString("username", userName);
        editor.putString("password", userPass);
        editor.apply();
    }

    private class GetUrlContentTask extends AsyncTask<String, String, String> {
        protected String doInBackground(String... params) {

            switch (params[1]) {

                case "ADD_USER":

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

                case "VERIFY_USER":

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

                case "CREATE_TABLES":

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
                case "RESET":

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

                case "SHOW":
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
                            publishProgress(line);
                        }
                        return content;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                default:
                    break;
            }

            return null;
        }

        protected void onProgressUpdate(String... progress) {
        }


        protected void onPostExecute(String result) {

            if (result == null)
            {Toast.makeText(Login.this, "Something went wrong!", Toast.LENGTH_LONG).show();
            return;}

            try {
                JSONArray jsonArray = new JSONArray(result);
                result = jsonArray.getJSONObject(0).getString("result");

                if (result.trim().equals("CREATED")) {
                    Toast.makeText(Login.this, "SUCCESSFULLY REGISTERED", Toast.LENGTH_LONG).show();
                    onCreateTables();
                } else if (result.trim().equals("EXISTS")) {
                    Toast.makeText(Login.this, "USERNAME TAKEN! TRY ANOTHER.", Toast.LENGTH_SHORT).show();
                } else if (result.trim().equals("LOGGED")) {
                    Toast.makeText(Login.this, "SUCCESSFULLY LOGGED IN", Toast.LENGTH_SHORT).show();
                    saveInfo(editname.getText().toString(), get_md5_from_string(editpass.getText().toString()));
                    Intent i = new Intent(Login.this, MainActivity.class);
                    startActivity(i);
                } else if (result.trim().equals("WRONG")) {
                    Toast.makeText(Login.this, "WRONG ID/PASS", Toast.LENGTH_SHORT).show();
                } else if (result.trim().equals("REFRESHED")) {
                    Toast.makeText(Login.this, "READY! SIGN IN TO PLAY!", Toast.LENGTH_SHORT).show();
                } else if (result.trim().equals("FAILED")) {
                    Toast.makeText(Login.this, "SOMETHING WENT WRONG.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Login.this, result, Toast.LENGTH_SHORT).show();
                }

            }catch (Exception e){
                e.printStackTrace();
                Toast.makeText(Login.this, "Something went wrong!", Toast.LENGTH_LONG).show();
                }
        }
    }

    public String get_md5_from_string(String s) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            StringBuilder hexString = new StringBuilder();
            for(int i:messageDigest)
                hexString.append(Integer.toHexString(0xFF & i));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

}
