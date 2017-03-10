package com.rpj.robin.appearance;

        import android.os.Bundle;
        import android.support.v7.app.AppCompatActivity;
        import android.widget.ArrayAdapter;
        import android.widget.ListView;
        import android.widget.Toast;
        import java.util.ArrayList;

public class Shoplist_offline extends AppCompatActivity {

    Sqealer sqealee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shoplist_offline);
        Toast.makeText(this, "SHOWING SNAPSHOT OF LAST SAVED LIST", Toast.LENGTH_SHORT).show();
        ArrayAdapter<String> adapter;
        ArrayList<String> mylist;
        ListView listView = (ListView) findViewById(R.id.listviewoff);
        sqealee = new Sqealer(this, null, null, 1);
        try {
            mylist = sqealee.getArray();
        }
        catch (Exception e){
            e.printStackTrace();
            mylist = new ArrayList<>();
            Toast.makeText(this, "No Values SNAPPED!", Toast.LENGTH_SHORT).show();
        }
        adapter = new ArrayAdapter<>(this, R.layout.list_items, R.id.checkedview, mylist);
        listView.setAdapter(adapter);
    }
}
