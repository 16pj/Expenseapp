package com.rpj.robin.appearance;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomUsersAdapter extends ArrayAdapter<Expense_item> {
    public CustomUsersAdapter(Context context, ArrayList<Expense_item> expenses) {
        super(context, 0, expenses);
     }

     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Expense_item expense_item = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
           convertView = LayoutInflater.from(getContext()).inflate(R.layout.expense_items, parent, false);
        }
        // Lookup view for data population
        TextView tvname = (TextView) convertView.findViewById(R.id.checkedview);
        TextView tvcost = (TextView) convertView.findViewById(R.id.separator);
        // Populate the data into the template view using the data object
        tvname.setText(expense_item.name);
        tvcost.setText(expense_item.cost);
        // Return the completed view to render on screen
        return convertView;
    }
}
