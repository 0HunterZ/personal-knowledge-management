import re

with open("src/main/resources/fxml/views/TasksView.fxml", "r", encoding="utf-8") as f:
    content = f.read()

# 1. Wrap with StackPane and set controller
content = content.replace(
    '<ScrollPane fitToWidth="true" styleClass="tasks-scroll-pane" xmlns="http://javafx.com/javafx/17" xmlns:fx="http://javafx.com/fxml/1">',
    '<StackPane xmlns="http://javafx.com/javafx/17" xmlns:fx="http://javafx.com/fxml/1" fx:controller="com.focusnode.controller.TasksViewController">\n    <ScrollPane fx:id="mainScrollPane" fitToWidth="true" styleClass="tasks-scroll-pane">'
)

content = content.replace('</ScrollPane>', '</ScrollPane>\n    <StackPane fx:id="editorContainer" visible="false" />\n</StackPane>')

# 2. Fix container padding
content = content.replace(
    '<HBox spacing="30" styleClass="tasks-main-container">',
    '<HBox spacing="30" styleClass="tasks-main-container" style="-fx-padding: 12px 18px; -fx-background-color: #F8FAFC;">'
)

# 3. Fix MenuButton
content = content.replace(
    '<Label styleClass="btn-new-note" text="+ New Task ⌄" />',
    '<MenuButton style="-fx-background-color: #10B981; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;" text="+ New Task">\n                        <items>\n                            <MenuItem text="Add Task" onAction="#createNewTask" />\n                            <MenuItem text="Add Project" />\n                        </items>\n                    </MenuButton>'
)

# 4. Fix Toggle alignment and add handlers
content = content.replace(
    '<HBox spacing="5">\n                        <Label styleClass="toggle-btn" text="≡" style="-fx-font-size: 18px;" />\n                        <Label styleClass="toggle-btn" text="📅" style="-fx-font-size: 16px;" />\n                    </HBox>',
    '<HBox spacing="5" alignment="CENTER_LEFT">\n                        <Label styleClass="toggle-btn" text="≡" style="-fx-font-size: 18px;" onMouseClicked="#handleViewToggle" />\n                        <Label styleClass="toggle-btn" text="📅" style="-fx-font-size: 16px;" onMouseClicked="#handleViewToggle" />\n                    </HBox>'
)

# 5. Inline Completed Stat Card and add fx:ids to all stat cards
content = content.replace(
    '<fx:include source="../components/TaskStatCard.fxml" />',
    '''<!-- Completed Stat -->
                <HBox spacing="15" alignment="CENTER_LEFT" styleClass="task-stat-card">
                    <VBox styleClass="stat-icon-wrapper" style="-fx-background-color: #DCFCE7;">
                        <Label style="-fx-font-size: 18px; -fx-text-fill: #22C55E;" text="✓" />
                    </VBox>
                    <VBox spacing="2">
                        <Label fx:id="completedCountLabel" style="-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1F2937;" text="0" />
                        <Label style="-fx-font-size: 11px; -fx-text-fill: #6B7280;" text="Completed" />
                        <Label style="-fx-font-size: 10px; -fx-text-fill: #94A3B8;" text="Today" />
                    </VBox>
                </HBox>'''
)

content = content.replace(
    '<Label style="-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1F2937;" text="8" />',
    '<Label fx:id="inProgressCountLabel" style="-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1F2937;" text="0" />'
)
content = content.replace(
    '<Label style="-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1F2937;" text="3" />',
    '<Label fx:id="pendingCountLabel" style="-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1F2937;" text="0" />'
)
content = content.replace(
    '<Label style="-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1F2937;" text="18" />',
    '<Label fx:id="totalCountLabel" style="-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1F2937;" text="0" />'
)

# 6. Add fx:id to searchField
content = content.replace(
    '<TextField prefWidth="250" promptText="🔍 Search tasks..." styleClass="search-bar" />',
    '<TextField fx:id="searchField" prefWidth="250" promptText="🔍 Search tasks..." styleClass="search-bar" />'
)

# 7. Add quick-filter-btn handlers
content = content.replace(
    '<Label styleClass="quick-filter-btn, quick-filter-btn-active" text="All" />',
    '<Label styleClass="quick-filter-btn, quick-filter-btn-active" text="All" onMouseClicked="#handleQuickFilter" />'
)
content = content.replace(
    '<Label styleClass="quick-filter-btn" text="Today" />',
    '<Label styleClass="quick-filter-btn" text="Today" onMouseClicked="#handleQuickFilter" />'
)
content = content.replace(
    '<Label styleClass="quick-filter-btn" text="Upcoming" />',
    '<Label styleClass="quick-filter-btn" text="Upcoming" onMouseClicked="#handleQuickFilter" />'
)
content = content.replace(
    '<Label styleClass="quick-filter-btn" text="Completed" />',
    '<Label styleClass="quick-filter-btn" text="Completed" onMouseClicked="#handleQuickFilter" />'
)

# 8. Add fx:id to taskListContainer and remove mock rows
# Find tasks-list-container
idx = content.find('<VBox styleClass="tasks-list-container">')
if idx != -1:
    content = content[:idx] + '<VBox fx:id="taskListContainer" styleClass="tasks-list-container">' + content[idx+len('<VBox styleClass="tasks-list-container">'):]

# Remove everything between <!-- Task Rows --> and <!-- Footer / Pagination -->
start_idx = content.find('<!-- Task Rows -->')
end_idx = content.find('<!-- Footer / Pagination -->')
if start_idx != -1 and end_idx != -1:
    content = content[:start_idx] + '<!-- Tasks will be loaded here dynamically -->\n                \n                ' + content[end_idx:]

# Add imports for MenuButton, MenuItem, StackPane
imports = "<?import javafx.scene.control.MenuButton?>\n<?import javafx.scene.control.MenuItem?>\n<?import javafx.scene.layout.StackPane?>\n"
content = content.replace("<?import javafx.scene.image.Image?>\n", "<?import javafx.scene.image.Image?>\n" + imports)

with open("src/main/resources/fxml/views/TasksView.fxml", "w", encoding="utf-8") as f:
    f.write(content)
