package com.google.android.accessibility.switchaccess.TestExecutor;

import android.graphics.Rect;

import androidx.annotation.NonNull;

import com.google.android.accessibility.switchaccess.SwitchAccessNodeCompat;

public class WidgetInfo {
    String resourceId;
    String contentDescription;
    String text;
    Rect box;
    SwitchAccessNodeCompat nodeCompat;
    boolean isMenu = false;
    boolean isLastNode = false;

    public static WidgetInfo create(boolean isMenu,
                                    boolean isLastNode,
                                    SwitchAccessNodeCompat nodeCompat){
        if(isMenu || isLastNode) {
            WidgetInfo widgetInfo = new WidgetInfo("");
            widgetInfo.isLastNode = isLastNode;
            widgetInfo.isMenu = isMenu;
            return new WidgetInfo("");
        }
        if (nodeCompat == null){
            return new WidgetInfo("");
        }
        Rect box = new Rect();
        nodeCompat.getBoundsInScreen(box);
        String contentDescription = nodeCompat.getContentDescription() != null
                ? nodeCompat.getContentDescription().toString()
                : "";
        String text = nodeCompat.getText() != null
                ? nodeCompat.getText().toString()
                : "";
        WidgetInfo widgetInfo = new WidgetInfo(nodeCompat.getViewIdResourceName(), contentDescription, text,box);
        widgetInfo.setNodeCompat(nodeCompat);
        return widgetInfo;
    }

    public WidgetInfo(String resourceId) {
        this(resourceId, "", "", null);
    }
    public WidgetInfo(String resourceId, String contentDescription, String text) {
        this(resourceId, contentDescription, text, null);
    }
    public WidgetInfo(String resourceId, String contentDescription, String text, Rect box) {
        this.resourceId = resourceId;
        this.contentDescription = contentDescription;
        this.text = text;
        this.box = box;
    }

    public boolean isSimilar(WidgetInfo other){
        if (this.resourceId.equals(other.resourceId))
            return true;
        if (this.isMenu && other.isMenu)
            return true;
        return false;
    }

    public SwitchAccessNodeCompat getNodeCompat() {
        return nodeCompat;
    }

    public void setNodeCompat(SwitchAccessNodeCompat nodeCompat) {
        this.nodeCompat = nodeCompat;
    }

    @NonNull
    @Override
    public String toString() {
        return "WidgetInfo " + resourceId;
    }
}
