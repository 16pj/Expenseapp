package com.rpj.robin.appearance;

class Expense_item {
     public String name;
     String cost;
     String date;
     String category;
    String modified;
    String deleted;
     public String client_id;
     public String serve_id;
    public String tag;

    Expense_item( String client_id, String name, String cost, String date, String category, String modified, String deleted, String tag, String serve_id) {
        this.name = name;
        this.cost = cost;
        this.date = date;
        this.category = category;
        this.modified = modified;
        this.deleted = deleted;
        this.client_id = client_id;
        this.tag = tag;
        this.serve_id =serve_id;
    }

}
