package com.optimalbi.GUI;

import com.optimalbi.AmazonAccount;
import com.optimalbi.Services.Service;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Timothy Gray on 18/02/15.
 * Version: 1.01
 */
public class ServiceDraw {
    public static final int buttonWidth = 190;
    public static final int buttonHeight = 20;
    public static final int serviceWidth = 260;
    public static final int serviceHeight = 250;
    public static final int labelWidth = serviceWidth / 2;
    public static final int textWidth = serviceWidth - labelWidth;
    private final String stylesheet;

    public ServiceDraw(String stylesheet) {
        this.stylesheet = stylesheet;
    }

    public VBox drawOne(Service service) {
        VBox drawing = null;
        if (service.serviceType().equalsIgnoreCase("ec2")) {
            drawing =  drawEc2(service);
        } else if (service.serviceType().equalsIgnoreCase("rds")) {
            drawing = drawRDS(service);
        } else if (service.serviceType().equalsIgnoreCase("redshift")) {
            drawing = drawRedshift(service);
        } else {
            drawing = drawGeneric(service);
        }
        drawing.setMinHeight(serviceHeight);
        drawing.setMaxHeight(serviceHeight);

        drawing.setMinWidth(serviceWidth);
        drawing.setMaxWidth(serviceWidth);

        return drawing;
    }

    private VBox drawGeneric(Service service) {
        VBox drawing = new VBox();

        List<Node> c = new ArrayList<>();

        //Instance Type
        Label instanceType = new Label(stringCap(service.serviceType()));
        switch (service.serviceType()) {
            case "RDS":
                instanceType.getStyleClass().add("rdsTitle");
                break;
            case "ec2":
                instanceType.getStyleClass().add("ec2Title");
                break;
            case "Redshift":
                instanceType.getStyleClass().add("redshiftTitle");
                break;
            default:
                instanceType.getStyleClass().add("statisticsTitle");
                break;
        }
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

        //Instance State
        Label instanceState = new Label(service.serviceState());
        instanceState.setAlignment(Pos.CENTER);
        instanceState.setPrefWidth(serviceWidth);
        if (instanceState.getText().equalsIgnoreCase("stopped")) {
            instanceState.setTextFill(Color.RED);
        } else if (instanceState.getText().equalsIgnoreCase("running") || instanceState.getText().equalsIgnoreCase("available")) {
            instanceState.setTextFill(Color.GREEN);
        } else {
            instanceState.setTextFill(Color.ORANGERED);
        }
        c.add(instanceState);

        drawing.getChildren().addAll(c);
        drawing.setAlignment(Pos.TOP_CENTER);
        drawing.setPrefHeight(serviceHeight);

        drawing.setMinWidth(serviceWidth);
        drawing.getStylesheets().add(stylesheet);
        drawing.getStyleClass().add("instance");
        return drawing;
    }

    private VBox drawEc2(Service service) {
        VBox drawing = new VBox();

        Pos align = Pos.CENTER;

        List<Node> c = new ArrayList<>();

        //Instance Type
        Label instanceType = new Label(stringCap(service.serviceType()));
        switch (service.serviceType()) {
            case "RDS":
                instanceType.getStyleClass().add("rdsTitle");
                break;
            case "ec2":
                instanceType.getStyleClass().add("ec2Title");
                break;
            case "Redshift":
                instanceType.getStyleClass().add("redshiftTitle");
                break;
            default:
                instanceType.getStyleClass().add("statisticsTitle");
                break;
        }
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
                Label instancePricing = new Label("Unknown");
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
        } else if (instanceState.getText().equalsIgnoreCase("running") || instanceState.getText().equalsIgnoreCase("available")) {
            instanceState.setTextFill(Color.GREEN);
        } else {
            instanceState.setTextFill(Color.ORANGERED);
        }
        c.add(instanceState);

        drawing.getChildren().addAll(c);
        drawing.setAlignment(Pos.TOP_CENTER);

        drawing.setMinWidth(serviceWidth);
        drawing.setMinHeight(serviceHeight);
        drawing.getStylesheets().add(stylesheet);
        drawing.getStyleClass().add("instance");
        return drawing;
    }

    private VBox drawRDS(Service service) {
        VBox drawing = new VBox();

        Pos align = Pos.CENTER;

        List<Node> c = new ArrayList<>();

        //Instance Type
        Label instanceType = new Label(stringCap(service.serviceType()));
        switch (service.serviceType()) {
            case "RDS":
                instanceType.getStyleClass().add("rdsTitle");
                break;
            case "ec2":
                instanceType.getStyleClass().add("ec2Title");
                break;
            case "Redshift":
                instanceType.getStyleClass().add("redshiftTitle");
                break;
            default:
                instanceType.getStyleClass().add("statisticsTitle");
                break;
        }
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
        Label dbName = new Label("DB Name: ");
        dbName.setPrefWidth(labelWidth);
        dbName.setAlignment(Pos.CENTER_RIGHT);
        dbName.getStyleClass().add("guiLabel");
        Label instanceID = new Label(service.serviceID());
        instanceID.setPrefWidth(labelWidth);

        HBox iID = new HBox(dbName, instanceID);
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
                Label instancePricing = new Label("Unknown");
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
        } else if (instanceState.getText().equalsIgnoreCase("running") || instanceState.getText().equalsIgnoreCase("available")) {
            instanceState.setTextFill(Color.GREEN);
        } else {
            instanceState.setTextFill(Color.ORANGERED);
        }
        c.add(instanceState);

        drawing.getChildren().addAll(c);
        drawing.setAlignment(Pos.TOP_CENTER);

        drawing.setMinWidth(serviceWidth);
        drawing.setMinHeight(serviceHeight);
        drawing.getStylesheets().add(stylesheet);
        drawing.getStyleClass().add("instance");
        return drawing;
    }

    private VBox drawRedshift(Service service) {
        VBox drawing = new VBox();

        Pos align = Pos.CENTER;

        List<Node> c = new ArrayList<>();

        //Instance Type
        Label instanceType = new Label(stringCap(service.serviceType()));
        switch (service.serviceType()) {
            case "RDS":
                instanceType.getStyleClass().add("rdsTitle");
                break;
            case "ec2":
                instanceType.getStyleClass().add("ec2Title");
                break;
            case "Redshift":
                instanceType.getStyleClass().add("redshiftTitle");
                break;
            default:
                instanceType.getStyleClass().add("statisticsTitle");
                break;
        }
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
                Label instancePricing = new Label("Unknown");
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
        } else if (instanceState.getText().equalsIgnoreCase("running") || instanceState.getText().equalsIgnoreCase("available")) {
            instanceState.setTextFill(Color.GREEN);
        } else {
            instanceState.setTextFill(Color.ORANGERED);
        }
        c.add(instanceState);

        drawing.getChildren().addAll(c);
        drawing.setAlignment(Pos.TOP_CENTER);

        drawing.setPrefHeight(serviceHeight);
        drawing.setMinWidth(serviceWidth);
        drawing.getStylesheets().add(stylesheet);
        drawing.getStyleClass().add("instance");
        return drawing;
    }

    public List<VBox> drawAccount(AmazonAccount account) {
        List<Service> accountServices = account.getServices();
        return accountServices.stream().map(this::drawOne).collect(Collectors.toList());
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
