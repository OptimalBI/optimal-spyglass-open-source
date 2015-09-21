package com.optimalbi.SimpleLog;

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


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

/**
 * Logs output only to file
 */
public class FileLogger implements Logger {
    private final File logFile;
    private PrintWriter writer;

    public FileLogger(File logFile) {
        addShutdownLines();
        this.logFile = logFile;
        write("Log file created: " + new Date().toString(), true);
        write(" ", true);
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

    public void resetLogFile() {
        try {
            logFile.delete();
            logFile.createNewFile();
        } catch (IOException e) {
            System.err.println("Failed to recreate log file" + e.getMessage());
        }

        write(" ", true);
        write(" ", true);
        write(" ", true);
        write("Log file created: " + new Date().toString(), true);
        write(" ", true);
    }

    private void logTo(String message, logType l) {
        switch (l) {
            case DEBUG:
                toFile("[DEBUG] " + message);
                break;
            case INFO:
                toFile("[INFO] " + message);
                break;
            case WARN:
                toFile("[WARN] " + message);
                break;
            case ERROR:
                toFile("[ERROR] " + message);
                break;
        }
    }

    private void toFile(String message) {
        write(message, true);
    }

    private void write(String w, boolean newLine) {
        if (logFile != null) {
            try {
                writer = new PrintWriter(new FileOutputStream(logFile, true));
                if (newLine) {
                    writer.println(w);
                } else {
                    writer.print(w);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                writer.close();
            }
        } else {
            System.err.print("Log file not found");
        }
    }

    private void addShutdownLines() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                write(" ", true);

                write(" ", true);

                write(" ", true);
            }
        });
    }


}
