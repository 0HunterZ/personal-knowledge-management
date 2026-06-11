package com.focusnode.controller;

import com.focusnode.model.CategoryBreakdown;
import com.focusnode.model.FocusPoint;
import com.focusnode.service.ServiceLocator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;

import java.util.stream.Collectors;

public class AnalyticsViewController {
    @FXML
    private LineChart<String, Number> focusLineChart;

    @FXML
    private PieChart categoryPieChart;

    @FXML
    public void initialize() {
        populateLineChart();
        populatePieChart();
    }

    private void populateLineChart() {
        if (focusLineChart != null) {
            com.focusnode.util.AsyncExecutor.execute(() -> {
                var weeklyFocus = ServiceLocator.getAppDataService().getWeeklyFocus();
                javafx.application.Platform.runLater(() -> {
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName("Focus");

                    weeklyFocus.stream()
                            .map(this::toDataPoint)
                            .forEach(series.getData()::add);

                    focusLineChart.getData().add(series);
                });
            });
        }
    }

    private XYChart.Data<String, Number> toDataPoint(FocusPoint point) {
        XYChart.Data<String, Number> data = new XYChart.Data<>(point.getLabel(), point.getValue());
        data.XValueProperty().bind(point.labelProperty());
        data.YValueProperty().bind(point.valueProperty());
        return data;
    }

    private void populatePieChart() {
        if (categoryPieChart != null) {
            com.focusnode.util.AsyncExecutor.execute(() -> {
                var breakdown = ServiceLocator.getAppDataService().getCategoryBreakdown();
                javafx.application.Platform.runLater(() -> {
                    categoryPieChart.setData(FXCollections.observableArrayList(
                            breakdown.stream()
                                    .map(this::toPieData)
                                    .collect(Collectors.toList())
                    ));
                });
            });
        }
    }

    private PieChart.Data toPieData(CategoryBreakdown breakdown) {
        PieChart.Data data = new PieChart.Data(breakdown.getCategory(), breakdown.getValue());
        data.pieValueProperty().bind(breakdown.valueProperty());
        data.nameProperty().bind(breakdown.categoryProperty());
        return data;
    }
}
