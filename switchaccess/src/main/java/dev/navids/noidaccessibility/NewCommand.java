package dev.navids.noidaccessibility;

public class NewCommand {


    NewWidgetInfo widgetInfo;
    
    String action;
    String actionExtra;

    int executionState = 0;
    public final static int NOT_STARTED = 0;
    public final static int SEARCH = 1;
    public final static int COMPLETED = 2;
    public final static int FAILED = 3;

    public final static String CMD_CLICK = "CLICK";
    public final static String CMD_TYPE = "TYPE";
    public final static String CMD_ASSERT = "ASSERT";

    public NewCommand(NewWidgetInfo widgetInfo, String action) {
        this(widgetInfo, action, null);
    }

    public NewCommand(NewWidgetInfo widgetInfo, String action, String actionExtra) {
        this.widgetInfo = widgetInfo;
        this.action = action;
        this.actionExtra = actionExtra;
    }

    public NewWidgetInfo getTargetWidgetInfo() {
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
