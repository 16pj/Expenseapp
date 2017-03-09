package com.rpj.robin.appearance;

public class Expense_item {
    public String name;
    public String cost;
    public String date;
    public int id;

    public Expense_item( int id, String name, String cost, String date) {
        this.name = name;
        this.cost = cost;
        this.date = date;
        this.id = id;
    }

}
