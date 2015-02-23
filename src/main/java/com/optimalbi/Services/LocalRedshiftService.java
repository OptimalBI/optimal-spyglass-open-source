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
import com.amazonaws.services.redshift.model.Cluster;
import com.optimalbi.Controller.Containers.AmazonCredentials;
import org.timothygray.SimpleLog.*;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;

import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Created by Timothy Gray(timg) on 27/11/2014.
 * Version: 0.0.2
 */
public class LocalRedshiftService extends AmazonService {
    private final Region region;
    private final Cluster thisCluster;
    private VBox drawing;
    private Label instanceState;

    public LocalRedshiftService(String id, AmazonCredentials credentials, Region region, Cluster cluster, Logger logger) {
        super(id, credentials, logger);
        this.region = region;
        this.thisCluster = cluster;
    }

    public String serviceState() {
        return thisCluster.getClusterStatus();
    }

    public String serviceType() {
        return "Redshift";
    }

    public String serviceName() {
        return stringCap(thisCluster.getDBName());
    }

    public String serviceSize() {
        return thisCluster.getNodeType();
    }

    public double servicePrice() {
        return 0;
    }

    public Region serviceRegion() {
        return region;
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
        instanceType.getStyleClass().add("redshiftTitle");
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
        instanceState = new Label(this.serviceState());
        if (instanceState.getText().equalsIgnoreCase("")) {
            instanceState.setTextFill(Color.RED);
        } else if (instanceState.getText().equalsIgnoreCase("available")) {
            instanceState.setTextFill(Color.GREEN);
        } else {
            instanceState.setTextFill(Color.RED);
        }
        c.add(instanceState);

        //Graph Button
        Button graphs = new Button("graphs");
        graphs.getStyleClass().add("popupButtons");
        graphs.setPrefSize(serviceWidth, buttonHeight);
        graphs.setOnAction(event -> drawGraph());
        graphs.setAlignment(Pos.CENTER);
        c.add(graphs);


        drawing.getChildren().addAll(c);
        drawing.setAlignment(Pos.TOP_CENTER);
        drawing.setMaxWidth(serviceWidth);

        return drawing;
    }

    @Override
    public void attachPricing(Map<String, Double> pricing) {

    }

    public Map<String, Double> getPricing() {
        return null;
    }

    private void drawGraph() {
        Popup popup = new Popup();
        Scene mainScene = drawing.getScene();

        //Time period for stats
        long DAY_IN_MS = 1000 * 60 * 60 * 24;
        Date date = new Date();
        Date start = new Date(date.getTime() - (7 * DAY_IN_MS));


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
        cpuUtilizationRequest.setEndTime(date);
        cpuUtilizationRequest.setStartTime(start);
        cpuUtilizationRequest.setNamespace("AWS/Redshift");

        List<String> statistics = new ArrayList<>();
        statistics.add("Maximum");
        statistics.add("Average");
        cpuUtilizationRequest.setStatistics(statistics);

        List<Dimension> dimensions = new ArrayList<>();
        Dimension instanceId = new Dimension();
        instanceId.setName("ClusterIdentifier");
        instanceId.setValue(thisCluster.getClusterIdentifier());
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
        popup.setAutoHide(true);
        popup.setAutoFix(true);
        popup.show(mainScene.getWindow());
    }

    public void refreshInstance() {

    }

    public void startService() {

    }

    public void stopService() {

    }
}
