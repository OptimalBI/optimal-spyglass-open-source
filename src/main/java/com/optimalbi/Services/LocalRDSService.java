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
import com.amazonaws.services.rds.model.DBInstance;
import com.optimalbi.Controller.Containers.AmazonCredentials;
import com.optimalbi.SimpleLog.Logger;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Timothy Gray(timg) on 3/12/2014.
 * Version: 0.0.2
 */
public class LocalRDSService extends AmazonService {
    private final Region region;
    private final DBInstance thisService;
    private VBox drawing = null;

    private Label instanceState;

    public LocalRDSService(String id, AmazonCredentials credentials, Region region, DBInstance instance, Logger logger) {
        super(id, credentials, logger);
        this.thisService = instance;
        this.region = region;
    }

    public void refreshInstance() {

    }

    public String serviceState() {
        return thisService.getDBInstanceStatus();
    }

    public String serviceName() {
        return thisService.getDBName();
    }

    public String serviceSize() {
        return null;
    }

    public double servicePrice() {
        return 0;
    }

    public Region serviceRegion() {
        return region;
    }

    public void startService() {

    }

    public void stopService() {

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
        instanceType.getStyleClass().add("rdsTitle");
        instanceType.setAlignment(Pos.CENTER);
        instanceType.setPrefWidth(serviceWidth);
        c.add(instanceType);

        //TODO: Remove temp region label
        Label regionName = new Label(this.region.getName());
        regionName.setPrefWidth(serviceWidth);
        regionName.setAlignment(Pos.CENTER);
        regionName.getStyleClass().add("regionTitle");
        c.add(regionName);

        //Instance Name
        Label instanceName = new Label(this.serviceName());
        instanceName.setPrefWidth(serviceWidth);
        instanceName.setAlignment(Pos.CENTER);
        c.add(instanceName);

        //Instance State
        instanceState = new Label(this.serviceState());
        instanceState.setAlignment(Pos.CENTER);
        instanceState.setPrefWidth(serviceWidth);
        if (instanceState.getText().equalsIgnoreCase("stopped")) {
            instanceState.setTextFill(Color.RED);
        } else if (instanceState.getText().equalsIgnoreCase("available")) {
            instanceState.setTextFill(Color.GREEN);
        } else {
            instanceState.setTextFill(Color.ORANGERED);
        }
        c.add(instanceState);

        drawing.getChildren().addAll(c);
        drawing.setPrefWidth(serviceWidth);
        drawing.setPrefHeight(serviceHeight);
        return drawing;
    }

    public void attachPricing(Map<String, Double> pricing) {

    }

    public String serviceType() {
        return "RDS";
    }

}
