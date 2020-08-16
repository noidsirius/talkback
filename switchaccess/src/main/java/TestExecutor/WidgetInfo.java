package com.google.android.accessibility.switchaccess.TestExecutor;

import android.graphics.Rect;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.accessibility.switchaccess.SwitchAccessNodeCompat;

import dev.navids.noidaccessibility.AccessibilityUtil;

public class WidgetInfo {
//    String resourceId = "";
//    String contentDescription = "";
//    String text = "";
//    String xpath = "";
//    Rect box;
//    SwitchAccessNodeCompat nodeCompat;
//    boolean isMenu = false;
//    boolean isLastNode = false;
//
//    public static WidgetInfo create(boolean isMenu,
//                                    boolean isLastNode,
//                                    SwitchAccessNodeCompat nodeCompat){
//        if(isMenu || isLastNode) {
//            WidgetInfo widgetInfo = new WidgetInfo("");
//            widgetInfo.isLastNode = isLastNode;
//            widgetInfo.isMenu = isMenu;
//            return widgetInfo;
//        }
//        if (nodeCompat == null){
//            return new WidgetInfo("");
//        }
//        Rect box = new Rect();
//        nodeCompat.getBoundsInScreen(box);
//        String contentDescription = nodeCompat.getContentDescription() != null
//                ? nodeCompat.getContentDescription().toString()
//                : "";
//        String text = nodeCompat.getText() != null
//                ? nodeCompat.getText().toString()
//                : "";
//        WidgetInfo widgetInfo = new WidgetInfo(nodeCompat.getViewIdResourceName(), contentDescription, text,box);
//        widgetInfo.setNodeCompat(nodeCompat);
//        widgetInfo.setXpath(widgetInfo.getXpath());
//        return widgetInfo;
//    }
//
//    public WidgetInfo(String resourceId) {
//        this(resourceId, "", "", null);
//    }
//    public WidgetInfo(String resourceId, String contentDescription, String text) {
//        this(resourceId, contentDescription, text, null);
//    }
//    public WidgetInfo(String resourceId, String contentDescription, String text, Rect box) {
//        this.resourceId = resourceId;
//        this.contentDescription = contentDescription;
//        this.text = text;
//        this.box = box;
//    }
//
//    public boolean isSimilar(WidgetInfo other){
//        // TODO: it's not correct, e.g., id != id , cd == cd => similar
//        Log.i(AccessibilityUtil.TAG, "  IsSimilar? " + this + " " + other);
//        if (!this.resourceId.equals("") && this.resourceId.equals(other.resourceId))
//            return true;
//        if (this.isMenu && other.isMenu)
//            return true;
//        if(!this.contentDescription.equals("") && this.contentDescription.equals(other.contentDescription))
//            return true;
//        return false;
//    }
//
//    public boolean similarXpath(WidgetInfo other){
//        return this.getXpath().contains(other.getXpath()) || other.getXpath().contains(this.getXpath());
//    }
//
//    public boolean similarText(WidgetInfo other){
//        return this.text.equals(other.text);
//    }
//
//    public boolean similarContentDescription(WidgetInfo other){
//        return this.contentDescription.equals(other.contentDescription);
//    }
//
//    public SwitchAccessNodeCompat getNodeCompat() {
//        return nodeCompat;
//    }
//
//    public void setNodeCompat(SwitchAccessNodeCompat nodeCompat) {
//        this.nodeCompat = nodeCompat;
//    }
//
//    public String getXpath(){
//        if(nodeCompat == null)
//            return xpath;
//        SwitchAccessNodeCompat it = nodeCompat;
//        String xpath = String.valueOf(it.getClassName());
//        while(it.getParent() != null){
//            it = it.getParent();
//            xpath = String.valueOf(it.getClassName()) + "/" + xpath;
//        }
//        return xpath;
//    }
//
//    public void setXpath(String xpath) {
//        this.xpath = xpath;
//    }
//
//    @NonNull
//    @Override
//    public String toString() {
//        String xpath = nodeCompat != null ? " Xpath: " + getXpath() + " " : "";
//        String menu = isMenu ? " (Menu) " : "";
//        String lastNode = isLastNode ? " (LastNode) " : "";
//        String id = resourceId != null && !resourceId.equals("") ? " ID: "+resourceId+" ": "";
//        String cd = contentDescription != null && !contentDescription.equals("") ? " CD: "+contentDescription+" ": "";
//        String tx = text != null && !text.equals("") ? " Text: "+text+" ": "";
//        return String.format("WidgetInfo %s%s%s%s%s%s",
//                menu,
//                lastNode,
//                id,
//                cd,
//                tx,
//                xpath);
//    }
}
