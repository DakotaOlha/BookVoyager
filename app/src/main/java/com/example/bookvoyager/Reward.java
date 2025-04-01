package com.example.bookvoyager;

public class Reward {
    String description;
    boolean condition;

    public Reward(String desc, boolean condition){
        description = desc;
        this.condition = condition;
    }

    public String getDescription(){
        return description;
    }

}
