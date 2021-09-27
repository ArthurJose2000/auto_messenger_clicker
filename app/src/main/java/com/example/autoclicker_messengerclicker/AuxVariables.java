package com.example.autoclicker_messengerclicker;

public class AuxVariables {

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

    //////////Use target type////////////////////////
    public static final int CONFIGCOORDINATES = 1;

}
