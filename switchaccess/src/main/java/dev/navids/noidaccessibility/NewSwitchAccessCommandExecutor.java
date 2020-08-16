package dev.navids.noidaccessibility;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityNodeInfo;


import com.google.android.accessibility.switchaccess.treenodes.TreeScanNode;
import com.google.android.accessibility.switchaccess.treenodes.TreeScanSystemProvidedNode;
import com.google.android.accessibility.switchaccess.treenodes.TreeScanSelectionNode;

import com.google.android.accessibility.switchaccess.SwitchAccessNodeCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewSwitchAccessCommandExecutor {
    private static NewSwitchAccessWidgetInfo focusedNode;
    public static TreeScanNode currentRootNode;
    public static long LAST_DOWN_TIME = 0;
    private static boolean changedFocused = false;
    public static Map<Integer, String> pendingActions = new HashMap<>();
    public static int pendingActionId = 0;
    public static interface MyCallBack{
        void callback();
    }
    public static void setFocusedNode(NewSwitchAccessWidgetInfo node){
        focusedNode = node;
        changedFocused = true;
    }

    public static List<AccessibilityNodeInfo> findNodesXpath(NewWidgetInfo target){
        List<AccessibilityNodeInfo> result = new ArrayList<>();
        for(AccessibilityNodeInfo node : AccessibilityUtil.getAllA11yNodeInfo(false)) {
            NewWidgetInfo currentNodeInfo = NewWidgetInfo.create(node);
            if (target.isSimilarAttribute(currentNodeInfo, "xpath"))
                result.add(node);
        }
        return result;
    }

    public static int executeCommand(NewCommand command){
        Log.i(AccessibilityUtil.TAG, String.format("Command state: %s, action: %s, actionExtra: %s, target: %s.",
                command.getExecutionState(),
                command.getAction(),
                command.getActionExtra(),
                command.getTargetWidgetInfo()));
        if(pendingActions.size() > 0){
            Log.i(AccessibilityUtil.TAG, "Ignore this manageCommand since we're waiting for another action");
            return command.getExecutionState();
        }
        switch (command.getExecutionState()){
            case NewCommand.NOT_STARTED:
                Log.i(AccessibilityUtil.TAG, "--- Change to SEARCH");
                command.setExecutionState(NewCommand.SEARCH);
            case NewCommand.SEARCH:
                List<AccessibilityNodeInfo> similarNodes = findNodesXpath(command.getTargetWidgetInfo());
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
                    // TODO: check if something inside focusedNode is equal to the node
                    if(focusedNode != null && focusedNode.getSwitchAccessNodeCompat() != null) {
                        AccessibilityNodeInfo it = node;
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
                            if (command.getAction().equals(NewCommand.CMD_ASSERT)) {
                                Log.i(AccessibilityUtil.TAG, "--- Do ASSERT");
                            } else if (command.getAction().equals(NewCommand.CMD_CLICK)) {
                                Log.i(AccessibilityUtil.TAG, "--- Do CLICK");
                                performSelect();
                            } else if (command.getAction().equals(NewCommand.CMD_TYPE)) {
                                Log.i(AccessibilityUtil.TAG, "--- Do TYPE AND NEXT");
                                performType(focusedNode.getSwitchAccessNodeCompat(), command.getActionExtra());
                                performNext();
                            } else {
                                Log.i(AccessibilityUtil.TAG, "Command's action is unknown " + command.getAction());
                            }
                            command.setExecutionState(NewCommand.COMPLETED);
                        } else {
                            Log.i(AccessibilityUtil.TAG, "--- Do NEXT");
                            performNext();
                        }
                    }
                    else{
                        Log.i(AccessibilityUtil.TAG, "--- Node is not focused. Do NEXT");
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

    public static void performType(SwitchAccessNodeCompat node, String message){
//        Log.i(TAG, " ======== BEGINNING of TYPE");
        Bundle arguments = new Bundle();
        arguments.putCharSequence(SwitchAccessNodeCompat
                .ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, message);
        node.performAction(SwitchAccessNodeCompat.ACTION_SET_TEXT, arguments);
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
//        Log.i(SwitchAccessCommandExecutor.TAG, prefix + "Node: " + startNode);
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
