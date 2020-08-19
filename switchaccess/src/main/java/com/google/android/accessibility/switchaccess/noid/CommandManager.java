package com.google.android.accessibility.switchaccess.noid;

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
    public static List<Command> commandList = new ArrayList<>();
    private static long startTime = -1;
    private static long endTime = -1;

    public interface TestExecutor{
        int executeCommand(Command cmd);
    }

    public static void setTestExecutor(TestExecutor testExecutor) {
        CommandManager.testExecutor = testExecutor;
    }

    private static TestExecutor testExecutor;

    public static boolean manageCommands(){
        return manageCommands(true);
    }

    public static boolean manageCommands(boolean isNext){
        if(lastCommandIndex < 0) {
            return false;
        }
        if(lastCommandIndex == 0)
            startTime = System.currentTimeMillis();
        if(!isNext)
            lastCommandIndex--;
        if(lastCommandIndex >= commandList.size()) {
            Log.i(AccessibilityUtil.TAG, "----------The test case is completed!---------");
            if(endTime == -1)
                endTime = System.currentTimeMillis();
            long totalTime = endTime -  startTime;
            long totalEvents = 0;
            long meanTime = 0;
            int meanEventCount = 0;
            int completeCount = 0;
            int unlocatedCount = 0;
            int unreachableCount = 0;
            for(int i=0; i<commandList.size(); i++) {
                Command cmd = commandList.get(i);
                meanTime += cmd.getTime();
                meanEventCount += cmd.getNumberOfActions();
                totalEvents += cmd.getNumberOfActions();
                if(cmd.getExecutionState() == Command.COMPLETED)
                    completeCount++;
                else if(cmd.getExecutionState() == Command.COMPLETED_BY_REGULAR)
                    unreachableCount++;
                else if(cmd.getExecutionState() == Command.COMPLETED_BY_REGULAR_UNABLE_TO_DETECT)
                    unlocatedCount++;
                Log.i(AccessibilityUtil.TAG, String.format("   CMD: %d $ State: %s $ #Events: %d $ Time: %d",
                        i + 1, Command.getActionStr(cmd.getExecutionState()), cmd.getNumberOfActions(), cmd.getTime()));
            }
            meanTime /= commandList.size();
            meanEventCount /= commandList.size();
            Log.i(AccessibilityUtil.TAG, String.format("Total Time: %d $ Mean Time: %d $ Total Events: %d $ Mean Event: %d", totalTime, meanTime, totalEvents, meanEventCount));
            Log.i(AccessibilityUtil.TAG, String.format("Completed Steps: %d $ Unlocatable Steps: %d $ Unreachable Steps: %d", completeCount, unlocatedCount, unreachableCount));
            return false;
        }
        Log.i(AccessibilityUtil.TAG, "Executing command " + (lastCommandIndex+1) + " Masks " + WidgetInfo.maskedAttributes);
        Command currentCommand = commandList.get(lastCommandIndex);
        if(!isNext)
            currentCommand.setExecutionState(Command.NOT_STARTED);
        if(currentCommand.getExecutionState() == Command.FAILED)
            currentCommand.setExecutionState(Command.NOT_STARTED);
        int result = executeCommand(currentCommand);
        if(result == Command.COMPLETED
                || result == Command.COMPLETED_BY_REGULAR
                || result == Command.COMPLETED_BY_REGULAR_UNABLE_TO_DETECT) {
            Log.i(AccessibilityUtil.TAG, "Command " + (lastCommandIndex+1) + " is completed!");
            lastCommandIndex++;
        }
        else if(result == Command.FAILED) {
            Log.i(AccessibilityUtil.TAG, "Command " + (lastCommandIndex + 1) + " is failed!");
            lastCommandIndex++;
        }
        return true;
    }

    public static void initCommands(){
        commandList.clear();
        startTime = -1;
        endTime = -1;
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
                    action = Command.CMD_CLICK;
                else if(action.equals("send_keys"))
                    action = Command.CMD_TYPE;
                else
                    action = "UNKNOWN";
                WidgetInfo widgetInfo = WidgetInfo.createFromJson(cmd);
                String message = "";
                if(cmd.containsKey("action_args")) {
                    JSONArray args = (JSONArray) cmd.get("action_args");
                    if(action.equals(Command.CMD_TYPE))
                        message = String.valueOf(args.get(0));
                    else
                        Log.i(AccessibilityUtil.TAG,"-------> Args: " + args);
                }
                Log.i(AccessibilityUtil.TAG, "Json " + action + " " + message + " " + widgetInfo);
                commandList.add(new Command(widgetInfo, action, message));
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

    public static int executeCommand(Command command){
        if(testExecutor == null)
            return RegularCommandExecutor.executeCommand(command);
        return testExecutor.executeCommand(command);
    }
}
