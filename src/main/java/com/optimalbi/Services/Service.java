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
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Timothy Gray(timg) on 25/11/2014.
 * Version: 0.0.2
 */
public interface Service extends Comparable<Service>{

    public static int buttonWidth = 190;
    public static int buttonHeight = 20;
    public static int serviceWidth = 260;
    public static int serviceHeight = 205;
    public static int labelWidth = serviceWidth/2;
    public static int textWidth = serviceWidth - labelWidth;

    @SuppressWarnings("SameReturnValue")
    public static int serviceWidth(){
        return serviceWidth;
    }

    void refreshInstance();

    String serviceID();

    String serviceState();

    String serviceType();

    String serviceName();

    String serviceSize();

    double servicePrice();

    Region serviceRegion();

    void startService();

    void stopService();

    VBox draw();

    void attachPricing(Map<String,Double> pricing);

    public Map<String, Double> getPricing();

    public static Map<String,String> regionNames() {
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
