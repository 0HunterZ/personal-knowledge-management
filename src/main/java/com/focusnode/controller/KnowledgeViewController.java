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
    @FXML private VBox gridContainer;
    @FXML private VBox foldersSection;
    @FXML private FlowPane foldersFlowPane;
    @FXML private VBox filesSection;
    @FXML private FlowPane filesFlowPane;
    @FXML private VBox listViewContainer;
    @FXML private VBox listItemsBox;
    
    @FXML private Label filterAllBtn;
    @FXML private Label filterFoldersBtn;
    @FXML private Label filterNotesBtn;
    @FXML private HBox breadcrumbContainer;
    @FXML private Label gridViewBtn;
    @FXML private Label listViewBtn;
    @FXML private StackPane editorContainer;
    
    @FXML private QuickNoteController quickNoteCardController;
    @FXML private TagsCloudController tagsCloudCardController;
    @FXML private StorageCardController storageCardController;

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
    private int currentFilterType = 0; // 0=All, 1=Folders, 2=Notes & Files

    @FXML
    public void initialize() {
        loadEditor();
        
        setupViewToggles();
        setupDragAndDrop();
        setupQuickFilters();
        
        showDashboard();
        
        if (searchInput != null) {
            searchInput.textProperty().addListener((obs, oldVal, newVal) -> filterContent(newVal));
        }

        if (quickNoteCardController != null) {
            quickNoteCardController.setOnNoteSaved(() -> {
                loadCurrentFolderData();
                if (tagsCloudCardController != null) tagsCloudCardController.refreshTags();
                if (storageCardController != null) storageCardController.updateStorageInfo();
            });
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
    
    private void setupQuickFilters() {
        if (filterAllBtn != null) {
            filterAllBtn.setOnMouseClicked(e -> {
                currentFilterType = 0;
                updateFilterStyles();
                filterContent(searchInput != null ? searchInput.getText() : "");
            });
        }
        if (filterFoldersBtn != null) {
            filterFoldersBtn.setOnMouseClicked(e -> {
                currentFilterType = 1;
                updateFilterStyles();
                filterContent(searchInput != null ? searchInput.getText() : "");
            });
        }
        if (filterNotesBtn != null) {
            filterNotesBtn.setOnMouseClicked(e -> {
                currentFilterType = 2;
                updateFilterStyles();
                filterContent(searchInput != null ? searchInput.getText() : "");
            });
        }
    }
    
    private void updateFilterStyles() {
        if (filterAllBtn != null) filterAllBtn.getStyleClass().remove("notion-filter-pill-active");
        if (filterFoldersBtn != null) filterFoldersBtn.getStyleClass().remove("notion-filter-pill-active");
        if (filterNotesBtn != null) filterNotesBtn.getStyleClass().remove("notion-filter-pill-active");
        
        if (currentFilterType == 0 && filterAllBtn != null) filterAllBtn.getStyleClass().add("notion-filter-pill-active");
        if (currentFilterType == 1 && filterFoldersBtn != null) filterFoldersBtn.getStyleClass().add("notion-filter-pill-active");
        if (currentFilterType == 2 && filterNotesBtn != null) filterNotesBtn.getStyleClass().add("notion-filter-pill-active");
    }
    
    private void updateViewMode() {
        if (gridViewBtn == null || listViewBtn == null) return;
        
        if (isGridView) {
            gridViewBtn.getStyleClass().add("toggle-btn-active");
            listViewBtn.getStyleClass().remove("toggle-btn-active");
            gridContainer.setVisible(true);
            gridContainer.setManaged(true);
            listViewContainer.setVisible(false);
            listViewContainer.setManaged(false);
        } else {
            listViewBtn.getStyleClass().add("toggle-btn-active");
            gridViewBtn.getStyleClass().remove("toggle-btn-active");
            listViewContainer.setVisible(true);
            listViewContainer.setManaged(true);
            gridContainer.setVisible(false);
            gridContainer.setManaged(false);
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
        if (gridContainer == null || listItemsBox == null) return;
        foldersFlowPane.getChildren().clear();
        filesFlowPane.getChildren().clear();
        listItemsBox.getChildren().clear();
        
        String lowerQuery = query == null ? "" : query.toLowerCase();
        
        List<KnowledgeNode> allItems = new ArrayList<>();
        allItems.addAll(currentFolders);
        allItems.addAll(allNotes);
        allItems.addAll(currentFiles);
        
        for (KnowledgeNode item : allItems) {
            if (lowerQuery.isEmpty() || item.getName().toLowerCase().contains(lowerQuery)) {
                if (currentFilterType == 1 && !(item instanceof Folder)) continue;
                if (currentFilterType == 2 && (item instanceof Folder)) continue;
                
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
                    if (item instanceof Folder) {
                        foldersFlowPane.getChildren().add(card);
                    } else {
                        filesFlowPane.getChildren().add(card);
                    }
                    listItemsBox.getChildren().add(row);
                }
            }
        }
        
        boolean showFolders = (currentFilterType == 0 || currentFilterType == 1) && !foldersFlowPane.getChildren().isEmpty();
        boolean showFiles = (currentFilterType == 0 || currentFilterType == 2) && !filesFlowPane.getChildren().isEmpty();
        
        foldersSection.setVisible(showFolders);
        foldersSection.setManaged(showFolders);
        filesSection.setVisible(showFiles);
        filesSection.setManaged(showFiles);
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
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-padding: 12 16; -fx-background-radius: 8; -fx-cursor: hand; -fx-border-color: #E5E7EB; -fx-border-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.02), 5, 0, 0, 2);");
        // About 260px width to fit 3 per row comfortably
        card.setPrefWidth(260);
        card.setPrefHeight(60);
        
        javafx.scene.layout.StackPane iconWrapper = new javafx.scene.layout.StackPane();
        iconWrapper.setPrefSize(32, 32);
        iconWrapper.setMaxSize(32, 32);
        iconWrapper.setStyle("-fx-background-color: #FEF3C7; -fx-background-radius: 6;");
        
        javafx.scene.shape.SVGPath icon = new javafx.scene.shape.SVGPath();
        icon.setContent("M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z");
        icon.setFill(javafx.scene.paint.Color.web("#D97706"));
        iconWrapper.getChildren().add(icon);
        
        Label title = new Label(folder.getName());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1F2937;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        javafx.scene.control.Button menuBtn = new javafx.scene.control.Button("⋮");
        menuBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #9CA3AF; -fx-font-weight: bold; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 0 5;");
        menuBtn.setOnMouseClicked(e -> {
            e.consume();
            card.fireEvent(new javafx.scene.input.ContextMenuEvent(javafx.scene.input.ContextMenuEvent.CONTEXT_MENU_REQUESTED, e.getSceneX(), e.getSceneY(), e.getScreenX(), e.getScreenY(), false, null));
        });
        
        card.getChildren().addAll(iconWrapper, title, spacer, menuBtn);
        
        card.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                currentFolder = folder;
                loadCurrentFolderData();
            }
        });
        
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #F9FAFB; -fx-padding: 12 16; -fx-background-radius: 8; -fx-cursor: hand; -fx-border-color: #D1D5DB; -fx-border-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 8, 0, 0, 3);"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #FFFFFF; -fx-padding: 12 16; -fx-background-radius: 8; -fx-cursor: hand; -fx-border-color: #E5E7EB; -fx-border-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.02), 5, 0, 0, 2);"));
        
        return card;
    }
    
    private Node createFileCard(FileResource file) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 4);");
        card.setPrefWidth(260);
        card.setMinHeight(280);

        // Top section: Preview thumbnail
        VBox topSection = new VBox();
        topSection.setAlignment(Pos.CENTER);
        String[] pastels = {"#ECFDF5", "#EFF6FF", "#FEF2F2", "#FFFBEB", "#F3E8FF", "#FCE7F3", "#E0F2FE"};
        String bgColor = pastels[Math.abs(file.getId()) % pastels.length];
        topSection.setStyle("-fx-background-color: " + bgColor + "; -fx-padding: 15; -fx-background-radius: 12 12 0 0;");
        topSection.setPrefHeight(120);
        topSection.setMinHeight(120);
        
        javafx.scene.shape.SVGPath icon = new javafx.scene.shape.SVGPath();
        icon.setContent("M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z");
        icon.setFill(javafx.scene.paint.Color.web("#4338CA"));
        icon.setScaleX(2.0);
        icon.setScaleY(2.0);
        topSection.getChildren().add(icon);

        // Bottom section: Info
        VBox infoSection = new VBox(8);
        infoSection.setStyle("-fx-padding: 15;");
        
        Label title = new Label(file.getFileName());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #1F2937;");
        title.setWrapText(true);
        title.setMaxHeight(40);
        
        String sizeStr = (file.getSizeBytes() / 1024) + " KB";
        Label description = new Label("File • " + sizeStr);
        description.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");
        description.setWrapText(true);
        description.setMaxHeight(40);
        
        // Tags - FileResource doesn't have tags
        FlowPane tagsPane = new FlowPane(5, 5);
        
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        // Footer
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_LEFT);
        String dateStr = file.getUpdatedAt() != null ? file.getUpdatedAt().format(DateTimeFormatter.ofPattern("MMM dd")) : "";
        Label dateLbl = new Label("Updated " + dateStr);
        dateLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #9CA3AF;");
        
        Region fSpacer = new Region();
        HBox.setHgrow(fSpacer, Priority.ALWAYS);
        
        javafx.scene.shape.SVGPath star = new javafx.scene.shape.SVGPath();
        star.setContent("M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z");
        star.setFill(javafx.scene.paint.Color.web("#9CA3AF"));
        star.setScaleX(0.7); star.setScaleY(0.7);
        
        javafx.scene.shape.SVGPath dots = new javafx.scene.shape.SVGPath();
        dots.setContent("M12 8c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z");
        dots.setFill(javafx.scene.paint.Color.web("#9CA3AF"));
        dots.setScaleX(0.8); dots.setScaleY(0.8);
        
        StackPane dotsBtn = new StackPane(dots);
        dotsBtn.setStyle("-fx-padding: 5; -fx-cursor: hand;");
        dotsBtn.setOnMouseClicked(e -> {
            e.consume();
            card.fireEvent(new javafx.scene.input.ContextMenuEvent(javafx.scene.input.ContextMenuEvent.CONTEXT_MENU_REQUESTED, e.getSceneX(), e.getSceneY(), e.getScreenX(), e.getScreenY(), false, null));
        });
        
        footer.getChildren().addAll(dateLbl, fSpacer, star, dotsBtn);
        
        infoSection.getChildren().addAll(title, description, tagsPane, spacer, footer);
        VBox.setVgrow(infoSection, Priority.ALWAYS);
        
        card.getChildren().addAll(topSection, infoSection);
        
        card.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                openFile(file);
            }
        });
        
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 15, 0, 0, 6);"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 4);"));
        
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
        VBox card = new VBox();
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 4);");
        card.setPrefWidth(260);
        card.setMinHeight(280);

        // Top section: Preview thumbnail
        VBox topSection = new VBox();
        String[] pastels = {"#ECFDF5", "#EFF6FF", "#FEF2F2", "#FFFBEB", "#F3E8FF", "#FCE7F3", "#E0F2FE"};
        String bgColor = pastels[Math.abs(note.getId()) % pastels.length];
        topSection.setStyle("-fx-background-color: " + bgColor + "; -fx-padding: 15; -fx-background-radius: 12 12 0 0;");
        topSection.setPrefHeight(120);
        topSection.setMinHeight(120);
        
        Label previewContent = new Label(note.getPreview());
        previewContent.setWrapText(true);
        previewContent.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 11px; -fx-text-fill: #4B5563;");
        topSection.getChildren().add(previewContent);

        // Bottom section: Info
        VBox infoSection = new VBox(8);
        infoSection.setStyle("-fx-padding: 15;");
        
        Label title = new Label(note.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #1F2937;");
        title.setWrapText(true);
        
        Label description = new Label(note.getPreview());
        description.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");
        description.setWrapText(true);
        description.setMaxHeight(40);
        
        // Tags
        FlowPane tagsPane = new FlowPane(5, 5);
        if (note.getTags() != null) {
            for (String tag : note.getTags()) {
                Label tagLbl = new Label("#" + tag);
                tagLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #059669; -fx-background-color: #D1FAE5; -fx-padding: 2 6; -fx-background-radius: 8;");
                tagsPane.getChildren().add(tagLbl);
            }
        }
        
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        // Footer
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_LEFT);
        String dateStr = note.getUpdatedAt() != null ? note.getUpdatedAt().format(DateTimeFormatter.ofPattern("MMM dd")) : "";
        Label dateLbl = new Label("Updated " + dateStr);
        dateLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #9CA3AF;");
        
        Region fSpacer = new Region();
        HBox.setHgrow(fSpacer, Priority.ALWAYS);
        
        javafx.scene.shape.SVGPath star = new javafx.scene.shape.SVGPath();
        star.setContent("M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z");
        star.setFill(javafx.scene.paint.Color.web("#9CA3AF"));
        star.setScaleX(0.7); star.setScaleY(0.7);
        
        javafx.scene.shape.SVGPath dots = new javafx.scene.shape.SVGPath();
        dots.setContent("M12 8c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z");
        dots.setFill(javafx.scene.paint.Color.web("#9CA3AF"));
        dots.setScaleX(0.8); dots.setScaleY(0.8);
        
        StackPane dotsBtn = new StackPane(dots);
        dotsBtn.setStyle("-fx-padding: 5; -fx-cursor: hand;");
        dotsBtn.setOnMouseClicked(e -> {
            e.consume();
            card.fireEvent(new javafx.scene.input.ContextMenuEvent(javafx.scene.input.ContextMenuEvent.CONTEXT_MENU_REQUESTED, e.getSceneX(), e.getSceneY(), e.getScreenX(), e.getScreenY(), false, null));
        });
        
        footer.getChildren().addAll(dateLbl, fSpacer, star, dotsBtn);
        
        infoSection.getChildren().addAll(title, description, tagsPane, spacer, footer);
        VBox.setVgrow(infoSection, Priority.ALWAYS);
        
        card.getChildren().addAll(topSection, infoSection);
        
        card.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                openEditor(note);
            }
        });
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 15, 0, 0, 6);"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 4);"));
        
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
            if (tagsCloudCardController != null) tagsCloudCardController.refreshTags();
            if (storageCardController != null) storageCardController.updateStorageInfo();
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
    public void openShared() {
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
        
        Platform.runLater(() -> {
            if (breadcrumbContainer != null) {
                breadcrumbContainer.getChildren().clear();
                Label rootLbl = new Label("Shared with me");
                rootLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #3B82F6; -fx-cursor: hand;");
                rootLbl.setOnMouseClicked(e -> showDashboard());
                breadcrumbContainer.getChildren().add(rootLbl);
            }
            if (foldersFlowPane != null) foldersFlowPane.getChildren().clear();
            if (filesFlowPane != null) filesFlowPane.getChildren().clear();
            if (listItemsBox != null) listItemsBox.getChildren().clear();
            foldersSection.setVisible(false);
            foldersSection.setManaged(false);
            
            if (newBtn != null) {
                newBtn.setVisible(false);
                newBtn.setManaged(false);
            }
        });

        com.focusnode.service.ServiceLocator.getAsyncExecutor().execute(() -> {
            try {
                com.focusnode.repository.LanTransferHistoryRepository repo = new com.focusnode.repository.LanTransferHistoryRepository();
                java.util.List<com.focusnode.model.LanTransferHistory> history = repo.getHistoryByUserId(1);
                
                java.util.List<com.focusnode.model.FileResource> sharedFiles = new java.util.ArrayList<>();
                String downloadsPath = System.getProperty("user.home") + java.io.File.separator + "Downloads" + java.io.File.separator + "FocusNode";
                
                int pseudoId = -1000;
                for (com.focusnode.model.LanTransferHistory h : history) {
                    if ("COMPLETED".equals(h.getStatus()) && h.getTargetName() != null && h.getTargetName().startsWith("From ")) {
                        java.io.File f = new java.io.File(downloadsPath, h.getFileName());
                        if (f.exists()) {
                            com.focusnode.model.FileResource fileRes = new com.focusnode.model.FileResource(
                                pseudoId--,
                                1,
                                null,
                                h.getFileName(),
                                f.getAbsolutePath(),
                                1, 
                                h.getSizeBytes(),
                                h.getCreatedAt(),
                                false
                            );
                            sharedFiles.add(fileRes);
                        }
                    }
                }
                
                Platform.runLater(() -> {
                    if (sharedFiles.isEmpty()) {
                        filesSection.setVisible(false);
                        filesSection.setManaged(false);
                    } else {
                        filesSection.setVisible(true);
                        filesSection.setManaged(true);
                        if (isGridView) {
                            for (com.focusnode.model.FileResource file : sharedFiles) {
                                filesFlowPane.getChildren().add(createFileCard(file));
                            }
                        } else {
                            for (com.focusnode.model.FileResource file : sharedFiles) {
                                String dateStr = file.getUpdatedAt() != null ? file.getUpdatedAt().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd")) : "";
                                listItemsBox.getChildren().add(createListItem(file.getName(), "Shared File", dateStr, () -> openFile(file)));
                            }
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    public void openRecent() {
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
        
        ServiceLocator.getAsyncExecutor().execute(() -> {
            try {
                int userId = 1;
                List<Note> recentNotes = noteRepo.getRecentNotesByUserId(userId, 20);
                
                Platform.runLater(() -> {
                    if (breadcrumbContainer != null) {
                        breadcrumbContainer.getChildren().clear();
                        Label rootLbl = new Label("Recent Activity");
                        rootLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #3B82F6; -fx-cursor: hand;");
                        rootLbl.setOnMouseClicked(e -> showDashboard());
                        breadcrumbContainer.getChildren().add(rootLbl);
                    }
                    if (newBtn != null) {
                        newBtn.setVisible(false);
                        newBtn.setManaged(false);
                    }
                    
                    // Populate recent notes
                    allNotes = recentNotes;
                    currentFolders = new ArrayList<>();
                    currentFiles = new ArrayList<>();
                    filterContent(searchInput != null ? searchInput.getText() : "");
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
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
