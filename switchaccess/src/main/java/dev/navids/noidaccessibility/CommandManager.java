package dev.navids.noidaccessibility;

import android.util.Log;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CommandManager {
    public static int lastCommandIndex = -1;
    public static List<NewCommand> commandList = new ArrayList<>();

    public static boolean manageCommands(){
        return manageCommands(true);
    }

    public static boolean manageCommands(boolean isNext){
        if(lastCommandIndex < 0) {
            return false;
        }
        if(!isNext)
            lastCommandIndex--;
        if(lastCommandIndex >= commandList.size()) {
            Log.i(AccessibilityUtil.TAG, "----------The test case is completed!---------");
            return false;
        }
        Log.i(AccessibilityUtil.TAG, "Executing command " + (lastCommandIndex+1) + " Masks " + NewWidgetInfo.maskedAttributes);
        NewCommand currentCommand = commandList.get(lastCommandIndex);
        if(!isNext)
            currentCommand.setExecutionState(NewCommand.NOT_STARTED);
        if(currentCommand.getExecutionState() == NewCommand.FAILED)
            currentCommand.setExecutionState(NewCommand.NOT_STARTED);
        int result = executeCommand(currentCommand);
        if(result == NewCommand.COMPLETED) {
            Log.i(AccessibilityUtil.TAG, "Command " + (lastCommandIndex+1) + " is completed!");
            lastCommandIndex++;
        }
        else if(result == NewCommand.FAILED) {
            Log.i(AccessibilityUtil.TAG, "Command " + (lastCommandIndex + 1) + " is failed!");
            lastCommandIndex++;
        }
        return true;
    }

    public static void initCommands(){
        commandList.clear();
        String fileName = "test_guideline.json";
        // TODO: check context.getFilesDir().getPath();
        String dir = "/data/local/tmp/";
//            String dir = context.getFilesDir().getPath();
        Log.i(AccessibilityUtil.TAG, "Path: " + dir);
        File file = new File(dir, fileName);
        //JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();
        JSONArray commandsJson = null;
        try (FileReader reader = new FileReader(file))
        {
            // TODO: tons of refactor!
            //Read JSON file
            Object obj = jsonParser.parse(reader);
            commandsJson = (JSONArray) obj;
            for(int i=0; i<commandsJson.size(); i++){
//                for(int i=0; i<5; i++){
                JSONObject cmd = (JSONObject) commandsJson.get(i);
                if(cmd == null){
                    Log.i(AccessibilityUtil.TAG, "Json Command is null!");
                    continue;
                }
                String action = (String) cmd.getOrDefault("action", "UNKNOWN");
                if(action.equals("click"))
                    action = NewCommand.CMD_CLICK;
                else if(action.equals("send_keys"))
                    action = NewCommand.CMD_TYPE;
                else
                    action = "UNKNOWN";
                NewWidgetInfo widgetInfo = NewWidgetInfo.createFromJson(cmd);
                String message = "";
                if(cmd.containsKey("action_args")) {
                    JSONArray args = (JSONArray) cmd.get("action_args");
                    if(action.equals(NewCommand.CMD_TYPE))
                        message = String.valueOf(args.get(0));
                    else
                        Log.i(AccessibilityUtil.TAG,"-------> Args: " + args);
                }
                Log.i(AccessibilityUtil.TAG, "Json " + action + " " + message + " " + widgetInfo);
                commandList.add(new NewCommand(widgetInfo, action, message));
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        lastCommandIndex = 0;
    }

    public static void setLastCommandIndex(int lastCommandIndex) {
        CommandManager.lastCommandIndex = lastCommandIndex;
        Log.i(AccessibilityUtil.TAG, "CMD: the command index is " + lastCommandIndex);
    }

    public static int executeCommand(NewCommand command){
        return NewSwitchAccessCommandExecutor.executeCommand(command);
//        return RegularCommandExecutor.executeCommand(command);
    }
}
