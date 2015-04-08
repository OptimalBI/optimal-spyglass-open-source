package com.optimalbi.GUI;
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

import com.amazonaws.AmazonClientException;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
import com.optimalbi.AmazonAccount;
import com.optimalbi.Controller.Containers.AmazonCredentials;
import com.optimalbi.Controller.Containers.AmazonRegion;
import com.optimalbi.GUI.TjfxFactory.PictureButton;
import com.optimalbi.GUI.TjfxFactory.TjfxFactory;
import com.optimalbi.ServicePricing;
import com.optimalbi.Services.Service;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.*;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimplePBEConfig;
import org.jasypt.properties.PropertyValueEncryptionUtils;
import org.jasypt.util.password.BasicPasswordEncryptor;
import org.jasypt.util.password.PasswordEncryptor;
import org.timothygray.SimpleLog.EmptyLogger;
import org.timothygray.SimpleLog.FileLogger;
import org.timothygray.SimpleLog.Logger;

import java.awt.*;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The main GUI controller and GUI logic for OptimalSpyglass. Includes encryption and connection/file connection/
 *
 * @author Timothy Gray
 */
public class Main extends Application {
    private static final int[] curVer = {0, 9, 0};
    private static double applicationHeight;
    private static double applicationWidth;
    private static Stage mainStage;
    //Gui Components
    private final double buttonWidth = 210;
    private final double buttonHeight = 25;
    private final String styleSheet = "style.css";
    private final BorderPane border = new BorderPane();
    private final File credentialsFile = new File("credentials");
    private final File settingsFile = new File("settings.cfg");
    private final File pricingDir = new File("pricing\\");
    private Map<Region, ServicePricing> pricings;
    //Bounds of the application
    private Rectangle2D primaryScreenBounds;
    private Map<String, TextField> fields;
    private Logger logger;
    private Popup dialog;
    private TjfxFactory guiFactory;
    private ProgressBar progressBar = null;
    private List<AmazonCredentials> credentials;
    private ArrayList<AmazonAccount> accounts;
    private List<AmazonRegion> allRegions;
    private List<Region> currentRegions;
    private List<Integer> versi;
    //Security stuff
    private SimplePBEConfig simplePBEConfig;
    private StandardPBEStringEncryptor encryptor;
    private String decryptedPassword = "";
    private String encryptedPassword = "";
    //Misc other
    private boolean redrawHook = false;
    private int totalAreas = 0;
    private int doneAreas = 0;
    @SuppressWarnings("FieldCanBeLocal")
    private String viewedRegion = "summary";
    private String listOrBoxes = "list";
    private Button summary;


    private final ChangeListener<Number> paintListener = new ChangeListener<Number>() {
        /*
         *   Creates a delayed draw event which will create a new thread and wait for delayTime number of seconds
         *   before redrawing the centre and top of the app.
         */
        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            if (!(oldValue.equals(newValue))) {
                int delayTime = 100;
                Task<Void> task = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        Thread.sleep(delayTime);
                        applicationHeight = mainStage.getHeight();
                        applicationWidth = mainStage.getWidth();

                        Platform.runLater(()->{
                            border.setTop(createTop());
                            border.setBottom(createBottom());
                            updatePaintingBoxes();
                        });

                        return null;
                    }
                };
                new Thread(task).start();
            }
        }
    };

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private static void download(URL input, File output) throws IOException {
        try (InputStream in = input.openStream()) {
            try (OutputStream out = new FileOutputStream(output)) {
                copy(in, out);
            }
        }
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        while (true) {
            int readCount = in.read(buffer);
            if (readCount == -1) {
                break;
            }
            out.write(buffer, 0, readCount);
        }
    }

    private static void openWebpage(URI uri) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void start(Stage primaryStage) throws Exception {
        currentRegions = new ArrayList<>();
        credentials = new ArrayList<>();

        //Setup the encryptor for Secret Keys
        simplePBEConfig = new SimplePBEConfig();
        simplePBEConfig.setKeyObtentionIterations(1000);
        encryptor = new StandardPBEStringEncryptor();
        encryptor.setConfig(simplePBEConfig);

        try {
            File logFile = new File("log.txt");
            logFile.delete();
            if (!logFile.createNewFile()) throw new IOException("Failed to create log file");
            logger = new FileLogger(logFile);
        } catch (IOException e) {
            System.err.print("Failed to create logFile: " + e.getLocalizedMessage());
            logger = new EmptyLogger();
        }

        //Setup global Main variables
        mainStage = primaryStage;
        primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        applicationHeight = primaryScreenBounds.getHeight() / 1.11;
        applicationWidth = primaryScreenBounds.getWidth() / 1.11;
        guiFactory = new TjfxFactory(buttonWidth, buttonHeight, styleSheet, "http://fonts.googleapis.com/css?family=Droid+Sans");
        createGUI();

        //TODO: Add icon image;
        mainStage.getIcons().add(new Image("Optimal_Logo_CMYK_1Icon.png"));
        mainStage.setTitle("OptimalSpyglass");
        mainStage.show();

        logger.info("Hello chaps");

        summary.requestFocus();

        //Process the settings file
        loadSettings();

        border.setTop(createTop());

        //Load access keys from file, if they don't exist ask for them
        //TODO: check if keys are valid on load
        if (encryptedPassword.equals("")) {
            newPassword("");
        } else {
            askForPassword("Please enter password", 0);
        }
    }

    private void newPassword(String failedMessage) {
        if (failedMessage == null) {
            failedMessage = "";
        }
        resetDialog();
        ArrayList<Node> c = new ArrayList<>();
        Pos alignment = Pos.BASELINE_LEFT;

        //Prompt Text
        Label promptLabel = new Label("Please enter the password you want use for this application");
        promptLabel.setAlignment(alignment);
        c.add(promptLabel);

        //Fail Text
        Label failText = new Label(failedMessage);
        if (!failedMessage.equals("")) {
            failText.setTextFill(Color.RED);
            c.add(failText);
        }

        //TextField
        Label fLabel = new Label("Password:");
        fLabel.setMinWidth(120);
        fLabel.setAlignment(alignment);
        PasswordField field = new PasswordField();
        field.setMinWidth(160);
        HBox fBox = new HBox(fLabel, field);
        fBox.setAlignment(alignment);


        //TextField2
        Label sFLabel = new Label("Confirmation:");
        sFLabel.setMinWidth(120);
        sFLabel.setAlignment(alignment);
        PasswordField secondField = new PasswordField();
        secondField.setMinWidth(160);
        HBox sFBox = new HBox(sFLabel, secondField);
        sFBox.setAlignment(alignment);

        c.add(fBox);
        c.add(sFBox);

        //Go button
        Button okBtn = guiFactory.createButton("Okay", 120, 12);
        HBox btnBox = new HBox(okBtn);
        btnBox.setMinWidth(applicationWidth / 3.2);
        btnBox.setAlignment(alignment);

        c.add(btnBox);


        EventHandler<ActionEvent> go = event -> {
            if (field.getText().length() > 1) {
                if (secondField.getText().equals(field.getText())) {
                    PasswordEncryptor pe = new BasicPasswordEncryptor();
                    encryptedPassword = pe.encryptPassword(field.getText());
                    decryptedPassword = field.getText();
                    encryptor.setPassword(decryptedPassword);
                    saveSettings();
                    resetDialog();
                    askForCredentials();
                } else {
                    resetDialog();
                    newPassword("Please enter the same password twice");
                }
            } else {
                resetDialog();
                newPassword("Please enter a valid password");
            }
        };
        field.setOnAction(go);
        secondField.setOnAction(go);
        okBtn.setOnAction(go);

        VBox layout = new VBox();
        layout.setAlignment(alignment);
        layout.getChildren().addAll(c);
        layout.getStylesheets().add(styleSheet);
        layout.getStyleClass().add("popup");
        layout.setPrefSize(applicationWidth, applicationHeight);

        setCentre(layout);

//        dialog = guiFactory.setupDialog(applicationWidth / 3.2, applicationHeight / 2, layout);
//        dialog.show(mainStage);
//        dialog.setAutoHide(true);
    }

    private void setCentre(Node centre) {
        border.setCenter(centre);
    }

    private void resetDialog() {
        if (dialog != null) {
            dialog.hide();
            dialog = null;
        }
    }

    private void askForPassword(String promptText, int attempts) {
        double textWidth = 90; //The minimum size the labels take up (aligns the Main)
        double boxWidth = 240;
        fields = new HashMap<>(); //Reset the fields collection so we can use it from the callback method
        VBox layout = new VBox();

        ArrayList<Node> c = new ArrayList<>();

        //Prompt text
        Label prompt = new Label(promptText);
        prompt.setMinWidth(textWidth + boxWidth);
        c.add(prompt);

        //Attempts text
        String caution = "";
        switch (attempts) {
            case 0:
                caution = "";
                break;
            case 1:
                caution = "Incorrect Password, Attempt 2";
                break;
            case 2:
                caution = "Incorrect Password, Attempt 3";
                break;
            case 3:
                System.exit(504);
                break;
            default:
                System.exit(504);
                break;
        }
        if (!caution.equals("")) {
            Label cautionText = new Label(caution);
            cautionText.setMinWidth(textWidth + boxWidth);
            cautionText.setTextFill(Color.ORANGE);
            c.add(cautionText);
        }


        //Password box
        Label passwordBoxTitle = new Label("Password: ");
        passwordBoxTitle.setPrefWidth(textWidth);
        PasswordField passwordField = new PasswordField();
        passwordField.setPrefWidth(boxWidth);
        passwordField.getStyleClass().add("password");
        HBox passwordCombo = new HBox(passwordBoxTitle, passwordField);
        passwordCombo.setAlignment(Pos.CENTER);
        c.add(passwordCombo);

        //Button
        Button okay = guiFactory.createButton("Okay", 120, 20);
        okay.getStyleClass().add("popupButton");
        HBox buttonBox = new HBox(okay);
        buttonBox.getStyleClass().add("popupButtons");
        buttonBox.setPrefWidth(textWidth + boxWidth);
        buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
        c.add(buttonBox);

        EventHandler<ActionEvent> finishEvent = event -> {
            if (passwordField.getText().equals("")) {
                askForPassword("Please enter a valid password", 0);
            } else if (!matchPassword(passwordField.getText())) {
                askForPassword("Password incorrect", attempts + 1);
            } else {
                decryptedPassword = passwordField.getText();
                simplePBEConfig.setPassword(decryptedPassword);
                encryptor.initialize();
                border.setCenter(waitingCentre());
                getAccessKeys();
                if ((credentials.size() == 0)) {
                    //If there are no credentials saved then ask for them
                    askForCredentials();
                } else {
                    //Else go and populate the services with their controllers
                    createAccounts();
                }
            }
        };
        passwordField.setOnAction(finishEvent);
        okay.setOnAction(finishEvent);


        layout.getChildren().addAll(c);
        layout.getStyleClass().add("settings");
        layout.getStylesheets().add(styleSheet);
        layout.setMaxWidth(textWidth + boxWidth);
        layout.setMaxHeight(prompt.getPrefHeight()*3);
        layout.setAlignment(Pos.CENTER);

        setCentre(layout);

        passwordField.requestFocus();
    }

    private boolean matchPassword(String password) {
        PasswordEncryptor pe = new BasicPasswordEncryptor();
        return pe.checkPassword(password, encryptedPassword);
    }

    private void createGUI() {
        //Create the gui sections
        border.setBottom(createBottom());
        border.setTop(createTop());
        VBox centrePlaceHolder = new VBox();
        centrePlaceHolder.setPrefSize(applicationWidth, applicationHeight);
        border.setCenter(centrePlaceHolder);
        border.getStylesheets().add(styleSheet);
        border.getStyleClass().add("borderPane");
        border.setFocusTraversable(false);
        //Add gui sections to the stage
        Scene scene = new Scene(border, applicationWidth, applicationHeight);
        mainStage.setScene(scene);

        //Add the hook to kill tasks on app close, prevents the app from creating threads while it is shutting down
        addShutdownHook();
    }

    private void addShutdownHook() {
        //Fixes a bug with the windows crashing on exit when using the OS close button
        mainStage.setOnCloseRequest(event -> System.exit(0));
    }

    private HBox createButtonMenu() {
        List<Node> guiComponents = new ArrayList<>();
        HBox buttons = new HBox();
        Pos alignment = Pos.CENTER;
        //Regions Button
        Button regions = guiFactory.createButton("Regions");
        regions.setAlignment(alignment);
        regions.getStyleClass().add("barItems");
        regions.setOnAction(ActionEvent -> selectRegions());
        guiComponents.add(regions);

        //Update button - This button repolls the AWS API
        Button update = guiFactory.createButton("Update");
        update.setAlignment(alignment);
        update.getStyleClass().add("barItems");
        update.setOnAction(ActionEvent -> {
            Platform.runLater(Main.this::createAccounts);
        });

        guiComponents.add(update);

        //Manage credentials
        Button manageCredentials = guiFactory.createButton("Add/Remove credentials");
        manageCredentials.setAlignment(alignment);
        manageCredentials.getStyleClass().add("barItems");
        manageCredentials.setOnAction(event -> drawAccountManagement());
        guiComponents.add(manageCredentials);

        //Change password
        Button changePassword = guiFactory.createButton("Change password");
        changePassword.setAlignment(alignment);
        changePassword.getStyleClass().add("barItems");
        changePassword.setOnAction(event -> changePassword(""));
        guiComponents.add(changePassword);

        //Exit button
        Button exit = guiFactory.createButton("Exit");
        exit.setAlignment(alignment);
        exit.getStyleClass().add("barItems");
        exit.setOnAction(event -> System.exit(0));
        exit.setFocusTraversable(false);
        guiComponents.add(exit);

        //Add all buttons to buttons box
        buttons.getChildren().addAll(guiComponents);
        buttons.setPrefWidth(applicationWidth);
        buttons.getStyleClass().add("barStyle");

        return buttons;
    }

    private void changePassword(String notification) {
        ArrayList<Node> c = new ArrayList<>();
        Pos alignment = Pos.CENTER;

        double textWidth = 200;
        double boxWidth = 240;

        //Notification Label
        if (notification != null && !notification.equalsIgnoreCase("")) {
            Label noti = new Label(notification);
            c.add(noti);
        }

        //Old password combo box
        Label oldPswdLabel = new Label("Old password: ");
        oldPswdLabel.setMinWidth(textWidth);
        PasswordField oldPswdField = new PasswordField();
        oldPswdField.setMinWidth(boxWidth);
        HBox oldPswdBox = new HBox(oldPswdLabel, oldPswdField);
        oldPswdBox.setAlignment(alignment);
        c.add(oldPswdBox);

        //New password combo box
        Label newPswdLabel = new Label("New password: ");
        newPswdLabel.setMinWidth(textWidth);
        PasswordField newPswdField = new PasswordField();
        newPswdField.setMinWidth(boxWidth);
        HBox newPswdBox = new HBox(newPswdLabel, newPswdField);
        newPswdBox.setAlignment(alignment);
        c.add(newPswdBox);

        //New password combo box
        Label newPswdLabe2 = new Label("Confirm password: ");
        newPswdLabe2.setMinWidth(textWidth);
        PasswordField newPswdField2 = new PasswordField();
        newPswdField2.setMinWidth(boxWidth);
        HBox newPswdBox2 = new HBox(newPswdLabe2, newPswdField2);
        newPswdBox2.setAlignment(alignment);
        c.add(newPswdBox2);

        //Buttons
        Button okay = guiFactory.createButton("Okay", (textWidth + boxWidth) / 2, 20);
        Button cancel = guiFactory.createButton("Cancel", (textWidth + boxWidth) / 2, 20);
        cancel.setOnAction(event -> {
            viewedRegion = "summary";
            updatePaintingBoxes();
        });
        HBox buttons = new HBox(okay, cancel);
        buttons.getStyleClass().add("popupButtons");
        buttons.setPrefWidth(boxWidth + textWidth);
        buttons.setAlignment(alignment);
        c.add(buttons);

        EventHandler<ActionEvent> goEvent = event -> {
            if (oldPswdField.getText().equals(decryptedPassword)) {
                if (!newPswdField.getText().equals(newPswdField2.getText())) {
                    changePassword("Please enter the same password twice");
                    return;
                }
                decryptedPassword = newPswdField2.getText();

                //Setup the encryptor for Secret Keys
                simplePBEConfig = new SimplePBEConfig();
                simplePBEConfig.setKeyObtentionIterations(1000);
                simplePBEConfig.setPassword(decryptedPassword);
                encryptor = new StandardPBEStringEncryptor();
                encryptor.setConfig(simplePBEConfig);
                encryptor.initialize();

                PasswordEncryptor pe = new BasicPasswordEncryptor();
                encryptedPassword = pe.encryptPassword(newPswdField2.getText());

                saveSettings();
                loadSettings();
                writeCredentials();

                if (dialog != null) {
                    dialog.hide();
                    dialog = null;
                }
            } else {
                dialog.hide();
                dialog = null;
                changePassword("Old password incorrect");
            }
        };
        okay.setOnAction(goEvent);
        newPswdField2.setOnAction(goEvent);

        VBox layout = new VBox();
        layout.getChildren().addAll(c);
        layout.getStylesheets().add(styleSheet);
        layout.getStyleClass().add("settings");
        layout.setAlignment(Pos.CENTER);
        layout.setMaxWidth(boxWidth + textWidth + 5);
        layout.setMaxHeight(oldPswdField.getHeight() * 4);
        layout.autosize();

        setCentre(layout);
        oldPswdField.requestFocus();
    }

    private VBox createBottom() {
        List<Node> guiComponents = new ArrayList<>();
        HBox layout = new HBox();

        //Copyright text
        HBox bottomLeft = new HBox();
        Label cr = new Label("Copyright 2015 OptimalBI - Licensed under Apache 2.0");
        cr.setMinWidth(320);
        cr.setTextFill(Color.WHITE);
        cr.setPrefWidth(320);
        bottomLeft.setAlignment(Pos.BOTTOM_LEFT);
        bottomLeft.getStylesheets().add("style.css");
        bottomLeft.getStyleClass().add("botStyle");
        bottomLeft.setPrefSize(applicationWidth, 20);
        bottomLeft.getChildren().add(cr);
        guiComponents.add(bottomLeft); //Add after debug output

        //Version Text
        HBox verTextBox = new HBox();
        Label verText = new Label(String.format("Version: %d.%d.%d", curVer[0], curVer[1], curVer[2]));
        verText.setMinWidth(320);
        verText.setTextFill(Color.WHITE);
        verText.setPrefWidth(320);
        verText.setAlignment(Pos.BOTTOM_RIGHT);
        verTextBox.setAlignment(Pos.BOTTOM_RIGHT);
        verTextBox.getStylesheets().add("style.css");
        verTextBox.getStyleClass().add("botStyle");
        verTextBox.setPrefSize(applicationWidth, 20);
        verTextBox.getChildren().add(verText);
        guiComponents.add(verTextBox); //Add after debug output

        layout.getChildren().addAll(guiComponents);
        layout.getStylesheets().add("style.css");
        layout.getStyleClass().add("otherBotStyle");
        layout.setPrefSize(applicationWidth, 20);
        layout.setAlignment(Pos.BOTTOM_LEFT);

        VBox botBar = new VBox();
        botBar.getChildren().add(layout);
        return botBar;
    }

    private VBox waitingCentre() {
        progressBar = new ProgressBar(0.03);
        progressBar.setPrefWidth(applicationWidth / 2);
        progressBar.setPrefHeight(15);

        HBox topBit = new HBox(progressBar);
        topBit.setAlignment(Pos.TOP_CENTER);
        topBit.setPrefHeight(applicationHeight / 2);
        topBit.setPrefWidth(applicationWidth);

        VBox layout = new VBox(topBit);

        layout.setAlignment(Pos.CENTER);
        layout.setPrefWidth(applicationWidth);
        layout.setPrefHeight(applicationHeight / 2);
        layout.getStylesheets().add("style.css");
        layout.getStyleClass().add("centreStyle");
        return layout;
    }

    private VBox labelCentre(String label) {
        Label centreLabel = new Label(label);

        VBox layout = new VBox();
        layout.getChildren().add(centreLabel);

        layout.getStylesheets().add("style.css");
        layout.getStyleClass().add("centreStyle");
        return layout;
    }

    private VBox createTop() {
        List<Node> guiComponents = new ArrayList<>();
        HBox topLayout = new HBox();
        HBox botLayout = new HBox();

        //TOP SECTION

        //Add Logo
        ImageView iv1 = guiFactory.imageView("header-logo.png");
        guiComponents.add(iv1);

        //Text for the title
        Label title = new Label();
        title.setText("OptimalSpyglass - Part of the OptimalBI AWS Toolkit");
        title.getStyleClass().add("topStyle");
        title.setPrefHeight(35);
        guiComponents.add(title);

        //Version notification
        if(versi == null) {
            versi = getLatestVersionNumber();
            logger.debug(String.format("Remote version: %d.%d.%d. Current version: %d.%d.%d.", versi.get(0), versi.get(1), versi.get(2), curVer[0], curVer[1], curVer[2]));
            //Int varibles to clear my head
            int curMaj = curVer[0];
            int curMin = curVer[1];
            int curPatch = curVer[2];
            int newMaj = versi.get(0);
            int newMin = versi.get(1);
            int newPatch = versi.get(2);

            boolean newVersion = false;

            if ((newMaj > curMaj) || (newMaj == curMaj && newMin > curMin) || (newMaj == curMaj && newMin == curMin && newPatch > curPatch)) {
                newVersion = true;
            }

            Label versionNotification = new Label("New version available, Click Here");
            versionNotification.getStyleClass().add("versionText");
            versionNotification.setMinWidth(220);
            versionNotification.setAlignment(Pos.CENTER);
            versionNotification.setOnMouseClicked(MouseEvent -> {
                try {
                    openWebpage(new URI("https://github.com/OptimalBI/optimal-spyglass-open-source/releases"));
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            });
            if (newVersion) {
                guiComponents.add(versionNotification);
            }
        }


        topLayout.getChildren().addAll(guiComponents);
        topLayout.setAlignment(Pos.CENTER);
        topLayout.getStyleClass().add("topStyle");

        double thisHeight = applicationHeight / 8;
        if (thisHeight > 20) thisHeight = 20;
        topLayout.setPrefWidth(applicationWidth);
        topLayout.setPrefHeight(thisHeight);

        //Bottom section of toolbar
        ToolBar topBar = updateToolbar();
//        topBar.getStyleClass().add("toolbar");
        topBar.setPrefWidth(applicationWidth * 2);

        botLayout.getChildren().add(topBar);

        HBox buttonMenu = createButtonMenu();
        buttonMenu.setPrefWidth(applicationWidth);
        buttonMenu.setAlignment(Pos.CENTER);

        VBox outline = new VBox();
        outline.setPrefWidth(applicationWidth);
        outline.getChildren().addAll(topLayout, botLayout);
        outline.getChildren().add(buttonMenu);
        outline.getStylesheets().add(styleSheet);
        return outline;
    }

    private List<Integer> getLatestVersionNumber() {
        List<Integer> vers = new ArrayList<>();
        File versionTemp = new File("verTemp.txt");
        versionTemp.deleteOnExit();
        vers.add(-1);
        vers.add(-1);
        vers.add(-1);
        try {
            URL dl = new URL("https://raw.githubusercontent.com/OptimalBI/optimal-spyglass-open-source/master/version");
            download(dl, versionTemp);
            BufferedReader fileReader = null;
            try {
                fileReader = new BufferedReader(new FileReader(versionTemp));
                String line = fileReader.readLine();
                String[] split = line.split(" ");
                vers.set(0, Integer.parseInt(split[0]));
                vers.set(1, Integer.parseInt(split[1]));
                vers.set(2, Integer.parseInt(split[2]));
            } catch (IOException e) {
                logger.error("Failed to save version file: " + e.getMessage());
            }
            versionTemp.delete();
            return vers;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return vers;
        } catch (IOException e) {
            e.printStackTrace();
            return vers;
        }
    }

    private ToolBar updateToolbar() {
        Map<String, String> regionNames = Service.regionNames();
        double minWidth = 60;

        List<Node> toolButtons = new ArrayList<>();
        Label allFilterLabel = new Label("View: ");
        allFilterLabel.getStyleClass().add("toolbarLabel");
        toolButtons.add(allFilterLabel);

        summary = guiFactory.createButton("Summary", -1, -1);
        summary.setOnAction(ActionEvent -> {
            viewedRegion = "summary";
            updatePaintingBoxes();
        });
        summary.setMinWidth(minWidth);
        toolButtons.add(summary);

        Button all = guiFactory.createButton("All", -1, -1);
        all.setOnAction(ActionEvent -> {
            viewedRegion = "all";
            updatePaintingBoxes();
        });
        all.setMinWidth(minWidth);
        toolButtons.add(all);

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        spacer.setPrefWidth(18);
        toolButtons.add(spacer);

        Label regionLabel = new Label("Region Filter: ");
        regionLabel.getStyleClass().add("toolbarLabel");
        toolButtons.add(regionLabel);

        for (Region region : currentRegions) {
            Button adding;
            if (regionNames.containsKey(region.getName())) {
                adding = guiFactory.createButton(regionNames.get(region.getName()), -1, -1);
            } else {
                adding = guiFactory.createButton(region.getName(), -1, -1);
            }
            adding.setOnAction(ActionEvent -> {
                viewedRegion = region.getName();
                updatePaintingBoxes();
            });
            toolButtons.add(adding);
        }

        ToolBar toolBar = new ToolBar();
        toolBar.getItems().addAll(toolButtons);
        toolBar.getStylesheets().add(styleSheet);
        toolBar.getStyleClass().add("toolbar");
        toolBar.setPrefWidth(primaryScreenBounds.getWidth());

        return toolBar;
    }

    private void updatePaintingBoxes() {
        if (!redrawHook) {
            //The first time we draw, hook the redraw listener to the changes in the Main's size
            Task task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    mainStage.widthProperty().addListener(paintListener);
                    mainStage.heightProperty().addListener(paintListener);
                    return null;
                }
            };
            new Thread(task).start();
            redrawHook = true;
        }
        if (!viewedRegion.equals("summary")) {
            BigDecimal iW = new BigDecimal((((mainStage.getWidth()) - 70) / ServiceDraw.serviceWidth));
            int instancesWide = iW.intValue();
            drawServiceBoxes(instancesWide);
        } else {
            drawSummary();
        }
    }

    /**
     * These loops create the section of the GUI where it is divided by account, the first loop loops over all currently added AWS accounts
     * The second loop goes over the AWS controllers, checks if they belong to the current looping account and if so draws them in their rows
     * If the rows exceed instancesWide it starts a new row
     *
     * @param instancesWide The number of instances to draw in each row.
     */
    private void drawServiceBoxes(int instancesWide) {
        if(listOrBoxes.equalsIgnoreCase("list")){
            drawListView();
            return;
        }

        ServiceDraw draw = new ServiceDraw(styleSheet,guiFactory,mainStage);
        VBox allInstances = new VBox();

        for (AmazonCredentials credential : credentials) {
            int i = 0;

            VBox thisAccount = new VBox();
            Label accountLabel = new Label(credential.getAccountName());
            accountLabel.getStyleClass().add("accountTitle");
            thisAccount.getChildren().add(accountLabel);

            for (AmazonAccount account : accounts) {
                if (account.getCredentials().getAccountName().equals(credential.getAccountName())) {
                    List<Service> services = account.getServices();
                    ArrayList<HBox> rows = new ArrayList<>();
                    HBox currentRow = new HBox();
                    for (Service service : services) {
                        VBox box;


                        if (service.serviceType().equalsIgnoreCase("ec2")) {
                            Runnable command = () -> {
                                resetDialog();
                                dialog = drawGraph(account.getCredentials(), service, mainStage.getScene());
                                dialog.setAutoHide(true);
                                dialog.show(mainStage);
                            };
                            box = draw.drawOne(service, new PictureButton("G", command, null));
                        } else {
                            box = draw.drawOne(service);
                        }

                        if (viewedRegion.equals("all")) {
                            box.getStyleClass().add("instance");
                            currentRow.getChildren().add(box);
                            i++;
                        } else {
                            if (service.serviceRegion().getName().equals(viewedRegion)) {
                                box.getStyleClass().add("instance");
                                currentRow.getChildren().add(box);
                                i++;
                            }
                        }
                        if (i + 1 >= instancesWide) {
                            rows.add(currentRow);
                            currentRow = new HBox();
                            i = 0;
                        }
                    }
                    rows.add(currentRow);
                    for (HBox row : rows) {
                        row.getStylesheets().add(styleSheet);
                        row.getStyleClass().add("instanceRows");
                    }
                    if (rows.size() > 0) {
                        thisAccount.getChildren().addAll(rows);
                        thisAccount.getStylesheets().add("style.css");
                        thisAccount.getStyleClass().add("centreStyle");
                    }
                }
            }
            allInstances.getChildren().add(thisAccount);
        }

        allInstances.getStylesheets().add(styleSheet);
        allInstances.getStyleClass().add("centreStyle");

        //Adds them to a scroll pane so if the window is too small we can scroll!
        ScrollPane scrollPane = new ScrollPane(allInstances);
        scrollPane.setPannable(true);
        scrollPane.getStylesheets().add(styleSheet);

        //Makes sure this is applied on the main thread so the GUI doesn't throw a fit
        Platform.runLater(() -> border.setCenter(scrollPane));
    }

    private void drawListView(){
        TableView<Service> tableView = new TableView<>();
        tableView.setFocusTraversable(false);
        tableView.setPrefSize(applicationWidth, applicationHeight);
        Map<AmazonAccount,Service> accountService = new HashMap<>();
        for(AmazonAccount acc : accounts){
            for(Service s : acc.getServices()){
                accountService.put(acc,s);
                tableView.getItems().add(s);
            }
        }

        //Service Type
        TableColumn<Service,String> serviceTypeCol = new TableColumn<>("Type");
        serviceTypeCol.setPrefWidth(65);
        serviceTypeCol.setCellValueFactory(cellData -> {
            String serviceType = cellData.getValue().serviceType();
            serviceType = serviceType.toUpperCase();
            return new SimpleStringProperty(serviceType);
        });
        tableView.getColumns().add(serviceTypeCol);

        //Service Name
        TableColumn<Service,String> serviceNameCol = new TableColumn<>("Name");
        serviceNameCol.setPrefWidth(200);
        serviceNameCol.setCellValueFactory(cellData -> {
            String serviceName = cellData.getValue().serviceName();
            return new SimpleStringProperty(serviceName);
        });
        tableView.getColumns().add(serviceNameCol);

        //Service Size
        TableColumn<Service,String> serviceSizeCol = new TableColumn<>("Size");
        serviceSizeCol.setPrefWidth(120);
        serviceSizeCol.setCellValueFactory(cellData -> {
            String serviceSize = cellData.getValue().serviceSize();
            return new SimpleStringProperty(serviceSize);
        });
        tableView.getColumns().addAll(serviceSizeCol);

        //Service Price
        TableColumn<Service,Double> serviceCostCol = new TableColumn<>("Cost $/hr");
        serviceCostCol.setPrefWidth(100);
        serviceCostCol.setCellValueFactory(cellData -> {
            Double cost = cellData.getValue().servicePrice();
            cost = round(cost,2);
            return new SimpleObjectProperty<>(cost);
        });
        tableView.getColumns().add(serviceCostCol);

        tableView.getStylesheets().add(styleSheet);
        VBox outer = new VBox(tableView);
        outer.setPrefSize(applicationWidth,applicationHeight);
        outer.getStyleClass().add("centrePlaceStyle");
        outer.getStylesheets().add(styleSheet);

        Platform.runLater(()-> border.setCenter(outer));
    }

    private void askForCredentials() {
        double textWidth = 160; //The minimum size the labels take up (aligns the Main)
        fields = new HashMap<>(); //Reset the fields collection so we can use it from the callback method
        Pos alignment = Pos.TOP_LEFT;

        VBox outerLayout = new VBox();
        outerLayout.setPrefWidth(mainStage.getWidth() / 2);
        List<Node> c = new ArrayList<>();

        //Label
        Label title = new Label("Please enter your AWS access and secret key\nThe secret key will be encrypted and stored in the local directory");
        c.add(title);

        //Account Name
        Label accountNameTitle = new Label("Account Name: ");
        accountNameTitle.setPrefWidth(textWidth);
        fields.put("account name", new TextField());
        fields.get("account name").setPrefWidth(outerLayout.getPrefWidth() - textWidth);
        HBox accountNameCombo = new HBox(accountNameTitle, fields.get("account name"));
        accountNameCombo.setAlignment(alignment);
        c.add(accountNameCombo);

        //Access Key
        Label accessKeyTitle = new Label("Access Key: ");
        accessKeyTitle.setPrefWidth(textWidth);
        fields.put("access key", new TextField());
        fields.get("access key").setPrefWidth(outerLayout.getPrefWidth() - textWidth);
        HBox accessKeyCombo = new HBox(accessKeyTitle, fields.get("access key"));
        accessKeyCombo.setAlignment(alignment);
        c.add(accessKeyCombo);

        //Secret Key
        Label secretKeyTitle = new Label("Secret Key: ");
        secretKeyTitle.setPrefWidth(textWidth);
        fields.put("secret key", new TextField());
        fields.get("secret key").setPrefWidth(outerLayout.getPrefWidth() - textWidth);
        fields.get("secret key").setOnAction(event -> saveCredentials());
        HBox secretKeyCombo = new HBox(secretKeyTitle, fields.get("secret key"));
        secretKeyCombo.setAlignment(alignment);
        c.add(secretKeyCombo);

        //Buttons
        Button okay = guiFactory.createButton("Okay", buttonWidth, buttonHeight / 2);
        okay.setOnAction(event -> saveCredentials());
        Button cancel = guiFactory.createButton("Cancel", buttonWidth, buttonHeight / 2);
        cancel.setOnAction(event -> {
            //If there are no credentials entered exit the app
            if (credentials == null || credentials.size() == 0) {
                System.exit(404);
            } else {
                viewedRegion = "summary";
                updatePaintingBoxes();
            }
        });
        HBox buttons = new HBox(okay, cancel);
        buttons.setPrefWidth(outerLayout.getPrefWidth());
        buttons.setAlignment(alignment);
        buttons.getStyleClass().add("subpopup");
        c.add(buttons);

        outerLayout.getStylesheets().add(styleSheet);
        outerLayout.getStyleClass().add("popup");
        outerLayout.getChildren().addAll(c);
        outerLayout.setAlignment(alignment);

        setCentre(outerLayout);

//        dialog = guiFactory.setupDialog(-1, -1, outerLayout);
//        dialog.setX(mainStage.getX() + mainStage.getWidth() / 2 - dialog.getWidth() / 2);
//        dialog.setY(mainStage.getY() + mainStage.getHeight() / 2 - dialog.getHeight() / 2);
//        dialog.show(mainStage);
//        dialog.setAutoHide(true);
    }

    private void saveCredentials() {
        if (fields.get("access key").getText() == null || fields.get("secret key").getText() == null) {
            askForCredentials();
            return;
        }
        credentials.add(new AmazonCredentials(fields.get("account name").getText(), fields.get("access key").getText(), fields.get("secret key").getText()));
        writeCredentials();
    }

    private void writeCredentials() {
        PrintWriter writer = null;
        try {
            //Reset the credentials file
            credentialsFile.delete();
            credentialsFile.createNewFile();

            writer = new PrintWriter(new FileWriter(credentialsFile));

            for (AmazonCredentials credential : credentials) {
                writer.println(String.format("AWS_ACCOUNT_NAME %s", credential.getAccountName()));
                writer.println(String.format("AWS_ACCESS_KEY %s", credential.getAccessKey()));
                writer.println(String.format("AWS_SECRET_KEY %s", PropertyValueEncryptionUtils.encrypt(credential.getSecretKey(), encryptor)));
                writer.println("");
            }
        } catch (IOException e) {
            logger.error("Failed to save credentials " + e.getMessage());
            labelCentre("Failed to save credentials " + e.getMessage());
        } finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
            fields = null;
        }

        //After saving to file reload the credentials into memory to prevent duplicates
        getAccessKeys();
        if (credentials != null) {
            try {
                createAccounts();
            } catch (AmazonClientException e) {
                logger.error("Failed to create credentials " + e.getMessage());
            }
        } else {
            askForCredentials();
        }
    }

    private void getAccessKeys() {
        if (!credentialsFile.exists()) {
            try {
                if (!credentialsFile.createNewFile()) logger.error("Failed to create credentials file");
            } catch (IOException e) {
                logger.error("Failed to create credentials file " + e.getMessage());
            }
        }
        boolean oldFile = false;

        List<String> accessKeys = new ArrayList<>();
        List<String> secretKeys = new ArrayList<>();
        List<String> accountNames = new ArrayList<>();
        BufferedReader fileReader = null;
        try {

            fileReader = new BufferedReader(new FileReader(credentialsFile));
            String line = fileReader.readLine();
            while (line != null) {
                String[] split = line.split(" ");
                if (split.length > 1) {
                    switch (split[0].toLowerCase()) {
                        case "accountname":
                            oldFile = true;
                            String adding = "";
                            if (split.length > 1) {
                                for (int z = 1; z < split.length; z++) {
                                    adding = adding + split[z] + " ";
                                }
                            }
                            accountNames.add(adding);
                            break;
                        case "aws_access_key":
                            accessKeys.add(split[1]);
                            break;
                        case "aws_secret_key":
                            secretKeys.add(PropertyValueEncryptionUtils.decrypt(split[1], encryptor));
                            break;
                        case "aws_account_name":
                            String adding1 = "";
                            if (split.length > 1) {
                                for (int z = 1; z < split.length; z++) {
                                    adding1 = adding1 + split[z] + " ";
                                }
                            }
                            accountNames.add(adding1);
                            break;
                        case "accesskey":
                            oldFile = true;
                            accessKeys.add(split[1]);
                            break;
                        case "secretkey":
                            oldFile = true;
                            secretKeys.add(PropertyValueEncryptionUtils.decrypt(split[1], encryptor));
                            break;
                        default:
                            logger.warn("Unknown setting " + split[0]);
                            break;
                    }
                } else {
                    if (!split[0].equals("")) logger.warn("No data entered for " + split[0]);
                }
                line = fileReader.readLine();
            }
        } catch (IOException e) {
            logger.error("Failed to read credentials file: " + e.getMessage());
            Platform.runLater(() -> border.setCenter(labelCentre("Failed to read credentials file: " + e.getMessage())));
        }
        credentials = new ArrayList<>();
        for (int i = 0; i != accountNames.size(); i++) {
            credentials.add(new AmazonCredentials(accountNames.get(i), accessKeys.get(i), secretKeys.get(i)));
        }
        if (oldFile) {
            writeCredentials();
        }
    }

    private void createAccounts() {
        border.setCenter(waitingCentre());
        currentRegions = new ArrayList<>();
        //If the region is currently marked as one we are interested in then add it to the current regions collection
        currentRegions.addAll(allRegions.stream().filter(AmazonRegion::getActive).map(AmazonRegion::getRegion).collect(Collectors.toList()));

        doneAreas = 0;
        totalAreas = credentials.size() * currentRegions.size() * 3;
        logger.debug("Total areas: " + totalAreas);


        readPricingDir();


        accounts = new ArrayList<>();
        for (AmazonCredentials credential : credentials) {
            AmazonAccount thisController = new AmazonAccount(credential, currentRegions, logger, pricings);
            thisController.getCompleted().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    if (newValue.intValue() > oldValue.intValue()) {
                        doneAreas++;
                        updateProgress();
                    }
                }
            });

            //Add a change listener to the ready flag from the controller
            thisController.getReady().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    boolean ready = true;
                    for (AmazonAccount controller : accounts) {
                        if (!controller.getReadyValue()) {
                            ready = false;
                        }
                    }
                    //If all controllers are ready then draw the Main
                    if (ready) {
                        updatePaintingBoxes();
                    }
                }
            });
            accounts.add(thisController);
            Task task = new Task<Void>() {
                protected Void call() throws Exception {
                    thisController.startConfigure();
                    return null;
                }
            };
            new Thread(task).start();
        }
    }

    private void readPricingDir() {
        pricings = new HashMap<>();
        if (!pricingDir.exists()) {
            logger.error("No pricings directory");
            return;
        }
        Set<File> pricingFiles;
        if (pricingDir.listFiles() != null) {
            pricingFiles = new HashSet<>(Arrays.asList(pricingDir.listFiles()));
        } else {
            logger.error("Pricing directory is empty");
            return;
        }
        for (File f : pricingFiles) {
            String[] nameSplit = f.getName().split(" ");
            String regionName = nameSplit[0];
            if (Service.regionNames().containsKey(regionName)) {
                Region thisRegion = Region.getRegion(Regions.fromName(regionName));
                pricings.put(thisRegion, new ServicePricing(f, logger, thisRegion));
                logger.debug("Got pricing for: " + Region.getRegion(Regions.fromName(regionName)).getName());
            } else {
                logger.warn("Invalid region: " + regionName);
            }
        }
    }

    private void updateProgress() {
        if (progressBar == null) return;
        double progress = ((double) doneAreas / (double) totalAreas);
        Platform.runLater(() -> progressBar.setProgress(progress));
    }

    private void drawAccountManagement() {
        VBox allCredentials = new VBox();
        allCredentials.setPrefWidth(primaryScreenBounds.getWidth() / 3);

        double textWidth = 350;

        for (AmazonCredentials credential : credentials) {
            Pos alignment = Pos.CENTER;

            //Account label
            Label label = new Label("Account Name: ");
            label.minWidth(textWidth);
            label.getStyleClass().add("textLabel");

            //Account name
            Label name = new Label(credential.getAccountName());
            name.setPrefWidth(allCredentials.getPrefWidth() - textWidth);


            //Remove button
            Button remove = guiFactory.createButton("Remove", buttonWidth / 2, buttonHeight / 2.5);
            remove.setOnAction(ActionEvent -> {
                removeAccount(credential);
                drawAccountManagement();
            });

            HBox layout = new HBox(label, name, remove);
            layout.setPrefWidth(allCredentials.getPrefWidth());
            layout.setAlignment(alignment);
            layout.getStyleClass().add("subpopup");
            allCredentials.getChildren().add(layout);
        }

        //Add button
        Button add = guiFactory.createButton("Add", buttonWidth, buttonHeight / 2);
        add.setOnAction(event -> {
            resetDialog();
            askForCredentials();
        });

        //Close button
        Button close = guiFactory.createButton("Close", buttonWidth, buttonHeight / 2);
        close.setOnAction(event -> {
            updatePaintingBoxes();
            resetDialog();
        });
        HBox buttons = new HBox(add, close);
        buttons.setPrefWidth(allCredentials.getPrefWidth());
        buttons.getStyleClass().add("popupButtons");
        buttons.setAlignment(Pos.CENTER);
        allCredentials.getChildren().add(buttons);

        //Config anything that need to go all the way across the gui
        buttons.setPrefWidth(allCredentials.getPrefWidth());

        allCredentials.getStylesheets().add(styleSheet);
        allCredentials.getStyleClass().add("settings");
        allCredentials.setAlignment(Pos.TOP_CENTER);

        setCentre(allCredentials);
    }

    private void selectRegions() {
        //TODO: Select regions on first start
        //Title label
        Label title = new Label("Please select the desired regions");
        HBox titleBox = new HBox(title);

        double regionButtonWidth = 140;

        //Regions layout
        HBox layout = new HBox();
        layout.getStyleClass().add("regionBar");
        layout.getStylesheets().add(styleSheet);

        final SimpleBooleanProperty toChange = new SimpleBooleanProperty(false);

        for (AmazonRegion reg : allRegions) {
            Button region;
            if (Service.regionNames().containsKey(reg.getRegion().getName())) {
                region = guiFactory.createButton(Service.regionNames().get(reg.getRegion().getName()), regionButtonWidth, buttonHeight / 3);
            } else {
                region = guiFactory.createButton(reg.getRegion().getName(), regionButtonWidth, buttonHeight / 3);
            }
            if (reg.getActive()) {
                region.getStyleClass().add("buttonDone");
            }
            region.setOnAction(ActionEvent -> {
                reg.toggleActive();
                toChange.set(true);
                if (reg.getActive()) {
                    region.getStyleClass().add("buttonDone");
                } else {
                    region.getStyleClass().removeAll("buttonDone");
                }
            });

            layout.getChildren().add(region);
        }

        //Close button
        Button close = guiFactory.createButton("close", regionButtonWidth, buttonHeight / 3);
        close.setOnAction(ActionEvent -> {
            dialog.hide();
            dialog = null;
            if (toChange.get()) {
                saveSettings();
                viewedRegion = "all";
                border.setCenter(waitingCentre());
                createAccounts();
                border.setTop(createTop());
            }
        });

        layout.getChildren().add(close);
        layout.setMaxWidth(applicationWidth);
        VBox outer = new VBox(titleBox, layout);
        outer.getStyleClass().add("popup");
        outer.getStylesheets().add(styleSheet);
        outer.setMaxWidth(applicationWidth);
        dialog = guiFactory.setupDialog(-1, -1, outer);
        dialog.show(mainStage);
        dialog.setAutoHide(true);
    }

    private void saveSettings() {
        PrintWriter writer = null;
        currentRegions = new ArrayList<>();
        currentRegions.addAll(allRegions.stream().filter(AmazonRegion::getActive).map(AmazonRegion::getRegion).collect(Collectors.toList()));
        try {
            settingsFile.delete();
            settingsFile.createNewFile();

            writer = new PrintWriter(new FileWriter(settingsFile));

            writer.print("regions ");

            for (Region r : currentRegions) {
                writer.print(r.getName() + ",");
            }
            writer.println();

            writer.print("password " + encryptedPassword);
        } catch (IOException e) {
            logger.error("Failed to save settings " + e.getMessage());
            labelCentre("Failed to save settings " + e.getMessage());
        } finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        }
    }

    private void loadSettings() {
        if (!settingsFile.exists()) {
            try {
                if (!settingsFile.createNewFile()) {
                    logger.error("Failed to create settings file");
                    return;
                }
            } catch (IOException e) {
                logger.error("Failed to create settings file " + e.getMessage());
            }
            allRegions = new ArrayList<>();
            currentRegions = new ArrayList<>();
            Regions[] regionses = Regions.values();
            for (Regions re : regionses) {
                if (!Region.getRegion(re).getName().equals(Regions.GovCloud.getName()) & !Region.getRegion(re).getName().equals(Regions.CN_NORTH_1.getName())) {
                    AmazonRegion tempRegion;
                    if (re.getName().equals(Regions.AP_SOUTHEAST_2.getName())) {
                        tempRegion = new AmazonRegion(Region.getRegion(re), true);
                        currentRegions.add(Region.getRegion(re));
                    } else {
                        tempRegion = new AmazonRegion(Region.getRegion(re), false);
                    }
                    allRegions.add(tempRegion);
                }
            }
            saveSettings();
        }

        BufferedReader fileReader = null;
        allRegions = new ArrayList<>();
        currentRegions = new ArrayList<>();

        List<String> activeRegions = new ArrayList<>();
        try {
            fileReader = new BufferedReader(new FileReader(settingsFile));
            String line = fileReader.readLine();
            while (line != null) {
                String[] split = line.split(" ");
                if (split.length > 1) {
                    switch (split[0].toLowerCase()) {
                        case "regions":
                            String[] argument = split[1].split(",");
                            if (argument.length > 0) {
                                Collections.addAll(activeRegions, argument);
                            }
                            break;
                        case "password":
                            encryptedPassword = split[1];
                            break;
                        default:
                            logger.warn("Unknown setting " + split[0]);
                            break;
                    }
                } else {
                    if (!split[0].equals("")) logger.warn("No data entered for " + split[0]);
                }
                line = fileReader.readLine();
            }
        } catch (IOException e) {
            logger.error("Failed to read settings file: " + e.getMessage());
            Platform.runLater(() -> border.setCenter(labelCentre("Failed to read settings file: " + e.getMessage())));
        }
        Regions[] regionses = Regions.values();
        for (Regions re : regionses) {
            if (!Region.getRegion(re).getName().equals(Regions.GovCloud.getName()) & !Region.getRegion(re).getName().equals(Regions.CN_NORTH_1.getName())) {
                AmazonRegion tempRegion;
                if (activeRegions.contains(re.getName())) {
                    tempRegion = new AmazonRegion(Region.getRegion(re), true);
                    currentRegions.add(Region.getRegion(re));
                } else {
                    tempRegion = new AmazonRegion(Region.getRegion(re), false);
                }
                allRegions.add(tempRegion);
            }
        }
    }

    private void removeAccount(AmazonCredentials credential) {
        if (credential == null) return;
        logger.debug(credential.getAccountName());

        if (credentials.remove(credential)) {
            logger.info("Removed: " + credential.getAccountName());
        } else {
            logger.error("Failed to remove " + credential.getAccountName());
        }
        writeCredentials();
    }

    private Popup drawGraph(AmazonCredentials credentials, Service service, Scene mainScene) {
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

        List<String> statistics = new ArrayList<>();
        statistics.add("Maximum");
        statistics.add("Average");
        cpuUtilizationRequest.setStatistics(statistics);

        List<com.amazonaws.services.cloudwatch.model.Dimension> dimensions = new ArrayList<>();
        com.amazonaws.services.cloudwatch.model.Dimension instanceId = new com.amazonaws.services.cloudwatch.model.Dimension();
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
        close.setOnAction(event -> {
            popup.hide();
            resetDialog();
        });
        buttons.getChildren().add(close);
        buttons.setAlignment(Pos.BOTTOM_RIGHT);

        outerLayout.getChildren().addAll(lineChart, buttons);
        outerLayout.getStylesheets().add("style.css");
        outerLayout.getStyleClass().add("popup");

        popup.getContent().add(outerLayout);
        return popup;
    }

    private void drawSummary() {
        int statisticsBoxWidth = 320;
        int labelWidth = 70;
        double textWidth = 195;


        HBox outer = new HBox();
        outer.setPrefSize(mainStage.getMinWidth(), mainStage.getMinHeight());
        VBox summaryStats = new VBox();
        summaryStats.setMinWidth(statisticsBoxWidth);

        String styleClass = "statisticsTitle";

        List<Node> c = new ArrayList<>();

        int totalRunningServices = 0;
        int totalServices = 0;

        for (AmazonAccount account : accounts) {
            totalServices = totalServices + account.getTotalServices();
            totalRunningServices = totalRunningServices + account.getRunningServices();
        }

        Label summaryTitle = new Label("All Accounts");
        summaryTitle.getStyleClass().add("textTitle");
        c.add(summaryTitle);

        HBox allCurrentServicesBox = guiFactory.labelAndField("Current Services: ", "" + totalServices, textWidth, labelWidth, "statisticsTitle");
        c.add(allCurrentServicesBox);

        HBox allRunningServicesBox = guiFactory.labelAndField("Running Services: ", "" + totalRunningServices, textWidth, labelWidth, "statisticsTitle");
        c.add(allRunningServicesBox);


        int runningEc2 = 0;
        int runningRDS = 0;
        int runningRedshift = 0;
        Double runningCosts = 0.0;
        //All running types
        for (AmazonAccount account : accounts) {
            Map<String, Integer> thisRC = account.getRunningCount();
            if (thisRC.size() > 0) {
                runningEc2 = runningEc2 + thisRC.get("ec2");
                runningRDS = runningRDS + thisRC.get("rds");
                runningRedshift = runningRedshift + thisRC.get("redshift");
                for (Service service : account.getServices()) {
                    if (!service.serviceState().equalsIgnoreCase("stopped")) {
                        runningCosts += service.servicePrice();
                    }
                }
            }
        }

        HBox space = new HBox();
        space.setMinHeight(10);
        c.add(space);

        HBox runningEc2Box = guiFactory.labelAndField("Running Ec2: ", "" + runningEc2, textWidth, labelWidth, styleClass);
        c.add(runningEc2Box);

        HBox runningRDSBox = guiFactory.labelAndField("Running RDS: ", "" + runningRDS, textWidth, labelWidth, styleClass);
        c.add(runningRDSBox);

        HBox runningRedshiftBox = guiFactory.labelAndField("Running Redshift: ", "" + runningRedshift, textWidth, labelWidth, styleClass);
        c.add(runningRedshiftBox);

        if (pricings.size() != 0) {
            HBox runningCostsBox = guiFactory.labelAndField("Running Costs ($/hr): ", "$" + String.format("%.2f", round(runningCosts, 2)), textWidth, labelWidth, styleClass);
            c.add(runningCostsBox);
        }

        List<Node> b = new ArrayList<>();

        summaryStats.getChildren().addAll(c);
        summaryStats.getStylesheets().add(styleSheet);
        summaryStats.getStyleClass().add("centreStyle");
        b.add(summaryStats);

        for (AmazonAccount account : accounts) {
            int thisRunningEc2 = 0;
            int thisRunningRedshift = 0;
            int thisRunningRDS = 0;
            double thisRunningCosts = 0;

            Map<String, Integer> thisRC = account.getRunningCount();
            if (thisRC.size() > 0) {
                thisRunningEc2 = thisRunningEc2 + thisRC.get("ec2");
                thisRunningRDS = thisRunningRDS + thisRC.get("rds");
                thisRunningRedshift = thisRunningRedshift + thisRC.get("redshift");

                for (Service service : account.getServices()) {
                    if (Service.runningTitles().contains(service.serviceState())) {
                        thisRunningCosts = thisRunningCosts + service.servicePrice();
                    }
                }
            }

            Label accountTitle = new Label(account.getCredentials().getAccountName());
            accountTitle.getStyleClass().add("textTitle");

            HBox allServicesBox = guiFactory.labelAndField("Current Services: ", "" + account.getTotalServices(), textWidth, labelWidth, styleClass);
            HBox allRunningBox = guiFactory.labelAndField("Running Services: ", "" + account.getRunningServices(), textWidth, labelWidth, styleClass);

            HBox space2 = new HBox();
            space2.setMinHeight(10);
            c.add(space2);

            HBox accountEc2Box = guiFactory.labelAndField("Running Ec2: ", "" + thisRunningEc2, textWidth, labelWidth, styleClass);

            HBox accountRDSBox = guiFactory.labelAndField("Running RDS: ", "" + thisRunningRDS, textWidth, labelWidth, styleClass);

            HBox accountRedshiftBox = guiFactory.labelAndField("Running Redshift: ", "" + thisRunningRedshift, textWidth, labelWidth, styleClass);

            VBox thisAccountStats = new VBox(accountTitle, allServicesBox, allRunningBox, space2, accountEc2Box, accountRDSBox, accountRedshiftBox);
            if (pricings.size() != 0) {
                HBox accountCostsBox = guiFactory.labelAndField("Current Costs ($/hr): ", "$" + String.format("%.2f", round(thisRunningCosts, 2)), textWidth, labelWidth, styleClass);
                thisAccountStats.getChildren().add(accountCostsBox);
            }

            thisAccountStats.setMinWidth(statisticsBoxWidth);
            thisAccountStats.getStylesheets().add(styleSheet);
            thisAccountStats.getStyleClass().add("centreStyle");

            b.add(thisAccountStats);
        }

        outer.getChildren().addAll(b);
        outer.getStylesheets().add(styleSheet);
        outer.setFocusTraversable(false);
        outer.getStyleClass().add("centreStyle");

        ScrollPane pane = new ScrollPane(outer);
        pane.getStylesheets().add(styleSheet);
        pane.setOnMouseClicked(event -> {
            if (dialog != null) {
                dialog.show(mainStage);
            }
        });
        Platform.runLater(() -> border.setCenter(pane));
    }
}
