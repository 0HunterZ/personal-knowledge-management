package com.focusnode.controller;

import com.focusnode.model.TagUsageItem;
import com.focusnode.service.ServiceLocator;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;

public class TagUsageController {

    @FXML
    private BarChart<String, Number> tagUsageChart;

    @FXML
    public void initialize() {
        if (tagUsageChart != null) {
            com.focusnode.util.AsyncExecutor.execute(() -> {
                var items = ServiceLocator.getAppDataService().getTagUsage();
                javafx.application.Platform.runLater(() -> {
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName("Tag Usage");

                    for (TagUsageItem item : items) {
                        series.getData().add(new XYChart.Data<>(item.getName(), item.getValue()));
                    }

                    tagUsageChart.getData().add(series);
                });
            });
        }
    }
}
