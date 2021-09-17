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


    //////////Finger release////////////////////////
    public static volatile boolean fingerReleaseTarget = false;
    
    
    public void setFingerReleaseTargetToTrue(){
        fingerReleaseTarget = true;
    }

    public void setFingerReleaseTargetToFalse(){
        fingerReleaseTarget = false;
    }

    public boolean returnFingerReleaseTarget(){
        return fingerReleaseTarget;
    }

}
