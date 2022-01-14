package com.amadorprog.autoclicker_messengerclicker;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import static android.content.Context.WINDOW_SERVICE;

import java.util.ArrayList;
import java.util.Scanner;


public class Window {

    // declaring required variables
    private Context context;
    private View mView;
    private WindowManager.LayoutParams mParams;
    private WindowManager mWindowManager;
    private LayoutInflater layoutInflater;
    Target targetSendMessage;
    Target targetTypeField;
    DataBase dbListenerCoordinates;
    DataBase dbListenerMessages;
    final int CONFIGSENDMESSAGECOORDINATE = 2; //update send message coordinate
    final int CONFIGTYPINGFIELDCOORDINATE = 3; //update type field coordinate

    boolean isRandomDelay;
    int delay;
    int maxDelay;
    int minDelay;
    boolean isInfiniteLoop;
    boolean isRandomOrder;
    String groupName;


    public Window(Context context){
        this.context = context;
        targetSendMessage = new Target(context, CONFIGSENDMESSAGECOORDINATE);
        targetSendMessage.open();
        targetSendMessage.hide();
        targetTypeField = new Target(context, CONFIGTYPINGFIELDCOORDINATE);
        targetTypeField.open();
        targetTypeField.hide();

        dbListenerCoordinates = new DataBase(context, "coordinates");
        dbListenerMessages = new DataBase(context, "messages");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }
        else{
            mParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }

        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = layoutInflater.inflate(R.layout.action_bar, null);

        mView.findViewById(R.id.button_play_clicker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isSendingCoordinatesRegistered())
                    runAlgorithm();
            }
        });

        mView.findViewById(R.id.button_close_bar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                close();
            }
        });

        mView.findViewById(R.id.button_send_message).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                targetSendMessage.unhide();
            }
        });

        mView.findViewById(R.id.button_type_field).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                targetTypeField.unhide();
            }
        });

        mView.findViewById(R.id.button_move_bar).setOnTouchListener(new View.OnTouchListener(){
            int initX, initY;
            float initTouchX, initTouchY;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent){
                switch(motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:


                        initX = mParams.x;
                        initY = mParams.y;

                        initTouchX = motionEvent.getRawX();
                        initTouchY = motionEvent.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        //long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;

                        mParams.x = initX + (int) (motionEvent.getRawX() - initTouchX);
                        mParams.y = initY + (int) (motionEvent.getRawY() - initTouchY);

                        return true;
                    case MotionEvent.ACTION_MOVE:
                        mParams.x = initX + (int) (motionEvent.getRawX() - initTouchX);
                        mParams.y = initY + (int) (motionEvent.getRawY() - initTouchY);

                        mWindowManager.updateViewLayout(mView, mParams);

                        return true;
                }

                return true;
            }

        });

        // Define the position of the
        // window within the screen
        mParams.gravity = Gravity.CENTER;
        mWindowManager = (WindowManager)context.getSystemService(WINDOW_SERVICE);
        //mWindowManager.addView(mView, mParams);

    }

    public void open(boolean random_delay, int delay_s, int maxDelay_s, int minDelay_s, boolean infiniteLoop, boolean random_order, String group) {

        try {
            // check if the view is already
            // inflated or present in the window
            if(mView.getWindowToken()==null) {
                if(mView.getParent()==null) {
                    mWindowManager.addView(mView, mParams);
                }
            }
            AutoClickService.instance.setActionBarStatus(true);
            isRandomDelay = random_delay;
            delay = delay_s;
            maxDelay = maxDelay_s;
            minDelay = minDelay_s;
            isInfiniteLoop = infiniteLoop;
            isRandomOrder = random_order;
            groupName = group;
            setInputsOfMainActivity(this.context, false);
        } catch (Exception e) {
            Log.d("Error1",e.toString());
        }

    }

    public boolean isOpen(){
        boolean open = false;
        try {
            if(mView.getWindowToken() != null && mView.getParent() != null)
                open = true;
        } catch (Exception e){
            Log.d("Error1",e.toString());
        }
        return open;
    }

    public void close() {

        try {
            // remove the view from the window
            ((WindowManager)context.getSystemService(WINDOW_SERVICE)).removeView(mView);
            // invalidate the view
            mView.invalidate();
            // remove all views
            //((ViewGroup)mView.getParent()).removeAllViews();

            //new target are created very time the enable action bar button is clicked (if action bar is closed)
            AutoClickService.instance.setActionBarStatus(false);
            targetSendMessage.hide();
            targetTypeField.hide();
            setInputsOfMainActivity(this.context, true);

            // the above steps are necessary when you are adding and removing
            // the view simultaneously, it might give some exceptions

            //dbListenerCoordinates.closeDataBase(context, "coordinates");
            //dbListenerMessages.closeDataBase(context, "messages");
        } catch (Exception e) {
            Log.d("Error2",e.toString());
        }
    }

    public void runAlgorithm() {
        AutoClickService.instance.setDefaultCoordinatesObtained(false); //used to infinite loop
        AutoClickService.instance.setTypingFieldClickStatus(true); //will be clicked
        String messages = dbListenerMessages.getMessageFromDataBase(groupName);
        if (isRandomOrder) {
            String randomMessages = "";
            Scanner scanner = new Scanner(messages);
            ArrayList<String> messagesStack = new ArrayList<String>();
            while (scanner.hasNextLine()) {
                messagesStack.add(scanner.nextLine() + "\n");
            }
            scanner.close();
            while (messagesStack.size() > 0){
                int randomIndex = (int) (Math.random() * (messagesStack.size() - 1));
                randomMessages += messagesStack.get(randomIndex);
                messagesStack.remove(randomIndex);
            }
            randomMessages = randomMessages.substring(0, randomMessages.length() - 1); //remove last '\n' because is unnecessary
            typeMessages(randomMessages);
        } else {
            typeMessages(messages);
        }
    }

    public void typeMessages(String message){
        int sizeStackCoordinates = 0;
        int[] auxCoordinates;
        ArrayList<ArrayList<Integer>> coordinates = new ArrayList<ArrayList<Integer>>();
        boolean lastKeyIsASpecialChar = false;

        coordinates.add(new ArrayList<Integer>());
        auxCoordinates = dbListenerCoordinates.getCoordinatesFromDataBase("typingfield");
        coordinates.get(sizeStackCoordinates).add(auxCoordinates[0]);
        coordinates.get(sizeStackCoordinates).add(auxCoordinates[1]);
        sizeStackCoordinates++;

        int sizeMessage = message.length();
        for(int i = 0; i < sizeMessage; i++){
            char key = message.charAt(i);
            if(Character.isLetter(key)){
                auxCoordinates = dbListenerCoordinates.getCoordinatesFromDataBase(Character.toString(Character.toLowerCase(key)));
                if(auxCoordinates == null){
                    coordinates.add(new ArrayList<Integer>());
                    auxCoordinates = dbListenerCoordinates.getCoordinatesFromDataBase("spacebar");
                    coordinates.get(sizeStackCoordinates).add(auxCoordinates[0]);
                    coordinates.get(sizeStackCoordinates).add(auxCoordinates[1]);
                    sizeStackCoordinates++;
                }
                else{
                    if(Character.isUpperCase(key)){
                        coordinates.add(new ArrayList<Integer>());
                        auxCoordinates = dbListenerCoordinates.getCoordinatesFromDataBase("capslock");
                        coordinates.get(sizeStackCoordinates).add(auxCoordinates[0]);
                        coordinates.get(sizeStackCoordinates).add(auxCoordinates[1]);
                        sizeStackCoordinates++;

                        coordinates.add(new ArrayList<Integer>());
                        auxCoordinates = dbListenerCoordinates.getCoordinatesFromDataBase(Character.toString(Character.toLowerCase(key)));
                        coordinates.get(sizeStackCoordinates).add(auxCoordinates[0]);
                        coordinates.get(sizeStackCoordinates).add(auxCoordinates[1]);
                        sizeStackCoordinates++;
                    }
                    else{
                        coordinates.add(new ArrayList<Integer>());
                        auxCoordinates = dbListenerCoordinates.getCoordinatesFromDataBase(Character.toString(key));
                        coordinates.get(sizeStackCoordinates).add(auxCoordinates[0]);
                        coordinates.get(sizeStackCoordinates).add(auxCoordinates[1]);
                        sizeStackCoordinates++;
                    }
                }
            }
            else if(key == ' '){
                coordinates.add(new ArrayList<Integer>());
                auxCoordinates = dbListenerCoordinates.getCoordinatesFromDataBase("spacebar");
                coordinates.get(sizeStackCoordinates).add(auxCoordinates[0]);
                coordinates.get(sizeStackCoordinates).add(auxCoordinates[1]);
                sizeStackCoordinates++;
            }
            else if(key == '\n'){
                coordinates.add(new ArrayList<Integer>());
                auxCoordinates = dbListenerCoordinates.getCoordinatesFromDataBase("sendfield");
                coordinates.get(sizeStackCoordinates).add(auxCoordinates[0]);
                coordinates.get(sizeStackCoordinates).add(auxCoordinates[1]);
                sizeStackCoordinates++;

                coordinates.add(new ArrayList<Integer>());
                coordinates.get(sizeStackCoordinates).add(0);
                coordinates.get(sizeStackCoordinates).add(0); //Coordenada 0 0 representa o fim de uma mensagem (break line)
                sizeStackCoordinates++; // a quantidade de cliques é igual à sizeStackCoordinates - (as ocorrências nesse if)

                coordinates.add(new ArrayList<Integer>());
                auxCoordinates = dbListenerCoordinates.getCoordinatesFromDataBase("typingfield");
                coordinates.get(sizeStackCoordinates).add(auxCoordinates[0]);
                coordinates.get(sizeStackCoordinates).add(auxCoordinates[1]);
                sizeStackCoordinates++;
            }
            else{
                auxCoordinates = dbListenerCoordinates.getCoordinatesFromDataBase(Character.toString(key));
                if(auxCoordinates == null){
                    coordinates.add(new ArrayList<Integer>());
                    auxCoordinates = dbListenerCoordinates.getCoordinatesFromDataBase("spacebar");
                    coordinates.get(sizeStackCoordinates).add(auxCoordinates[0]);
                    coordinates.get(sizeStackCoordinates).add(auxCoordinates[1]);
                    sizeStackCoordinates++;
                }
                else{
                    if(lastKeyIsASpecialChar == false) {
                        coordinates.add(new ArrayList<Integer>());
                        auxCoordinates = dbListenerCoordinates.getCoordinatesFromDataBase("specialchar");
                        coordinates.get(sizeStackCoordinates).add(auxCoordinates[0]);
                        coordinates.get(sizeStackCoordinates).add(auxCoordinates[1]);
                        sizeStackCoordinates++;
                    }

                    coordinates.add(new ArrayList<Integer>());
                    auxCoordinates = dbListenerCoordinates.getCoordinatesFromDataBase(Character.toString(key));
                    coordinates.get(sizeStackCoordinates).add(auxCoordinates[0]);
                    coordinates.get(sizeStackCoordinates).add(auxCoordinates[1]);
                    sizeStackCoordinates++;

                    if(i + 1 < sizeMessage){  //check if next key not is a special char
                        char auxKey = message.charAt(i + 1);
                        auxCoordinates = dbListenerCoordinates.getCoordinatesFromDataBase(Character.toString(auxKey));
                        if(auxCoordinates == null || Character.isLetter(auxKey) || auxKey == '\n' || auxKey == ' '){
                            coordinates.add(new ArrayList<Integer>());
                            auxCoordinates = dbListenerCoordinates.getCoordinatesFromDataBase("specialchar");
                            coordinates.get(sizeStackCoordinates).add(auxCoordinates[0]);
                            coordinates.get(sizeStackCoordinates).add(auxCoordinates[1]);
                            sizeStackCoordinates++;
                            lastKeyIsASpecialChar = false; //next key not is a special char
                        }
                        else{
                            lastKeyIsASpecialChar = true; //next key is a special char
                        }
                    }
                    else{  //end of messages
                        coordinates.add(new ArrayList<Integer>());
                        auxCoordinates = dbListenerCoordinates.getCoordinatesFromDataBase("specialchar");
                        coordinates.get(sizeStackCoordinates).add(auxCoordinates[0]);
                        coordinates.get(sizeStackCoordinates).add(auxCoordinates[1]);
                        sizeStackCoordinates++;
                        lastKeyIsASpecialChar = false; //next key not is a special char
                    }
                }
            }
        }

        coordinates.add(new ArrayList<Integer>());
        auxCoordinates = dbListenerCoordinates.getCoordinatesFromDataBase("sendfield");
        coordinates.get(sizeStackCoordinates).add(auxCoordinates[0]);
        coordinates.get(sizeStackCoordinates).add(auxCoordinates[1]);
        sizeStackCoordinates++;

        AutoClickService.instance.chainedAutoClick(500, 100, coordinates, isRandomDelay, delay, maxDelay, minDelay, isInfiniteLoop);
    }

    public boolean isSendingCoordinatesRegistered(){
        //Check if send message button and typing field are registered

        int[] coordinates = dbListenerCoordinates.getCoordinatesFromDataBase("sendfield");
        if(coordinates == null) //remove
            return false;
        else if(coordinates[0] == 0 || coordinates[1] == 0)
            return false;

        coordinates = dbListenerCoordinates.getCoordinatesFromDataBase("typingfield");
        if(coordinates == null) //remove
            return false;
        else if(coordinates[0] == 0 || coordinates[1] == 0)
            return false;

        return true;
    }

    public void setInputsOfMainActivity(Context context, boolean b){
        if(b){
            ((Activity) context).findViewById(R.id.db_msg_group).setEnabled(true);
            ((Activity) context).findViewById(R.id.checkbox_random_order).setEnabled(true);
            ((Activity) context).findViewById(R.id.checkbox_infinite_loop).setEnabled(true);
            ((Activity) context).findViewById(R.id.checkbox_random_delay).setEnabled(true);
            if(isRandomDelay){
                ((Activity) context).findViewById(R.id.spinner_unit_time_1).setEnabled(false);
                ((Activity) context).findViewById(R.id.num_delay_time_simple).setEnabled(false);
                ((Activity) context).findViewById(R.id.spinner_unit_time_2).setEnabled(true);
                ((Activity) context).findViewById(R.id.spinner_unit_time_3).setEnabled(true);
                ((Activity) context).findViewById(R.id.num_delay_time_max).setEnabled(true);
                ((Activity) context).findViewById(R.id.num_delay_time_min).setEnabled(true);
            }
            else{
                ((Activity) context).findViewById(R.id.spinner_unit_time_1).setEnabled(true);
                ((Activity) context).findViewById(R.id.num_delay_time_simple).setEnabled(true);
                ((Activity) context).findViewById(R.id.spinner_unit_time_2).setEnabled(false);
                ((Activity) context).findViewById(R.id.spinner_unit_time_3).setEnabled(false);
                ((Activity) context).findViewById(R.id.num_delay_time_max).setEnabled(false);
                ((Activity) context).findViewById(R.id.num_delay_time_min).setEnabled(false);
            }
        }
        else {
            ((Activity) context).findViewById(R.id.db_msg_group).setEnabled(false);
            ((Activity) context).findViewById(R.id.spinner_unit_time_1).setEnabled(false);
            ((Activity) context).findViewById(R.id.spinner_unit_time_2).setEnabled(false);
            ((Activity) context).findViewById(R.id.spinner_unit_time_3).setEnabled(false);
            ((Activity) context).findViewById(R.id.num_delay_time_simple).setEnabled(false);
            ((Activity) context).findViewById(R.id.num_delay_time_max).setEnabled(false);
            ((Activity) context).findViewById(R.id.num_delay_time_min).setEnabled(false);
            ((Activity) context).findViewById(R.id.checkbox_random_order).setEnabled(false);
            ((Activity) context).findViewById(R.id.checkbox_infinite_loop).setEnabled(false);
            ((Activity) context).findViewById(R.id.checkbox_random_delay).setEnabled(false);
        }
    }

}
