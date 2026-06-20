package com.focusnode.controller;

import com.focusnode.model.TagUsageItem;
import com.focusnode.service.ServiceLocator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import java.util.List;

public class TagsCloudController {

    @FXML private FlowPane tagsContainer;

    @FXML
    public void initialize() {
        refreshTags();
    }

    public void refreshTags() {
        ServiceLocator.getAsyncExecutor().execute(() -> {
            List<TagUsageItem> tags = ServiceLocator.getAppDataService().getTagUsage();

            Platform.runLater(() -> {
                tagsContainer.getChildren().clear();
                
                if (tags.isEmpty()) {
                    Label emptyLabel = new Label("No tags available yet.");
                    emptyLabel.setStyle("-fx-text-fill: #94A3B8;");
                    tagsContainer.getChildren().add(emptyLabel);
                    return;
                }

                for (TagUsageItem tag : tags) {
                    HBox badge = new HBox(5);
                    badge.setAlignment(Pos.CENTER);
                    badge.getStyleClass().add("tag-cloud-badge");
                    
                    // Simple light background trick: append "20" (hex for ~12% opacity) to color for background
                    String bgColor = tag.getColorHex() + "20";
                    badge.setStyle("-fx-background-color: " + bgColor + ";");

                    Label nameLabel = new Label("#" + tag.getName());
                    nameLabel.setStyle("-fx-text-fill: " + tag.getColorHex() + ";");

                    Label countLabel = new Label(String.valueOf(tag.getValue()));
                    countLabel.getStyleClass().add("tag-cloud-count");

                    badge.getChildren().addAll(nameLabel, countLabel);
                    tagsContainer.getChildren().add(badge);
                }
            });
        });
    }
}
