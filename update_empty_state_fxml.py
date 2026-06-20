import os

def write_file(path, content):
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content.strip() + '\n')

def read_file(path):
    with open(path, 'r', encoding='utf-8') as f:
        return f.read()

# 1. Update LanActiveRoomCard.fxml
lan_active_room = """
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.scene.control.Label?>
<?import javafx.scene.layout.VBox?>
<?import javafx.scene.layout.HBox?>
<?import javafx.scene.layout.Region?>
<?import javafx.scene.shape.Circle?>
<?import javafx.scene.image.ImageView?>
<?import javafx.scene.image.Image?>
<?import javafx.scene.layout.StackPane?>

<StackPane xmlns="http://javafx.com/javafx/17" xmlns:fx="http://javafx.com/fxml/1" fx:controller="com.focusnode.controller.LanActiveRoomController">
    <!-- EMPTY STATE -->
    <VBox fx:id="emptyStateContainer" spacing="20" styleClass="card" alignment="CENTER" style="-fx-padding: 40 20;">
        <ImageView fitWidth="200" preserveRatio="true">
            <Image url="@../images/lan_empty_state.png" />
        </ImageView>
        <Label style="-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #1F2937;" text="Not in a Focus Room" />
        <Label style="-fx-font-size: 13px; -fx-text-fill: #6B7280; -fx-text-alignment: center;" wrapText="true" text="Create or join a room on your local network to start studying together." />
    </VBox>

    <!-- ACTIVE STATE -->
    <VBox fx:id="activeStateContainer" spacing="20" styleClass="card" visible="false">
        <!-- 1. Header (Active Room Info) -->
        <VBox spacing="15">
            <Label style="-fx-text-fill: #6B7280; -fx-font-size: 12px; -fx-font-weight: bold;" text="Active Room" />
            <HBox alignment="CENTER_LEFT" spacing="15">
                <Label styleClass="member-avatar, avatar-purple" text="U" style="-fx-font-size: 16px;" />
                <VBox spacing="2">
                    <HBox spacing="10" alignment="CENTER_LEFT">
                        <Label fx:id="roomTitleLabel" style="-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1F2937;" text="Da Nang Study Lab" />
                        <Label styleClass="lan-room-host-badge" text="You (Server Host)" />
                    </HBox>
                    <Label fx:id="roomIpLabel" style="-fx-font-size: 11px; -fx-text-fill: #6B7280;" text="IP: 192.168.1.200  •  Port: 5051" />
                </VBox>
                <Region HBox.hgrow="ALWAYS" />
                <Label styleClass="lan-room-leave-btn" text="⍈ Leave Room" />
            </HBox>
            
            <HBox spacing="10">
                <HBox alignment="CENTER" HBox.hgrow="ALWAYS" styleClass="lan-stat-box" spacing="5">
                    <Label text="👥" style="-fx-font-size: 14px;" />
                    <VBox alignment="CENTER_LEFT">
                        <Label fx:id="memberCountLabel" style="-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1F2937;" text="6" />
                        <Label style="-fx-font-size: 10px; -fx-text-fill: #6B7280;" text="Members" />
                    </VBox>
                </HBox>
                <HBox alignment="CENTER" HBox.hgrow="ALWAYS" styleClass="lan-stat-box" spacing="5">
                    <Label text="⏱" style="-fx-font-size: 14px; -fx-text-fill: #3B82F6;" />
                    <VBox alignment="CENTER_LEFT">
                        <Label style="-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1F2937;" text="45:00" />
                        <Label style="-fx-font-size: 10px; -fx-text-fill: #6B7280;" text="Synced Time" />
                    </VBox>
                </HBox>
                <HBox alignment="CENTER" HBox.hgrow="ALWAYS" styleClass="lan-stat-box" spacing="5">
                    <Label text="📈" style="-fx-font-size: 14px; -fx-text-fill: #10B981;" />
                    <VBox alignment="CENTER_LEFT">
                        <Label style="-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1F2937;" text="Focus" />
                        <Label style="-fx-font-size: 10px; -fx-text-fill: #6B7280;" text="Mode" />
                    </VBox>
                </HBox>
                <HBox alignment="CENTER" HBox.hgrow="ALWAYS" styleClass="lan-stat-box" spacing="5">
                    <Label text="✅" style="-fx-font-size: 14px;" />
                    <VBox alignment="CENTER_LEFT">
                        <Label style="-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1F2937;" text="Excellent" />
                        <Label style="-fx-font-size: 10px; -fx-text-fill: #6B7280;" text="Connection" />
                    </VBox>
                </HBox>
            </HBox>
        </VBox>
        
        <!-- 2. Members List -->
        <VBox spacing="10">
            <Label style="-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1F2937;" text="Members" />
            <VBox fx:id="membersContainer" spacing="5" />
        </VBox>
        
        <!-- 3. Room Activity -->
        <VBox spacing="10" style="-fx-padding: 10 0 0 0; -fx-border-color: #F1F5F9; -fx-border-width: 1 0 0 0;">
            <Label style="-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1F2937;" text="Room Activity" />
            <VBox spacing="8" fx:id="activityContainer" />
        </VBox>
    </VBox>
</StackPane>
"""
write_file(r'C:\Users\HunterZ\Desktop\FINAL EXAM JAVA\src\main\resources\fxml\components\LanActiveRoomCard.fxml', lan_active_room)

# 2. Update LanDiscoverRoomsCard.fxml
lan_discover_rooms = """
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.scene.control.Label?>
<?import javafx.scene.layout.VBox?>
<?import javafx.scene.layout.HBox?>
<?import javafx.scene.layout.Region?>
<?import javafx.scene.layout.StackPane?>

<VBox spacing="15" styleClass="card" xmlns="http://javafx.com/javafx/17" xmlns:fx="http://javafx.com/fxml/1" fx:controller="com.focusnode.controller.LanDiscoverRoomsController">
    <HBox alignment="CENTER_LEFT">
        <Label styleClass="card-title" text="Discover Rooms" />
        <Region HBox.hgrow="ALWAYS" />
        <Label style="-fx-text-fill: #6B7280; -fx-cursor: hand;" text="🔄" />
    </HBox>
    
    <StackPane>
        <!-- EMPTY STATE -->
        <VBox fx:id="emptyStateContainer" spacing="10" alignment="CENTER" style="-fx-padding: 20 0;">
            <Label style="-fx-font-size: 24px; -fx-text-fill: #94A3B8;" text="🔍" />
            <Label style="-fx-font-size: 13px; -fx-text-fill: #6B7280;" text="No rooms found on LAN" />
        </VBox>

        <!-- ACTIVE STATE -->
        <VBox fx:id="discoverRoomsContainer" spacing="10" visible="false" />
    </StackPane>

    <Label style="-fx-text-fill: #3B82F6; -fx-font-size: 12px; -fx-padding: 10 0 0 0;" text="📶 Broadcasting for rooms..." />
</VBox>
"""
write_file(r'C:\Users\HunterZ\Desktop\FINAL EXAM JAVA\src\main\resources\fxml\components\LanDiscoverRoomsCard.fxml', lan_discover_rooms)

# 3. Update LanFileTransfersCard.fxml
lan_file_transfers = """
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.scene.control.Label?>
<?import javafx.scene.layout.VBox?>
<?import javafx.scene.layout.HBox?>
<?import javafx.scene.layout.Region?>
<?import javafx.scene.layout.StackPane?>

<VBox spacing="15" styleClass="card" xmlns="http://javafx.com/javafx/17" xmlns:fx="http://javafx.com/fxml/1" fx:controller="com.focusnode.controller.LanFileTransfersController">
    <Label styleClass="card-title" text="File Transfers" />
    
    <StackPane>
        <!-- EMPTY STATE -->
        <VBox fx:id="emptyStateContainer" spacing="10" alignment="CENTER" style="-fx-padding: 10 0;">
            <Label style="-fx-font-size: 24px; -fx-text-fill: #94A3B8;" text="📁" />
            <Label style="-fx-font-size: 13px; -fx-text-fill: #6B7280;" text="No active transfers" />
        </VBox>

        <!-- ACTIVE STATE -->
        <VBox fx:id="transfersContainer" spacing="15" visible="false" />
    </StackPane>

    <HBox alignment="CENTER" fx:id="viewHistoryContainer" visible="false">
        <Label style="-fx-text-fill: #3B82F6; -fx-font-weight: bold; -fx-font-size: 12px; -fx-cursor: hand;" text="View Transfer History →" />
    </HBox>
</VBox>
"""
write_file(r'C:\Users\HunterZ\Desktop\FINAL EXAM JAVA\src\main\resources\fxml\components\LanFileTransfersCard.fxml', lan_file_transfers)

print("Empty state FXMLs successfully rewritten.")
