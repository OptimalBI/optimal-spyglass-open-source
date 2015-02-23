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
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
import com.optimalbi.Controller.Containers.AmazonCredentials;
import org.timothygray.SimpleLog.*;
import com.amazonaws.AmazonClientException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import com.optimalbi.GUI.TjfxFactory.TjfxFactory;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Timothy Gray on 13/11/2014.
 * Version: 0.0.2
 */
public class LocalEc2Service extends AmazonService implements Comparable<Service> {
    private Instance thisInstance;
    private final AmazonEC2 amazonEC2;
    private VBox drawing = null;
    private final Region region;
    private Map<String, Double> pricing = null;

    //Global Main Components
    private Label instanceState;
    private Timer timer = new Timer();

    public LocalEc2Service(String id, AmazonCredentials credentials, Region region, AmazonEC2 amazonEC2, Logger logger) {
        //TODO: Figure out if part of auto-scaling group

        super(id, credentials, logger);
        this.amazonEC2 = amazonEC2;
        this.region = region;
        refreshInstance();
        addShutdownHook();
    }

    public void refreshInstance() {
        DescribeInstancesResult describeInstancesResult = amazonEC2.describeInstances(new DescribeInstancesRequest().withInstanceIds(this.serviceID()));
        List<Reservation> reservations = describeInstancesResult.getReservations();
        List<Instance> inst = new ArrayList<>();

        for (Reservation reservation : reservations) {
            inst.addAll(reservation.getInstances());
        }
        if (inst.size() > 1) {
            logger.error("Error in drawing instance " + this.serviceID());
            throw new AmazonClientException(this.serviceID() + " failed to draw");
        }
        thisInstance = inst.get(0);
    }

    public String serviceState() {
        return thisInstance.getState().getName();
    }

    public String serviceType() {
        return "ec2";
    }

    public String serviceName() {
        List<Tag> tags = thisInstance.getTags();

        String testString = "";
        for (Tag t : tags) {
            if (t.getKey().equals("Name")) {
                testString = t.getValue();
            }
        }
        return stringCap(testString);
    }

    public String serviceSize() {
        return thisInstance.getInstanceType();
    }

    public double servicePrice() {
        if(pricing != null){
            if (pricing.containsKey(this.serviceSize())) {
                return pricing.get(this.serviceSize());
            }
        }
        return 0;
    }

    public Region serviceRegion() {
        return region;
    }

    public void startService() {
        logger.info("Instance \"Started\"");
        List<String> instanceId = new ArrayList<>();
        instanceId.add(this.serviceID());
        amazonEC2.startInstances(new StartInstancesRequest(instanceId));
        waitForStateChange("running");
    }

    public void stopService() {
        logger.info("Instance \"Stopped\"");
        List<String> instanceId = new ArrayList<>();
        instanceId.add(this.serviceID());
        amazonEC2.stopInstances(new StopInstancesRequest(instanceId));
        waitForStateChange("stopped");
    }

    private void waitForStateChange(String oldState) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                logger.debug("Running state change waiter for " + serviceID());
                int timeout = 0;
                String intermediateState = "";
                while (!(thisInstance.getState().getName().equals(oldState))) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        logger.error("Failed to sleep thread in " + thisInstance.getInstanceId());
                    }
                    refreshInstance();
                    if ((!intermediateState.equals(thisInstance.getState().getName()))) {
                        intermediateState = thisInstance.getState().getName();
                        Platform.runLater(() -> {
                            instanceState.setText(serviceState());
                            if (instanceState.getText().equalsIgnoreCase("stopped")) {
                                instanceState.setTextFill(Color.RED);
                            } else if (instanceState.getText().equalsIgnoreCase("running")) {
                                instanceState.setTextFill(Color.GREEN);
                            } else {
                                instanceState.setTextFill(Color.ORANGERED);
                            }
                        });
                    }
                    timeout++;
                    logger.debug("Waiting for: " + thisInstance.getState().getName() + "(" + intermediateState + ")" + " to become: " + oldState);
                    if (timeout > 100) break; //Timeout condition
                }
                Platform.runLater(LocalEc2Service.this::draw);
            }
        };
        timer.schedule(task, 2);
    }

    public VBox draw() {
        TjfxFactory guiFactory = new TjfxFactory(buttonWidth, buttonHeight, "stlye.css");

        Pos align = Pos.CENTER;

        List<Node> c = new ArrayList<>();
        if (drawing == null) {
            drawing = new VBox();
        } else {
            drawing.getChildren().setAll();
        }

        //Instance Type
        Label instanceType = new Label(stringCap(this.serviceType()));
        instanceType.getStyleClass().add("ec2Title");
        instanceType.setAlignment(Pos.CENTER);
        instanceType.setPrefWidth(serviceWidth);
        c.add(instanceType);

        //TODO: Remove temp region label
        Label regionName;
        if(Service.regionNames().containsKey(this.region.getName())){
            regionName = new Label(Service.regionNames().get(this.region.getName()));
        }else {
            regionName = new Label(this.region.getName());
        }
        regionName.setPrefWidth(serviceWidth);
        regionName.setAlignment(Pos.CENTER);
        regionName.getStyleClass().add("regionTitle");
        c.add(regionName);

        //Instance Name
        Label instanceName = new Label(this.serviceName());
        instanceName.setPrefWidth(serviceWidth);
        instanceName.setAlignment(Pos.CENTER);
        c.add(instanceName);

        //Instance ID
        Label instanceIDLabel = new Label("Service ID: ");
        instanceIDLabel.setPrefWidth(labelWidth);
        instanceIDLabel.setAlignment(Pos.CENTER_RIGHT);
        instanceIDLabel.getStyleClass().add("guiLabel");
        Label instanceID = new Label(this.serviceID());
        instanceID.setPrefWidth(labelWidth);

        HBox iID = new HBox(instanceIDLabel, instanceID);
        iID.setPrefWidth(serviceWidth);
        iID.setAlignment(align);
        c.add(iID);

        //EC2 Size
        Label instanceSizeLabel = new Label("Size: ");
        instanceSizeLabel.setPrefWidth(labelWidth);
        instanceSizeLabel.setAlignment(Pos.CENTER_RIGHT);
        instanceSizeLabel.getStyleClass().add("guiLabel");
        Label instanceSize = new Label(this.serviceSize());
        instanceSize.setPrefWidth(labelWidth);

        HBox sizeBox = new HBox(instanceSizeLabel, instanceSize);
        sizeBox.setPrefWidth(serviceWidth);
        sizeBox.setAlignment(align);
        c.add(sizeBox);

        //Costs area
        if (pricing != null) {
            if (pricing.containsKey(this.serviceSize())) {
                double cost = pricing.get(this.serviceSize());
                Label instancePricingLabel = new Label("Cost: ");
                instancePricingLabel.setPrefWidth(labelWidth);
                instancePricingLabel.setAlignment(Pos.CENTER_RIGHT);
                instancePricingLabel.getStyleClass().add("guiLabel");
                Label instancePricing = new Label("$" + cost + "/hr");
                instancePricing.setPrefWidth(labelWidth);

                HBox pricingBox = new HBox(instancePricingLabel, instancePricing);
                pricingBox.setPrefWidth(serviceWidth);
                pricingBox.setAlignment(align);
                c.add(pricingBox);
            } else {
                Label instancePricingLabel = new Label("Cost: ");
                instancePricingLabel.setPrefWidth(labelWidth);
                instancePricingLabel.setAlignment(Pos.CENTER_RIGHT);
                instancePricingLabel.getStyleClass().add("guiLabel");
                Label instancePricing = new Label("Not found");
                instancePricing.setPrefWidth(labelWidth);

                HBox pricingBox = new HBox(instancePricingLabel, instancePricing);
                pricingBox.setPrefWidth(serviceWidth);
                pricingBox.setAlignment(align);
                c.add(pricingBox);
            }
        }


        //Instance State
        instanceState = new Label(this.serviceState());
        instanceState.setAlignment(Pos.CENTER);
        instanceState.setPrefWidth(serviceWidth);
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
        start.setPrefSize((serviceWidth / 2), buttonHeight);
        start.setOnAction(event -> startService());
        Button stop = new Button("stop");
        stop.setOnAction(event -> this.stopService());

        stop.setPrefSize((serviceWidth / 2), buttonHeight);
//        startStop.getChildren().addAll(start, stop);

        //Graph Button
        Button graphs = new Button("graphs");
        graphs.setPrefSize(serviceWidth, buttonHeight);
        graphs.setOnAction(event -> drawGraph());

        //Button box
        VBox buttonBox = new VBox(startStop,graphs);
        buttonBox.setPrefWidth(serviceWidth+5);
        buttonBox.setPrefHeight(serviceHeight);
        buttonBox.getStyleClass().add("popupButtons");
        c.add(buttonBox);

        drawing.getChildren().addAll(c);
        drawing.setAlignment(Pos.TOP_CENTER);
        drawing.setPrefHeight(serviceHeight);

        buttonBox.setAlignment(Pos.TOP_CENTER);


        drawing.setPrefWidth(serviceWidth);
        return drawing;
    }

    public void attachPricing(Map<String, Double> pricing) {
        this.pricing = pricing;
    }

    public Map<String, Double> getPricing(){
        return pricing;
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            timer.cancel();
            timer.purge();
            timer = null;
        }));
    }

    private void drawGraph() {
        Popup popup = new Popup();
        Scene mainScene = drawing.getScene();

        //Time period for stats
        long DAY_IN_MS = 1000 * 60 * 60 * 24;
        Date end = new Date();
        Date start = new Date(end.getTime() - (7 * DAY_IN_MS));

        VBox outerLayout = new VBox();
        outerLayout.setPrefSize(mainScene.getWindow().getWidth() / 1.12, mainScene.getWindow().getHeight() / 1.12);

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();

        yAxis.setLabel("Percent, %");
        xAxis.setLabel("Date");

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);

        lineChart.setTitle(this.serviceName() + " Metrics");

        XYChart.Series<String, Number> cpuUtilMax = new XYChart.Series<>();
        cpuUtilMax.setName("CPU Utilization, Max");
        XYChart.Series<String, Number> cpuUtilAve = new XYChart.Series<>();
        cpuUtilAve.setName("CPU Utilization, Median");

        //Request CPU Util stats
        GetMetricStatisticsRequest cpuUtilizationRequest = new GetMetricStatisticsRequest();
        cpuUtilizationRequest.setMetricName("CPUUtilization");
        cpuUtilizationRequest.setEndTime(end);
        cpuUtilizationRequest.setStartTime(start);
        cpuUtilizationRequest.setNamespace("AWS/EC2");

        //Request CPU Util stats
        GetMetricStatisticsRequest diskUtilizationRequest = new GetMetricStatisticsRequest();
        cpuUtilizationRequest.setMetricName("CPUUtilization");
        cpuUtilizationRequest.setEndTime(end);
        cpuUtilizationRequest.setStartTime(start);
        cpuUtilizationRequest.setNamespace("AWS/EC2");

        List<String> statistics = new ArrayList<>();
        statistics.add("Maximum");
        statistics.add("Average");
        cpuUtilizationRequest.setStatistics(statistics);

        List<Dimension> dimensions = new ArrayList<>();
        Dimension instanceId = new Dimension();
        instanceId.setName("InstanceId");
        instanceId.setValue(this.serviceID());
        dimensions.add(instanceId);
        cpuUtilizationRequest.setDimensions(dimensions);

        cpuUtilizationRequest.setPeriod(86400 / 6); //Period in seconds

        AmazonCloudWatch cloudWatch = new AmazonCloudWatchClient(getCredentials().getCredentials());
        cloudWatch.setRegion(region);

        GetMetricStatisticsResult metricResult = cloudWatch.getMetricStatistics(cpuUtilizationRequest);

        List<Datapoint> data = metricResult.getDatapoints();
        Collections.sort(data, new Comparator<Datapoint>() {
            @Override
            public int compare(Datapoint o1, Datapoint o2) {
                if (o1.getTimestamp().equals(o2.getTimestamp())) return 0;
                else if (o1.getTimestamp().before(o2.getTimestamp())) return -1;
                else return 1;
            }
        });

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm a, MMM d");
        for (Datapoint da : data) {
            cpuUtilMax.getData().add(new XYChart.Data<>(dateFormat.format(da.getTimestamp()), da.getMaximum()));
            cpuUtilAve.getData().add(new XYChart.Data<>(dateFormat.format(da.getTimestamp()), da.getAverage()));
        }


        //noinspection unchecked
        lineChart.getData().addAll(cpuUtilMax, cpuUtilAve);
        lineChart.setPrefSize(outerLayout.getPrefWidth(), outerLayout.getPrefHeight());

        //Buttons
        HBox buttons = new HBox();
        buttons.setPrefWidth(outerLayout.getPrefWidth());
        //Close
        Button close = new Button("Close");
        close.setAlignment(Pos.BOTTOM_RIGHT);
        close.setOnAction(event -> popup.hide());
        buttons.getChildren().add(close);
        buttons.setAlignment(Pos.BOTTOM_RIGHT);

        outerLayout.getChildren().addAll(lineChart, buttons);
        outerLayout.getStylesheets().add("style.css");
        outerLayout.getStyleClass().add("popup");

        popup.getContent().add(outerLayout);
        popup.show(mainScene.getWindow());
    }
}
