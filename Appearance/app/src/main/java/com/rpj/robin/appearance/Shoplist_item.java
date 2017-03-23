package com.rpj.robin.appearance;

class Shoplist_item {
     String client_id;
     String name;
     String priority;
     String modified;
     String deleted;
     String tag;
     String serve_id;


    Shoplist_item(String client_id, String name, String priority, String deleted, String modified,String tag, String serve_id) {
        this.client_id = client_id;
        this.name = name;
        this.priority = priority;
        this.modified = modified;
        this.deleted = deleted;
        this.tag = tag;
        this.serve_id =serve_id;

    }

}
