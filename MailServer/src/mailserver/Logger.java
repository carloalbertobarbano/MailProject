/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mailserver;

import javafx.scene.control.TextArea;

/**
 *
 * @author carloalberto
 */
public class Logger {
    public static TextArea textAreaLog = null;
    
    public static void log(String message) {
        StackTraceElement stackTraceElements[] = Thread.currentThread().getStackTrace();
        String stackTrace = stackTraceElements[2].getClassName() + "." +
                            stackTraceElements[2].getMethodName() + ": ";
         
        if (textAreaLog != null) {
            textAreaLog.appendText(stackTrace + message + "\n");
        }
               
        System.out.println(message);
    }
    
    public static void error(String message) {
        StackTraceElement stackTraceElements[] = Thread.currentThread().getStackTrace();
        String stackTrace = stackTraceElements[2].getClassName() + "." +
                            stackTraceElements[2].getMethodName() + ": " + 
                            stackTraceElements[2].getLineNumber();
        
        if (textAreaLog != null) {
            textAreaLog.appendText("ERROR: " + message);
            textAreaLog.appendText(" (from: " + stackTrace + ")\n");
        }
        
        System.err.println("ERROR: " + message);
    }
    
    public static void warning(String message) {
        StackTraceElement stackTraceElements[] = Thread.currentThread().getStackTrace();
        String stackTrace = stackTraceElements[2].getClassName() + "." +
                            stackTraceElements[2].getMethodName() + ": " + 
                            stackTraceElements[2].getLineNumber();
        
        if (textAreaLog != null) {
            textAreaLog.appendText("WARNING: " + message);
            textAreaLog.appendText(" (from: " + stackTrace + ")\n");
        }
        
        System.out.println("WARNING: " + message);
    }
}
