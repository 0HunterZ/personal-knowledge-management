package com.focusnode.model;

public class PomodoroSyncState {
    private String sessionId;
    private String phase;
    private int taskId;
    private String taskTitle;
    private int totalSeconds;
    private int remainingSeconds;
    private boolean running;
    private long deadlineEpochMillis;
    private long sequence;
    private long createdAtEpochMillis;

    public PomodoroSyncState() {
    }

    public PomodoroSyncState(
            String sessionId,
            String phase,
            int taskId,
            String taskTitle,
            int totalSeconds,
            int remainingSeconds,
            boolean running,
            long deadlineEpochMillis,
            long sequence,
            long createdAtEpochMillis
    ) {
        this.sessionId = sessionId;
        this.phase = phase;
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.totalSeconds = totalSeconds;
        this.remainingSeconds = remainingSeconds;
        this.running = running;
        this.deadlineEpochMillis = deadlineEpochMillis;
        this.sequence = sequence;
        this.createdAtEpochMillis = createdAtEpochMillis;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public int getTotalSeconds() {
        return totalSeconds;
    }

    public void setTotalSeconds(int totalSeconds) {
        this.totalSeconds = totalSeconds;
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }

    public void setRemainingSeconds(int remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public long getDeadlineEpochMillis() {
        return deadlineEpochMillis;
    }

    public void setDeadlineEpochMillis(long deadlineEpochMillis) {
        this.deadlineEpochMillis = deadlineEpochMillis;
    }

    public long getSequence() {
        return sequence;
    }

    public void setSequence(long sequence) {
        this.sequence = sequence;
    }

    public long getCreatedAtEpochMillis() {
        return createdAtEpochMillis;
    }

    public void setCreatedAtEpochMillis(long createdAtEpochMillis) {
        this.createdAtEpochMillis = createdAtEpochMillis;
    }
}
