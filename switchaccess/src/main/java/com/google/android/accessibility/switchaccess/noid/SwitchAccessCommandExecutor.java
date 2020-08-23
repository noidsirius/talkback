package com.google.android.accessibility.switchaccess.noid;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityNodeInfo;


import com.google.android.accessibility.switchaccess.treenodes.TreeScanNode;
import com.google.android.accessibility.switchaccess.treenodes.TreeScanSystemProvidedNode;
import com.google.android.accessibility.switchaccess.treenodes.TreeScanSelectionNode;

import com.google.android.accessibility.switchaccess.SwitchAccessNodeCompat;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckPreset;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheck;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.checks.TraversalOrderCheck;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SwitchAccessCommandExecutor {
    private static SwitchAccessWidgetInfo focusedNode;
    public static TreeScanNode currentRootNode;
    public static long LAST_DOWN_TIME = 0;
    private static boolean changedFocused = false;
    private static int waitingForFocusChange = 0;
    private static final int MAX_WAIT_FOR_CHANGE = 2;
    public static Map<Integer, String> pendingActions = new HashMap<>();
    public static int pendingActionId = 0;
    public static interface MyCallBack{
        void callback();
    }
    public static void setFocusedNode(SwitchAccessWidgetInfo node, TreeScanNode currentNode){
        focusedNode = node;
        if(currentNode != null)
            changedFocused = true;

    }

    public static List<AccessibilityNodeInfo> findNodesXpath(WidgetInfo target){
        List<AccessibilityNodeInfo> result = new ArrayList<>();
        for(AccessibilityNodeInfo node : AccessibilityUtil.getAllA11yNodeInfo(false)) {
            WidgetInfo currentNodeInfo = WidgetInfo.create(node);
            if (target.isSimilarAttribute(currentNodeInfo, "xpath"))
                result.add(node);
        }
        return result;
    }

    public static List<AccessibilityHierarchyCheckResult> getA11yIssues(AccessibilityNodeInfo rootNode){
        Set<AccessibilityHierarchyCheck> checks = Collections.singleton(AccessibilityCheckPreset.getHierarchyCheckForClass(TraversalOrderCheck.class));
        return AccessibilityUtil.getA11yIssues(rootNode, true, checks);
    }

    public static int executeCommand(Command command){
        Log.i(AccessibilityUtil.TAG, String.format("SwitchAccess Command state: %s, action: %s, actionExtra: %s, target: %s.",
                command.getExecutionState(),
                command.getAction(),
                command.getActionExtra(),
                command.getTargetWidgetInfo()));
        if(focusedNode == null ||
                (!focusedNode.isMenu
                        && !focusedNode.isLastNode
                        && focusedNode.getSwitchAccessNodeCompat() == null)){
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
        switch (command.getExecutionState()){
            case Command.NOT_STARTED:
                Log.i(AccessibilityUtil.TAG, "--- Change to SEARCH");
                command.setExecutionState(Command.SEARCH);
            case Command.SEARCH:
                List<AccessibilityNodeInfo> similarNodes = findNodesXpath(command.getTargetWidgetInfo());
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
                    // TODO: check if something inside focusedNode is equal to the node
                    if(focusedNode != null && focusedNode.getSwitchAccessNodeCompat() != null) {
                        AccessibilityNodeInfo it = node;
                        int trackAction = command.trackAction(focusedNode.getSwitchAccessNodeCompat().unwrap());
                        Log.i(AccessibilityUtil.TAG, String.format("The following widget has been visited %d times: %s", trackAction, WidgetInfo.create(node)));
                        if(trackAction > Command.MAX_VISITED_WIDGET){
                            Log.i(AccessibilityUtil.TAG, "Execute the command using regular test executor");
                            RegularCommandExecutor.executeCommand(command);
                            if(command.getExecutionState() == Command.COMPLETED)
                                command.setExecutionState(Command.COMPLETED_BY_REGULAR);
                            return command.getExecutionState();
                        }
                        boolean isSimilar = false;
                        SwitchAccessNodeCompat firstReachableNodeCompat = null;
                        Map<AccessibilityNodeInfo, SwitchAccessNodeCompat> allNodeCompats = getAllNodes(currentRootNode);
                        while(it != null){
                            if(allNodeCompats.containsKey(it)) {
                                firstReachableNodeCompat = allNodeCompats.get(it);
                                break;
                            }
                            it = it.getParent();
                        }
                        if(firstReachableNodeCompat == null){
                            Log.i(AccessibilityUtil.TAG, "-- FIRST REACHABLE NODECOMPAT IS NULL");
                        }
                        isSimilar = firstReachableNodeCompat != null && firstReachableNodeCompat.equals(focusedNode.getSwitchAccessNodeCompat());
//                        List<AccessibilityNodeInfo> queue = new ArrayList<>();
//                        queue.add(it);
//                        for(int i=0; i< queue.size(); i++){
//                            if(it.equals(node)){
//                                isSimilar = true;
//                                break;
//                            }
//                            for(int j=0; j<it.getChildCount(); j++)
//                                queue.add(it.getChild(j));
//                        }

//                        boolean isSimilar = focusedNode.getSwitchAccessNodeCompat().unwrap().equals(node);
                        if (isSimilar) {
                            if (command.getAction().equals(Command.CMD_ASSERT)) {
                                Log.i(AccessibilityUtil.TAG, "--- Do ASSERT");
                            } else if (command.getAction().equals(Command.CMD_CLICK)) {
                                Log.i(AccessibilityUtil.TAG, "--- Do CLICK");
//                                command.numberOfActions++;
                                performSelect();
                            } else if (command.getAction().equals(Command.CMD_TYPE)) {
                                Log.i(AccessibilityUtil.TAG, "--- Do TYPE AND NEXT");
                                AccessibilityUtil.performType(focusedNode.getSwitchAccessNodeCompat().unwrap(), command.getActionExtra());
//                                command.numberOfActions++;
//                                performNext();
                            } else {
                                Log.i(AccessibilityUtil.TAG, "Command's action is unknown " + command.getAction());
                            }
                            command.setExecutionState(Command.COMPLETED);
                        } else {
                            Log.i(AccessibilityUtil.TAG, "--- Do NEXT");
//                            command.numberOfActions++;
                            performNext();
                        }
                    }
                    else{
                        Log.i(AccessibilityUtil.TAG, "--- Node is not focused. Do NEXT");
//                        command.numberOfActions++;
                        performNext();
                    }
                    return command.getExecutionState();
                }
                break;
            default:
                Log.i(AccessibilityUtil.TAG, "What I'm doing here?");
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
//        WidgetInfo myWidgetInfo = latestWidgetInfo;
//        Log.i(TAG, " ======== BEGINNING of " + (isSelect ? "SELECT" : "NEXT") + " " + myWidgetInfo);
        changedFocused = false;
        focusedNode = null;
        int code = isSelect ? KeyEvent.KEYCODE_VOLUME_UP: KeyEvent.KEYCODE_VOLUME_DOWN;
        if(com.android.switchaccess.SwitchAccessService.getInstance() != null){
            final int thisActionId = pendingActionId;
            pendingActionId++;
            pendingActions.put(thisActionId, "Pending: I'm going to do " + (isSelect ? "SELECT" : "NEXT"));// + " " + myWidgetInfo);
            Handler mainHandler = new Handler(com.android.switchaccess.SwitchAccessService.getInstance().getMainLooper());
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.i(AccessibilityUtil.TAG, " ======== KEYBOARD_DOWN " + (isSelect ? "SELECT" : "NEXT"));// + " My " + myWidgetInfo + " Latest " + latestWidgetInfo);
                    pendingActions.remove(thisActionId);
                    long new_down_time = LAST_DOWN_TIME + 1000;
                    KeyEvent down = new KeyEvent(new_down_time,new_down_time, KeyEvent.ACTION_DOWN, code, 0,0,0,114,0x8,0x101);
                    KeyEvent up = new KeyEvent(new_down_time,new_down_time, KeyEvent.ACTION_UP, code, 0,0,0,114,0x8,0x101);

                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(AccessibilityUtil.TAG, " ======== KEYBOARD_UP " + (isSelect ? "SELECT" : "NEXT"));// + " My " + myWidgetInfo + " Latest " + latestWidgetInfo);
                            com.android.switchaccess.SwitchAccessService.getInstance().onKeyEvent(up);//new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_VOLUME_DOWN));
                            if(myCallBack != null)
                                myCallBack.callback();
                        }
                    });
                    com.android.switchaccess.SwitchAccessService.getInstance().onKeyEvent(down);//new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_DOWN));
                }
            }, 100);
        }
    }

    public static Map<AccessibilityNodeInfo, SwitchAccessNodeCompat> getAllNodes(TreeScanNode root){
        List<TreeScanSystemProvidedNode> result = new ArrayList<>();
        addItemNodesToSet(root, result, "##");
        Map<AccessibilityNodeInfo, SwitchAccessNodeCompat> nodes = new HashMap<>();
        AccessibilityNodeInfo oneParent = null;
        for (TreeScanSystemProvidedNode treeScanSystemProvidedNode : result) {
            SwitchAccessNodeCompat nodeCompat = treeScanSystemProvidedNode.getNodeInfoCompat();
            if(nodeCompat != null) {
//                SwitchAccessNodeCompat parent = nodeCompat;
//                AccessibilityWindowInfoCompat parentWindow = nodeCompat.getWindow();
//                while(parentWindow!= nullparentWindow.getParent() != null)
//                    parentWindow = parentWindow.getParent();
//                AccessibilityNodeInfoCompat nn = parentWindow.getRoot();
//                while(parent.getParent() != null)
//                    parent = parent.getParent();
//                if(oneParent == null)
//                    oneParent = nn.unwrap();
//                else if(!oneParent.equals(nn.unwrap()))
//                    Log.i(SwitchAccessCommandExecutor.TAG, "\n\n\n%%%%%%%%%%%%%%% Parent is not one! %%%%%%%%%%%% " + nn.unwrap() + "\n\n");
//                Log.i(SwitchAccessCommandExecutor.TAG, "\t\tleafNodeCompat " + " " + nodeCompat.getViewIdResourceName() + " " + " " + nodeCompat.getText() + " " + nodeCompat.getContentDescription());
//                getAllA11yNodeInfo(nodeCompat.unwrap(), "\t\t--");
                nodes.put(nodeCompat.unwrap(), nodeCompat);
            }
            else
                Log.i(AccessibilityUtil.TAG, "\t\tleafNodeCompat null");
        }
//        Log.i(SwitchAccessCommandExecutor.TAG, "\n\nNow Parent");
//        getAllA11yNodeInfo(oneParent);
//        Log.i(SwitchAccessCommandExecutor.TAG, "---------\n\n");
        return nodes;
    }

    private static void addItemNodesToSet(TreeScanNode startNode, List<TreeScanSystemProvidedNode> nodeSet, String prefix) {
//        Log.i(AccessibilityUtil.TAG, prefix + "Node: " + startNode);
        if (startNode instanceof TreeScanSystemProvidedNode) {
            nodeSet.add((TreeScanSystemProvidedNode) startNode);
        }
        if (startNode instanceof TreeScanSelectionNode) {
            TreeScanSelectionNode selectionNode = (TreeScanSelectionNode) startNode;
            for (int i = 0; i < selectionNode.getChildCount(); ++i) {
                addItemNodesToSet(selectionNode.getChild(i), nodeSet, prefix+"  ");
            }
        }
    }
}
