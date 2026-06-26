package com.focusnode.model;

import java.time.LocalDateTime;

public class CrdtNote {
    private int noteId;
    private String content;
    private String clientId;
    private long logicalTimestamp;

    public CrdtNote() {}

    public CrdtNote(int noteId, String content, String clientId, long logicalTimestamp) {
        this.noteId = noteId;
        this.content = content;
        this.clientId = clientId;
        this.logicalTimestamp = logicalTimestamp;
    }

    public int getNoteId() {
        return noteId;
    }

    public void setNoteId(int noteId) {
        this.noteId = noteId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public long getLogicalTimestamp() {
        return logicalTimestamp;
    }

    public void setLogicalTimestamp(long logicalTimestamp) {
        this.logicalTimestamp = logicalTimestamp;
    }

    // Merge logic: Last Writer Wins based on Logical Timestamp, fallback to Client ID comparison
    public boolean merge(CrdtNote incoming) {
        if (incoming.getNoteId() != this.noteId) return false;

        if (incoming.getLogicalTimestamp() > this.logicalTimestamp) {
            apply(incoming);
            return true;
        } else if (incoming.getLogicalTimestamp() == this.logicalTimestamp) {
            // Tie-breaker
            if (incoming.getClientId() != null && this.clientId != null) {
                if (incoming.getClientId().compareTo(this.clientId) > 0) {
                    apply(incoming);
                    return true;
                }
            }
        }
        return false;
    }

    private void apply(CrdtNote incoming) {
        this.content = incoming.getContent();
        this.clientId = incoming.getClientId();
        this.logicalTimestamp = incoming.getLogicalTimestamp();
    }
}
