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

package com.optimalbi.GUI.TjfxFactory;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Popup;

/**
 * Created by Timothy Gray(timg) on 18/11/2014.
 * Version: 0.0.1
 */
public class TjfxFactory {
    private final String styleSheet;
    private double buttonWidth;
    private double buttonHeight;
    private String googleSheet = "";

    public TjfxFactory(double buttonWidth, double buttonHeight, String styleSheet) {
        this.buttonHeight = buttonHeight;
        this.buttonWidth = buttonWidth;
        this.styleSheet = styleSheet;
    }

    public TjfxFactory(double buttonWidth, double buttonHeight, String styleSheet, String googleSheet) {
        this.buttonHeight = buttonHeight;
        this.buttonWidth = buttonWidth;
        this.styleSheet = styleSheet;
        this.googleSheet = googleSheet;
    }

    Button createButton(String title, String styleClass) {
        Button button = new Button(title);
        if (buttonWidth != -1) {
            button.setPrefSize(buttonWidth, buttonHeight);
        }
        button.getStylesheets().add(styleSheet);
        if (!googleSheet.equals("")) button.getStylesheets().add(googleSheet);
        if (styleClass != null) {
            button.getStyleClass().add(styleClass);
        }
        button.setFocusTraversable(false);
        return button;
    }

    public Button createButton(String title) {
        return createButton(title, null);
    }

    public Button createButton(String title, double width, double height) {
        return createButton(title, null, width, height);
    }

    public Button createButton(String title, String styleClass, double width, double height) {
        double saveButtonWidth = buttonWidth;
        double saveButtonHeight = buttonHeight;
        this.buttonWidth = width;
        this.buttonHeight = height;
        Button button = createButton(title, styleClass);
        this.buttonWidth = saveButtonWidth;
        this.buttonHeight = saveButtonHeight;
        return button;
    }

    public Popup setupDialog(double width, double height, Node content) {
        Popup popup = new Popup();
        if (height != -1) {
            popup.setHeight(height);
        }
        if (width != -1) {
            popup.setWidth(width);
        }

        popup.getContent().add(content);

        return popup;
    }

    public HBox labelAndField(String label, String field, double textWidth, double fieldWidth, String styleClassLabel) {
        Label labelNode = new Label(label);
        labelNode.getStyleClass().add(styleClassLabel);
        labelNode.setPrefWidth(textWidth);
        labelNode.setAlignment(Pos.CENTER_RIGHT);
        TextField textField = new TextField(field);
        textField.setAlignment(Pos.CENTER);
        textField.setPrefWidth(fieldWidth);
        textField.setEditable(false);
        textField.setFocusTraversable(false);

        HBox box = new HBox(labelNode, textField);
        box.setPrefWidth(textWidth);
        box.setAlignment(Pos.CENTER_LEFT);

        return box;
    }

    public HBox textAndLabel(String label, String field, double textWidth, double fieldWidth, String styleClassLabel) {
        Text textNode = new Text(label);
        textNode.getStyleClass().add(styleClassLabel);
        textNode.setTextAlignment(TextAlignment.RIGHT);
        textNode.setWrappingWidth(textWidth);
        TextField textField = new TextField(field);
        textField.setAlignment(Pos.CENTER);
        textField.setPrefWidth(fieldWidth);
        textField.getStyleClass().add(styleClassLabel);
        textField.setEditable(false);
        textField.setFocusTraversable(false);

        HBox box = new HBox(textNode, textField);
        box.setPrefWidth(textWidth);
        box.setAlignment(Pos.CENTER_LEFT);

        return box;
    }

    public HBox labelAndLabel(String label, String label2, double textWidth, double fieldWidth, String styleClassLabel) {
        Label labelNode = new Label(label);
        labelNode.getStyleClass().add(styleClassLabel);
        labelNode.setPrefWidth(textWidth);
        labelNode.setAlignment(Pos.CENTER_RIGHT);
        Label label2Node = new Label(label2);
        label2Node.setAlignment(Pos.CENTER);
        label2Node.setPrefWidth(fieldWidth);

        HBox box = new HBox(labelNode, label2Node);
        box.setPrefWidth(textWidth);
        box.setAlignment(Pos.CENTER_LEFT);

        return box;
    }

    public ImageView imageView(String imageLocation) {
        Image image = new Image(imageLocation);

        ImageView iv1 = new ImageView();
        iv1.setImage(image);
        iv1.setSmooth(true);
        iv1.setPreserveRatio(true);
        iv1.setFitHeight(image.getHeight());
        return iv1;
    }
}
