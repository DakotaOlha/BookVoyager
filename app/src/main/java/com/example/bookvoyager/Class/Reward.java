package com.example.bookvoyager.Class;

public class Reward {

    private String id;
    private String name;
    private String description;
    private String iconUrl;
    private RewardCondition conditions;
    boolean condition;

    public Reward() {}

    public Reward(String desc, boolean condition){
        description = desc;
        this.condition = condition;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public RewardCondition getConditions() {
        return conditions;
    }

    public void setConditions(RewardCondition conditions) {
        this.conditions = conditions;
    }

    public String getDescription(){
        return description;
    }

}
