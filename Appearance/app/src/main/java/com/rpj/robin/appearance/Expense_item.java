package com.rpj.robin.appearance;

class Expense_item {
     public String name;
     String cost;
     String date;
     String category;
     public String id;

    Expense_item( String id, String name, String cost, String date, String category) {
        this.name = name;
        this.cost = cost;
        this.date = date;
        this.category = category;
        this.id = id;
    }

}
