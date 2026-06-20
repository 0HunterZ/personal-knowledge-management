package com.focusnode.repository;

import com.focusnode.model.Note;
import com.focusnode.model.FileResource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NoteRepository {

    public List<Note> findAll() {
        List<Note> notes = new ArrayList<>();
        String sql = "SELECT n.NoteId, n.UserId, n.SubjectId, n.FolderId, n.Title, n.Content, n.CreatedAt, n.UpdatedAt, n.IsDeleted, " +
                     "s.Name AS SubjectName, " +
                     "(SELECT STRING_AGG(t.Name, ',') FROM dbo.NoteTags nt JOIN dbo.Tags t ON nt.TagId = t.TagId WHERE nt.NoteId = n.NoteId) AS TagsList " +
                     "FROM dbo.Notes n " +
                     "LEFT JOIN dbo.Subjects s ON n.SubjectId = s.SubjectId " +
                     "WHERE (n.IsDeleted = 0 OR n.IsDeleted IS NULL)";
                     
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
             
            while (rs.next()) {
                String tagsStr = rs.getString("TagsList");
                List<String> tags = (tagsStr == null || tagsStr.trim().isEmpty()) ? List.of() : Arrays.asList(tagsStr.split(","));
                
                Integer subjectId = rs.getObject("SubjectId") != null ? rs.getInt("SubjectId") : null;
                Integer folderId = rs.getObject("FolderId") != null ? rs.getInt("FolderId") : null;
                String subjectName = rs.getString("SubjectName") != null ? rs.getString("SubjectName") : "";
                
                Note newNote = new Note(
                    rs.getInt("NoteId"),
                    rs.getInt("UserId"),
                    subjectId,
                    folderId,
                    rs.getString("Title"),
                    rs.getString("Content"),
                    subjectName,
                    tags,
                    rs.getTimestamp("CreatedAt") != null ? rs.getTimestamp("CreatedAt").toLocalDateTime() : null,
                    rs.getTimestamp("UpdatedAt") != null ? rs.getTimestamp("UpdatedAt").toLocalDateTime() : null,
                    rs.getBoolean("IsDeleted")
                );
                
                // Fetch attached files for this note
                try (PreparedStatement fps = conn.prepareStatement(
                        "SELECT f.* FROM dbo.FileResources f JOIN dbo.NoteFiles nf ON f.FileId = nf.FileId WHERE nf.NoteId = ? AND (f.IsDeleted = 0 OR f.IsDeleted IS NULL)")) {
                    fps.setInt(1, newNote.getId());
                    try (ResultSet frs = fps.executeQuery()) {
                        while (frs.next()) {
                            newNote.getAttachedFiles().add(new FileResource(
                                    frs.getInt("FileId"),
                                    frs.getInt("UserId"),
                                    frs.getObject("FolderId") != null ? frs.getInt("FolderId") : null,
                                    frs.getString("FileName"),
                                    frs.getString("FilePath"),
                                    frs.getInt("FileTypeId"),
                                    frs.getLong("SizeBytes"),
                                    frs.getTimestamp("UploadedAt").toLocalDateTime(),
                                    frs.getBoolean("IsDeleted")
                            ));
                        }
                    }
                }
                
                notes.add(newNote);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notes;
    }

    public List<Note> getNotesByUserIdAndFolder(int userId, Integer folderId) {
        List<Note> notes = new ArrayList<>();
        String sql = "SELECT n.NoteId, n.UserId, n.SubjectId, n.FolderId, n.Title, n.Content, n.CreatedAt, n.UpdatedAt, n.IsDeleted, " +
                     "s.Name AS SubjectName, " +
                     "(SELECT STRING_AGG(t.Name, ',') FROM dbo.NoteTags nt JOIN dbo.Tags t ON nt.TagId = t.TagId WHERE nt.NoteId = n.NoteId) AS TagsList " +
                     "FROM dbo.Notes n " +
                     "LEFT JOIN dbo.Subjects s ON n.SubjectId = s.SubjectId " +
                     "WHERE (n.IsDeleted = 0 OR n.IsDeleted IS NULL) AND n.UserId = ? AND n.FolderId " + (folderId == null ? "IS NULL" : "= ?") + " ORDER BY n.Title ASC";
                     
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setInt(1, userId);
            if (folderId != null) {
                stmt.setInt(2, folderId);
            }
             
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String tagsStr = rs.getString("TagsList");
                    List<String> tags = (tagsStr == null || tagsStr.trim().isEmpty()) ? List.of() : Arrays.asList(tagsStr.split(","));
                    
                    Integer subjectId = rs.getObject("SubjectId") != null ? rs.getInt("SubjectId") : null;
                    Integer fId = rs.getObject("FolderId") != null ? rs.getInt("FolderId") : null;
                    String subjectName = rs.getString("SubjectName") != null ? rs.getString("SubjectName") : "";
                    
                    Note newNote = new Note(
                        rs.getInt("NoteId"),
                        rs.getInt("UserId"),
                        subjectId,
                        fId,
                        rs.getString("Title"),
                        rs.getString("Content"),
                        subjectName,
                        tags,
                        rs.getTimestamp("CreatedAt") != null ? rs.getTimestamp("CreatedAt").toLocalDateTime() : null,
                        rs.getTimestamp("UpdatedAt") != null ? rs.getTimestamp("UpdatedAt").toLocalDateTime() : null,
                        rs.getBoolean("IsDeleted")
                    );
                    
                    // Fetch attached files for this note
                    try (PreparedStatement fps = conn.prepareStatement(
                            "SELECT f.* FROM dbo.FileResources f JOIN dbo.NoteFiles nf ON f.FileId = nf.FileId WHERE nf.NoteId = ? AND (f.IsDeleted = 0 OR f.IsDeleted IS NULL)")) {
                        fps.setInt(1, newNote.getId());
                        try (ResultSet frs = fps.executeQuery()) {
                            while (frs.next()) {
                                newNote.getAttachedFiles().add(new FileResource(
                                        frs.getInt("FileId"),
                                        frs.getInt("UserId"),
                                        frs.getObject("FolderId") != null ? frs.getInt("FolderId") : null,
                                        frs.getString("FileName"),
                                        frs.getString("FilePath"),
                                        frs.getInt("FileTypeId"),
                                        frs.getLong("SizeBytes"),
                                        frs.getTimestamp("UploadedAt").toLocalDateTime(),
                                        frs.getBoolean("IsDeleted")
                                ));
                            }
                        }
                    }
                    notes.add(newNote);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notes;
    }

    public List<Note> getRecentNotesByUserId(int userId, int limit) {
        List<Note> notes = new ArrayList<>();
        String sql = "SELECT TOP (?) n.NoteId, n.UserId, n.SubjectId, n.FolderId, n.Title, n.Content, n.CreatedAt, n.UpdatedAt, n.IsDeleted, " +
                     "s.Name AS SubjectName, " +
                     "(SELECT STRING_AGG(t.Name, ',') FROM dbo.NoteTags nt JOIN dbo.Tags t ON nt.TagId = t.TagId WHERE nt.NoteId = n.NoteId) AS TagsList " +
                     "FROM dbo.Notes n " +
                     "LEFT JOIN dbo.Subjects s ON n.SubjectId = s.SubjectId " +
                     "WHERE (n.IsDeleted = 0 OR n.IsDeleted IS NULL) AND n.UserId = ? " +
                     "ORDER BY n.UpdatedAt DESC";
                     
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setInt(1, limit);
            stmt.setInt(2, userId);
             
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String tagsStr = rs.getString("TagsList");
                    List<String> tags = (tagsStr == null || tagsStr.trim().isEmpty()) ? List.of() : Arrays.asList(tagsStr.split(","));
                    
                    Integer subjectId = rs.getObject("SubjectId") != null ? rs.getInt("SubjectId") : null;
                    Integer fId = rs.getObject("FolderId") != null ? rs.getInt("FolderId") : null;
                    String subjectName = rs.getString("SubjectName") != null ? rs.getString("SubjectName") : "";
                    
                    Note newNote = new Note(
                        rs.getInt("NoteId"),
                        rs.getInt("UserId"),
                        subjectId,
                        fId,
                        rs.getString("Title"),
                        rs.getString("Content"),
                        subjectName,
                        tags,
                        rs.getTimestamp("CreatedAt") != null ? rs.getTimestamp("CreatedAt").toLocalDateTime() : null,
                        rs.getTimestamp("UpdatedAt") != null ? rs.getTimestamp("UpdatedAt").toLocalDateTime() : null,
                        rs.getBoolean("IsDeleted")
                    );
                    
                    // Fetch attached files for this note
                    try (PreparedStatement fps = conn.prepareStatement(
                            "SELECT f.* FROM dbo.FileResources f JOIN dbo.NoteFiles nf ON f.FileId = nf.FileId WHERE nf.NoteId = ? AND (f.IsDeleted = 0 OR f.IsDeleted IS NULL)")) {
                        fps.setInt(1, newNote.getId());
                        try (ResultSet frs = fps.executeQuery()) {
                            while (frs.next()) {
                                newNote.getAttachedFiles().add(new FileResource(
                                        frs.getInt("FileId"),
                                        frs.getInt("UserId"),
                                        frs.getObject("FolderId") != null ? frs.getInt("FolderId") : null,
                                        frs.getString("FileName"),
                                        frs.getString("FilePath"),
                                        frs.getInt("FileTypeId"),
                                        frs.getLong("SizeBytes"),
                                        frs.getTimestamp("UploadedAt").toLocalDateTime(),
                                        frs.getBoolean("IsDeleted")
                                ));
                            }
                        }
                    }
                    notes.add(newNote);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notes;
    }

    public List<Note> getDeletedNotesByUserId(int userId) {
        List<Note> notes = new ArrayList<>();
        String sql = "SELECT n.NoteId, n.UserId, n.SubjectId, n.FolderId, n.Title, n.Content, n.CreatedAt, n.UpdatedAt, n.IsDeleted, " +
                     "s.Name AS SubjectName, " +
                     "(SELECT STRING_AGG(t.Name, ',') FROM dbo.NoteTags nt JOIN dbo.Tags t ON nt.TagId = t.TagId WHERE nt.NoteId = n.NoteId) AS TagsList " +
                     "FROM dbo.Notes n " +
                     "LEFT JOIN dbo.Subjects s ON n.SubjectId = s.SubjectId " +
                     "WHERE n.IsDeleted = 1 AND n.UserId = ? ORDER BY n.Title ASC";
                     
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setInt(1, userId);
             
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String tagsStr = rs.getString("TagsList");
                    List<String> tags = (tagsStr == null || tagsStr.trim().isEmpty()) ? List.of() : Arrays.asList(tagsStr.split(","));
                    
                    Integer subjectId = rs.getObject("SubjectId") != null ? rs.getInt("SubjectId") : null;
                    Integer fId = rs.getObject("FolderId") != null ? rs.getInt("FolderId") : null;
                    String subjectName = rs.getString("SubjectName") != null ? rs.getString("SubjectName") : "";
                    
                    Note newNote = new Note(
                        rs.getInt("NoteId"),
                        rs.getInt("UserId"),
                        subjectId,
                        fId,
                        rs.getString("Title"),
                        rs.getString("Content"),
                        subjectName,
                        tags,
                        rs.getTimestamp("CreatedAt") != null ? rs.getTimestamp("CreatedAt").toLocalDateTime() : null,
                        rs.getTimestamp("UpdatedAt") != null ? rs.getTimestamp("UpdatedAt").toLocalDateTime() : null,
                        rs.getBoolean("IsDeleted")
                    );
                    
                    // Fetch attached files for this note
                    try (PreparedStatement fps = conn.prepareStatement(
                            "SELECT f.* FROM dbo.FileResources f JOIN dbo.NoteFiles nf ON f.FileId = nf.FileId WHERE nf.NoteId = ?")) {
                        fps.setInt(1, newNote.getId());
                        try (ResultSet frs = fps.executeQuery()) {
                            while (frs.next()) {
                                newNote.getAttachedFiles().add(new FileResource(
                                        frs.getInt("FileId"),
                                        frs.getInt("UserId"),
                                        frs.getObject("FolderId") != null ? frs.getInt("FolderId") : null,
                                        frs.getString("FileName"),
                                        frs.getString("FilePath"),
                                        frs.getInt("FileTypeId"),
                                        frs.getLong("SizeBytes"),
                                        frs.getTimestamp("UploadedAt").toLocalDateTime(),
                                        frs.getBoolean("IsDeleted")
                                ));
                            }
                        }
                    }
                    notes.add(newNote);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notes;
    }

    public void add(Note note) {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int userId = note.getUserId() > 0 ? note.getUserId() : 1; // Default to 1 if not set
                Integer subjectId = getOrCreateSubject(conn, userId, note.getSubjectName());
                
                String sql = "INSERT INTO dbo.Notes(UserId, SubjectId, FolderId, Title, Content, CreatedAt, UpdatedAt, IsDeleted) " +
                             "VALUES(?,?,?,?,?,?,?,?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setInt(1, userId);
                    if (subjectId != null) pstmt.setInt(2, subjectId); else pstmt.setNull(2, java.sql.Types.INTEGER);
                    if (note.getFolderId() != null) pstmt.setInt(3, note.getFolderId()); else pstmt.setNull(3, java.sql.Types.INTEGER);
                    pstmt.setString(4, note.getTitle());
                    pstmt.setString(5, note.getContent());
                    pstmt.setTimestamp(6, Timestamp.valueOf(note.getCreatedAt() != null ? note.getCreatedAt() : LocalDateTime.now()));
                    pstmt.setTimestamp(7, Timestamp.valueOf(note.getUpdatedAt() != null ? note.getUpdatedAt() : LocalDateTime.now()));
                    pstmt.setBoolean(8, false);
                    pstmt.executeUpdate();
                    
                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            note.setId(rs.getInt(1));
                        }
                    }
                }
                
                // Process Tags
                updateNoteTags(conn, note.getId(), userId, note.getTags());
                
                // Process Attached Files
                updateNoteFiles(conn, note.getId(), userId, note.getAttachedFiles());
                
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(Note note) {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int userId = note.getUserId() > 0 ? note.getUserId() : 1;
                Integer subjectId = getOrCreateSubject(conn, userId, note.getSubjectName());
                
                String sql = "UPDATE dbo.Notes SET SubjectId=?, FolderId=?, Title=?, Content=?, UpdatedAt=? WHERE NoteId=?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    if (subjectId != null) pstmt.setInt(1, subjectId); else pstmt.setNull(1, java.sql.Types.INTEGER);
                    if (note.getFolderId() != null) pstmt.setInt(2, note.getFolderId()); else pstmt.setNull(2, java.sql.Types.INTEGER);
                    pstmt.setString(3, note.getTitle());
                    pstmt.setString(4, note.getContent());
                    pstmt.setTimestamp(5, Timestamp.valueOf(note.getUpdatedAt() != null ? note.getUpdatedAt() : LocalDateTime.now()));
                    pstmt.setInt(6, note.getId());
                    pstmt.executeUpdate();
                }
                
                // Clear old tags
                try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM dbo.NoteTags WHERE NoteId=?")) {
                    pstmt.setInt(1, note.getId());
                    pstmt.executeUpdate();
                }
                
                // Add new tags
                updateNoteTags(conn, note.getId(), userId, note.getTags());
                
                // Process Attached Files
                updateNoteFiles(conn, note.getId(), userId, note.getAttachedFiles());
                
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        String sql = "UPDATE dbo.Notes SET IsDeleted = 1 WHERE NoteId=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void restore(int id) {
        String sql = "UPDATE dbo.Notes SET IsDeleted = 0 WHERE NoteId=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deletePermanently(int id) {
        String sql = "DELETE FROM dbo.Notes WHERE NoteId=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Helper Methods for 3NF Normalization handling
    
    private Integer getOrCreateSubject(Connection conn, int userId, String subjectName) throws SQLException {
        if (subjectName == null || subjectName.trim().isEmpty()) return null;
        subjectName = subjectName.trim();
        
        // Try to find it
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT SubjectId FROM dbo.Subjects WHERE Name = ? AND UserId = ?")) {
            pstmt.setString(1, subjectName);
            pstmt.setInt(2, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        
        // Create it if not found
        try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO dbo.Subjects(UserId, Name) VALUES(?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, subjectName);
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return null;
    }
    
    private void updateNoteTags(Connection conn, int noteId, int userId, List<String> tagNames) throws SQLException {
        if (tagNames == null || tagNames.isEmpty()) return;
        
        for (String tagName : tagNames) {
            tagName = tagName.trim();
            if (tagName.startsWith("#")) tagName = tagName.substring(1); // remove # if exists
            if (tagName.isEmpty()) continue;
            
            int tagId = -1;
            // Find Tag
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT TagId FROM dbo.Tags WHERE Name = ? AND UserId = ?")) {
                pstmt.setString(1, tagName);
                pstmt.setInt(2, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) tagId = rs.getInt(1);
                }
            }
            
            // Insert Tag if not exists
            if (tagId == -1) {
                try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO dbo.Tags(UserId, Name) VALUES(?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setInt(1, userId);
                    pstmt.setString(2, tagName);
                    pstmt.executeUpdate();
                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) tagId = rs.getInt(1);
                    }
                }
            }
            
            // Insert into NoteTags
            if (tagId != -1) {
                try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO dbo.NoteTags(NoteId, TagId) VALUES(?, ?)")) {
                    pstmt.setInt(1, noteId);
                    pstmt.setInt(2, tagId);
                    pstmt.executeUpdate();
                } catch (SQLException ignore) {
                    // Ignore duplicate key exception if note already has this tag
                }
            }
        }
    }
    
    private void updateNoteFiles(Connection conn, int noteId, int userId, List<FileResource> files) throws SQLException {
        if (files == null) return;
        
        // Remove existing links
        try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM dbo.NoteFiles WHERE NoteId=?")) {
            pstmt.setInt(1, noteId);
            pstmt.executeUpdate();
        }
        
        for (FileResource file : files) {
            int fileId = file.getFileId();
            if (fileId <= 0) {
                // Insert new file
                try (PreparedStatement pstmt = conn.prepareStatement(
                        "INSERT INTO dbo.FileResources(UserId, FileName, FilePath, FileTypeId, SizeBytes) VALUES(?, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setInt(1, userId);
                    pstmt.setString(2, file.getFileName());
                    pstmt.setString(3, file.getFilePath());
                    pstmt.setInt(4, 1); // Default to 1 (General File) for now
                    pstmt.setLong(5, file.getSizeBytes());
                    pstmt.executeUpdate();
                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            fileId = rs.getInt(1);
                            file.setFileId(fileId);
                        }
                    }
                }
            }
            
            // Insert into NoteFiles
            try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO dbo.NoteFiles(NoteId, FileId) VALUES(?, ?)")) {
                pstmt.setInt(1, noteId);
                pstmt.setInt(2, fileId);
                pstmt.executeUpdate();
            } catch (SQLException ignore) {
                // Ignore duplicates
            }
        }
    }
}
