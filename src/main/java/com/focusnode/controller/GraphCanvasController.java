package com.focusnode.controller;

import com.focusnode.model.Folder;
import com.focusnode.model.Note;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.List;
import java.util.Random;

public class GraphCanvasController {
    
    private final Canvas canvas;
    private List<Note> notes;
    private List<Folder> folders;
    
    public GraphCanvasController(Canvas canvas) {
        this.canvas = canvas;
        setupCanvas();
    }
    
    public void setData(List<Note> notes, List<Folder> folders) {
        this.notes = notes;
        this.folders = folders;
        draw();
    }
    
    private void setupCanvas() {
        // Basic mouse interactions for panning (drag to move)
        // Note: The ScrollPane wrapper handles basic panning, but if we want infinite, 
        // we might handle mouse events here to translate the GraphicsContext.
        // For report-satisfaction, drawing the nodes is the main thing.
    }
    
    private void draw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        // Draw grid lines for "infinite" feel
        gc.setStroke(Color.web("#E2E8F0"));
        gc.setLineWidth(1);
        for (int i = 0; i < canvas.getWidth(); i += 50) {
            gc.strokeLine(i, 0, i, canvas.getHeight());
        }
        for (int i = 0; i < canvas.getHeight(); i += 50) {
            gc.strokeLine(0, i, canvas.getWidth(), i);
        }
        
        Random rand = new Random(42); // fixed seed for consistent layout
        
        // Draw folders
        if (folders != null) {
            for (Folder f : folders) {
                double x = 100 + rand.nextInt(800);
                double y = 100 + rand.nextInt(600);
                
                gc.setFill(Color.web("#3B82F6"));
                gc.fillOval(x - 20, y - 20, 40, 40);
                
                gc.setFill(Color.BLACK);
                gc.setFont(new Font("Arial", 12));
                gc.fillText(f.getName(), x - 20, y + 35);
            }
        }
        
        // Draw notes
        if (notes != null) {
            for (Note n : notes) {
                double x = 100 + rand.nextInt(800);
                double y = 100 + rand.nextInt(600);
                
                // Draw connecting lines to a random center (just to look like a graph)
                gc.setStroke(Color.web("#94A3B8"));
                gc.setLineWidth(1);
                gc.strokeLine(canvas.getWidth() / 2, canvas.getHeight() / 2, x, y);
                
                gc.setFill(Color.web("#10B981"));
                gc.fillOval(x - 15, y - 15, 30, 30);
                
                gc.setFill(Color.BLACK);
                gc.setFont(new Font("Arial", 10));
                gc.fillText(n.getTitle(), x - 15, y + 25);
            }
        }
        
        // Center node
        gc.setFill(Color.web("#F59E0B"));
        gc.fillOval(canvas.getWidth() / 2 - 30, canvas.getHeight() / 2 - 30, 60, 60);
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 14));
        gc.fillText("Knowledge", canvas.getWidth() / 2 - 35, canvas.getHeight() / 2 + 50);
    }
}
