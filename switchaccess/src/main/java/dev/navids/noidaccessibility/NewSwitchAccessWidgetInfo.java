package dev.navids.noidaccessibility;

import com.google.android.accessibility.switchaccess.SwitchAccessNodeCompat;


public class NewSwitchAccessWidgetInfo extends NewWidgetInfo {
    boolean isMenu = false;
    boolean isLastNode = false;
    SwitchAccessNodeCompat switchAccessNodeCompat;

    public NewSwitchAccessWidgetInfo(String resourceId) {
        super(resourceId);
    }

    public NewSwitchAccessWidgetInfo(String resourceId, String contentDescription, String text, String clsName) {
        super(resourceId, contentDescription, text, clsName);
    }

    public static NewSwitchAccessWidgetInfo create(boolean isMenu,
                                                   boolean isLastNode,
                                                   SwitchAccessNodeCompat nodeCompat){
        if(isMenu || isLastNode) {
            NewSwitchAccessWidgetInfo widgetInfo = new NewSwitchAccessWidgetInfo("");
            widgetInfo.isLastNode = isLastNode;
            widgetInfo.isMenu = isMenu;
            return widgetInfo;
        }
        if (nodeCompat == null){
            return new NewSwitchAccessWidgetInfo("");
        }
        NewWidgetInfo superWidgetInfo = NewWidgetInfo.create(nodeCompat.unwrap());
        NewSwitchAccessWidgetInfo newWidgetInfo = new NewSwitchAccessWidgetInfo(
                superWidgetInfo.getAttr("resourceId"),
                superWidgetInfo.getAttr("contentDescription"),
                superWidgetInfo.getAttr("text"),
                superWidgetInfo.getAttr("class"));
        newWidgetInfo.setLocatedBy("xpath");
        newWidgetInfo.switchAccessNodeCompat = nodeCompat;
        newWidgetInfo.setXpath(superWidgetInfo.getXpath());
        return newWidgetInfo;
    }

    @Override
    public boolean isSimilar(NewWidgetInfo other) {
        return isSimilarAttribute(other, "xpath");
    }

    public SwitchAccessNodeCompat getSwitchAccessNodeCompat() {
        return switchAccessNodeCompat;
    }
}
