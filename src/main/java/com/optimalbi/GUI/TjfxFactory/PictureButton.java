package com.optimalbi.GUI.TjfxFactory;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


/**
 * Container for a button with attached method as on click
 * @author Timothy Gray
 */
public class PictureButton extends MainButton {
    private final Runnable command;
    private final ImageView image;

    public PictureButton(String name, Runnable command, Image picture) {
        super(name);
        this.command = command;
        if (picture == null) {
            this.image = null;
        } else {
            this.image = new ImageView(picture);
        }
    }

    public Runnable command() {
        return command;
    }

    public Node display() {
        return image;
    }

    public Node display(int width, int height){
        Image temp = image.getImage();
        ImageView imageView = new ImageView(temp);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(true);
        return imageView;
    }
}
