package com.google.android.accessibility.switchaccess.noid;

import android.view.accessibility.AccessibilityNodeInfo;

import java.util.HashMap;
import java.util.Map;

public class Command {


    WidgetInfo widgetInfo;
    
    String action;
    String actionExtra;
    long startTime = -1;
    long endTime = -1;

    public long getTime(){
        return (endTime - startTime);
    }


    int executionState = 0;
    public final static int NOT_STARTED = 0;
    public final static int SEARCH = 1;
    public final static int COMPLETED = 2;
    public final static int FAILED = 3;
    public final static int COMPLETED_BY_REGULAR = 4;
    public final static int COMPLETED_BY_REGULAR_UNABLE_TO_DETECT = 5;
    public static String getActionStr(int action){
        switch (action){
            case NOT_STARTED:
                return "NOT_STARTED";
            case SEARCH:
                return "SEARCH";
            case COMPLETED:
                return "COMPLETED";
            case FAILED:
                return "FAILED";
            case COMPLETED_BY_REGULAR:
                return "COMPLETED_BY_REGULAR";
            case COMPLETED_BY_REGULAR_UNABLE_TO_DETECT:
                return "COMPLETED_BY_REGULAR_UNABLE_TO_DETECT";
            default:
                return "UNKNOWN";
        }
    }

    public final static String CMD_CLICK = "CLICK";
    public final static String CMD_TYPE = "TYPE";
    public final static String CMD_ASSERT = "ASSERT";

    public int numberOfAttempts = 0;
    private int numberOfActions = 0;
    private Map<String, Integer> visitedWidgets = new HashMap<>();
    public int trackAction(AccessibilityNodeInfo node){
        WidgetInfo widgetInfo = WidgetInfo.create(node);
        numberOfActions++;
        int visitedWidgetCount = visitedWidgets.getOrDefault(widgetInfo.getXpath(), 0);
        visitedWidgetCount++;
        visitedWidgets.put(widgetInfo.getXpath(), visitedWidgetCount);
        return visitedWidgetCount;
    }

    public int getNumberOfActions() {
        return numberOfActions;
    }

    public final static int MAX_ATTEMPT = 3;
    public final static int MAX_VISITED_WIDGET = 3;

    public Command(WidgetInfo widgetInfo, String action) {
        this(widgetInfo, action, null);
    }

    public Command(WidgetInfo widgetInfo, String action, String actionExtra) {
        this.widgetInfo = widgetInfo;
        this.action = action;
        this.actionExtra = actionExtra;
    }

    public WidgetInfo getTargetWidgetInfo() {
        return widgetInfo;
    }

    public int getExecutionState() {
        return executionState;
    }

    public void setExecutionState(int executionState) {
        this.executionState = executionState;
        if(executionState == NOT_STARTED){
            startTime = endTime = -1;
        }
        else if(executionState == SEARCH) {
            startTime = System.currentTimeMillis();
            endTime = -1;
        }
        else if(executionState == COMPLETED
                || executionState == COMPLETED_BY_REGULAR_UNABLE_TO_DETECT
                || executionState == COMPLETED_BY_REGULAR){
            endTime = System.currentTimeMillis();
        }
    }

    public String getAction() {
        return action;
    }

    public String getActionExtra() {
        return actionExtra;
    }
}
