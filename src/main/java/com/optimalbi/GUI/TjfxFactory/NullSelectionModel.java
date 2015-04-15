package com.optimalbi.GUI.TjfxFactory;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;

public class NullSelectionModel extends TableView.TableViewSelectionModel {

    public NullSelectionModel(TableView tableView) {
        super(tableView);
    }

    @Override
    public ObservableList<TablePosition> getSelectedCells() {
        return FXCollections.emptyObservableList();
    }

    @Override
    public boolean isSelected(int row, TableColumn column) {
        return false;
    }

    @Override
    public void selectLeftCell() {

    }

    @Override
    public void selectRightCell() {

    }

    @Override
    public void selectAboveCell() {

    }

    @Override
    public void selectBelowCell() {

    }

    @Override
    public void clearSelection(int i, TableColumn tableColumn) {

    }

    @Override
    public void clearAndSelect(int i, TableColumn tableColumn) {

    }

    @Override
    public void select(int i, TableColumn tableColumn) {

    }

    @Override
    public ObservableList<Integer> getSelectedIndices() {
        return FXCollections.emptyObservableList();
    }

    @Override
    public ObservableList getSelectedItems() {
        return FXCollections.emptyObservableList();
    }

    @Override
    public void selectIndices(int i, int... ints) {

    }

    @Override
    public void selectAll() {

    }

    @Override
    public void clearAndSelect(int i) {

    }

    @Override
    public void select(int i) {

    }

    @Override
    public void select(Object o) {

    }

    @Override
    public void clearSelection(int i) {

    }

    @Override
    public void clearSelection() {

    }

    @Override
    public boolean isSelected(int i) {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void selectPrevious() {

    }

    @Override
    public void selectNext() {

    }

    @Override
    public void selectFirst() {

    }

    @Override
    public void selectLast() {

    }
}
