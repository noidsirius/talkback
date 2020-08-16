package com.google.android.accessibility.switchaccess.TestExecutor;


import com.google.android.accessibility.switchaccess.treenodes.TreeScanNode;
import com.google.android.accessibility.switchaccess.treenodes.TreeScanSystemProvidedNode;
import com.google.android.accessibility.switchaccess.treenodes.TreeScanSelectionNode;

import com.android.switchaccess.SwitchAccessService;
import com.google.android.accessibility.switchaccess.SwitchAccessNodeCompat;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import androidx.annotation.RequiresApi;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityWindowInfoCompat;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class SwitchAccessCommandExecutor2 {
//    public static long LAST_DOWN_TIME = 0;
//    public static List<Command> commandList = new ArrayList<>();
//    public static int lastCommandIndex = -1;
//    public static String TAG = "NOID_SA";
//    public static boolean lock = false;
//    public static Map<Integer, String> pendingActions = new HashMap<>();
//    public static int pendingActionId = 0;
//    public static WidgetInfo latestWidgetInfo;
//    public static interface MyCallBack{
//        void callback();
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.N)
//    public static void initCommands(){
//        if(commandList.size() > 0)
//            return;
//        String fileName = "test_guideline.json";
//        // TODO: check context.getFilesDir().getPath();
//        String dir = "/data/local/tmp/";
////            String dir = context.getFilesDir().getPath();
//        Log.i(TAG, "Path: " + dir);
//        File file = new File(dir, fileName);
//        //JSON parser object to parse read file
//        JSONParser jsonParser = new JSONParser();
//        JSONArray commandsJson = null;
//        try (FileReader reader = new FileReader(file))
//        {
//            // TODO: tons of refactor!
//            //Read JSON file
//            Object obj = jsonParser.parse(reader);
//            commandsJson = (JSONArray) obj;
//            for(int i=0; i<commandsJson.size(); i++){
////                for(int i=0; i<5; i++){
//                JSONObject cmd = (JSONObject) commandsJson.get(i);
//                if(cmd == null){
//                    Log.i(TAG, "Json Command is null!");
//                    continue;
//                }
//                String action = (String) cmd.getOrDefault("action", "UNKNOWN");
//                if(action.equals("click"))
//                    action = Command.CMD_CLICK;
//                else if(action.equals("send_keys"))
//                    action = Command.CMD_TYPE;
//                else
//                    action = "UNKNOWN";
//                String resourceId = (String) cmd.getOrDefault("id", "");
//                String contentDescription = (String) cmd.getOrDefault("accessibility_id", "");
//                String message = "";
//                if(cmd.containsKey("action_args")) {
//                    JSONArray args = (JSONArray) cmd.get("action_args");
//                    if(action.equals(Command.CMD_TYPE))
//                        message = String.valueOf(args.get(0));
//                    else
//                        Log.i(TAG,"-------> Args: " + args);
//                }
//                Log.i(TAG, "Json " + action + " " + resourceId + " " + message + " " + contentDescription);
//                commandList.add(new Command(new WidgetInfo(resourceId, contentDescription, ""),
//                                            action,
//                                            message));
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
////        commandList.add(new Command(
////                new WidgetInfo("dev.navids.demoapp:id/username"),
////                Command.CMD_TYPE,
////                "test@test.com"));
////        commandList.add(new Command(
////                new WidgetInfo("dev.navids.demoapp:id/password"),
////                Command.CMD_TYPE,
////                "12345678"));
////        commandList.add(new Command(
////                new WidgetInfo("dev.navids.demoapp:id/checkBox"),
////                Command.CMD_CLICK));
////        commandList.add(new Command(
////                new WidgetInfo("dev.navids.demoapp:id/login"),
////                Command.CMD_CLICK));
//
////        commandList.add(new Command(
////                new WidgetInfo("com.android.systemui:id/home"),
////                Command.CMD_ASSERT));
//        lastCommandIndex = 100;
//    }
//
//    public static void manageCommands(WidgetInfo currentNodeInfo){
//        latestWidgetInfo = currentNodeInfo;
//        lock = false;
//        Log.i(TAG, "--------------------------\nPending Actions");
//        for(Map.Entry<Integer, String> entry : pendingActions.entrySet()){
//            Log.i(TAG, "  " + entry.getKey() + " " + entry.getValue());
//        }
//        if(pendingActions.size() > 0){
//            Log.i(TAG, "Ignore this manageCommand since we're waiting for another action");
//            return;
//        }
//        if(lastCommandIndex == -1)
//            initCommands();
//        if(lastCommandIndex >= commandList.size()) {
//            Log.i(TAG, "----------The test case is completed!---------");
//            return;
//        }
//        Command currentCommand = commandList.get(lastCommandIndex);
//        if(executeCommand(currentCommand, currentNodeInfo) == Command.COMPLETED)
//            lastCommandIndex++;
//    }
//
//    public static int executeCommand(Command command, WidgetInfo currentNodeInfo){
//        Log.i(TAG, String.format("Command state: %s, action: %s, actionExtra: %s, target: %s, current: %s .",
//                command.getExecutionState(),
//                command.getAction(),
//                command.getActionExtra(),
//                command.getTargetWidgetInfo(),
//                currentNodeInfo));
//        switch (command.getExecutionState()){
//            case Command.NOT_STARTED:
//                Log.i(TAG, "--- Change to SEARCH");
//                command.setExecutionState(Command.SEARCH);
//            case Command.SEARCH:
//                if(command.getTargetWidgetInfo().isSimilar(currentNodeInfo)){
//                    if(command.getAction().equals(Command.CMD_ASSERT)){
//                        Log.i(TAG, "--- Do ASSERT");
//                    }
//                    else if (command.getAction().equals(Command.CMD_CLICK)){
//                        Log.i(TAG, "--- Do CLICK");
//                        performSelect();
//                    }
//                    else if (command.getAction().equals(Command.CMD_TYPE)){
//                        Log.i(TAG, "--- Do TYPE AND NEXT");
//                        performType(currentNodeInfo.getNodeCompat(), command.getActionExtra());
//                        performNext();
//                    }
//                    else{
//                        Log.i(TAG, "Command's action is unknown " + command.getAction());
//                    }
//                    command.setExecutionState(Command.COMPLETED);
////                    lock = true;
////                    new Handler().postDelayed(() -> {
////                        if(lock) {
////                            Log.i(TAG, "Trigger manageCommands");
////                            manageCommands(new WidgetInfo(""));
////                        }
////                    }, 500);
//                }
//                else{
//                    Log.i(TAG, "--- Do NEXT");
//                    performNext();
//                }
//        }
//        return command.getExecutionState();
//    }
//
//    public static void performNext(){ performNext(null); }
//
//    public static void performNext(MyCallBack myCallBack){
//        performMyAction(false, myCallBack);
//    }
//
//    public static void performSelect(){ performSelect(null); }
//
//    public static void performSelect(MyCallBack myCallBack){
//        performMyAction(true, myCallBack);
//    }
//
//    public static void performMyAction(boolean isSelect, MyCallBack myCallBack){
//        WidgetInfo myWidgetInfo = latestWidgetInfo;
//        Log.i(TAG, " ======== BEGINNING of " + (isSelect ? "SELECT" : "NEXT") + " " + myWidgetInfo);
//        int code = isSelect ? KeyEvent.KEYCODE_VOLUME_UP: KeyEvent.KEYCODE_VOLUME_DOWN;
//        if(SwitchAccessService.getInstance() != null){
//            final int thisActionId = pendingActionId;
//            pendingActionId++;
//            pendingActions.put(thisActionId, "Pending: I'm going to do " + (isSelect ? "SELECT" : "NEXT") + " " + myWidgetInfo);
//            Handler mainHandler = new Handler(SwitchAccessService.getInstance().getMainLooper());
//            mainHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    Log.i(TAG, " ======== KEYBOARD_DOWN " + (isSelect ? "SELECT" : "NEXT") + " My " + myWidgetInfo + " Latest " + latestWidgetInfo);
//                    pendingActions.remove(thisActionId);
//                    long new_down_time = 0;//LAST_DOWN_TIME + 1000;
//                    KeyEvent down = new KeyEvent(new_down_time,new_down_time, KeyEvent.ACTION_DOWN, code, 0,0,0,114,0x8,0x101);
//                    KeyEvent up = new KeyEvent(new_down_time,new_down_time, KeyEvent.ACTION_UP, code, 0,0,0,114,0x8,0x101);
//
//                    mainHandler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            Log.i(TAG, " ======== KEYBOARD_UP " + (isSelect ? "SELECT" : "NEXT") + " My " + myWidgetInfo + " Latest " + latestWidgetInfo);
//                            SwitchAccessService.getInstance().onKeyEvent(up);//new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_VOLUME_DOWN));
//                            if(myCallBack != null)
//                                myCallBack.callback();
//                        }
//                    });
//                    SwitchAccessService.getInstance().onKeyEvent(down);//new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_DOWN));
//                }
//            }, 400);
//        }
//    }
//
//    public static void performType(SwitchAccessNodeCompat node, String message){
////        Log.i(TAG, " ======== BEGINNING of TYPE");
//        Bundle arguments = new Bundle();
//        arguments.putCharSequence(SwitchAccessNodeCompat
//                .ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, message);
//        node.performAction(SwitchAccessNodeCompat.ACTION_SET_TEXT, arguments);
//    }
}
