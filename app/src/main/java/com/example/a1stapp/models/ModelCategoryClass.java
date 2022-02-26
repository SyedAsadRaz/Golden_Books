package com.example.a1stapp.models;

public class ModelCategoryClass {

    String ID,Category,userID;
    long timstamp;

    // empty Counstrator for firebase


    public ModelCategoryClass() {

    }
    //Parametrized Constructors

    public ModelCategoryClass(String ID, String category, String userID, long timstamp) {
        this.ID = ID;
        Category = category;
        this.userID = userID;
        this.timstamp = timstamp;
    }


    //............Getter & Setters...

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getCategory() {
        return Category;
    }

    public void setCategory(String category) {
        Category = category;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public long getTimstamp() {
        return timstamp;
    }

    public void setTimstamp(long timstamp) {
        this.timstamp = timstamp;
    }
}
