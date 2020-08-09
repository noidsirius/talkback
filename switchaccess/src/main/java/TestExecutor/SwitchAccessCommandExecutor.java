package com.google.android.accessibility.switchaccess.TestExecutor;


import com.android.switchaccess.SwitchAccessService;
import com.google.android.accessibility.switchaccess.SwitchAccessNodeCompat;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;

import java.util.ArrayList;
import java.util.List;

public class SwitchAccessCommandExecutor {
    public static long LAST_DOWN_TIME = 0;
    public static List<Command> commandList = new ArrayList<>();
    public static int lastCommandIndex = -1;
    public static String TAG = "NOID_SA";
    public static boolean lock = false;
    public static int pendingActions = 0;
    public static interface MyCallBack{
        void callback();
    }

    public static void initCommands(){
        if(commandList.size() > 0)
            return;
        commandList.add(new Command(
                new WidgetInfo("dev.navids.demoapp:id/username"),
                Command.CMD_TYPE,
                "test@test.com"));
        commandList.add(new Command(
                new WidgetInfo("dev.navids.demoapp:id/password"),
                Command.CMD_TYPE,
                "12345678"));
        commandList.add(new Command(
                new WidgetInfo("dev.navids.demoapp:id/checkBox"),
                Command.CMD_CLICK));
        commandList.add(new Command(
                new WidgetInfo("dev.navids.demoapp:id/login"),
                Command.CMD_CLICK));
//        commandList.add(new Command(
//                new WidgetInfo("com.android.systemui:id/home"),
//                Command.CMD_ASSERT));
        lastCommandIndex = 0;
    }

    public static void manageCommands(WidgetInfo currentNodeInfo){
        lock = false;
        if(pendingActions > 0){
            Log.i(TAG, "Ignore this manageCommand since we're waiting for another action");
            return;
        }
        if(lastCommandIndex == -1)
            initCommands();
        if(lastCommandIndex >= commandList.size())
            return;
        Command currentCommand = commandList.get(lastCommandIndex);
        if(executeCommand(currentCommand, currentNodeInfo) == Command.COMPLETED)
            lastCommandIndex++;
    }

    public static int executeCommand(Command command, WidgetInfo currentNodeInfo){
        Log.i(TAG, String.format("Command state: %s, action: %s, actionExtra: %s, target: %s, current: %s .",
                command.getExecutionState(),
                command.getAction(),
                command.getActionExtra(),
                command.getTargetWidgetInfo(),
                currentNodeInfo));
        switch (command.getExecutionState()){
            case Command.NOT_STARTED:
                Log.i(TAG, "--- Change to SEARCH");
                command.setExecutionState(Command.SEARCH);
            case Command.SEARCH:
                if(command.getTargetWidgetInfo().isSimilar(currentNodeInfo)){
                    if(command.getAction().equals(Command.CMD_ASSERT)){
                        Log.i(TAG, "--- Do ASSERT");
                    }
                    else if (command.getAction().equals(Command.CMD_CLICK)){
                        Log.i(TAG, "--- Do CLICK");
                        performSelect();
                    }
                    else if (command.getAction().equals(Command.CMD_TYPE)){
                        Log.i(TAG, "--- Do TYPE AND NEXT");
                        performType(currentNodeInfo.getNodeCompat(), command.getActionExtra());
                        performNext();
                    }
                    else{
                        Log.i(TAG, "Command's action is unknown " + command.getAction());
                    }
                    command.setExecutionState(Command.COMPLETED);
//                    lock = true;
//                    new Handler().postDelayed(() -> {
//                        if(lock) {
//                            Log.i(TAG, "Trigger manageCommands");
//                            manageCommands(new WidgetInfo(""));
//                        }
//                    }, 500);
                }
                else{
                    Log.i(TAG, "--- Do NEXT");
                    performNext();
                }
        }
        return command.getExecutionState();
    }

    public static void performNext(){ performNext(null); }

    public static void performNext(MyCallBack myCallBack){
        performMyAction(false, myCallBack);
    }

    public static void performSelect(){ performSelect(null); }

    public static void performSelect(MyCallBack myCallBack){
        performMyAction(true, myCallBack);
    }

    public static void performMyAction(boolean isSelect, MyCallBack myCallBack){
        Log.i(TAG, " ======== BEGINNING of " + (isSelect ? "SELECT" : "NEXT"));
        int code = isSelect ? KeyEvent.KEYCODE_VOLUME_UP: KeyEvent.KEYCODE_VOLUME_DOWN;
        if(SwitchAccessService.getInstance() != null){
            pendingActions++;
            Handler mainHandler = new Handler(SwitchAccessService.getInstance().getMainLooper());
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, " ======== KEYBOARD_DOWN " + (isSelect ? "SELECT" : "NEXT"));
                    pendingActions--;
                    long new_down_time = LAST_DOWN_TIME + 1000;
                    KeyEvent down = new KeyEvent(new_down_time,new_down_time, KeyEvent.ACTION_DOWN, code, 0,0,0,114,0x8,0x101);
                    KeyEvent up = new KeyEvent(new_down_time,new_down_time, KeyEvent.ACTION_UP, code, 0,0,0,114,0x8,0x101);
                    SwitchAccessService.getInstance().onKeyEvent(down);//new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_DOWN));
                    mainHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, " ======== KEYBOARD_UP " + (isSelect ? "SELECT" : "NEXT"));
                            SwitchAccessService.getInstance().onKeyEvent(up);//new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_VOLUME_DOWN));
                            if(myCallBack != null)
                                myCallBack.callback();
                        }
                    }, 50);
                }
            }, 1200);
        }
    }

    public static void performType(SwitchAccessNodeCompat node, String message){
//        Log.i(TAG, " ======== BEGINNING of TYPE");
        Bundle arguments = new Bundle();
        arguments.putCharSequence(SwitchAccessNodeCompat
                .ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, message);
        node.performAction(SwitchAccessNodeCompat.ACTION_SET_TEXT, arguments);
    }
}
