package com.google.android.accessibility.switchaccess.noid;

public class Command {


    WidgetInfo widgetInfo;
    
    String action;
    String actionExtra;

    int executionState = 0;
    public final static int NOT_STARTED = 0;
    public final static int SEARCH = 1;
    public final static int COMPLETED = 2;
    public final static int FAILED = 3;
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
            default:
                return "UNKNOWN";
        }
    }

    public final static String CMD_CLICK = "CLICK";
    public final static String CMD_TYPE = "TYPE";
    public final static String CMD_ASSERT = "ASSERT";

    public int numberOfAttempts = 0;
    public int numberOfActions = 0;
    public final static int MAX_ATTEMPT = 3;

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
    }

    public String getAction() {
        return action;
    }

    public String getActionExtra() {
        return actionExtra;
    }
}
