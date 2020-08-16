package dev.navids.noidaccessibility;

import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class NewWidgetInfo {
    List<String> attributeNames = Arrays.asList(
            "resourceId", "contentDescription", "text", "class", "xpath");
    public static List<String> maskedAttributes = new ArrayList<>();
    public List<NewWidgetInfo> leftContext = new ArrayList<>();
    public List<NewWidgetInfo> rightContext = new ArrayList<>();
    public final static int contextSize = 3;
    Map<String, String> attributes = new HashMap<>();
    String locatedBy = "";
    AccessibilityNodeInfo node;
    public static Map<AccessibilityNodeInfo, NewWidgetInfo> createdWidgets = new HashMap<>();
    public static void createAll(List<AccessibilityNodeInfo> nodes){
        createdWidgets.clear();
        List<NewWidgetInfo> contextInfo = new ArrayList<>();
        for(AccessibilityNodeInfo node : nodes) {
            NewWidgetInfo widgetInfo = NewWidgetInfo.create(node);
            createdWidgets.put(node, widgetInfo);
            if(node.getChildCount() == 0 && node.isVisibleToUser())
                contextInfo.add(widgetInfo);
        }
        String s = "";
        for(int i=0; i<contextInfo.size(); i++){
            for(int j=i-1; j>Integer.max(i-1-contextSize,-1); j--)
                contextInfo.get(i).leftContext.add(contextInfo.get(j));
            for(int j=i+1; j<Integer.min(i+1+contextSize,contextInfo.size()); j++)
                contextInfo.get(i).rightContext.add(contextInfo.get(j));
            Log.i(AccessibilityUtil.TAG, "  CW: " + i + " " + contextInfo.get(i));

        }
    }

    public static NewWidgetInfo create(AccessibilityNodeInfo node){
        if (node == null){
            return new NewWidgetInfo("");
        }
        if(createdWidgets.containsKey(node))
            return createdWidgets.get(node);
        String resourceId = String.valueOf(node.getViewIdResourceName());
        String contentDescription = String.valueOf(node.getContentDescription());
        String text = String.valueOf(node.getText());
        String clsName = String.valueOf(node.getClassName());
        NewWidgetInfo widgetInfo = new NewWidgetInfo(resourceId, contentDescription, text, clsName);
        widgetInfo.setNodeCompat(node);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            widgetInfo.setXpath(widgetInfo.getXpath());
        }
        return widgetInfo;
    }

    public static NewWidgetInfo getWidget(AccessibilityNodeInfo node){
        if(node != null && createdWidgets.containsKey(node))
            return createdWidgets.get(node);
        return null;
    }

    public NewWidgetInfo(String resourceId) {
        this(resourceId, "", "", null);
    }
    public static NewWidgetInfo createFromJson(JSONObject cmd) {
        if(cmd == null)
            return new NewWidgetInfo("");
        String resourceId = (String) cmd.getOrDefault("resourceId", "");
        String contentDescription = (String) cmd.getOrDefault("contentDescription", "");
        String text = (String) cmd.getOrDefault("text", "");
        String clsName = (String) cmd.getOrDefault("class", "");
        NewWidgetInfo widgetInfo = new NewWidgetInfo(resourceId, contentDescription,text, clsName);

        String xpath = (String) cmd.getOrDefault("xpath", "");
        if(!xpath.equals("")) {
            // TODO: handle xpath with content description
            // TODO: handle partial xpath
            if(xpath.startsWith("/hierarchy"))
                xpath = xpath.substring("/hierarchy".length());
            widgetInfo.setXpath(xpath);
        }

        String locatedBy = (String) cmd.getOrDefault("located_by", "");
        widgetInfo.setLocatedBy(locatedBy);
        JSONArray leftContextJson = (JSONArray) cmd.getOrDefault("left_context", new JSONArray());
        for(int i=0; i<leftContextJson.size(); i++){
            JSONObject subCmd = (JSONObject) leftContextJson.get(i);
            widgetInfo.leftContext.add(NewWidgetInfo.createFromJson(subCmd));
        }
        JSONArray rightContextJson = (JSONArray) cmd.getOrDefault("right_context", new JSONArray());
        for(int i=0; i<rightContextJson.size(); i++){
            JSONObject subCmd = (JSONObject) rightContextJson.get(i);
            widgetInfo.rightContext.add(NewWidgetInfo.createFromJson(subCmd));
        }
        return widgetInfo;
    }
    public NewWidgetInfo(String resourceId, String contentDescription, String text, String clsName) {
        attributes.put(attributeNames.get(0), resourceId);
        attributes.put(attributeNames.get(1), contentDescription);
        attributes.put(attributeNames.get(2), text);
        attributes.put(attributeNames.get(3), clsName);
    }

    public boolean isSimilar(NewWidgetInfo other){
        boolean contentSimilar = isSimilarWithoutContext(other, maskedAttributes);
        boolean contextSimilar = true;isSimilarContext(other);
        return contentSimilar && contextSimilar;
    }

    private boolean isSimilarWithoutContext(NewWidgetInfo other, List<String> myMaskedAttributes){
        return this.getAttr("xpath").equals(other.getAttr("xpath"));
//        boolean isSimilar = true;
//        for(String attrName : attributeNames){
//            if(myMaskedAttributes.contains(attrName))
//                continue;
//            boolean isSimilarAttribute = isSimilarAttribute(other, attrName);
//            if(isLocatedBy(attrName) || other.isLocatedBy(attrName))
//                return isSimilarAttribute;
//            if(!attrName.equals("xpath"))
//                isSimilar &= isSimilarAttribute;
//        }
//        return isSimilar;
    }

    private boolean isSimilarContext(NewWidgetInfo other){
        int thisLeftSize = Integer.min(contextSize, this.leftContext.size());
        int thisRightSize = Integer.min(contextSize, this.rightContext.size());
        int otherLeftSize = Integer.min(contextSize, other.leftContext.size());
        int otherRightSize = Integer.min(contextSize, other.rightContext.size());
        if(thisLeftSize != otherLeftSize)
            return false;
        if(thisRightSize != otherRightSize)
            return false;
        // The contexts do not have xpath
        List<String> myMaskedAttributes = new ArrayList<>(maskedAttributes);
        myMaskedAttributes.add("xpath");
        int mismatches = 0;
        for(int i=0; i<thisLeftSize; i++) {
            if (!this.leftContext.get(i).isSimilarWithoutContext(other.leftContext.get(i), myMaskedAttributes))
                mismatches++;
        }
        for(int i=0; i<thisRightSize; i++) {
            if (!this.rightContext.get(i).isSimilarWithoutContext(other.rightContext.get(i), myMaskedAttributes))
                mismatches++;
        }
        // TODO: CONFIGURABLE
        return mismatches <= 1;
    }

    public String getAttr(String attributeName){
        return attributes.getOrDefault(attributeName, "");
    }

    public boolean hasAttr(String attributeName){
        String s = attributes.getOrDefault(attributeName, "");
        return s != null && !s.equals("") && !s.equals("null");
    }

    public boolean isSimilarAttribute(NewWidgetInfo other, String attributeName){
        if(this.isLocatedBy(attributeName) || other.isLocatedBy(attributeName))
            if(!this.hasAttr(attributeName) || !other.hasAttr(attributeName))
                return false;
        if(!this.hasAttr(attributeName))
            return !other.hasAttr(attributeName);
        return getAttr(attributeName).equals(other.getAttr(attributeName));
    }

    public boolean similarXpath(NewWidgetInfo other){
        return this.getXpath().contains(other.getXpath()) || other.getXpath().contains(this.getXpath());
    }

    public AccessibilityNodeInfo getNodeCompat() {
        return node;
    }

    public void setNodeCompat(AccessibilityNodeInfo nodeCompat) {
        this.node = nodeCompat;
    }

    public Rect getBox() {
        if (node == null)
            return null;
        Rect box = new Rect();
        node.getBoundsInScreen(box);
        return box;
    }

    public String getXpath(){
        if(node == null)
            return getAttr("xpath");
        List<String> names = new ArrayList<>();
        AccessibilityNodeInfo it = node;
        names.add(0, String.valueOf(it.getClassName()));
        while(it.getParent() != null){

            int count = 0;
            int length = 0;
            String itClsName = String.valueOf(it.getClassName());
            for(int i=0; i<it.getParent().getChildCount(); i++) {
                String childClsName = String.valueOf(it.getParent().getChild(i).getClassName());
                if(!it.getParent().getChild(i).isVisibleToUser())
                    continue;
                if (itClsName.equals(childClsName))
                    length++;
                if(it.getParent().getChild(i).equals(it)) {
                    count = length;
                }
            }
            if(length > 1)
                names.set(0, String.format("%s[%d]", names.get(0), count));
            it = it.getParent();
            names.add(0, String.valueOf(it.getClassName()));
//            xpath = String.valueOf(it.getClassName()) + "/" + xpath;
        }
        String xpath = "/"+String.join("/", names);
        return xpath;
    }

    public void setXpath(String xpath) {
        this.attributes.put("xpath", xpath);
    }

    public void setLocatedBy(String locatedBy) {
        this.locatedBy = locatedBy;
    }

    public boolean isLocatedBy(String locatedBy) {
        return this.locatedBy.equals(locatedBy);
    }

    @NonNull
    @Override
    public String toString() {
        String xpath = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            xpath = !getXpath().equals("")? " xpath: " + getXpath() : "";
        String id = hasAttr("resourceId") ? " ID: "+getAttr("resourceId")+" ": "";
        String cd = hasAttr("contentDescription") ? " CD: "+getAttr("contentDescription")+" ": "";
        String tx = hasAttr("text") ? " TX: "+getAttr("text")+" ": "";
        String cl = hasAttr("class") ? " CL: "+getAttr("class")+" ": "";
        String left_context_count = String.format("LC: %d", leftContext.size());
        String right_context_count = String.format("RC: %d", rightContext.size());
        String locatedByLabel = !locatedBy.equals("")  ? " LocBy: ("+locatedBy+") ": "";
        return String.format("WidgetInfo %s%s%s%s%s%s %s %s",
                locatedByLabel,
                id,
                cd,
                tx,
                cl,
                xpath,
                left_context_count,
                right_context_count);
    }
}
