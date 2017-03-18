package com.rpj.robin.appearance;

class Expense_item {
     public String name;
     String cost;
     String date;
     String category;
    String modified;
    String deleted;
      String client_id;
      String serve_id;
     String tag;

    Expense_item( String client_id, String name, String cost, String date, String category, String deleted, String modified, String tag, String serve_id) {
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
