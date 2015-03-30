package com.optimalbi.GUI.TjfxFactory;

/**
 * Created by Timothy Gray on 3/31/2015.
 */
abstract class MainButton implements GuiButton {
    private final String name;

    public MainButton(String name){
        if(name == null){
            this.name = "";
        } else {
            this.name = name;
        }
    }

    public String name(){
        return name;
    }
}
