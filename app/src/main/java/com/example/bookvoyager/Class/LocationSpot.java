package com.example.bookvoyager.Class;

public class LocationSpot {

    private String locationId;
    private int countRequiredBooks;
    private boolean ifUnlocked;

    public LocationSpot() {}

    public LocationSpot(String locationId, int count){
        this.locationId = locationId;
        this.countRequiredBooks = count;
        this.ifUnlocked = false;
    }

    public void increaseCount(){
        countRequiredBooks++;
    }

    public boolean isIfUnlocked() {
        return ifUnlocked;
    }

    public void setIfUnlocked(boolean ifUnlocked) {
        this.ifUnlocked = ifUnlocked;
    }
}
