package com.rpj.robin.appearance;

class Expense_item {
     public String name;
     String cost;
     String date;
     String category;
     public String id;
     public String serve_id;

    Expense_item( String id, String name, String cost, String date, String category, String serve_id) {
        this.name = name;
        this.cost = cost;
        this.date = date;
        this.category = category;
        this.id = id;
        this.serve_id =serve_id;
    }

}
