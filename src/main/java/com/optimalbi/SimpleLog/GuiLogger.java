package org.timothygray.SimpleLog;

/*
   Copyright 2015 OptimalBI

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */


import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.io.File;

/**
 * Logs output to a JavaFX TextArea and a file
 */
public class GuiLogger implements Logger {
    private final TextArea textArea;
    private final FileLogger fileLogger;
    private final Boolean hasLogFile;

    public GuiLogger(File logFile, TextArea textArea) {
        fileLogger = new FileLogger(logFile);
        this.textArea = textArea;
        hasLogFile = true;
    }

    public GuiLogger(TextArea textArea) {
        this.textArea = textArea;
        fileLogger = null;
        hasLogFile = false;
    }

    public GuiLogger(File logFile, TextArea textArea, logType loggerLevel) {
        fileLogger = new FileLogger(logFile);
        this.textArea = textArea;
        hasLogFile = true;
    }

    public void debug(String message) {
        logTo(message, logType.DEBUG);
    }

    public void warn(String message) {
        logTo(message, logType.WARN);
    }

    public void error(String message) {
        logTo(message, logType.ERROR);
    }

    public void info(String message) {
        logTo(message, logType.INFO);
    }

    private void logTo(String message, logType l) {
        switch (l) {
            case DEBUG:
                toSystem("[DEBUG] " + message);
                toFile(message, l);
                break;
            case INFO:
                toUser(message);
                toSystem("[INFO] " + message);
                toFile(message, l);
                break;
            case WARN:
                toUser("[WARN] " + message);
                toSystem("[WARN] " + message);
                toFile(message, l);
                break;
            case ERROR:
                toUser("[ERROR] " + message);
                toSystem("[ERROR] " + message);
                toFile(message, l);
                break;
        }
    }

    private void toUser(final String message) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                textArea.appendText("\n"+message);
            }
        });
    }

    private void toSystem(String message) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        if (stackTraceElements[4] != null) message = stackTraceElements[4].getClassName() + " " + message;
        System.out.println(message);
    }

    private void toFile(String message, logType l) {
        if (!hasLogFile) return;
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        if (stackTraceElements[4] != null) message = stackTraceElements[4].getClassName() + " " + message;

        switch (l) {
            case DEBUG:
                fileLogger.debug(message);
                break;
            case INFO:
                fileLogger.info(message);
                break;
            case WARN:
                fileLogger.warn(message);
                break;
            case ERROR:
                fileLogger.error(message);
                break;
        }
    }

}
