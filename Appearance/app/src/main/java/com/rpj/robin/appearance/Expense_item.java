package com.rpj.robin.appearance;

public class Expense_item {
    public String name;
    public String cost;
    public String date;
    public String category;
    public String id;

    public Expense_item( String id, String name, String cost, String date, String category) {
        this.name = name;
        this.cost = cost;
        this.date = date;
        this.category = category;
        this.id = id;
    }

}
