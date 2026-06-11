package com.focusnode.service;

import com.focusnode.model.CategoryBreakdown;
import com.focusnode.model.FocusPoint;
import com.focusnode.model.Note;
import com.focusnode.model.TagUsageItem;
import com.focusnode.model.Task;
import com.focusnode.repository.*;

import java.time.LocalTime;
import java.util.List;

public class SqliteAppDataService implements AppDataService {

    private TaskRepository taskRepo = new TaskRepository();
    private NoteRepository noteRepo = new NoteRepository();
    private TagUsageRepository tagRepo = new TagUsageRepository();
    private FocusPointRepository focusRepo = new FocusPointRepository();
    private CategoryBreakdownRepository categoryRepo = new CategoryBreakdownRepository();
    private FocusSessionRepository focusSessionRepo = new FocusSessionRepository();
    private ReviewItemRepository reviewItemRepo = new ReviewItemRepository();

    @Override
    public String getUserName() {
        return "User";
    }

    @Override
    public String getGreetingMessage() {
        int hour = LocalTime.now().getHour();
        if (hour < 12) {
            return "Good morning, " + getUserName() + "! 👋";
        } else if (hour < 18) {
            return "Good afternoon, " + getUserName() + "! 👋";
        } else {
            return "Good evening, " + getUserName() + "! 👋";
        }
    }

    @Override
    public List<Task> getTasks() {
        return taskRepo.findAll();
    }

    @Override
    public void saveTask(Task task) {
        if (task.getId() <= 0) {
            taskRepo.add(task);
        } else {
            taskRepo.update(task);
        }
    }

    @Override
    public void updateTask(Task task) {
        taskRepo.update(task);
    }

    @Override
    public void deleteTask(Task task) {
        taskRepo.delete(task.getId());
    }

    @Override
    public List<Note> getNotes() {
        return noteRepo.findAll();
    }

    @Override
    public void saveNote(Note note) {
        if (note.getId() <= 0) {
            noteRepo.add(note);
        } else {
            noteRepo.update(note);
        }
    }

    @Override
    public void updateNote(Note note) {
        noteRepo.update(note);
    }

    @Override
    public void deleteNote(Note note) {
        noteRepo.delete(note.getId());
    }

    @Override
    public List<TagUsageItem> getTagUsage() {
        return tagRepo.findAll();
    }

    @Override
    public List<FocusPoint> getWeeklyFocus() {
        return focusRepo.findAll();
    }

    @Override
    public List<CategoryBreakdown> getCategoryBreakdown() {
        return categoryRepo.findAll();
    }

    @Override
    public List<com.focusnode.model.FocusSession> getFocusSessions() {
        return focusSessionRepo.findAll();
    }

    @Override
    public void saveFocusSession(com.focusnode.model.FocusSession session) {
        focusSessionRepo.add(session);
    }

    @Override
    public List<com.focusnode.model.ReviewItem> getReviewItems() {
        return reviewItemRepo.findAll();
    }

    @Override
    public void saveReviewItem(com.focusnode.model.ReviewItem item) {
        if (item.getId() <= 0) {
            // we don't have add method for ReviewItemRepo yet, but for now we only need update
            reviewItemRepo.update(item);
        } else {
            reviewItemRepo.update(item);
        }
    }
}
