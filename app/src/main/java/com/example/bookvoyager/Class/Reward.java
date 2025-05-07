package com.example.bookvoyager.Class;

public class Reward {

    private String id;
    private String name;
    private String description;
    private String iconUrl;
    private RewardCondition condition;

    public Reward() {}

    public Reward(String desc){
        description = desc;
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

    public RewardCondition getCondition() {
        return condition;
    }

    public void setCondition(RewardCondition conditions) {
        this.condition = conditions;
    }

    public String getDescription(){
        return description;
    }

}
