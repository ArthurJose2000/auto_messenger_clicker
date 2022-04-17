package com.amadorprog.autoclicker_messengerclicker;

public class DataManager {
    private static DataManager instance;
    private DataManager(){}

    private boolean isPremium;

    public static DataManager getInstace(){
        if(instance == null)
            synchronized (DataManager.class){
                instance = new DataManager();
                instance.isPremium = false;
            }
        return instance;
    }

    public void isPremiumUpdate(boolean update){
        isPremium = update;
    }

    public boolean isUserPremium(){
        return isPremium;
    }
}