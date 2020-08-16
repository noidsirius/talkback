package com.google.android.accessibility.switchaccess.TestExecutor;

import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import com.google.android.accessibility.switchaccess.treenodes.TreeScanNode;
import com.google.android.accessibility.switchaccess.treenodes.TreeScanSystemProvidedNode;
import com.google.android.accessibility.switchaccess.treenodes.TreeScanSelectionNode;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityWindowInfoCompat;
import com.android.switchaccess.SwitchAccessService;
import com.google.android.accessibility.switchaccess.SwitchAccessNodeCompat;
import com.google.android.accessibility.utils.AccessibilityServiceCompatUtils;

import java.util.ArrayList;
import java.util.List;

public class WidgetUtil {

//    public static void alaki(WidgetInfo currentWidget, TreeScanNode rootSacnNode, AccessibilityNodeInfo rootNode, WidgetInfo targetWidget){
//        Log.i(SwitchAccessCommandExecutor.TAG, "----------");
//        List<AccessibilityWindowInfo> windows = AccessibilityServiceCompatUtils.getWindows(SwitchAccessService.getInstance());
//        for(AccessibilityWindowInfo windowInfo : windows) {
//            Log.i(SwitchAccessCommandExecutor.TAG, "Window " + windowInfo);
//            AccessibilityNodeInfo anchor = windowInfo.getAnchor();
//            if(anchor != null){
//                Log.i(SwitchAccessCommandExecutor.TAG, "   hasAnchor " + anchor);
//                getAllA11yNodeInfo(anchor);
//            }
////            AccessibilityNodeInfo r = windowInfo.getRoot();
////            getAllA11yNodeInfo(r);
//            Log.i(SwitchAccessCommandExecutor.TAG, "   %%%% ");
//        }
//        Log.i(SwitchAccessCommandExecutor.TAG, "Current " + currentWidget);
////        List<SwitchAccessNodeCompat> reachableNodes = getAllNodes(rootSacnNode);
////        List<AccessibilityNodeInfo> inScreenNodes = getAllA11yNodeInfo(rootNode);
////        for(AccessibilityNodeInfo node : inScreenNodes){
////            boolean isReachable = false;
////            for(SwitchAccessNodeCompat sNode : reachableNodes)
////                if(sNode.unwrap().equals(node)) {
////                    isReachable = true;
////                    break;
////                }
////            WidgetInfo thisWidget = new WidgetInfo(node.getViewIdResourceName(), String.valueOf(node.getContentDescription()), String.valueOf(node.getText()), null);
////            thisWidget.setXpath(getXpath(node));
////            boolean isXpathRelevant = thisWidget.similarXpath(targetWidget);
////            boolean isSimilarText = thisWidget.similarText(targetWidget);
////            Log.i(SwitchAccessCommandExecutor.TAG,
////                    String.format(" R: %d X: %d T: %d, %s id: %s",
////                        isReachable ? 1 : 0,
////                        isXpathRelevant ? 1 : 0,
////                        isSimilarText ? 1 : 0,
////                        getXpath(node),
////                        node.getViewIdResourceName()));
////        }
//        Log.i(SwitchAccessCommandExecutor.TAG, "----------");
//    }
//
//
//    public static List<SwitchAccessNodeCompat> getAllNodes(TreeScanNode root){
//        List<TreeScanSystemProvidedNode> result = new ArrayList<>();
//        addItemNodesToSet(root, result, "##");
//        List<SwitchAccessNodeCompat> nodes = new ArrayList<>();
//        AccessibilityNodeInfo oneParent = null;
//        for (TreeScanSystemProvidedNode treeScanSystemProvidedNode : result) {
//            SwitchAccessNodeCompat nodeCompat = treeScanSystemProvidedNode.getNodeInfoCompat();
//            if(nodeCompat != null) {
////                SwitchAccessNodeCompat parent = nodeCompat;
////                AccessibilityWindowInfoCompat parentWindow = nodeCompat.getWindow();
////                while(parentWindow!= nullparentWindow.getParent() != null)
////                    parentWindow = parentWindow.getParent();
////                AccessibilityNodeInfoCompat nn = parentWindow.getRoot();
////                while(parent.getParent() != null)
////                    parent = parent.getParent();
////                if(oneParent == null)
////                    oneParent = nn.unwrap();
////                else if(!oneParent.equals(nn.unwrap()))
////                    Log.i(SwitchAccessCommandExecutor.TAG, "\n\n\n%%%%%%%%%%%%%%% Parent is not one! %%%%%%%%%%%% " + nn.unwrap() + "\n\n");
////                Log.i(SwitchAccessCommandExecutor.TAG, "\t\tleafNodeCompat " + " " + nodeCompat.getViewIdResourceName() + " " + " " + nodeCompat.getText() + " " + nodeCompat.getContentDescription());
////                getAllA11yNodeInfo(nodeCompat.unwrap(), "\t\t--");
//                nodes.add(nodeCompat);
//            }
//            else
//                Log.i(SwitchAccessCommandExecutor.TAG, "\t\tleafNodeCompat null");
//        }
////        Log.i(SwitchAccessCommandExecutor.TAG, "\n\nNow Parent");
////        getAllA11yNodeInfo(oneParent);
////        Log.i(SwitchAccessCommandExecutor.TAG, "---------\n\n");
//        return nodes;
//    }
//
//    private static void addItemNodesToSet(TreeScanNode startNode, List<TreeScanSystemProvidedNode> nodeSet, String prefix) {
////        Log.i(SwitchAccessCommandExecutor.TAG, prefix + "Node: " + startNode);
//        if (startNode instanceof TreeScanSystemProvidedNode) {
//            nodeSet.add((TreeScanSystemProvidedNode) startNode);
//        }
//        if (startNode instanceof TreeScanSelectionNode) {
//            TreeScanSelectionNode selectionNode = (TreeScanSelectionNode) startNode;
//            for (int i = 0; i < selectionNode.getChildCount(); ++i) {
//                addItemNodesToSet(selectionNode.getChild(i), nodeSet, prefix+"  ");
//            }
//        }
//    }
//
//    public static List<AccessibilityNodeInfo> getAllA11yNodeInfo(AccessibilityNodeInfo rootNode){
//        return getAllA11yNodeInfo(rootNode, " /");
//    }
//
//    private static List<AccessibilityNodeInfo> getAllA11yNodeInfo(AccessibilityNodeInfo rootNode, String prefix){
//        List<AccessibilityNodeInfo> result = new ArrayList<>();
//        result.add(rootNode);
//        Log.i(SwitchAccessCommandExecutor.TAG, prefix + " " + rootNode.getClassName() + " " + rootNode.getViewIdResourceName() + " " + rootNode.getText() + " " + rootNode.getContentDescription());
//        for(int i=0; i<rootNode.getChildCount(); i++)
//            result.addAll(getAllA11yNodeInfo(rootNode.getChild(i), " " +prefix+rootNode.getClassName()+"/"));
//        return result;
//    }
//
//    public static String getXpath(AccessibilityNodeInfo node){
//        if(node == null)
//            return "";
//        AccessibilityNodeInfo it = node;
//        String xpath = String.valueOf(it.getClassName());
//        while(it.getParent() != null){
//            it = it.getParent();
//            xpath = String.valueOf(it.getClassName()) + "/" + xpath;
//        }
//        return xpath;
//    }

//    public static List<SwitchAccessNodeCompat> getAllA11yNodeInfo(SwitchAccessNodeCompat rootNode){
//
//        return getAllA11yNodeInfo(rootNode, "--/");
//    }
//
//    private static List<SwitchAccessNodeCompat> getAllA11yNodeInfo(SwitchAccessNodeCompat rootNode, String prefix){
//        List<SwitchAccessNodeCompat> result = new ArrayList<>();
//        result.add(rootNode);
//        Log.i(TAG, prefix + rootNode.getClassName() + " " + rootNode.getViewIdResourceName() + " " + rootNode.getText() + " " + rootNode.getContentDescription());
//        for(int i=0; i<rootNode.getChildCount(); i++)
//            result.addAll(getAllA11yNodeInfo(rootNode.getChild(i), " " +prefix+rootNode.getClassName()+"/"));
//        return result;
//    }
}
