package com.optimalbi.GUI;

import com.optimalbi.AmazonAccount;
import com.optimalbi.GUI.TjfxFactory.GuiButton;
import com.optimalbi.GUI.TjfxFactory.PictureButton;
import com.optimalbi.GUI.TjfxFactory.TjfxFactory;
import com.optimalbi.Services.Service;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.Bloom;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Shadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Timothy Gray on 18/02/15.
 * Version: 1.01
 */
public class ServiceDraw {
    public static final int buttonWidth = 40;
    public static final int buttonHeight = buttonWidth;
    public static final int serviceWidth = 240;
    public static int serviceHeight = 220;
    public static int labelWidth = serviceWidth / 2;
    public static final int textWidth = serviceWidth - labelWidth;
    private final String stylesheet;
    private final TjfxFactory guiFactory;
    private final Stage mainStage;

    public ServiceDraw(String stylesheet, TjfxFactory guiFactory, Stage mainStage) {
        this.stylesheet = stylesheet;
        this.guiFactory = guiFactory;
        this.mainStage = mainStage;
    }

    public VBox drawOne(Service service, GuiButton... buttons) {
        VBox drawing = null;
        if (service.serviceType().equalsIgnoreCase("ec2")) {
            drawing =  drawEc2(service, buttons);
        } else if (service.serviceType().equalsIgnoreCase("rds")) {
            drawing = drawRDS(service);
        } else if (service.serviceType().equalsIgnoreCase("redshift")) {
            drawing = drawRedshift(service, buttons);
        } else if (service.serviceType().equalsIgnoreCase("dynamodb")){
            drawing = drawDynamoDB(service,buttons);
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

    private VBox drawEc2(Service service, GuiButton... buttons) {
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
        if(service.serviceName().equals("")){
            instanceName = new Label(service.serviceID());
        }
        instanceName.setPrefWidth(serviceWidth);
        instanceName.setAlignment(Pos.CENTER);
        instanceName.getStylesheets().add(stylesheet);
        instanceName.getStyleClass().add("instanceName");
        c.add(instanceName);

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


        List<GuiButton> guiButtons = addEc2Buttons(service,buttons);

        GuiButton[] finalButtons = new GuiButton[guiButtons.size()];
        finalButtons = guiButtons.toArray(finalButtons);

        HBox buttonBox = createButtons(finalButtons);
        c.add(buttonBox);

        drawing.getChildren().addAll(c);
        drawing.setAlignment(Pos.TOP_CENTER);

        drawing.setMinWidth(serviceWidth);
        drawing.getStylesheets().add(stylesheet);
        drawing.getStyleClass().add("instance");
        return drawing;
    }

    private List<GuiButton> addEc2Buttons(Service service, GuiButton... currentButtons){
        List<GuiButton> buttons = new ArrayList<>();
        Collections.addAll(buttons, currentButtons);

        //Tags button
        Runnable tagCommand = () -> {
            double textWidth = 100;
            double labelWidth = 100;

            Map<String,String> tags = service.getTags();
            List<Node> guiComponents = new LinkedList<>();

            //Get the width for all the text
            for(String name: tags.keySet()){
                Text textNode = new Text(name+": ");

                Scene tempScene = new Scene(new Group(textNode));
                tempScene.getStylesheets().add(stylesheet);
                textNode.getStyleClass().add("tagTitle");
                textNode.applyCss();

                double tempWidth = textNode.getLayoutBounds().getWidth();
                if(tempWidth>textWidth){
                    textWidth = tempWidth;
                }
            }

            for(String name: tags.values()){
                Text textNode = new Text(name+": ");

                Scene tempScene = new Scene(new Group(textNode));
                tempScene.getStylesheets().add(stylesheet);
                textNode.getStyleClass().add("tagValue");
                textNode.applyCss();

                double tempWidth = textNode.getLayoutBounds().getWidth()+40;
                if(tempWidth>labelWidth){
                    labelWidth = tempWidth;
                }
            }

            DropShadow shadow = new DropShadow(1.8,Color.LIGHTGREY);
            shadow.setOffsetY(1);

            Text title = new Text(service.serviceName() + "'s tags");
            title.setWrappingWidth(textWidth + labelWidth);
            title.getStyleClass().add("tagTitle");
            title.setTextAlignment(TextAlignment.CENTER);
            title.setEffect(shadow);
            guiComponents.add(title);

            for(Map.Entry<String,String> tag : tags.entrySet()){
                Text textNode = new Text(tag.getKey()+": ");
                textNode.setFill(Color.BLACK);
                textNode.getStyleClass().add("tagTitle");
                textNode.setTextAlignment(TextAlignment.RIGHT);
                textNode.setWrappingWidth(textWidth);
                TextField textField = new TextField(tag.getValue());
                textField.setAlignment(Pos.CENTER);
                textField.setPrefWidth(labelWidth);
                textField.getStyleClass().add("tagValue");
                textField.setEditable(false);
                textField.setFocusTraversable(false);

                HBox box = new HBox(textNode, textField);
                box.setPrefWidth(textWidth);
                box.getStylesheets().add(stylesheet);
                box.setAlignment(Pos.CENTER_LEFT);

                box.getStyleClass().add("tagTitle");
                guiComponents.add(box);
            }
            //Close button
            Popup tagWindow = new Popup();

            Button close = guiFactory.createButton("Close");
            close.setOnAction(event -> {
                tagWindow.hide();
            });
            guiComponents.add(close);

            VBox vBox = new VBox();
            vBox.setAlignment(Pos.CENTER_RIGHT);
            vBox.getStylesheets().add(stylesheet);
            vBox.getStyleClass().add("tagBox");
            vBox.setPrefWidth(textWidth+labelWidth+5);
            vBox.getChildren().addAll(guiComponents);
            tagWindow.getContent().add(vBox);
            tagWindow.show(mainStage);
            tagWindow.setAutoHide(true);
        };
        GuiButton tag = new PictureButton("T",tagCommand,null);
        buttons.add(tag);

        return buttons;
    }

    private VBox drawRDS(Service service, GuiButton... buttons) {
        VBox drawing = new VBox();

        Pos align = Pos.CENTER;

        List<Node> c = new ArrayList<>();

        //Instance Type
        Label instanceType = new Label(stringCap(service.serviceType()));
        instanceType.getStyleClass().add("rdsTitle");
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
        if(service.serviceName().equals("")){
            instanceName = new Label(service.serviceID());
        }
        instanceName.setPrefWidth(serviceWidth);
        instanceName.setAlignment(Pos.CENTER);
        instanceName.getStylesheets().add(stylesheet);
        instanceName.getStyleClass().add("instanceName");
        c.add(instanceName);

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

        List<GuiButton> guiButtons = addRDSButtons(service, buttons);

        GuiButton[] finalButtons = new GuiButton[guiButtons.size()];
        finalButtons = guiButtons.toArray(finalButtons);

        HBox buttonBox = createButtons(finalButtons);
        c.add(buttonBox);

        drawing.getChildren().addAll(c);
        drawing.setAlignment(Pos.TOP_CENTER);

        drawing.setMinWidth(serviceWidth);
        drawing.setMinHeight(serviceHeight);
        drawing.getStylesheets().add(stylesheet);
        drawing.getStyleClass().add("instance");
        return drawing;
    }

    private List<GuiButton> addRDSButtons(Service service, GuiButton... currentButtons){
        List<GuiButton> buttons = new ArrayList<>();
        Collections.addAll(buttons, currentButtons);

        return buttons;
    }

    private VBox drawRedshift(Service service, GuiButton... buttons) {
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
        if(service.serviceName().equals("")){
            instanceName = new Label(service.serviceID());
        }
        instanceName.setPrefWidth(serviceWidth);
        instanceName.setAlignment(Pos.CENTER);
        instanceName.getStylesheets().add(stylesheet);
        instanceName.getStyleClass().add("instanceName");
        c.add(instanceName);

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

        List<GuiButton> guiButtons = addRedshiftButtons(service, buttons);

        GuiButton[] finalButtons = new GuiButton[guiButtons.size()];
        finalButtons = guiButtons.toArray(finalButtons);

        HBox buttonBox = createButtons(finalButtons);
        c.add(buttonBox);

        drawing.getChildren().addAll(c);
        drawing.setAlignment(Pos.TOP_CENTER);

        drawing.setPrefHeight(serviceHeight);
        drawing.setMinWidth(serviceWidth);
        drawing.getStylesheets().add(stylesheet);
        drawing.getStyleClass().add("instance");
        return drawing;
    }

    private List<GuiButton> addRedshiftButtons(Service service, GuiButton... currentButtons){
        List<GuiButton> buttons = new ArrayList<>();
        Collections.addAll(buttons, currentButtons);

        //Tags button
        Runnable tagCommand = () -> {
            double textWidth = 100;
            double labelWidth = 100;

            Map<String,String> tags = service.getTags();
            List<Node> guiComponents = new LinkedList<>();

            //Get the width for all the text
            for(String name: tags.keySet()){
                Text textNode = new Text(name+": ");

                Scene tempScene = new Scene(new Group(textNode));
                tempScene.getStylesheets().add(stylesheet);
                textNode.getStyleClass().add("tagTitle");
                textNode.applyCss();

                double tempWidth = textNode.getLayoutBounds().getWidth();
                if(tempWidth>textWidth){
                    textWidth = tempWidth;
                }
            }

            for(String name: tags.values()){
                Text textNode = new Text(name+": ");

                Scene tempScene = new Scene(new Group(textNode));
                tempScene.getStylesheets().add(stylesheet);
                textNode.getStyleClass().add("tagValue");
                textNode.applyCss();

                double tempWidth = textNode.getLayoutBounds().getWidth()+40;
                if(tempWidth>labelWidth){
                    labelWidth = tempWidth;
                }
            }

            DropShadow shadow = new DropShadow(1.8,Color.LIGHTGREY);
            shadow.setOffsetY(1);

            Text title = new Text(service.serviceName() + "'s tags");
            title.setWrappingWidth(textWidth + labelWidth);
            title.getStyleClass().add("tagTitle");
            title.setTextAlignment(TextAlignment.CENTER);
            title.setEffect(shadow);
            guiComponents.add(title);

            for(Map.Entry<String,String> tag : tags.entrySet()){
                Text textNode = new Text(tag.getKey()+": ");
                textNode.setFill(Color.BLACK);
                textNode.getStyleClass().add("tagTitle");
                textNode.setTextAlignment(TextAlignment.RIGHT);
                textNode.setWrappingWidth(textWidth);
                TextField textField = new TextField(tag.getValue());
                textField.setAlignment(Pos.CENTER);
                textField.setPrefWidth(labelWidth);
                textField.getStyleClass().add("tagValue");
                textField.setEditable(false);
                textField.setFocusTraversable(false);

                HBox box = new HBox(textNode, textField);
                box.setPrefWidth(textWidth);
                box.getStylesheets().add(stylesheet);
                box.setAlignment(Pos.CENTER_LEFT);

                box.getStyleClass().add("tagTitle");
                guiComponents.add(box);
            }
            //Close button
            Popup tagWindow = new Popup();

            Button close = guiFactory.createButton("Close");
            close.setOnAction(event -> {
                tagWindow.hide();
            });
            guiComponents.add(close);

            VBox vBox = new VBox();
            vBox.setAlignment(Pos.CENTER_RIGHT);
            vBox.getStylesheets().add(stylesheet);
            vBox.getStyleClass().add("tagBox");
            vBox.setPrefWidth(textWidth+labelWidth+5);
            vBox.getChildren().addAll(guiComponents);
            tagWindow.getContent().add(vBox);
            tagWindow.show(mainStage);
            tagWindow.setAutoHide(true);
        };
        GuiButton tag = new PictureButton("T",tagCommand,null);
        buttons.add(tag);

        return buttons;
    }

    private VBox drawDynamoDB(Service service, GuiButton... buttons) {
        VBox drawing = new VBox();

        Pos align = Pos.CENTER;

        List<Node> c = new ArrayList<>();

        //Instance Type
        Label instanceType = new Label(stringCap(service.serviceType()));
        instanceType.setAlignment(Pos.CENTER);
        instanceType.getStyleClass().add("DBTitle");
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
        if(service.serviceName().equals("")){
            instanceName = new Label(service.serviceID());
        }
        instanceName.setPrefWidth(serviceWidth);
        instanceName.setAlignment(Pos.CENTER);
        instanceName.getStylesheets().add(stylesheet);
        instanceName.getStyleClass().add("instanceName");
        c.add(instanceName);
;
        //Size
        Label instanceSizeLabel = new Label("Rate: ");
        instanceSizeLabel.setPrefWidth(labelWidth/2);
        instanceSizeLabel.setAlignment(Pos.CENTER_RIGHT);
        instanceSizeLabel.getStyleClass().add("guiLabel");
        Label instanceSize = new Label(service.serviceSize());
        instanceSize.setPrefWidth(160);



        HBox sizeBox = new HBox(instanceSizeLabel, instanceSize);
        sizeBox.setPrefWidth(serviceWidth);
        sizeBox.setAlignment(align);
        c.add(sizeBox);

        Label instanceCostLabel = new Label("Cost: ");
        instanceCostLabel.setPrefWidth(labelWidth/2);
        instanceCostLabel.setAlignment(Pos.CENTER_RIGHT);
        instanceCostLabel.getStyleClass().add("guiLabel");
        Label instanceCost = new Label("Coming Soon!");
        instanceCost.setPrefWidth(160);

        HBox costBox = new HBox(instanceCostLabel, instanceCost);
        costBox.setPrefWidth(serviceWidth);
        costBox.setAlignment(align);
        c.add(costBox);


        //Instance State
        Label instanceState = new Label(service.serviceState().toLowerCase());
        instanceState.setAlignment(Pos.CENTER);
        instanceState.setPrefWidth(serviceWidth);
        if (Service.runningTitles().contains(service.serviceState().toLowerCase())) {
            instanceState.setTextFill(Color.GREEN);
        } else if (service.serviceState().equalsIgnoreCase("stopped")) {
            instanceState.setTextFill(Color.RED);
        } else {
            instanceState.setTextFill(Color.ORANGERED);
        }
        c.add(instanceState);

        List<GuiButton> guiButtons = addDynamoDBButtons(service, buttons);

        GuiButton[] finalButtons = new GuiButton[guiButtons.size()];
        finalButtons = guiButtons.toArray(finalButtons);

        HBox buttonBox = createButtons(finalButtons);
        c.add(buttonBox);

        drawing.getChildren().addAll(c);
        drawing.setAlignment(Pos.TOP_CENTER);

        drawing.setPrefHeight(serviceHeight);
        drawing.setMinWidth(serviceWidth);
        drawing.getStylesheets().add(stylesheet);
        drawing.getStyleClass().add("instance");
        return drawing;
    }

    private List<GuiButton> addDynamoDBButtons(Service service, GuiButton... currentButtons){
        List<GuiButton> buttons = new ArrayList<>();
        Collections.addAll(buttons, currentButtons);
        return buttons;
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

    private HBox createButtons(GuiButton... buttons){
        ArrayList<Node> c = new ArrayList<>();
        for (GuiButton b : buttons){
            Button button;
            if(b.display() == null){
                button = new Button(b.name());
            } else {
                button = new Button("",b.display());
            }
            if(b.command() != null){
                button.setOnAction(event->{
                    b.command().run();
                });
            }
            button.getStyleClass().add("serviceButton");
            button.setPrefSize(buttonWidth, buttonHeight);
            c.add(button);
        }
        HBox hbox = new HBox();
        hbox.getChildren().addAll(c);
        hbox.setAlignment(Pos.BOTTOM_CENTER);
        hbox.getStyleClass().add("serviceButtons");
        hbox.setMaxWidth(serviceWidth);
        hbox.setPrefHeight(serviceHeight/2);
        return hbox;
    }
}
