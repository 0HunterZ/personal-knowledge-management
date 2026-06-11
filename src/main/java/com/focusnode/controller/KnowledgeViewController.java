package com.focusnode.controller;

import com.focusnode.model.Note;
import com.focusnode.service.ServiceLocator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.DirectoryChooser;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import java.awt.Desktop;
import java.util.Optional;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import com.focusnode.repository.FolderRepository;
import com.focusnode.repository.NoteRepository;
import com.focusnode.repository.FileResourceRepository;
import com.focusnode.model.Folder;
import com.focusnode.model.FileResource;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.focusnode.model.KnowledgeNode;

public class KnowledgeViewController {

    @FXML private ScrollPane notesScrollPane;
    @FXML private FlowPane notesFlowPane;
    @FXML private VBox listViewContainer;
    @FXML private VBox listItemsBox;
    @FXML private HBox breadcrumbContainer;
    @FXML private Label gridViewBtn;
    @FXML private Label listViewBtn;
    @FXML private StackPane editorContainer;
    @FXML private VBox rightSidebar;
    @FXML private TextField searchInput;

    @FXML private VBox dashboardContainer;
    @FXML private VBox personalRepoContainer;
    @FXML private Label repoStatsLbl;
    
    @FXML private MenuButton newBtn;

    private NoteEditorController noteEditorController;
    private Node noteEditorNode;
    
    private final FolderRepository folderRepo = new FolderRepository();
    private final NoteRepository noteRepo = new NoteRepository();
    private final FileResourceRepository fileRepo = new FileResourceRepository();
    
    private List<Note> allNotes = new ArrayList<>();
    private List<Folder> currentFolders = new ArrayList<>();
    private List<FileResource> currentFiles = new ArrayList<>();
    
    private Folder currentFolder = null; // null means root "My Drive"
    private boolean isGridView = true;
    private boolean isTrashMode = false;

    @FXML
    public void initialize() {
        loadEditor();
        
        setupViewToggles();
        setupDragAndDrop();
        
        showDashboard();
        
        if (searchInput != null) {
            searchInput.textProperty().addListener((obs, oldVal, newVal) -> filterContent(newVal));
        }
    }
    
    private void setupViewToggles() {
        if (gridViewBtn != null && listViewBtn != null) {
            gridViewBtn.setOnMouseClicked(e -> {
                isGridView = true;
                updateViewMode();
            });
            listViewBtn.setOnMouseClicked(e -> {
                isGridView = false;
                updateViewMode();
            });
            updateViewMode();
        }
    }
    
    private void updateViewMode() {
        if (gridViewBtn == null || listViewBtn == null) return;
        
        if (isGridView) {
            gridViewBtn.getStyleClass().add("toggle-btn-active");
            listViewBtn.getStyleClass().remove("toggle-btn-active");
            notesFlowPane.setVisible(true);
            notesFlowPane.setManaged(true);
            listViewContainer.setVisible(false);
            listViewContainer.setManaged(false);
        } else {
            listViewBtn.getStyleClass().add("toggle-btn-active");
            gridViewBtn.getStyleClass().remove("toggle-btn-active");
            listViewContainer.setVisible(true);
            listViewContainer.setManaged(true);
            notesFlowPane.setVisible(false);
            notesFlowPane.setManaged(false);
        }
    }
    
    private void setupDragAndDrop() {
        if (notesScrollPane != null) {
            notesScrollPane.setOnDragOver(event -> {
                if (event.getGestureSource() != notesScrollPane && event.getDragboard().hasFiles()) {
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                }
                event.consume();
            });

            notesScrollPane.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasFiles()) {
                    success = true;
                    for (File file : db.getFiles()) {
                        handleDroppedFile(file);
                    }
                }
                event.setDropCompleted(success);
                event.consume();
            });
        }
    }
    
    private String getPhysicalFolderPath(Folder folder) {
        if (folder == null) return "C:/FocusNode_Vault/";
        String path = "";
        Folder current = folder;
        while (current != null) {
            path = current.getName() + "/" + path;
            if (current.getParentFolderId() != null) {
                current = folderRepo.getFolderById(current.getParentFolderId());
            } else {
                current = null;
            }
        }
        return "C:/FocusNode_Vault/" + path;
    }

    private void handleDroppedFile(File file) {
        ServiceLocator.getAsyncExecutor().execute(() -> {
            try {
                // Determine destination path
                String vaultPath = getPhysicalFolderPath(currentFolder);
                File vaultDir = new File(vaultPath);
                if (!vaultDir.exists()) vaultDir.mkdirs();
                
                String newFileName = System.currentTimeMillis() + "_" + file.getName();
                Path destPath = Paths.get(vaultPath, newFileName);
                
                // Copy file physically
                Files.copy(file.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
                
                FileResource newFile = new FileResource(
                    -1,
                    1,
                    currentFolder != null ? currentFolder.getId() : null,
                    file.getName(),
                    destPath.toString(),
                    1,
                    file.length(),
                    java.time.LocalDateTime.now(),
                    false
                );
                
                // Add to DB
                fileRepo.addFile(newFile);
                
                // Refresh
                Platform.runLater(this::loadCurrentFolderData);
                
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void loadEditor() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/components/NoteEditor.fxml"));
            noteEditorNode = loader.load();
            noteEditorController = loader.getController();
            editorContainer.getChildren().add(noteEditorNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadCurrentFolderData() {
        System.out.println("DEBUG: loadCurrentFolderData called. currentFolder=" + (currentFolder != null ? currentFolder.getName() : "root"));
        ServiceLocator.getAsyncExecutor().execute(() -> {
            try {
                int userId = 1; // Default
                Integer parentId = currentFolder != null ? currentFolder.getId() : null;
                
                // Build Breadcrumb Path
                List<Folder> path = new ArrayList<>();
                Folder temp = currentFolder;
                while (temp != null) {
                    path.add(0, temp);
                    if (temp.getParentFolderId() != null) {
                        temp = folderRepo.getFolderById(temp.getParentFolderId());
                    } else {
                        temp = null;
                    }
                }
                
                System.out.println("DEBUG: Fetching from DB...");
                if (isTrashMode) {
                    currentFolders = folderRepo.getDeletedFoldersByUserId(userId);
                    allNotes = noteRepo.getDeletedNotesByUserId(userId);
                    currentFiles = fileRepo.getDeletedFilesByUserId(userId);
                } else {
                    currentFolders = folderRepo.getFoldersByUserIdAndParent(userId, parentId);
                    allNotes = noteRepo.getNotesByUserIdAndFolder(userId, parentId);
                    currentFiles = fileRepo.getFilesByUserIdAndFolder(userId, parentId);
                }
                
                Platform.runLater(() -> {
                    if (newBtn != null) {
                        newBtn.setVisible(!isTrashMode);
                        newBtn.setManaged(!isTrashMode);
                    }
                    if (dashboardContainer.isVisible()) {
                        updateDashboardStats();
                    } else {
                        updateBreadcrumbs(path);
                        filterContent(searchInput != null ? searchInput.getText() : "");
                    }
                });
            } catch (Exception e) {
                System.out.println("DEBUG: Exception in async executor!");
                e.printStackTrace();
            }
        });
    }

    private void updateBreadcrumbs(List<Folder> path) {
        if (breadcrumbContainer == null) return;
        breadcrumbContainer.getChildren().clear();
        
        String rootName = isTrashMode ? "Trash" : "Knowledge";
        Label rootLbl = new Label(rootName);
        rootLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #3B82F6; -fx-cursor: hand;");
        rootLbl.setOnMouseClicked(e -> showDashboard());
        breadcrumbContainer.getChildren().add(rootLbl);
        
        if (!isTrashMode) {
            Label separator1 = new Label(" > ");
            separator1.setStyle("-fx-font-size: 14px; -fx-text-fill: #6B7280;");
            
            Label prLbl = new Label("Personal Repository");
            prLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: " + (currentFolder == null ? "#1F2937" : "#3B82F6") + "; -fx-font-weight: " + (currentFolder == null ? "bold" : "normal") + "; -fx-cursor: " + (currentFolder == null ? "default" : "hand") + ";");
            if (currentFolder != null) {
                prLbl.setOnMouseClicked(e -> {
                    currentFolder = null;
                    loadCurrentFolderData();
                });
            }
            breadcrumbContainer.getChildren().addAll(separator1, prLbl);
        }
        
        for (Folder folder : path) {
            Label separator = new Label(" > ");
            separator.setStyle("-fx-font-size: 14px; -fx-text-fill: #6B7280;");
            
            Label folderLbl = new Label(folder.getName());
            if (folder == path.get(path.size() - 1)) {
                // Last item
                folderLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #1F2937; -fx-font-weight: bold;");
            } else {
                folderLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #3B82F6; -fx-cursor: hand;");
                folderLbl.setOnMouseClicked(e -> {
                    currentFolder = folder;
                    loadCurrentFolderData();
                });
            }
            
            breadcrumbContainer.getChildren().addAll(separator, folderLbl);
        }
    }

    private void filterContent(String query) {
        if (notesFlowPane == null || listItemsBox == null) return;
        notesFlowPane.getChildren().clear();
        listItemsBox.getChildren().clear();
        
        String lowerQuery = query == null ? "" : query.toLowerCase();
        
        List<KnowledgeNode> allItems = new ArrayList<>();
        allItems.addAll(currentFolders);
        allItems.addAll(allNotes);
        allItems.addAll(currentFiles);
        
        for (KnowledgeNode item : allItems) {
            if (lowerQuery.isEmpty() || item.getName().toLowerCase().contains(lowerQuery)) {
                Node card = null;
                Node row = null;
                
                String dateStr = item.getUpdatedAt() != null ? item.getUpdatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "";
                
                if (item instanceof Folder folder) {
                    card = createFolderCard(folder);
                    row = createListItem(folder.getName(), "Folder", dateStr, () -> {
                        currentFolder = folder;
                        loadCurrentFolderData();
                    });
                    attachFolderContextMenu(card, folder);
                    attachFolderContextMenu(row, folder);
                } else if (item instanceof Note note) {
                    card = createNoteCard(note);
                    row = createListItem(note.getName(), "Note", dateStr, () -> openEditor(note));
                    attachNoteContextMenu(card, note);
                    attachNoteContextMenu(row, note);
                } else if (item instanceof FileResource file) {
                    card = createFileCard(file);
                    row = createListItem(file.getName(), "File", dateStr, () -> openFile(file));
                    attachFileContextMenu(card, file);
                    attachFileContextMenu(row, file);
                }
                
                if (card != null && row != null) {
                    notesFlowPane.getChildren().add(card);
                    listItemsBox.getChildren().add(row);
                }
            }
        }
    }
    
    private void openFile(FileResource file) {
        try {
            File f = new File(file.getFilePath());
            if (f.exists()) {
                Desktop.getDesktop().open(f);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void attachFolderContextMenu(Node node, Folder folder) {
        ContextMenu contextMenu = new ContextMenu();
        
        if (isTrashMode) {
            MenuItem restoreItem = new MenuItem("Restore");
            restoreItem.setOnAction(e -> {
                folderRepo.restoreFolder(folder.getId());
                loadCurrentFolderData();
            });
            MenuItem deleteItem = new MenuItem("Delete Permanently");
            deleteItem.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Delete Permanently");
                alert.setHeaderText("Are you sure you want to permanently delete this folder?");
                alert.showAndWait().ifPresent(btn -> {
                    if (btn == ButtonType.OK) {
                        folderRepo.deleteFolderPermanently(folder.getId());
                        loadCurrentFolderData();
                    }
                });
            });
            contextMenu.getItems().addAll(restoreItem, deleteItem);
        } else {
            MenuItem exploreItem = new MenuItem("Show in Explorer");
            exploreItem.setOnAction(e -> {
                try {
                    String path = getPhysicalFolderPath(folder);
                    File dir = new File(path);
                    if (dir.exists()) {
                        if (System.getProperty("os.name").toLowerCase().contains("win")) {
                            Runtime.getRuntime().exec("explorer.exe /select,\"" + dir.getAbsolutePath() + "\"");
                        } else {
                            java.awt.Desktop.getDesktop().open(dir);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            MenuItem renameItem = new MenuItem("Rename");
            renameItem.setOnAction(e -> {
                TextInputDialog dialog = new TextInputDialog(folder.getName());
                dialog.setTitle("Rename Folder");
                dialog.setHeaderText("Enter new folder name:");
                dialog.showAndWait().ifPresent(newName -> {
                    if (!newName.trim().isEmpty()) {
                        folderRepo.renameFolder(folder.getId(), newName.trim());
                        loadCurrentFolderData();
                    }
                });
            });
            
            MenuItem deleteItem = new MenuItem("Move to Trash");
            deleteItem.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Move to Trash");
                alert.setHeaderText("Move this folder to trash?");
                alert.showAndWait().ifPresent(btn -> {
                    if (btn == ButtonType.OK) {
                        folderRepo.deleteFolder(folder.getId());
                        loadCurrentFolderData();
                    }
                });
            });
            contextMenu.getItems().addAll(exploreItem, renameItem, deleteItem);
        }
        
        node.setOnContextMenuRequested(e -> contextMenu.show(node, e.getScreenX(), e.getScreenY()));
    }

    private void attachNoteContextMenu(Node node, Note note) {
        ContextMenu contextMenu = new ContextMenu();
        
        if (isTrashMode) {
            MenuItem restoreItem = new MenuItem("Restore");
            restoreItem.setOnAction(e -> {
                noteRepo.restore(note.getId());
                loadCurrentFolderData();
            });
            MenuItem deleteItem = new MenuItem("Delete Permanently");
            deleteItem.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Delete Permanently");
                alert.setHeaderText("Are you sure you want to permanently delete this note?");
                alert.showAndWait().ifPresent(btn -> {
                    if (btn == ButtonType.OK) {
                        noteRepo.deletePermanently(note.getId());
                        loadCurrentFolderData();
                    }
                });
            });
            contextMenu.getItems().addAll(restoreItem, deleteItem);
        } else {
            MenuItem deleteItem = new MenuItem("Move to Trash");
            deleteItem.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Move to Trash");
                alert.setHeaderText("Move this note to trash?");
                alert.showAndWait().ifPresent(btn -> {
                    if (btn == ButtonType.OK) {
                        noteRepo.delete(note.getId());
                        loadCurrentFolderData();
                    }
                });
            });
            contextMenu.getItems().addAll(deleteItem);
        }
        node.setOnContextMenuRequested(e -> contextMenu.show(node, e.getScreenX(), e.getScreenY()));
    }

    private void attachFileContextMenu(Node node, FileResource fileRes) {
        ContextMenu contextMenu = new ContextMenu();
        
        if (isTrashMode) {
            MenuItem restoreItem = new MenuItem("Restore");
            restoreItem.setOnAction(e -> {
                fileRepo.restoreFile(fileRes.getId());
                loadCurrentFolderData();
            });
            MenuItem deleteItem = new MenuItem("Delete Permanently");
            deleteItem.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Delete Permanently");
                alert.setHeaderText("Are you sure you want to permanently delete this file?");
                alert.showAndWait().ifPresent(btn -> {
                    if (btn == ButtonType.OK) {
                        fileRepo.deleteFilePermanently(fileRes.getId());
                        File f = new File(fileRes.getFilePath());
                        if (f.exists()) f.delete();
                        loadCurrentFolderData();
                    }
                });
            });
            contextMenu.getItems().addAll(restoreItem, deleteItem);
        } else {
            MenuItem openItem = new MenuItem("Open");
            openItem.setOnAction(e -> openFile(fileRes));
            
            MenuItem exploreItem = new MenuItem("Show in Explorer");
            exploreItem.setOnAction(e -> {
                try {
                    File f = new File(fileRes.getFilePath());
                    if (f.exists()) {
                        if (System.getProperty("os.name").toLowerCase().contains("win")) {
                            Runtime.getRuntime().exec("explorer.exe /select,\"" + f.getAbsolutePath() + "\"");
                        } else {
                            java.awt.Desktop.getDesktop().open(f.getParentFile());
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            
            MenuItem deleteItem = new MenuItem("Move to Trash");
            deleteItem.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Move to Trash");
                alert.setHeaderText("Move this file to trash?");
                alert.showAndWait().ifPresent(btn -> {
                    if (btn == ButtonType.OK) {
                        fileRepo.deleteFile(fileRes.getId());
                        loadCurrentFolderData();
                    }
                });
            });
            contextMenu.getItems().addAll(openItem, exploreItem, deleteItem);
        }
        node.setOnContextMenuRequested(e -> contextMenu.show(node, e.getScreenX(), e.getScreenY()));
    }
    
    private Node createFolderCard(Folder folder) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-padding: 20; -fx-background-radius: 12; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 10, 0, 0, 4);");
        card.setPrefWidth(200);
        card.setPrefHeight(120);
        
        javafx.scene.layout.StackPane iconWrapper = new javafx.scene.layout.StackPane();
        iconWrapper.setPrefSize(40, 40);
        iconWrapper.setMaxSize(40, 40);
        iconWrapper.setStyle("-fx-background-color: #FEF3C7; -fx-background-radius: 8;");
        
        javafx.scene.shape.SVGPath icon = new javafx.scene.shape.SVGPath();
        icon.setContent("M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z");
        icon.setFill(javafx.scene.paint.Color.web("#D97706"));
        icon.setScaleX(1.2);
        icon.setScaleY(1.2);
        
        iconWrapper.getChildren().add(icon);
        
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        Label title = new Label(folder.getName());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #1F2937;");
        
        // We can show stats if we have them, otherwise just generic or date
        Label subtitle = new Label("Folder");
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");
        
        card.getChildren().addAll(iconWrapper, spacer, title, subtitle);
        card.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                currentFolder = folder;
                loadCurrentFolderData();
            }
        });
        
        // Add hover effect via Java code since we don't have a specific CSS class here, 
        // but applying a class is better. Let's stick to inline for simplicity or add a class.
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #FFFFFF; -fx-padding: 20; -fx-background-radius: 12; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 15, 0, 0, 6);"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #FFFFFF; -fx-padding: 20; -fx-background-radius: 12; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 10, 0, 0, 4);"));
        
        return card;
    }
    
    private Node createFileCard(FileResource file) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-padding: 20; -fx-background-radius: 12; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 10, 0, 0, 4);");
        card.setPrefWidth(200);
        card.setPrefHeight(120);
        
        javafx.scene.layout.StackPane iconWrapper = new javafx.scene.layout.StackPane();
        iconWrapper.setPrefSize(40, 40);
        iconWrapper.setMaxSize(40, 40);
        iconWrapper.setStyle("-fx-background-color: #E0E7FF; -fx-background-radius: 8;");
        
        javafx.scene.shape.SVGPath icon = new javafx.scene.shape.SVGPath();
        icon.setContent("M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z");
        icon.setFill(javafx.scene.paint.Color.web("#4338CA"));
        icon.setScaleX(1.2);
        icon.setScaleY(1.2);
        
        iconWrapper.getChildren().add(icon);
        
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        Label title = new Label(file.getFileName());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1F2937;");
        title.setWrapText(true);
        title.setMaxHeight(40);
        
        String sizeStr = (file.getSizeBytes() / 1024) + " KB";
        Label sizeLbl = new Label("File • " + sizeStr);
        sizeLbl.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");
        
        card.getChildren().addAll(iconWrapper, spacer, title, sizeLbl);
        card.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                openFile(file);
            }
        });
        
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #FFFFFF; -fx-padding: 20; -fx-background-radius: 12; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 15, 0, 0, 6);"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #FFFFFF; -fx-padding: 20; -fx-background-radius: 12; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 10, 0, 0, 4);"));
        
        return card;
    }
    
    private Node createListItem(String name, String type, String date, Runnable onClick) {
        HBox row = new HBox();
        row.setStyle("-fx-padding: 10; -fx-background-color: white; -fx-background-radius: 5; -fx-cursor: hand;");
        if (onClick != null) {
            row.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY) {
                    if (type.equals("File") && e.getClickCount() != 2) return;
                    onClick.run();
                }
            });
        }
        
        HBox nameBox = new HBox(8);
        nameBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        nameBox.setPrefWidth(400);

        javafx.scene.shape.SVGPath iconView = new javafx.scene.shape.SVGPath();
        String pathData = type.equals("Folder") ? "M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z" : 
                         (type.equals("Note") ? "M9 2H4a2 2 0 00-2 2v16a2 2 0 002 2h14a2 2 0 002-2V8l-6-6H9zm6 1.5L19.5 8H15V3.5z" : "M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z");
        iconView.setContent(pathData);
        String iconColor = type.equals("Folder") ? "#D97706" : (type.equals("Note") ? "#7E22CE" : "#4338CA");
        iconView.setFill(javafx.scene.paint.Color.web(iconColor));
        iconView.setScaleX(0.9);
        iconView.setScaleY(0.9);
        
        javafx.scene.layout.StackPane iconWrapper = new javafx.scene.layout.StackPane();
        iconWrapper.setPrefSize(32, 32);
        iconWrapper.setMaxSize(32, 32);
        String bgColor = type.equals("Folder") ? "#FEF3C7" : (type.equals("Note") ? "#F3E8FF" : "#E0E7FF");
        iconWrapper.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 6;");
        iconWrapper.getChildren().add(iconView);
        
        Label nameLbl = new Label(name);
        nameLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #1F2937;");
        nameBox.getChildren().addAll(iconWrapper, nameLbl);
        
        Label typeLbl = new Label(type);
        typeLbl.setStyle("-fx-text-fill: #6B7280; -fx-pref-width: 150;");
        
        Label dateLbl = new Label(date);
        dateLbl.setStyle("-fx-text-fill: #6B7280; -fx-pref-width: 150;");
        
        row.getChildren().addAll(nameBox, typeLbl, dateLbl);
        return row;
    }

    private Node createNoteCard(Note note) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-padding: 20; -fx-background-radius: 12; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 10, 0, 0, 4);");
        card.setPrefWidth(200);
        card.setPrefHeight(120);

        javafx.scene.layout.StackPane iconWrapper = new javafx.scene.layout.StackPane();
        iconWrapper.setPrefSize(40, 40);
        iconWrapper.setMaxSize(40, 40);
        iconWrapper.setStyle("-fx-background-color: #F3E8FF; -fx-background-radius: 8;");

        javafx.scene.shape.SVGPath icon = new javafx.scene.shape.SVGPath();
        icon.setContent("M9 2H4a2 2 0 00-2 2v16a2 2 0 002 2h14a2 2 0 002-2V8l-6-6H9zm6 1.5L19.5 8H15V3.5z");
        icon.setFill(javafx.scene.paint.Color.web("#7E22CE"));
        icon.setScaleX(1.2);
        icon.setScaleY(1.2);
        
        iconWrapper.getChildren().add(icon);
        
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label title = new Label(note.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #1F2937;");
        title.setWrapText(true);
        title.setMaxHeight(40);

        Label category = new Label("Note • " + note.getSubjectName());
        category.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");

        card.getChildren().addAll(iconWrapper, spacer, title, category);
        
        card.setOnMouseClicked(e -> openEditor(note));
        
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #FFFFFF; -fx-padding: 20; -fx-background-radius: 12; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 15, 0, 0, 6);"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #FFFFFF; -fx-padding: 20; -fx-background-radius: 12; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 10, 0, 0, 4);"));
        
        return card;
    }

    private void openEditor(Note note) {
        noteEditorController.setNote(note, () -> {
            // Save note to database before closing
            if (note.getId() == -1) {
                noteRepo.add(note);
            } else {
                noteRepo.update(note);
            }
            closeEditor();
            loadCurrentFolderData(); // Refresh

        }, this::closeEditor);
        editorContainer.setVisible(true);
    }

    @FXML
    private void closeEditor() {
        if (editorContainer != null) {
            editorContainer.setVisible(false);
        }
    }

    @FXML
    public void createNewNote() {
        Note newNote = new Note(
            -1,
            1,
            null,
            currentFolder != null ? currentFolder.getId() : null,
            "New Note",
            "",
            null,
            java.util.List.of(),
            java.time.LocalDateTime.now(),
            java.time.LocalDateTime.now(),
            false
        );
        openEditor(newNote);
    }
    
    @FXML
    public void createNewFolder() {
        TextInputDialog dialog = new TextInputDialog("New Folder");
        dialog.setTitle("New Folder");
        dialog.setHeaderText("Create a new folder");
        dialog.setContentText("Please enter folder name:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                Folder folder = new Folder(-1, 1, name.trim(), currentFolder != null ? currentFolder.getId() : null, java.time.LocalDateTime.now());
                if (folderRepo.createFolder(folder)) {
                    // Create physical folder
                    String path = getPhysicalFolderPath(folder);
                    File dir = new File(path);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    loadCurrentFolderData();
                }
            }
        });
    }

    @FXML
    public void uploadFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Upload File");
        List<File> files = fileChooser.showOpenMultipleDialog(notesScrollPane.getScene().getWindow());
        if (files != null && !files.isEmpty()) {
            for (File f : files) {
                handleDroppedFile(f);
            }
        }
    }

    @FXML
    public void showDashboard() {
        if (dashboardContainer != null) {
            dashboardContainer.setVisible(true);
            dashboardContainer.setManaged(true);
        }
        if (personalRepoContainer != null) {
            personalRepoContainer.setVisible(false);
            personalRepoContainer.setManaged(false);
        }
        
        // Load data just to get stats
        loadCurrentFolderData();
    }
    
    private void updateDashboardStats() {
        if (repoStatsLbl != null) {
            repoStatsLbl.setText("Your personal knowledge base");
        }
    }

    @FXML
    public void openPersonalRepository() {
        isTrashMode = false;
        currentFolder = null;
        if (dashboardContainer != null) {
            dashboardContainer.setVisible(false);
            dashboardContainer.setManaged(false);
        }
        if (personalRepoContainer != null) {
            personalRepoContainer.setVisible(true);
            personalRepoContainer.setManaged(true);
        }
        loadCurrentFolderData();
    }

    @FXML
    public void openTrash() {
        isTrashMode = true;
        currentFolder = null;
        if (dashboardContainer != null) {
            dashboardContainer.setVisible(false);
            dashboardContainer.setManaged(false);
        }
        if (personalRepoContainer != null) {
            personalRepoContainer.setVisible(true);
            personalRepoContainer.setManaged(true);
        }
        loadCurrentFolderData();
    }
}
