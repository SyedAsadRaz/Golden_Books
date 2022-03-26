package com.example.a1stapp.models;

public class ModelComment {

    //variables
    String id,bookId,comment,timestamp,uid;

    //constructor empty required by firebase
    public ModelComment() {
    }


    // constructor with all params

    public ModelComment(String id, String bookId, String comment, String timestamp, String uid) {
        this.id = id;
        this.bookId = bookId;
        this.comment = comment;
        this.timestamp = timestamp;
        this.uid = uid;
    }

    //getter setter

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
