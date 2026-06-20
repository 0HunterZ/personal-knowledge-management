import os

def write_file(path, content):
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content.strip() + '\n')

def read_file(path):
    with open(path, 'r', encoding='utf-8') as f:
        return f.read()

# 1. Update LanHubView.fxml
lan_hub_view = """
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.scene.control.Label?>
<?import javafx.scene.control.ScrollPane?>
<?import javafx.scene.layout.HBox?>
<?import javafx.scene.layout.Region?>
<?import javafx.scene.layout.VBox?>
<?import javafx.scene.shape.Circle?>

<ScrollPane fitToWidth="true" style="-fx-background-color: #F8FAFC;" xmlns="http://javafx.com/javafx/17" xmlns:fx="http://javafx.com/fxml/1">
    
    <HBox spacing="30" style="-fx-padding: 30; -fx-background-color: #F8FAFC;">
        
        <!-- Phần Trung tâm (Lan Hub Main Area) -->
        <VBox spacing="20" HBox.hgrow="ALWAYS">
            
            <!-- Header -->
            <HBox alignment="CENTER_LEFT">
                <VBox spacing="5">
                    <HBox alignment="CENTER_LEFT" spacing="10">
                        <Label style="-fx-font-weight: bold; -fx-font-size: 28px; -fx-text-fill: #1F2937;" text="LAN Focus Hub" />
                        <Label style="-fx-font-size: 24px;" text="🖧" />
                    </HBox>
                    <Label style="-fx-font-size: 14px; -fx-text-fill: #6B7280;" text="Study together locally. Stay focused together." />
                </VBox>
                
                <Region HBox.hgrow="ALWAYS" />
                
                <!-- Status Badge -->
                <HBox spacing="10" alignment="CENTER_LEFT">
                    <HBox alignment="CENTER" spacing="8" styleClass="lan-status-badge">
                        <Circle radius="4" style="-fx-fill: #10B981;" />
                        <Label text="Connected to LAN" style="-fx-font-weight: bold; -fx-text-fill: #1F2937;" />
                    </HBox>
                    <Label styleClass="btn-outline" text="🔄" style="-fx-padding: 6 10; -fx-border-radius: 8;" />
                </HBox>
            </HBox>
            
            <!-- 3 Columns for LAN Features -->
            <HBox spacing="20">
                <!-- Column 1: Actions & Discover -->
                <VBox spacing="20" minWidth="220" prefWidth="250">
                    <fx:include source="../components/LanRoomActionsCard.fxml" />
                    <fx:include source="../components/LanDiscoverRoomsCard.fxml" />
                </VBox>
                
                <!-- Column 2: Active Room -->
                <VBox spacing="20" HBox.hgrow="ALWAYS" minWidth="380">
                    <fx:include source="../components/LanActiveRoomCard.fxml" />
                </VBox>
                
                <!-- Column 3: File Transfers -->
                <VBox spacing="20" minWidth="250" prefWidth="280">
                    <fx:include source="../components/LanAirdropCard.fxml" />
                    <fx:include source="../components/LanFileTransfersCard.fxml" />
                </VBox>
            </HBox>
        </VBox>
        
        <!-- Cột Phải (Widgets thông thường) -->
        <VBox minWidth="280" prefWidth="280" spacing="20">
            <fx:include source="../components/QuoteCard.fxml" />
            <fx:include source="../components/QuickNoteCard.fxml" />
            <fx:include source="../components/DailyStreakCard.fxml" />
        </VBox>
        
    </HBox>
</ScrollPane>
"""
write_file(r'C:\Users\HunterZ\Desktop\FINAL EXAM JAVA\src\main\resources\fxml\views\LanHubView.fxml', lan_hub_view)

# 2. Update LanRoomActionsCard.fxml
lan_room_actions = """
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.scene.control.Label?>
<?import javafx.scene.layout.VBox?>
<?import javafx.scene.layout.HBox?>

<VBox spacing="15" styleClass="card" xmlns="http://javafx.com/javafx/17" xmlns:fx="http://javafx.com/fxml/1" fx:controller="com.focusnode.controller.LanRoomActionsController">
    <Label styleClass="card-title" text="Room Actions" />
    
    <VBox spacing="10">
        <!-- Create Room -->
        <HBox fx:id="createRoomBtn" styleClass="room-action-btn, room-create-btn">
            <Label style="-fx-font-size: 20px; -fx-text-fill: #10B981;" text="+" />
            <VBox>
                <Label style="-fx-font-weight: bold; -fx-text-fill: #059669;" text="Create Room" />
                <Label style="-fx-font-size: 11px; -fx-text-fill: #10B981;" text="Host a new focus room" />
            </VBox>
        </HBox>

        <!-- Join Room -->
        <HBox fx:id="joinRoomBtn" styleClass="room-action-btn, room-join-btn">
            <Label style="-fx-font-size: 18px; -fx-text-fill: #3B82F6;" text="👤+" />
            <VBox>
                <Label style="-fx-font-weight: bold; -fx-text-fill: #2563EB;" text="Join Room" />
                <Label style="-fx-font-size: 11px; -fx-text-fill: #3B82F6;" text="Join with room IP" />
            </VBox>
        </HBox>
    </VBox>
</VBox>
"""
write_file(r'C:\Users\HunterZ\Desktop\FINAL EXAM JAVA\src\main\resources\fxml\components\LanRoomActionsCard.fxml', lan_room_actions)

# 3. Update LanActiveRoomCard.fxml
lan_active_room = """
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.scene.control.Label?>
<?import javafx.scene.layout.VBox?>
<?import javafx.scene.layout.HBox?>
<?import javafx.scene.layout.Region?>
<?import javafx.scene.shape.Circle?>

<VBox spacing="20" styleClass="card" xmlns="http://javafx.com/javafx/17" xmlns:fx="http://javafx.com/fxml/1" fx:controller="com.focusnode.controller.LanActiveRoomController">
    
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
"""
write_file(r'C:\Users\HunterZ\Desktop\FINAL EXAM JAVA\src\main\resources\fxml\components\LanActiveRoomCard.fxml', lan_active_room)

# 4. Update LanAirdropCard.fxml
lan_airdrop = """
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.scene.control.Label?>
<?import javafx.scene.layout.VBox?>

<VBox spacing="15" styleClass="card" xmlns="http://javafx.com/javafx/17" xmlns:fx="http://javafx.com/fxml/1">
    <Label styleClass="card-title" text="LAN Airdrop" />
    
    <VBox styleClass="airdrop-zone" spacing="15">
        <Label style="-fx-font-size: 32px; -fx-text-fill: #94A3B8;" text="☁️" />
        <Label style="-fx-font-weight: bold; -fx-text-fill: #1F2937;" text="Drag &amp; drop files here" />
        <Label style="-fx-text-fill: #6B7280; -fx-font-size: 12px;" text="or" />
        <Label style="-fx-background-color: #ECFDF5; -fx-text-fill: #10B981; -fx-padding: 8 20; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;" text="Select Files" />
    </VBox>
</VBox>
"""
write_file(r'C:\Users\HunterZ\Desktop\FINAL EXAM JAVA\src\main\resources\fxml\components\LanAirdropCard.fxml', lan_airdrop)

# 5. Update LanFileTransfersCard.fxml
lan_file_transfers = """
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.scene.control.Label?>
<?import javafx.scene.layout.VBox?>
<?import javafx.scene.layout.HBox?>
<?import javafx.scene.layout.Region?>

<VBox spacing="15" styleClass="card" xmlns="http://javafx.com/javafx/17" xmlns:fx="http://javafx.com/fxml/1" fx:controller="com.focusnode.controller.LanFileTransfersController">
    <Label styleClass="card-title" text="File Transfers" />
    <VBox fx:id="transfersContainer" spacing="15" />
    <HBox alignment="CENTER">
        <Label style="-fx-text-fill: #3B82F6; -fx-font-weight: bold; -fx-font-size: 12px; -fx-cursor: hand;" text="View Transfer History →" />
    </HBox>
</VBox>
"""
write_file(r'C:\Users\HunterZ\Desktop\FINAL EXAM JAVA\src\main\resources\fxml\components\LanFileTransfersCard.fxml', lan_file_transfers)

print("FXML files successfully rewritten.")
