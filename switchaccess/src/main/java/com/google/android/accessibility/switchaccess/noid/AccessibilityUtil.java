package com.google.android.accessibility.switchaccess.noid;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.RequiresApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AccessibilityUtil {
    AccessibilityService accessibilityService;
    AccessibilityService.GestureResultCallback defaultCallBack;
    @RequiresApi(api = Build.VERSION_CODES.N)
    public AccessibilityUtil(AccessibilityService accessibilityService) {
        this.accessibilityService = accessibilityService;
        defaultCallBack = new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                Log.i(TAG, "Complete Gesture " + gestureDescription.toString());
                super.onCompleted(gestureDescription);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                Log.i(TAG, "Cancel Gesture " + gestureDescription.toString());
                super.onCancelled(gestureDescription);
            }
        };
    }

    void logTestGuideline(AccessibilityEvent event) throws JSONException {
        if(event == null)
            return;
        if(event.getEventType() != AccessibilityEvent.TYPE_VIEW_CLICKED
                && event.getEventType() != AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED
                && event.getEventType() != AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED
        )
            return;

        JSONObject guiEvent = new JSONObject();
        guiEvent.put("PackageName", event.getPackageName());
        guiEvent.put("EventText", event.getText());
        guiEvent.put("EventType", AccessibilityEvent.eventTypeToString(event.getEventType()));
        guiEvent.put("EventClass", event.getClassName());
        AccessibilityNodeInfo currentNode = event.getSource();
        if(currentNode != null){
            guiEvent.put("ViewID", currentNode.getViewIdResourceName());
            guiEvent.put("ContentDescription", currentNode.getContentDescription());
            guiEvent.put("Text", currentNode.getText());
            Rect box = new Rect();
            currentNode.getBoundsInScreen(box);
            guiEvent.put("Box", box);
        }
        else{
            guiEvent.put("ViewID", "");
            guiEvent.put("ContentDescription", "");
            guiEvent.put("Text", "");
            guiEvent.put("Box", "");
        }
        AccessibilityNodeInfo rootNode = this.accessibilityService.getRootInActiveWindow();
        if(rootNode != null ){
            guiEvent.put("Root", rootNode);
        }
        else{
            guiEvent.put("Root", "");
        }
        Log.i(AccessibilityUtil.INSTRUMENT_TAG, guiEvent.toString());
    }


    public static final String TAG = "NOID_SA_2";
    public static final String INSTRUMENT_TAG = "NOID_INSTRUMENT";
    public static final int tapDuration = 100;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean performTap(int x, int y){ return performTap(x, y, tapDuration); }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean performTap(int x, int y, int duration){ return performTap(x, y, 0, duration); }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean performTap(int x, int y, int startTime, int duration){ return performTap(x, y, startTime, duration, defaultCallBack); }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean performTap(int x, int y, int startTime, int duration, AccessibilityService.GestureResultCallback callback){
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        Path swipePath = new Path();
        swipePath.moveTo(x, y);
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, startTime, duration));
        GestureDescription gestureDescription = gestureBuilder.build();
        Log.i(TAG, "Execute Gesture " + gestureDescription.toString());
        return accessibilityService.dispatchGesture(gestureDescription, callback, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean performNext(AccessibilityService.GestureResultCallback callback){
        Log.i(TAG, "performNext");
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        Path swipePath = new Path();
        swipePath.moveTo(300, 500);
        swipePath.lineTo(700, 500);
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, 200));
        GestureDescription gestureDescription = gestureBuilder.build();
        Log.i(TAG, "Execute Gesture " + gestureDescription.toString());
        return accessibilityService.dispatchGesture(gestureDescription, callback, null);
    }

    public static boolean performType(AccessibilityNodeInfo node, String message){
        Log.i(TAG, "performType");
        Bundle arguments = new Bundle();
        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, message);
        return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean performDoubleTap(){
        Log.i(TAG, "performDoubleTap");
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return performDoubleTap(0, 0); }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean performDoubleTap(int x, int y){ return performDoubleTap(x, y, tapDuration); }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean performDoubleTap(int x, int y, int duration){ return performDoubleTap(x, y, 0, duration); }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean performDoubleTap(int x, int y, int startTime, int duration){ return performDoubleTap(x, y, startTime, duration, defaultCallBack); }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean performDoubleTap(final int x, final int y, final int startTime, final int duration, final AccessibilityService.GestureResultCallback callback){
        AccessibilityService.GestureResultCallback newClickCallBack = new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                Log.i(TAG, "Complete Gesture " + gestureDescription.getStrokeCount());
                super.onCompleted(gestureDescription);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                performTap(x, y, startTime, duration, callback);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                Log.i(TAG, "Cancel Gesture");
                super.onCancelled(gestureDescription);
            }
        };
        return performTap(x, y, startTime, duration, newClickCallBack);
    }


    public static List<AccessibilityNodeInfo> getAllA11yNodeInfo(boolean log){
        return getAllA11yNodeInfo(com.android.switchaccess.SwitchAccessService.getInstance().getRootInActiveWindow(), " /", log);
    }

    public static List<AccessibilityNodeInfo> getAllA11yNodeInfo(AccessibilityNodeInfo rootNode, boolean log){
        return getAllA11yNodeInfo(rootNode, " /", log);
    }

    private static List<AccessibilityNodeInfo> getAllA11yNodeInfo(AccessibilityNodeInfo rootNode, String prefix, boolean log){
        if(rootNode == null) {
            Log.i(AccessibilityUtil.TAG, "The root node is null!");
            return new ArrayList<>();
        }
        List<AccessibilityNodeInfo> result = new ArrayList<>();
        result.add(rootNode);
        if(log)
            Log.i(AccessibilityUtil.TAG, prefix + "/" + rootNode.getClassName() + " " + rootNode.getViewIdResourceName() + " " + rootNode.getText() + " " + rootNode.getContentDescription());
        for(int i=0; i<rootNode.getChildCount(); i++)
            result.addAll(getAllA11yNodeInfo(rootNode.getChild(i), " " +prefix+rootNode.getClassName()+"/", log));
        return result;
    }

    public static List<AccessibilityNodeInfo> findNodes(WidgetInfo target){
        List<AccessibilityNodeInfo> result = new ArrayList<>();
        for(AccessibilityNodeInfo node : getAllA11yNodeInfo(false)) {
            WidgetInfo currentNodeInfo = WidgetInfo.create(node);
            if (target.isSimilar(currentNodeInfo))
                result.add(node);
        }
        return result;
    }

}