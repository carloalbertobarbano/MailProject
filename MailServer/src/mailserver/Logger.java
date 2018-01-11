/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mailserver;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

/**
 *
 * @author carloalberto
 */
public class Logger {
    public static TextArea textAreaLog = null;
    
    public static void log(String message) {
        StackTraceElement stackTraceElements[] = Thread.currentThread().getStackTrace();
        int index = stackTraceElements.length > 2 ? 2 : stackTraceElements.length - 1;
        String stackTrace = stackTraceElements[index].getClassName() + "." +
                            stackTraceElements[index].getMethodName() + ": ";
         
        if (textAreaLog != null) {
            Platform.runLater(() -> {
                textAreaLog.appendText(stackTrace + message + "\n");
                textAreaLog.setScrollTop(Double.MAX_VALUE);
            });
        }
               
        System.out.println(message);
    }
    
    public static void error(String message) {
        StackTraceElement stackTraceElements[] = Thread.currentThread().getStackTrace();
        int index = stackTraceElements.length > 2 ? 2 : stackTraceElements.length - 1;
        String stackTrace = stackTraceElements[index].getClassName() + "." +
                            stackTraceElements[index].getMethodName() + ": " + 
                            stackTraceElements[index].getLineNumber();
        
        if (textAreaLog != null) {
            Platform.runLater(() -> {
                textAreaLog.appendText("ERROR: " + message);
                textAreaLog.appendText(" (from: " + stackTrace + ")\n");
                textAreaLog.setScrollTop(Double.MAX_VALUE);
            });
        }
        
        System.err.println("ERROR: " + message);
    }
    
    public static void warning(String message) {
        StackTraceElement stackTraceElements[] = Thread.currentThread().getStackTrace();
        int index = stackTraceElements.length > 2 ? 2 : stackTraceElements.length - 1;
        String stackTrace = stackTraceElements[index].getClassName() + "." +
                            stackTraceElements[index].getMethodName() + ": " + 
                            stackTraceElements[index].getLineNumber();
        
        if (textAreaLog != null) {
            Platform.runLater(() -> {
                textAreaLog.appendText("WARNING: " + message);
                textAreaLog.appendText(" (from: " + stackTrace + ")\n");
                textAreaLog.setScrollTop(Double.MAX_VALUE);
            }); 
        }
        
        System.out.println("WARNING: " + message);
    }
}
