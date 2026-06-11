package com.focusnode.navigation;

public enum AppView {
    HOME("/fxml/views/HomeView.fxml"),
    KNOWLEDGE("/fxml/views/KnowledgeView.fxml"),
    LAN_HUB("/fxml/views/LanHubView.fxml"),
    TASKS("/fxml/views/TasksView.fxml"),
    ZEN_MODE("/fxml/views/ZenMode.fxml"),
    TAGS("/fxml/views/TagsView.fxml"),
    ANALYTICS("/fxml/views/AnalyticsView.fxml"),
    REVIEW("/fxml/views/ReviewView.fxml");

    private final String fxmlPath;

    AppView(String fxmlPath) {
        this.fxmlPath = fxmlPath;
    }

    public String getFxmlPath() {
        return fxmlPath;
    }
}
