package com.optimalbi.GUI;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
import com.optimalbi.AmazonAccount;
import com.optimalbi.Controller.Containers.AmazonCredentials;
import com.optimalbi.GUI.TjfxFactory.TjfxFactory;
import com.optimalbi.Services.Service;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import java.util.stream.Collectors;

/**
 * Created by Timothy Gray on 18/02/15.
 * Version: 1.01
 */
public class ServiceDraw {
    public static int buttonWidth = 190;
    public static int buttonHeight = 20;
    public static int serviceWidth = 260;
    public static int serviceHeight = 205;
    public static int labelWidth = serviceWidth / 2;
    public static int textWidth = serviceWidth - labelWidth;
    public final String stylesheet;

    public ServiceDraw(String stylesheet) {
        this.stylesheet = stylesheet;
    }

    public VBox drawOne(Service service) {
        TjfxFactory guiFactory = new TjfxFactory(buttonWidth, buttonHeight, stylesheet);
        VBox drawing = new VBox();

        Pos align = Pos.CENTER;

        List<Node> c = new ArrayList<>();

        //Instance Type
        Label instanceType = new Label(stringCap(service.serviceType()));
        instanceType.getStyleClass().add("ec2Title");
        instanceType.setAlignment(Pos.CENTER);
        instanceType.setPrefWidth(serviceWidth);
        c.add(instanceType);

        //TODO: Remove temp region label
        Label regionName;
        if (Service.regionNames().containsKey(service.serviceRegion().getName())) {
            regionName = new Label(Service.regionNames().get(service.serviceRegion().getName()));
        } else {
            regionName = new Label(service.serviceID());
        }
        regionName.setPrefWidth(serviceWidth);
        regionName.setAlignment(Pos.CENTER);
        regionName.getStyleClass().add("regionTitle");
        c.add(regionName);

        //Instance Name
        Label instanceName = new Label(service.serviceName());
        instanceName.setPrefWidth(serviceWidth);
        instanceName.setAlignment(Pos.CENTER);
        instanceName.getStylesheets().add(stylesheet);
        instanceName.getStyleClass().add("instanceName");
        c.add(instanceName);

        //Instance ID
        Label instanceIDLabel = new Label("Service ID: ");
        instanceIDLabel.setPrefWidth(labelWidth);
        instanceIDLabel.setAlignment(Pos.CENTER_RIGHT);
        instanceIDLabel.getStyleClass().add("guiLabel");
        Label instanceID = new Label(service.serviceID());
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
        Label instanceSize = new Label(service.serviceSize());
        instanceSize.setPrefWidth(labelWidth);

        HBox sizeBox = new HBox(instanceSizeLabel, instanceSize);
        sizeBox.setPrefWidth(serviceWidth);
        sizeBox.setAlignment(align);
        c.add(sizeBox);

        //Costs area
        Map<String, Double> pricing = service.getPricing();
        if (pricing != null) {
            if (pricing.containsKey(service.serviceSize())) {
                double cost = pricing.get(service.serviceSize());
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
        Label instanceState = new Label(service.serviceState());
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

        drawing.getChildren().addAll(c);
        drawing.setAlignment(Pos.TOP_CENTER);
        drawing.setPrefHeight(serviceHeight);

        drawing.setPrefWidth(serviceWidth);
        drawing.getStylesheets().add(stylesheet);
        drawing.getStyleClass().add("instance");
        return drawing;
    }

    public List<VBox> drawAccount(AmazonAccount account) {
        List<Service> accountServices = account.getServices();
        return accountServices.stream().map(this::drawOne).collect(Collectors.toList());
    }

    public Popup drawGraph(AmazonCredentials credentials, Service service, Scene mainScene) {
        Popup popup = new Popup();

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

        lineChart.setTitle(service.serviceName() + " Metrics");

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
        instanceId.setValue(service.serviceID());
        dimensions.add(instanceId);
        cpuUtilizationRequest.setDimensions(dimensions);

        cpuUtilizationRequest.setPeriod(86400 / 6); //Period in seconds

        AmazonCloudWatch cloudWatch = new AmazonCloudWatchClient(credentials.getCredentials());
        cloudWatch.setRegion(service.serviceRegion());

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
        return popup;
    }

    private String stringCap(String text) {
        int maxChars = 99;
        char[] chars = text.toCharArray();
        if (chars.length > 0) {
            chars[0] = Character.toUpperCase(chars[0]);
        } else return "";
        String shortString = String.valueOf(chars);
        if (shortString.length() > maxChars) {
            shortString = shortString.substring(0, maxChars - 3);
            shortString = shortString + "...";
        }
        return shortString;
    }

    private Map<String, String> regionNames() {
        Map<String, String> regionNames = new HashMap<>();
        regionNames.put("ap-northeast-1", "Asia Pacific (Tokyo)");
        regionNames.put("ap-southeast-1", "Asia Pacific (Singapore)");
        regionNames.put("ap-southeast-2", "Asia Pacific (Sydney)");
        regionNames.put("eu-central-1", "EU (Frankfurt)");
        regionNames.put("eu-west-1", "EU (Ireland)");
        regionNames.put("sa-east-1", "South America (Sao Paulo)");
        regionNames.put("us-east-1", "US East (N. Virginia)");
        regionNames.put("us-west-1", "US West (N. California)");
        regionNames.put("us-west-2", "US West (Oregon)");

        return regionNames;
    }
}
