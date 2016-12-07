package com.example.hareenudayanath.appbar;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hareenudayanath.braillebandcommunicator.BrailleBandCommunicator;

public class CommunicationActivity extends AppCompatActivity {

    private Button btnMSEND, btnMBACK,btnMFORWARD,btnMSTOP;
    private static CommunicationActivity inst;

    private SeekBar seekBarCHAR,seekBarWORD;
    public EditText textReader;

    private static String address;

    /*
    * Globle Settings........
    * */

    public static final String PREFS_NAME = "MyPrefsFile";
    private boolean autoSend = false;

    /*
    * .......................
    * */



    private static int MAX_CHARACTER_GAP = 4000;
    private static int MAX_WORD_GAP = 4000;
    private static double character_ratio = 0.5;
    private static double word_ratio = 0.5;
    private static String message = "";

    private static int position = 0;
    private static Handler handler;
    private static Runnable task;

    private static int CHR_GAP = 1000;
    private static int WRD_GAP = 1000;
    private static String STOP_STATE = "stop";
    private static String RUN_STATE = "run";
    private static String PAUSE_STATE = "pause";
    private static String state = STOP_STATE;

    /*
    * Button sound
    * */

    private static boolean isSendPressed = false;
    private static boolean isPausePressed = false;
    private static boolean isStopPressed = false;
    private static boolean isFawardPressed = false;
    private static boolean isBackPressed = false;


    private static void setAllPressedFalse(){
        isSendPressed = false;
        isPausePressed = false;
        isStopPressed= false;
        isFawardPressed = false;
        isBackPressed = false;
    }

    public static boolean isReading = false;

    private static BrailleBandCommunicator brailleBandCommunicator;


    private static String getChar(){
        String chr = "";
        if(position < message.length()){
            chr = message.substring(position,++position);
        }else{
            position = 0;
        }
        return chr;
    }

    private static void back(){
        if(position==0)
            return;
        if(!message.substring(position,position+1).equals(" ")){
            position--;
            back();
        }else{
            position++;
        }
    }
    private static void forward(){
        if(position==message.length()-1){
            position++;
            return;
        }
        if(!message.substring(position,position+1).equals(" ")){
            position++;
            forward();
        }else{
            position++;
        }
    }

    public static void gotoBackWord(){
        if(position>0 && state!=RUN_STATE)
            position--;
        if(message.substring(position,position+1).equals(" "))
            position--;
        if(state==RUN_STATE&&position>3)
            position-=3;
        if(state==RUN_STATE&&position<2)
            position=0;
        back();
    }

    public static void gotoNextWord(){
        if(position>0 && state==RUN_STATE)
            position--;
        forward();
    }

    public void go(){
        state = RUN_STATE;
        if(task!=null)
            handler.removeCallbacks(task);
        handler = new Handler();

        task = new Runnable() {
            public void run() {
                textReader.setSelection(position);
                String chr = getChar();
                if(!brailleBandCommunicator.write(chr))
                    finish();
                if (position < message.length()) {
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
        handler.postDelayed(task, 10);
    }

    public static CommunicationActivity instance() {
        return inst;
    }

    @Override
    protected void onStart() {
        super.onStart();
        inst = this;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(autoSend)
            ((MenuItem) menu.findItem(R.id.auto_send)).setChecked(true);
        else
            ((MenuItem) menu.findItem(R.id.manual_send)).setChecked(true);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        switch (item.getItemId()){
            case R.id.auto_send:
                item.setChecked(true);
                autoSend = true;
                editor.putBoolean("autoSend", true);

                // Commit the edits!
                editor.commit();
                return true;
            case R.id.manual_send:
                item.setChecked(true);
                autoSend = false;
                editor.putBoolean("autoSend", false);

                // Commit the edits!
                editor.commit();
                return true;
            case R.id.check_sensors:
                if(!isReading){
                    isReading = true;
                    brailleBandCommunicator.write("%");
                    textReader.setText("");

                }


                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        //Get MAC address from DeviceListActivity via intent
        Intent intent = getIntent();

        //Get the MAC address from the DeviceListActivty via EXTRA
        address = intent.getStringExtra(MainActivity.EXTRA_DEVICE_ADDRESS);

        if(!brailleBandCommunicator.connect(address)) {
            showToastMessage("Cannot connect to the device");
            finish();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        brailleBandCommunicator.disConnect();
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btnMSEND = (Button) findViewById(R.id.btnSend);
        btnMBACK = (Button) findViewById(R.id.btnBack);
        btnMFORWARD = (Button) findViewById(R.id.btnForward);
        btnMSTOP = (Button) findViewById(R.id.btnStop);
        seekBarCHAR = (SeekBar) findViewById(R.id.seekBarChar);
        seekBarCHAR.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY));
        seekBarWORD = (SeekBar) findViewById(R.id.seekBarWord);
        seekBarWORD.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY));
        textReader = (EditText) findViewById(R.id.editText);

        /*
        * Bluetooth Connection.................
        * */

        brailleBandCommunicator = new BrailleBandCommunicator();
        checkBTState();

        //Get MAC address from DeviceListActivity via intent
        Intent intent = getIntent();

        //Get the MAC address from the DeviceListActivty via EXTRA
        address = intent.getStringExtra(MainActivity.EXTRA_DEVICE_ADDRESS);

        if(!brailleBandCommunicator.connect(address)) {
            showToastMessage("Cannot connect to the device");
            finish();
        }

         /*
        * button sounds
        * */

        final MediaPlayer mpStart = MediaPlayer.create(this, R.raw.start);
        final MediaPlayer mpPause = MediaPlayer.create(this, R.raw.pause);
        final MediaPlayer mpStop = MediaPlayer.create(this, R.raw.stop);
        final MediaPlayer mpFaward = MediaPlayer.create(this, R.raw.forward);
        final MediaPlayer mpBack = MediaPlayer.create(this, R.raw.back);


        /*
        * ........................................
        * */


        // Set up onClick listeners for buttons to send 1 or 0 to turn on/off LED
        btnMBACK.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!isBackPressed) {
                    setAllPressedFalse();
                    mpBack.start();
                    isBackPressed = true;
                } else {

                    try {
                        if (state.equals(STOP_STATE)) {
                            String input = textReader.getText().toString();
                            if (input.startsWith("#"))
                                CHR_GAP = Integer.valueOf(input.substring(1, input.length()));
                            else {
                                gotoBackWord();
                                textReader.setSelection(position);
                            }
                        } else {
                            if (task != null)
                                handler.removeCallbacks(task);
                            gotoBackWord();
                            textReader.setSelection(position);
                            if (state.equals(RUN_STATE))
                                go();
                        }
                    } catch (Exception ex) {
                    }
                    setAllPressedFalse();
                }
            }

        });



        btnMFORWARD.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!isFawardPressed) {
                    setAllPressedFalse();
                    mpFaward.start();
                    isFawardPressed = true;
                } else {

                    try {
                        if(state.equals(STOP_STATE)){
                            String input = textReader.getText().toString();
                            if(input.startsWith("#"))
                                WRD_GAP = Integer.valueOf(textReader.getText().toString());
                            else{
                                gotoNextWord();
                                textReader.setSelection(position);
                            }
                        }else{
                            if(task!=null)
                                handler.removeCallbacks(task);
                            gotoNextWord();
                            textReader.setSelection(position);
                            if(state.equals(RUN_STATE))
                                go();
                        }

                    }catch(Exception ex){}
                    setAllPressedFalse();
                }
            }
        });


        btnMSEND.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!isSendPressed) {
                    setAllPressedFalse();
                    mpStart.start();
                    isSendPressed = true;
                }else {
                    if (state.equals(STOP_STATE)) {
                        message = textReader.getText().toString();
                        position = 0;
                        go();
                    } else if (state.equals(PAUSE_STATE)) {
                        go();
                    } else if (state.equals(RUN_STATE)) {
                        if (task != null)
                            handler.removeCallbacks(task);
                        mpPause.start();
                        state = PAUSE_STATE;
                    }
                    setAllPressedFalse();
                }
            }
        });
        btnMSTOP.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!isStopPressed) {
                    setAllPressedFalse();
                    mpStop.start();
                    isStopPressed = true;
                }else {

                    if (task != null)
                        handler.removeCallbacks(task);
                    state = STOP_STATE;
                    position = 0;
                    textReader.setSelection(position);
                    setAllPressedFalse();
                }
            }
        });

        seekBarCHAR.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {


            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt("character_progress", progress);
                // Commit the edits!
                editor.commit();

                character_ratio = progress/100.0;
                if(character_ratio < 0.1)
                    character_ratio = 0.1;
                CHR_GAP = (int)(MAX_CHARACTER_GAP * character_ratio);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarWORD.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {


            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt("word_progress", progress);
                // Commit the edits!
                editor.commit();

                word_ratio = progress/100.0;
                if(word_ratio < 0.1)
                    word_ratio = 0.1;
                WRD_GAP = (int)(MAX_WORD_GAP * word_ratio);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });



         /*
        * Check previous preferences......
        * */

        //Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        autoSend = settings.getBoolean("autoSend", false);

        int character_progress  = settings.getInt("character_progress", 50);
        int word_progress  = settings.getInt("word_progress", 50);
        character_ratio = character_progress/100.0;
        word_ratio = word_progress/100.0;


        /*
        * ................................
        * */

        /*
        * Set Preferences.................
        * */

        seekBarCHAR.setProgress(character_progress);
        seekBarWORD.setProgress(word_progress);

        /*
        * .................................
        * */

        setColors();

    }

    private void setColors(){
        TextView textView1 = (TextView) findViewById(R.id.wdGap);
        textView1.setTextColor(ContextCompat.getColor(this,R.color.white));


        TextView textView2 = (TextView) findViewById(R.id.chGap);
        textView2.setTextColor(ContextCompat.getColor(this,R.color.white));


        Button btnSend = (Button) findViewById(R.id.btnSend);
        btnSend.setTextColor(ContextCompat.getColor(this,R.color.white));
        Button btnStop = (Button) findViewById(R.id.btnStop);
        btnStop.setTextColor(ContextCompat.getColor(this,R.color.white));

        Button btnFButton = (Button) findViewById(R.id.btnForward);
        btnFButton.setTextColor(ContextCompat.getColor(this,R.color.white));

        Button btnBButton = (Button) findViewById(R.id.btnBack);
        btnBButton.setTextColor(ContextCompat.getColor(this,R.color.white));


    }

    @Override
    public void onStop()
    {
        super.onStop();
        brailleBandCommunicator.disConnect();

    }

    private void checkBTState() {
        int check = brailleBandCommunicator.checkBTState();
        if(check == -1) {
            Toast.makeText(getBaseContext(), "Device does not support bluetooth", Toast.LENGTH_LONG).show();
            finish();
        } else if(check == 1){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
    }

    private void showToastMessage(String msg){
        Toast.makeText(this,msg, Toast.LENGTH_LONG).show();

    }



    public void onSmsReceived(String sms){

        textReader.setText(sms);

        if(autoSend) {
            message = textReader.getText().toString();
            position = 0;
            go();
        }
        showToastMessage(sms);
    }
}
