package com.optimalbi.GUI.TjfxFactory;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;

/**
 * Created by Timothy Gray on 16/04/2015.
 */
public class NullFocusModel extends TableView.TableViewFocusModel {
    /**
     * Creates a default TableViewFocusModel instance that will be used to
     * manage focus of the provided TableView control.
     *
     * @param tableView The tableView upon which this focus model operates.
     * @throws NullPointerException The TableView argument can not be null.
     */
    public NullFocusModel(TableView tableView) {
        super(tableView);
    }

    @Override
    public void focus(int index){

    }

    @Override
    public void focus(int row, TableColumn column){

    }

    @Override
    public void focusAboveCell(){

    }

    @Override
    public void focusBelowCell(){

    }

    @Override
    public void focusLeftCell(){

    }

    @Override
    public void focusRightCell(){

    }

    @Override
    public boolean isFocused(int row, TableColumn column){
        return false;
    }
}
