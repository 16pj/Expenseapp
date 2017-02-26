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
        import android.widget.Toast;

        import org.json.JSONArray;

        import java.io.BufferedReader;
        import java.io.InputStreamReader;
        import java.net.HttpURLConnection;
        import java.net.URL;
        import java.util.ArrayList;

public class Shoplist_offline extends AppCompatActivity {


    private ArrayList<String> mylist;
    private ArrayList<String> selecteditems;
    private ArrayAdapter<String> adapter;
    private int i;
    private ListView listView;
    private EditText editText;
    Sqealer sqealee;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shoplist_offline);
        Toast.makeText(this, "SHOWING SNAPSHOT OF LAST SAVED LIST", Toast.LENGTH_SHORT).show();

        listView = (ListView) findViewById(R.id.listviewoff);
        sqealee = new Sqealer(this, null, null, 1);
        try {
            mylist = sqealee.getArray();
        }
        catch (Exception e){
            e.printStackTrace();
            mylist = new ArrayList<>();
            Toast.makeText(this, "No Values SNAPPED!", Toast.LENGTH_SHORT).show();
        }
        adapter = new ArrayAdapter<String>(this, R.layout.list_items, R.id.checkedview, mylist);
        listView.setAdapter(adapter);
    }
}
