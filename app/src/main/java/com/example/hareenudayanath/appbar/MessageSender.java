package com.example.hareenudayanath.appbar;

import android.os.Handler;

import com.example.hareenudayanath.braillebandcommunicator.BrailleBandCommunicator;

/**
 * Created by Hareen Udayanath on 12/4/2016.
 */
public class MessageSender {

    private BrailleBandCommunicator communicator;
    /*
    * Message sending variables......................
    * */

    // State variables
    public static final String STOP_STATE = "stop";
    public static final String RUN_STATE = "run";
    public static final String PAUSE_STATE = "pause";

    private String message = "";

    private int position = 0;
    private Handler handler;
    private Runnable task;

    private int CHR_GAP = 1000;
    private int WRD_GAP = 1000;

    private String state = STOP_STATE;


    public MessageSender (BrailleBandCommunicator communicator){

        this.communicator = communicator;

    }
     /*
      * Message sending methods..........................
      * */

    private String getChar(){
        String chr = "";
        if(position < getMessage().length()){
            chr = getMessage().substring(position, ++position);
        }else{
            position = 0;
        }
        return chr;
    }

    private void back(){
        if(position==0)
            return;
        if(!getMessage().substring(position, position + 1).equals(" ")){
            position--;
            back();
        }else{
            position++;
        }
    }
    private void forward(){
        if(position== getMessage().length()-1){
            position++;
            return;
        }
        if(!getMessage().substring(position, position + 1).equals(" ")){
            position++;
            forward();
        }else{
            position++;
        }
    }

    private void gotoBackWord(){
        if(position>0 && state!=RUN_STATE)
            position--;
        if(getMessage().substring(position, position + 1).equals(" "))
            position--;
        if(state==RUN_STATE&&position>3)
            position-=3;
        if(state==RUN_STATE&&position<2)
            position=0;
        back();
    }

    private void gotoNextWord(){
        if(position>0 && state==RUN_STATE)
            position--;
        forward();
    }

    private void go(){
        state = RUN_STATE;
        //remove previously running tasks
        if(task!=null)
            handler.removeCallbacks(task);
        handler = new Handler();
        //create a new running task
        task = new Runnable() {
            public void run() {
                String chr = getChar();
                if(communicator.isConnected())
                    communicator.write(chr);
                else
                    stopSending();
                if (position < getMessage().length()) {
                    if (chr.equals(" "))
                        handler.postDelayed(this, WRD_GAP);
                    else
                        handler.postDelayed(this, CHR_GAP);
                }else {
                    position = 0;
                    state = STOP_STATE;
                }
            }
        };
        // set the running task to the handler with a delay
        handler.postDelayed(task, 10);
    }

    /*
    * Methods to control
    * */

    public void startSending(){
        System.out.println("Test -------------------------- 3");
        System.out.println("Test -----"+this.state);
        if (state.equals(STOP_STATE)) {
            position = 0;
            System.out.println("Test -------------------------- 3");
            go();
        } else if (state.equals(PAUSE_STATE)) {
            go();
        } else if (state.equals(RUN_STATE)) {
            if (task != null)
                handler.removeCallbacks(task);
            state = PAUSE_STATE;
        }

    }

    public void stopSending(){
        if (task != null)
            handler.removeCallbacks(task);
        state = STOP_STATE;
        position = 0;

    }

    public void goToNextWord(){

        if(state.equals(STOP_STATE)){
            gotoNextWord();
        }else{
            if(task!=null)
                handler.removeCallbacks(task);
            gotoNextWord();
            if(state.equals(RUN_STATE))
                go();
        }


    }

    public void goToPreviousWord(){

        if (state.equals(STOP_STATE)) {
            gotoBackWord();
        } else {
            if (task != null)
                handler.removeCallbacks(task);
            gotoBackWord();
            if (state.equals(RUN_STATE))
                go();
        }

    }

    /*
    * Getters and setters
    * */
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        state = STOP_STATE;
        this.message = message;
    }

    public int getPosition() {
        return position;
    }

    public int getCHR_GAP() {
        return CHR_GAP;
    }

    public void setCHR_GAP(int CHR_GAP) {
        this.CHR_GAP = CHR_GAP;
    }

    public int getWRD_GAP() {
        return WRD_GAP;
    }

    public void setWRD_GAP(int WRD_GAP) {
        this.WRD_GAP = WRD_GAP;
    }

    public String getState() {
        return state;
    }
}
