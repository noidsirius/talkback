package dev.navids.noidaccessibility;


import android.accessibilityservice.AccessibilityService;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import com.android.switchaccess.SwitchAccessService;

public class CLIController {
    public static boolean isPolling = false;
    private static boolean updated = false;
    private static long delayBetweenCommands = 500;
    private static String commandFilePath = "/data/local/tmp/command.txt";

    public static void readInput(){
        isPolling = true;
        SwitchAccessService service = SwitchAccessService.getInstance();
        if(service == null || !service.connected) {
            isPolling = false;
            return;
        }
        File file = new File(commandFilePath);
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            Scanner scanner = new Scanner(fileInputStream);
            String nextCommand = "NONE";
            if(scanner.hasNext())
                nextCommand = scanner.next();
            fileInputStream.close();
            if(nextCommand.equals("NONE") || nextCommand.equals("")){

            }
            else {
                WidgetInfo.createAll(AccessibilityUtil.getAllA11yNodeInfo(false));
                if (nextCommand.equals("log")) {
                    Log.i(AccessibilityUtil.TAG, "CMD: log");
                    logCurrentState();
                    clearCommandFile();
                } else if (nextCommand.equals("log_widgets")) {
                    Log.i(AccessibilityUtil.TAG, "CMD: log_widgets");
                    for (AccessibilityNodeInfo node : AccessibilityUtil.getAllA11yNodeInfo(false))
                        Log.i(AccessibilityUtil.TAG,  "  " + WidgetInfo.getWidget(node));
                    clearCommandFile();
                } else if (nextCommand.startsWith("mask_")) {
                    Log.i(AccessibilityUtil.TAG, "CMD: mask");
                    String[] tokens = nextCommand.split("_");
                    Log.i(AccessibilityUtil.TAG, "tokens " + tokens);
                    WidgetInfo.maskedAttributes.clear();
                    for (int i = 1; i < tokens.length; i++) {
                        Log.i(AccessibilityUtil.TAG, "  token " + tokens[i]);
                        WidgetInfo.maskedAttributes.add(tokens[i]);
                    }
                    clearCommandFile();
                } else if (nextCommand.equals("init")) {
                    Log.i(AccessibilityUtil.TAG, "CMD: init");
                    CommandManager.initCommands();
                    clearCommandFile();
                } else if (nextCommand.startsWith("goto_")) {
                    Log.i(AccessibilityUtil.TAG, "CMD: goto");
                    CommandManager.setLastCommandIndex(Integer.parseInt(nextCommand.substring("goto_".length())));
                    clearCommandFile();
                } else if (nextCommand.equals("next")) {
                    Log.i(AccessibilityUtil.TAG, "CMD: next");
                    CommandManager.manageCommands();
                    clearCommandFile();
                } else if (nextCommand.equals("start")) {
                    Log.i(AccessibilityUtil.TAG, "CMD: start");
                    if (!CommandManager.manageCommands())
                        clearCommandFile();
                } else if (nextCommand.equals("prev")) {
                    Log.i(AccessibilityUtil.TAG, "CMD: prev");
                    CommandManager.manageCommands(false);
                    clearCommandFile();
                }
                else if (nextCommand.startsWith("delay_")) {
                    Log.i(AccessibilityUtil.TAG, "CMD: set delay");
                    delayBetweenCommands = Long.parseLong(nextCommand.substring("delay_".length()));
                    Log.i(AccessibilityUtil.TAG, "CMD: the delay between each command is " + delayBetweenCommands);
                    clearCommandFile();
                }
                Log.i(AccessibilityUtil.TAG,"--------- Complete command " + nextCommand);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Handler().postDelayed(CLIController::readInput, delayBetweenCommands);
    }


    public static void clearCommandFile(){
        File file = new File(commandFilePath);
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write("NONE\n");
            fileWriter.close();
        }  catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void onAccessibilityEvent(AccessibilityEvent event){
        updated = true;
    }

    public static void logCurrentState(){
        AccessibilityNodeInfo rootNode = com.android.switchaccess.SwitchAccessService.getInstance().getRootInActiveWindow();
        Log.i(AccessibilityUtil.TAG, "RootNode: "+ rootNode);
        AccessibilityUtil.getAllA11yNodeInfo(rootNode, true);
    }
}
