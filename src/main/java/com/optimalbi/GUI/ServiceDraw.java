package com.optimalbi.GUI;

import com.optimalbi.GUI.TjfxFactory.TjfxFactory;
import com.optimalbi.Services.Service;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Timothy Gray on 18/02/15.
 * Version: 1.01
 */
public class ServiceDraw {
    public static int buttonWidth = 190;
    public static int buttonHeight = 20;
    public static int serviceWidth = 260;
    public static int serviceHeight = 205;
    public static int labelWidth = serviceWidth/2;
    public static int textWidth = serviceWidth - labelWidth;

    public static VBox draw(Service service){
        TjfxFactory guiFactory = new TjfxFactory(buttonWidth, buttonHeight, "stlye.css");
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
        if(Service.regionNames().containsKey(service.serviceRegion().getName())){
            regionName = new Label(Service.regionNames().get(service.serviceRegion().getName()));
        }else {
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


        //Start Stop Buttons
        HBox startStop = new HBox();
        Button start = new Button("start");
        start.setPrefSize((serviceWidth / 2), buttonHeight);
        start.setOnAction(event -> service.startService());
        Button stop = new Button("stop");
        stop.setOnAction(event -> service.stopService());

        stop.setPrefSize((serviceWidth / 2), buttonHeight);
//        startStop.getChildren().addAll(start, stop);

        //Graph Button
        Button graphs = new Button("graphs");
        graphs.setPrefSize(serviceWidth, buttonHeight);
        graphs.setOnAction(event -> drawGraph(service));

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

    public static void drawGraph(Service service){
    }

    private static String stringCap(String text) {
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

    private static Map<String,String> regionNames() {
        Map<String,String> regionNames = new HashMap<>();
        regionNames.put("ap-northeast-1","Asia Pacific (Tokyo)");
        regionNames.put("ap-southeast-1","Asia Pacific (Singapore)");
        regionNames.put("ap-southeast-2","Asia Pacific (Sydney)");
        regionNames.put("eu-central-1","EU (Frankfurt)");
        regionNames.put("eu-west-1","EU (Ireland)");
        regionNames.put("sa-east-1","South America (Sao Paulo)");
        regionNames.put("us-east-1","US East (N. Virginia)");
        regionNames.put("us-west-1","US West (N. California)");
        regionNames.put("us-west-2","US West (Oregon)");

        return regionNames;
    }
}
