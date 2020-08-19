package com.google.android.accessibility.switchaccess.noid;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.RequiresApi;

import com.android.switchaccess.SwitchAccessService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TalkBackCommandExecutor {
    private static AccessibilityNodeInfo focusedNode;
    private static boolean changedFocused = false;
    private static int waitingForFocusChange = 0;
    private static final int MAX_WAIT_FOR_CHANGE = 2;
    public static Map<Integer, String> pendingActions = new HashMap<>();
    public static int pendingActionId = 0;
    public static interface MyCallBack{
        void callback();
    }
    public static void setFocusedNode(AccessibilityNodeInfo node){
        focusedNode = node;
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public static int executeCommand(Command command){
        Log.i(AccessibilityUtil.TAG, String.format("TalkBack Command state: %s, action: %s, actionExtra: %s, target: %s.",
                command.getExecutionState(),
                command.getAction(),
                command.getActionExtra(),
                command.getTargetWidgetInfo()));
        Log.i(AccessibilityUtil.TAG, "-- Current Focused Node" + focusedNode);
        if(focusedNode == null){
            waitingForFocusChange++;
            if(waitingForFocusChange < MAX_WAIT_FOR_CHANGE) {
                Log.i(AccessibilityUtil.TAG, "Ignore this manageCommand since no node has been changed " + waitingForFocusChange);
                return command.getExecutionState();
            }
            waitingForFocusChange = 0;
        }
        if(pendingActions.size() > 0){
            Log.i(AccessibilityUtil.TAG, "Ignore this manageCommand since we're waiting for another action");
            return command.getExecutionState();
        }
        AccessibilityUtil accessibilityUtil = new AccessibilityUtil(SwitchAccessService.getInstance());
        switch (command.getExecutionState()){
            case Command.NOT_STARTED:
                Log.i(AccessibilityUtil.TAG, "--- Change to SEARCH");
                command.setExecutionState(Command.SEARCH);
            case Command.SEARCH:
                List<AccessibilityNodeInfo> similarNodes = AccessibilityUtil.findNodes(command.getTargetWidgetInfo());
                if(similarNodes.size() == 0){
                    Log.i(AccessibilityUtil.TAG, "The target widget could not be found in current screen.");
                    command.numberOfAttempts++;
                    if(command.numberOfAttempts >= Command.MAX_ATTEMPT) {
                        Log.i(AccessibilityUtil.TAG, "Couldn't locate the widget, Execute the command using regular test executor");
                        List<String> maskedAttributes = new ArrayList<>(WidgetInfo.maskedAttributes);
                        WidgetInfo.maskedAttributes.clear();
                        RegularCommandExecutor.executeCommand(command);
                        WidgetInfo.maskedAttributes = new ArrayList<>(maskedAttributes);
                        if(command.getExecutionState() == Command.COMPLETED)
                            command.setExecutionState(Command.COMPLETED_BY_REGULAR_UNABLE_TO_DETECT);
                        else
                            command.setExecutionState(Command.FAILED);
                        return command.getExecutionState();
                    }
                }
                else if(similarNodes.size() > 1){
                    Log.i(AccessibilityUtil.TAG, "There are more than one candidates for the target.");
                    for(AccessibilityNodeInfo node : similarNodes){
                        Log.i(AccessibilityUtil.TAG, " Node: " + node);
                    }
                    command.numberOfAttempts++;
                    if(command.numberOfAttempts >= Command.MAX_ATTEMPT) {
                        Log.i(AccessibilityUtil.TAG, "Couldn't locate the widget, Execute the command using regular test executor");
                        List<String> maskedAttributes = new ArrayList<>(WidgetInfo.maskedAttributes);
                        WidgetInfo.maskedAttributes.clear();
                        RegularCommandExecutor.executeCommand(command);
                        WidgetInfo.maskedAttributes = new ArrayList<>(maskedAttributes);
                        if(command.getExecutionState() == Command.COMPLETED)
                            command.setExecutionState(Command.COMPLETED_BY_REGULAR_UNABLE_TO_DETECT);
                        else
                            command.setExecutionState(Command.FAILED);
                        return command.getExecutionState();
                    }
                }
                else{
                    AccessibilityNodeInfo node = similarNodes.get(0);
                    if(focusedNode != null) {
                        int trackAction = command.trackAction(focusedNode);
                        Log.i(AccessibilityUtil.TAG, String.format("The following widget has been visited %d times: %s", trackAction, WidgetInfo.create(focusedNode)));
                        if(trackAction > Command.MAX_VISITED_WIDGET){
                            Log.i(AccessibilityUtil.TAG, "Execute the command using regular test executor");
                            RegularCommandExecutor.executeCommand(command);
                            if(command.getExecutionState() == Command.COMPLETED)
                                command.setExecutionState(Command.COMPLETED_BY_REGULAR);
                            return command.getExecutionState();
                        }
                        AccessibilityNodeInfo it = node;
                        boolean isSimilar = false;
                        AccessibilityNodeInfo firstReachableNode = null;
                        while(it != null){

                            if(it.isClickable()){
                                firstReachableNode = it;
                                break;
                            }
                            it = it.getParent();
                        }
                        Log.i(AccessibilityUtil.TAG, "-- FIRST REACHABLE NODE IS " + firstReachableNode);
                        isSimilar = firstReachableNode != null && firstReachableNode.equals(focusedNode);
                        if (isSimilar) {
                            if (command.getAction().equals(Command.CMD_ASSERT)) {
                                Log.i(AccessibilityUtil.TAG, "--- Do ASSERT");
                            } else if (command.getAction().equals(Command.CMD_CLICK)) {
                                Log.i(AccessibilityUtil.TAG, "--- Do CLICK");
//                                command.numberOfActions++;
                                accessibilityUtil.performDoubleTap();
                            } else if (command.getAction().equals(Command.CMD_TYPE)) {
                                Log.i(AccessibilityUtil.TAG, "--- Do TYPE AND NEXT");
                                AccessibilityUtil.performType(focusedNode, command.getActionExtra());
//                                command.numberOfActions++;
                                performNext(null);
                            } else {
                                Log.i(AccessibilityUtil.TAG, "Command's action is unknown " + command.getAction());
                            }
                            command.setExecutionState(Command.COMPLETED);
                        } else {
                            Log.i(AccessibilityUtil.TAG, "--- Do NEXT");
//                            command.numberOfActions++;
                            performNext(null);
                        }
                    }
                    else{
                        Log.i(AccessibilityUtil.TAG, "--- Node is not focused. Do NEXT");
//                        command.numberOfActions++;
                        performNext(null);
                    }
                    return command.getExecutionState();
                }
                break;
            default:
                Log.i(AccessibilityUtil.TAG, "What I'm doing here?");
        }
        return command.getExecutionState();
    }

    public static boolean  performNext(AccessibilityService.GestureResultCallback callback){
        Log.i(AccessibilityUtil.TAG, "performNext");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        Path swipePath = new Path();
        swipePath.moveTo(200, 500);
        swipePath.lineTo(800, 550);
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, 400));
        GestureDescription gestureDescription = gestureBuilder.build();
        Log.i(AccessibilityUtil.TAG, "Execute Gesture " + gestureDescription.toString());
        return SwitchAccessService.getInstance().dispatchGesture(gestureDescription, callback, null);
    }



}
