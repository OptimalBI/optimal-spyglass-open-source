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

package com.optimalbi.Services;

import com.amazonaws.regions.Region;
import com.optimalbi.Controller.Containers.AmazonCredentials;
import com.optimalbi.SimpleLog.Logger;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.*;

/**
 * Created by Timothy Gray(timg) on 25/11/2014.
 * Version: 0.0.1
 */
public class TestInstance extends AmazonService {

    private final int timeInterval;
    private final Logger logger;
    private VBox drawing = null;
    private String state;
    private final String name;
    private Region region;

    //Temp timer
    private Timer timer = new Timer();

    public TestInstance(AmazonCredentials credentials,int timeInterval, Logger logger) {
        super("" + UUID.randomUUID(),credentials,logger);
        this.timeInterval = timeInterval;
        this.name = "Test-" + serviceID();
        this.logger = logger;
        state = randomState();
        addShutdownHook();
    }

    public void refreshInstance() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            logger.warn(e.getMessage());
        }
    }

    public String serviceName() {
        String testString = this.name;
        if (testString.length() > 21) {
            testString = testString.substring(0, 21);
            testString = testString + "...";
        }
        return testString;
    }

    public String serviceSize() {
        return "ItsATestBro";
    }

    public double servicePrice() {
        return 0;
    }

    public Region serviceRegion() {
        return region;
    }

    public String serviceState() {
        return this.state;
    }

    public String serviceType() {
        return "TestService";
    }

    public void startService() {
        changeState("running");
    }

    public void stopService() {
        changeState("stopped");
    }

    public VBox draw() {
        List<Node> c = new ArrayList<>();
        if (drawing == null) {
            drawing = new VBox();
        } else {
            drawing.getChildren().setAll();
        }

        //Instance Type
        Label instanceType = new Label(stringCap(this.serviceType()));
        instanceType.getStyleClass().add("testInstanceTitle");
        instanceType.setAlignment(Pos.CENTER);
        c.add(instanceType);

        //Instance Name
        Label instanceName = new Label(this.serviceName());
        instanceName.setPrefWidth(textWidth);
        instanceName.setAlignment(Pos.CENTER);
        c.add(instanceName);

        //Instance ID
        Label instanceID = new Label(this.serviceID());

        c.add(instanceID);

        //Instance State
        Label instanceState;
        instanceState = new Label(this.serviceState());
        if (instanceState.getText().equalsIgnoreCase("stopped")) {
            instanceState.setTextFill(Color.RED);
        } else if (instanceState.getText().equalsIgnoreCase("running")) {
            instanceState.setTextFill(Color.GREEN);
        } else {
            instanceState.setTextFill(Color.ORANGERED);
        }
        c.add(instanceState);

        //Start Stop Buttons
        HBox startStop = new HBox();
        Button start = new Button("start");
        start.setPrefSize(serviceWidth / 2, buttonHeight);
        start.setOnAction(event -> startService());
        Button stop = new Button("stop");
        stop.setOnAction(event -> this.stopService());

        stop.setPrefSize(serviceWidth / 2, buttonHeight);
        startStop.getChildren().addAll(start, stop);
        startStop.getStyleClass().add("popupButtons");
        startStop.setAlignment(Pos.BOTTOM_CENTER);
        c.add(startStop);

        drawing.getChildren().addAll(c);
        drawing.setAlignment(Pos.CENTER);
        drawing.setPrefHeight(serviceHeight);
        drawing.setPrefWidth(serviceWidth);
        return drawing;
    }

    public void attachPricing(Map<String, Double> pricing) {

    }

    public int compareTo(Service o) {
        return this.serviceName().compareTo(o.serviceName());
    }

    private String randomState() {
        int randomNumber = (int) (Math.random() * 2) + 1;
        if (randomNumber == 2) {
            return "running";
        } else {
            return "stopped";
        }
    }

    private void changeState(String toState) {
        this.state = "changing";
        draw();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    Thread.sleep(timeInterval);
                } catch (InterruptedException e) {
                    logger.warn(e.getMessage());
                }
                state = toState;
                Platform.runLater(TestInstance.this::draw);
            }
        };
        timer.schedule(task,1);
    }

    private void addShutdownHook(){
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            timer.cancel();
            timer.purge();
            timer = null;
        }));
    }
}
