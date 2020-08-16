package dev.navids.noidaccessibility;

import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

public class RegularCommandExecutor{
    public static int executeCommand(NewCommand command){
        Log.i(AccessibilityUtil.TAG, String.format("Command state: %s, action: %s, actionExtra: %s, target: %s.",
                command.getExecutionState(),
                command.getAction(),
                command.getActionExtra(),
                command.getTargetWidgetInfo()));
        switch (command.getExecutionState()){
            case NewCommand.NOT_STARTED:
                Log.i(AccessibilityUtil.TAG, "--- Change to SEARCH");
                command.setExecutionState(NewCommand.SEARCH);
            case NewCommand.SEARCH:
                List<AccessibilityNodeInfo> similarNodes = AccessibilityUtil.findNodes(command.getTargetWidgetInfo());
                if(similarNodes.size() == 0){
                    Log.i(AccessibilityUtil.TAG, "The target widget could not be found in current screen.");
                    command.setExecutionState(NewCommand.FAILED);
                }
                else if(similarNodes.size() > 1){
                    Log.i(AccessibilityUtil.TAG, "There are more than one candidates for the target.");
                    for(AccessibilityNodeInfo node : similarNodes){
                        Log.i(AccessibilityUtil.TAG, " Node: " + node);
                    }
                    command.setExecutionState(NewCommand.FAILED);
                }
                else{
                    AccessibilityNodeInfo node = similarNodes.get(0);
                    NewWidgetInfo currentNodeInfo = NewWidgetInfo.create(node);
                    if (command.getAction().equals(NewCommand.CMD_ASSERT)) {
                        Log.i(AccessibilityUtil.TAG, "--- Do ASSERT");
                        command.setExecutionState(NewCommand.COMPLETED);
                    } else if (command.getAction().equals(NewCommand.CMD_CLICK)) {
                        Log.i(AccessibilityUtil.TAG, "--- Do CLICK " + node);
                        boolean clickResult = node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        Log.i(AccessibilityUtil.TAG, "--- Result " + clickResult);
                        if(!clickResult) {
                            AccessibilityNodeInfo clickableNode = node;
                            while (clickableNode != null && !clickableNode.isClickable())
                                clickableNode = clickableNode.getParent();
                            if (clickableNode == null || !clickableNode.isClickable()) {
                                Log.i(AccessibilityUtil.TAG, "The widget is not clickable.");
                                command.setExecutionState(NewCommand.FAILED);
                            } else {
                                clickableNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                command.setExecutionState(NewCommand.COMPLETED);
                            }
                        }
                        else{
                            command.setExecutionState(NewCommand.COMPLETED);
                        }
                    } else if (command.getAction().equals(NewCommand.CMD_TYPE)) {
                        Log.i(AccessibilityUtil.TAG, "--- Do TYPE AND NEXT");
                        AccessibilityUtil.performType(currentNodeInfo.getNodeCompat(), command.getActionExtra());
                        command.setExecutionState(NewCommand.COMPLETED);

                    } else {
                        Log.i(AccessibilityUtil.TAG, "Command's action is unknown " + command.getAction());
                        command.setExecutionState(NewCommand.FAILED);
                    }
                    return command.getExecutionState();
                }
                break;
            default:
                Log.i(AccessibilityUtil.TAG, "What I'm doing here?");
        }
        return command.getExecutionState();
    }
}
