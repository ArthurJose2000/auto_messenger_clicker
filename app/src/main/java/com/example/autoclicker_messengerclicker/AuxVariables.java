package com.example.autoclicker_messengerclicker;

public class AuxVariables {

    //////////Main options////////////////////////
    public static volatile String groupName = "";
    public static volatile String timeUnityDelay = "s";
    public static volatile String timeUnityMaxDelay = "s";
    public static volatile String timeUnityMinDelay = "s";
    public static volatile int delay = 20;
    public static volatile int maxDelay = 30;
    public static volatile int minDelay = 20;
    public static volatile boolean randomOrder = false;
    public static volatile boolean randomDelay = false;

    public void setTimeUnityDelay(String un){ timeUnityDelay = un; }
    public String returnTimeUnityDelay(){ return timeUnityDelay; };

    public void setTimeUnityMaxDelay(String un){ timeUnityMaxDelay = un; }
    public String returnTimeUnityMaxDelay(){ return timeUnityMaxDelay; };

    public void setTimeUnityMinDelay(String un){ timeUnityMinDelay = un; }
    public String returnTimeUnityMinDelay(){ return timeUnityMinDelay; };

    public void setGroupName(String s){ groupName = s; }
    public String returnGroupName(){ return groupName; }

    public void setDelay(int d){
        delay = d;
    }
    public int returnDelay(){
        return delay;
    }

    public void setMaxDelay(int d){
        maxDelay = d;
    }
    public int returnMaxDelay(){
        return maxDelay;
    }

    public void setMinDelay(int d){
        minDelay = d;
    }
    public int returnMinDelay(){
        return minDelay;
    }

    public void setRandomOrderToTrue(){
        randomOrder = true;
    }
    public void setRandomOrderToFalse(){
        randomOrder = false;
    }
    public boolean isRandomOrder(){ return randomOrder; }

    public void setRandomDelayToTrue(){
        randomDelay = true;
    }
    public void setRandomDelayToFalse(){
        randomDelay = false;
    }
    public boolean isRandomDelay(){ return randomDelay; }

    //////////Coordinates////////////////////////
    public static volatile int coordX = 0;
    public static volatile int coordY = 0;
    
    public void setCoordinates(int x, int y){
        coordX = x;
        coordY = y;
    }
    
    public int returnCoordinateX(){
        return coordX;
    }

    public int returnCoordinateY(){
        return coordY;
    }

    //////////Test coordinate (key == "a")////////////////////////
    //teste correspondente à letra 'a' (verifica se 'a' maiúsculo é digitado)
    public static volatile int testCoordX = 0;
    public static volatile int testCoordY = 0;

    public void setTestCoordinates(int x, int y){
        testCoordX = x;
        testCoordY = y;
    }

    public int returnTestCoordinateX(){
        return testCoordX;
    }

    public int returnTestCoordinateY(){
        return testCoordY;
    }


    //////////Artificial touch////////////////////////
    public static volatile boolean artificialTouch = false;

    public void setArtificialTouchToTrue(){
        artificialTouch = true;
    }

    public void setArtificialTouchToFalse(){
        artificialTouch = false;
    }

    public boolean isArtificialTouch() {
        return artificialTouch;
    }

    //////////Finished gesture////////////////////////
    public static volatile boolean finishedGesture = false;

    public void setFinishedGestureToTrue(){
        finishedGesture = true;
    }

    public void setFinishedGestureToFalse(){
        finishedGesture = false;
    }

    public boolean isFinishedGesture() {
        return finishedGesture;
    }

    //////////Check Caps Lock////////////////////////
    public static volatile boolean checkCapsLock = false;

    public void setCheckCapsLockToTrue(){
        checkCapsLock = true;
    }

    public void setCheckCapsLockToFalse(){
        checkCapsLock = false;
    }

    public boolean isTimeToCheckCapsLock() {
        return checkCapsLock;
    }

    //////////Check Special Char////////////////////////
    public static volatile boolean checkSpecialChar = false;

    public void setCheckSpecialCharToTrue(){
        checkSpecialChar = true;
    }

    public void setCheckSpecialCharToFalse(){
        checkSpecialChar = false;
    }

    public boolean isTimeToCheckSpecialChar() {
        return checkSpecialChar;
    }

    //////////Check Space Bar////////////////////////
    public static volatile boolean checkSpaceBar = false;

    public void setCheckSpaceBarToTrue(){
        checkSpaceBar = true;
    }

    public void setCheckSpaceBarToFalse(){
        checkSpaceBar = false;
    }

    public boolean isTimeToCheckSpaceBar() {
        return checkSpaceBar;
    }

    //////////Use target type////////////////////////
    public static final int CONFIGCOORDINATES = 1; //config all coordinates

    public static final int CONFIGSENDMESSAGECOORDINATE = 2; //update send message coordinate

    public static final int CONFIGTYPEFIELDCOORDINATE = 3; //update type field coordinate

}
