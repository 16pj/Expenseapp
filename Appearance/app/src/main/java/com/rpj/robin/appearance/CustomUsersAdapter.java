package com.rpj.robin.appearance;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

class CustomUsersAdapter extends ArrayAdapter<Expense_item> {

    private String [] months = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};

     CustomUsersAdapter(Context context, ArrayList<Expense_item> expenses) {
        super(context, 0, expenses);
     }

     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Expense_item expense_item = getItem(position);
        Expense_item next_item;


         if(position != 0) next_item = getItem(position-1);
         else next_item = getItem(position);
       
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
           convertView = LayoutInflater.from(getContext()).inflate(R.layout.expense_items, parent, false);
        }
        // Lookup view for data population
        TextView tvname = (TextView) convertView.findViewById(R.id.checkedview);
        TextView tvcost = (TextView) convertView.findViewById(R.id.separator);
        Button more = (Button) convertView.findViewById(R.id.more);

         TextView headingview = (TextView) convertView.findViewById(R.id.heading);
        // Populate the data into the template view using the data object
        tvname.setText(expense_item.name);
        tvcost.setText(expense_item.cost);

         String month = expense_item.date.substring(2);
         String year = "20" + expense_item.date.substring(0,2);

         if (month.equals("00")) month = "LOAD MORE";
         else month = months[Integer.parseInt(month)-1];

         if(position == getCount()-1){
             more.setVisibility(View.VISIBLE);
         }
         else
             more.setVisibility(View.INVISIBLE);


         if(position==0){

             if (!month.equals("LOAD MORE")) headingview.setText(month + ", " + year);
             else headingview.setText(month);
                headingview.setVisibility(View.VISIBLE);
         }
         else if(!expense_item.date.equals(next_item.date)){

             if (!month.equals("LOAD MORE")) headingview.setText(month + ", " + year);
             else headingview.setText(month);
             headingview.setVisibility(View.VISIBLE);

         }
         else {
             headingview.setVisibility(View.INVISIBLE);
         }
        // Return the completed view to render on screen
        return convertView;
    }


}
