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

    //////////Use target type////////////////////////
    public static final int CONFIGCOORDINATES = 1;

}
