package com.optimalbi.GUI.TjfxFactory;

import javafx.scene.Node;

/**
 * Created by Timothy Gray on 3/31/2015.
 */
public interface GuiButton {

    public String name();
    public Runnable command();
    public Node display();
}
