package com.google.android.accessibility.switchaccess.noid;

import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckPreset;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheck;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.checks.TraversalOrderCheck;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class RegularCommandExecutor{
    public static boolean clickPhysical = true;
    public static boolean reportA11YIssue = true;
    public static int executeCommand(Command command){
        Log.i(AccessibilityUtil.TAG, String.format("Regular Command state: %s, action: %s, actionExtra: %s, target: %s.",
                command.getExecutionState(),
                command.getAction(),
                command.getActionExtra(),
                command.getTargetWidgetInfo()));
        switch (command.getExecutionState()){
            case Command.NOT_STARTED:
                Log.i(AccessibilityUtil.TAG, "--- Change to SEARCH");
                command.setExecutionState(Command.SEARCH);
            case Command.SEARCH:
                List<AccessibilityNodeInfo> similarNodes = AccessibilityUtil.findNodes(command.getTargetWidgetInfo());
                if(similarNodes.size() == 0){
                    Log.i(AccessibilityUtil.TAG, "The target widget could not be found in current screen.");
                    command.numberOfAttempts++;
                    if(command.numberOfAttempts >= Command.MAX_ATTEMPT)
                        command.setExecutionState(Command.FAILED);
                }
                else if(similarNodes.size() > 1){
                    Log.i(AccessibilityUtil.TAG, "There are more than one candidates for the target.");
                    for(AccessibilityNodeInfo node : similarNodes){
                        Log.i(AccessibilityUtil.TAG, " Node: " + node);
                    }
                    command.numberOfAttempts++;
                    if(command.numberOfAttempts >= Command.MAX_ATTEMPT)
                        command.setExecutionState(Command.FAILED);
                }
                else{
                    AccessibilityNodeInfo node = similarNodes.get(0);
                    if(reportA11YIssue)
                        command.setHasWidgetA11YIssue(getA11yIssues(node).size() > 0);
                    int trackAction = command.trackAction(node);
                    Log.i(AccessibilityUtil.TAG, String.format("The following widget has been visited %d times: %s", trackAction, WidgetInfo.create(node)));
                    WidgetInfo currentNodeInfo = WidgetInfo.create(node);
                    if (command.getAction().equals(Command.CMD_ASSERT)) {
                        Log.i(AccessibilityUtil.TAG, "--- Do ASSERT");
                        command.setExecutionState(Command.COMPLETED);
                    } else if (command.getAction().equals(Command.CMD_CLICK)) {
                        Log.i(AccessibilityUtil.TAG, "--- Do CLICK " + node);
                        boolean clickResult = false;
                        if(clickPhysical) {
                            AccessibilityUtil autil = new AccessibilityUtil(com.android.switchaccess.SwitchAccessService.getInstance());
                            Rect box = new Rect();
                            node.getBoundsInScreen(box);
                            int x = (box.left + box.right) / 2;
                            int y = (box.top + box.bottom) / 2;
                            clickResult = autil.performTap(x, y);
                        }
                        else {
                            clickResult = node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        }
                        Log.i(AccessibilityUtil.TAG, "--- Result " + clickResult);
                        if(!clickResult) {
                            AccessibilityNodeInfo clickableNode = node;
                            while (clickableNode != null && !clickableNode.isClickable())
                                clickableNode = clickableNode.getParent();
                            if (clickableNode == null || !clickableNode.isClickable()) {
                                Log.i(AccessibilityUtil.TAG, "The widget is not clickable.");
                                command.setExecutionState(Command.FAILED);
                            } else {
                                clickableNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                                command.numberOfActions++;
                                command.setExecutionState(Command.COMPLETED);
                            }
                        }
                        else{
//                            command.numberOfActions++;
                            command.setExecutionState(Command.COMPLETED);
                        }
                    } else if (command.getAction().equals(Command.CMD_TYPE)) {
                        Log.i(AccessibilityUtil.TAG, "--- Do TYPE AND NEXT");
                        AccessibilityUtil.performType(currentNodeInfo.getNodeCompat(), command.getActionExtra());
//                        command.numberOfActions++;
                        command.setExecutionState(Command.COMPLETED);

                    } else {
                        Log.i(AccessibilityUtil.TAG, "Command's action is unknown " + command.getAction());
                        command.setExecutionState(Command.FAILED);
                    }
                    return command.getExecutionState();
                }
                break;
            default:
                Log.i(AccessibilityUtil.TAG, "What I'm doing here?");
        }
        return command.getExecutionState();
    }

    public static int executeInTalkBack(Command cmd){
        List<String> maskedAttributes = new ArrayList<>(WidgetInfo.maskedAttributes);
        WidgetInfo.maskedAttributes.clear();
        boolean preClickMode = RegularCommandExecutor.clickPhysical;
        RegularCommandExecutor.clickPhysical = false;
        RegularCommandExecutor.reportA11YIssue = false;
        RegularCommandExecutor.executeCommand(cmd);
        RegularCommandExecutor.reportA11YIssue = true;
        RegularCommandExecutor.clickPhysical = preClickMode;
        WidgetInfo.maskedAttributes = new ArrayList<>(maskedAttributes);
        return cmd.getExecutionState();
    }

    public static int executeInSwitchAccess(Command cmd){
        List<String> maskedAttributes = new ArrayList<>(WidgetInfo.maskedAttributes);
        WidgetInfo.maskedAttributes.clear();
        RegularCommandExecutor.reportA11YIssue = false;
        RegularCommandExecutor.executeCommand(cmd);
        RegularCommandExecutor.reportA11YIssue = true;
        WidgetInfo.maskedAttributes = new ArrayList<>(maskedAttributes);
        return cmd.getExecutionState();
    }

    public static List<AccessibilityHierarchyCheckResult> getA11yIssues(AccessibilityNodeInfo rootNode){
        return AccessibilityUtil.getA11yIssues(rootNode);
    }
}
