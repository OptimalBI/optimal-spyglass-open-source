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

package com.optimalbi.Controller;

import com.amazonaws.AmazonClientException;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.ServiceAbbreviations;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.rds.AmazonRDSClient;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.DescribeDBInstancesResult;
import com.amazonaws.services.redshift.AmazonRedshiftClient;
import com.amazonaws.services.redshift.model.Cluster;
import com.amazonaws.services.redshift.model.DescribeClustersResult;
import com.optimalbi.Controller.Containers.AmazonCredentials;
import com.optimalbi.Services.Service;
import com.optimalbi.Services.*;
import com.optimalbi.SimpleLog.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.layout.VBox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Timothy Gray on 30/10/2014.
 * Version: 0.0.2
 */
public class AmazonAccount{
    private final AmazonCredentials credentials;
    private final List<Region> regions;
    private final Logger logger;

    private final BooleanProperty ready = new SimpleBooleanProperty(false);
    //File names
    private final File ec2Pricing = new File("EC2.Pricing.csv");
    private final IntegerProperty completed = new SimpleIntegerProperty(0);
    private boolean readyValue = false;
    private List<Service> services;
    //Statistics
    private int totalServices = 0;
    private int runningServices = 0;
    private Map<String, Integer> runningCount;

    public AmazonAccount(AmazonCredentials credentials, List<Region> regions, Logger logger) {
        this.credentials = credentials;
        this.regions = regions;
        this.logger = logger;

        runningCount = new HashMap<>();
    }

    public void startConfigure() {
        try {
            configure();
            if (getRegions().size() > 0) {
                populateEc2();
                populateRedshift();
                populateRDS();
                populateStatistics();
            }
        } catch (AmazonClientException e) {
            getLogger().error("Error in starting service: " + e.getMessage());
        } finally {
            readyValue = true;
            ready.setValue(true);
        }

    }

    private void configure() throws AmazonClientException {
        services = new ArrayList<>();
            if (getCredentials() == null) {
                throw new AmazonClientException("No credentials provided");
            }
    }

    private void populateEc2() throws AmazonClientException {
        Map<String, Double> pricing = readEc2Pricing();
        for (Region region : getRegions()) {
            try {
//                services.addAll(Ec2Service.populateServices(region, getCredentials(), getLogger(), pricing));
                AmazonEC2Client ec2 = new AmazonEC2Client(getCredentials().getCredentials());
                ec2.setRegion(region);
                DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
                List<Reservation> reservations = describeInstancesRequest.getReservations();
                Set<Instance> inst = new HashSet<>();
                for (Reservation reservation : reservations) {
                    inst.addAll(reservation.getInstances());
                }

                getLogger().info("EC2, Adding " + inst.size() + " instances from " + region.getName());

                for (Instance i : inst) {
                    Service temp = new LocalEc2Service(i.getInstanceId(), getCredentials(), region, ec2, getLogger());
                    if (pricing != null) {
                        temp.attachPricing(pricing);
                    }
                    services.add(temp);
                }

            } catch (AmazonClientException e) {
                throw new AmazonClientException(region.getName() + " " + e.getMessage());
            }
            completed.set(completed.get()+1);
        }

    }

    private void populateRedshift() throws AmazonClientException {
        for (Region region : getRegions()) {
            try {
                if (region.isServiceSupported(ServiceAbbreviations.RedShift)) {
//                    services.addAll(RedshiftService.populateServices(region, getCredentials(), getLogger()));
                    AmazonRedshiftClient redshift = new AmazonRedshiftClient(getCredentials().getCredentials());
                    redshift.setRegion(region);

                    DescribeClustersResult clusterResult;
                    List<Cluster> clusters;
                    try {
                        clusterResult = redshift.describeClusters();
                        clusters = clusterResult.getClusters();
                    } catch (Exception e) {
                        throw new AmazonClientException("Failed to get clusters " + e.getMessage());
                    }

                    getLogger().info("Redshift, Adding " + clusters.size() + " clusters from " + region.getName());
                    for (Cluster cluster : clusters) {
                        getLogger().info("Cluster: " + cluster.getClusterIdentifier());
                        services.add(new LocalRedshiftService(cluster.getClusterIdentifier(), getCredentials(), region, cluster, getLogger()));
                    }
                }else{
                    getLogger().info("Redshift, NOPE from " + region.getName());

                }
            } catch (AmazonClientException e) {
                throw new AmazonClientException(region.getName() + " " + e.getMessage());
            }
            completed.set(completed.get() + 1);
        }
    }

    private void populateRDS() throws AmazonClientException {
        for (Region region : getRegions()) {
            try {
                if(region.isServiceSupported(ServiceAbbreviations.RDS)) {
//                services.addAll(RDSService.populateServices(region, getCredentials(), getLogger()));
                    AmazonRDSClient rds = new AmazonRDSClient(getCredentials().getCredentials());
                    rds.setRegion(region);

                    DescribeDBInstancesResult result = rds.describeDBInstances();
                    List<DBInstance> instances = result.getDBInstances();

                    getLogger().info("RDS, Adding " + instances.size() + " instances from " + region.getName());
                    services.addAll(instances.stream().map(i -> new LocalRDSService(i.getDBInstanceIdentifier(), getCredentials(), region, i, getLogger())).collect(Collectors.toList()));
                }else {
                    getLogger().info("RDS, NOPE from " + region.getName());
                }
            } catch (AmazonClientException e) {
                throw new AmazonClientException(region.getName() + " " + e.getMessage());
            }
            completed.set(completed.get()+1);

        }
    }

    private void populateStatistics() {
        runningCount = new HashMap<>();
        runningCount.put("costs", 0);

        totalServices = services.size();
        runningServices = 0;

        int runningEc2 = 0;
        int runningRDS = 0;
        int runningRedshift = 0;

        for (Service s : services) {
            switch (s.serviceType().toLowerCase()) {
                case "ec2":
                    if (s.serviceState().equalsIgnoreCase("running")) {
                        runningEc2++;
                        runningServices++;
                    }
                    break;
                case "rds":
                    if (s.serviceState().toLowerCase().equals("available")) {
                        runningRDS++;
                        runningServices++;
                    }
                    break;
                case "redshift":
                    if (s.serviceState().toLowerCase().equals("available")) {
                        runningRedshift++;
                        runningServices++;
                    }
                    break;
            }
        }
        runningCount.put("ec2", runningEc2);
        runningCount.put("rds", runningRDS);
        runningCount.put("redshift", runningRedshift);
    }

    public BooleanProperty getReady() {
        return ready;
    }

    public boolean getReadyValue() {
        return readyValue;
    }

    public List<VBox> drawInstances() {
        return services.stream().map(Service::draw).collect(Collectors.toList());
    }

    public List<Service> getServices() {
        return services;
    }

    public int getTotalServices() {
        return totalServices;
    }

    public int getRunningServices() {
        return runningServices;
    }

    public Map<String, Integer> getRunningCount() {
        return runningCount;
    }

    private Map<String, Double> readEc2Pricing() {
        Map<String, Double> pricing = new HashMap<>();

        BufferedReader fileReader = null;

        if (!ec2Pricing.exists()) {
            return null;
        }

        try {
            fileReader = new BufferedReader(new FileReader(ec2Pricing));

            fileReader.readLine();

            String line = fileReader.readLine();

            while (line != null) {
                String[] split = line.split(",");
                pricing.put(split[0], Double.valueOf(split[6]));
                line = fileReader.readLine();
            }

        } catch (IOException e) {
            getLogger().error("Failed to read ec2Pricing file properly");
            return null;
        } finally {
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e) {
                    getLogger().error("Failed to close fileReader");
                }
            }
        }
        return pricing;
    }

    public IntegerProperty getCompleted() {
        return completed;
    }

    List<Region> getRegions(){
        return regions;
    }

    Logger getLogger(){
        return this.logger;
    }

    public AmazonCredentials getCredentials(){
        return credentials;
    }
}
