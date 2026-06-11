-- =========================================
-- SCRIPT TẠO DỮ LIỆU MẪU (DUMMY DATA) 
-- CHO DATABASE FOCUS-NODE
-- CHẠY SAU KHI ĐÃ CHẠY SCRIPT SCHEMA
-- =========================================

-- Tắt kiểm tra khóa ngoại (nếu cần thiết) hoặc xóa sạch data trước
-- Tránh lỗi khi chạy nhiều lần
DELETE FROM dbo.NoteTags;
DELETE FROM dbo.TaskNotes;
DELETE FROM dbo.TaskTags;
DELETE FROM dbo.NoteFiles;
DELETE FROM dbo.FocusSessions;
DELETE FROM dbo.Tasks;
DELETE FROM dbo.QuickNotes;
DELETE FROM dbo.Notes;
DELETE FROM dbo.Tags;
DELETE FROM dbo.Subjects;
DELETE FROM dbo.PomodoroPresets;
DELETE FROM dbo.UserSettings;
DELETE FROM dbo.FileResources;
DELETE FROM dbo.Users;

DELETE FROM dbo.TaskStatuses;
DELETE FROM dbo.TaskPriorities;
DELETE FROM dbo.FileTypes;

-- Khôi phục IDENTITY về 1
DBCC CHECKIDENT ('dbo.Users', RESEED, 0);
DBCC CHECKIDENT ('dbo.Subjects', RESEED, 0);
DBCC CHECKIDENT ('dbo.Tags', RESEED, 0);
DBCC CHECKIDENT ('dbo.Notes', RESEED, 0);
DBCC CHECKIDENT ('dbo.QuickNotes', RESEED, 0);
DBCC CHECKIDENT ('dbo.Tasks', RESEED, 0);
DBCC CHECKIDENT ('dbo.PomodoroPresets', RESEED, 0);
DBCC CHECKIDENT ('dbo.FocusSessions', RESEED, 0);
DBCC CHECKIDENT ('dbo.FileResources', RESEED, 0);

-- =========================================
-- 1. SEED DỮ LIỆU LOOKUP TABLES
-- =========================================
INSERT INTO dbo.TaskStatuses (StatusId, StatusName) VALUES 
(1, 'To Do'),
(2, 'In Progress'),
(3, 'Done'),
(4, 'Archived');

INSERT INTO dbo.TaskPriorities (PriorityId, PriorityName) VALUES 
(1, 'Low'),
(2, 'Medium'),
(3, 'High'),
(4, 'Urgent');

INSERT INTO dbo.FileTypes (FileTypeId, Extension, Description) VALUES 
(1, 'pdf', 'PDF Document'),
(2, 'png', 'PNG Image'),
(3, 'jpg', 'JPEG Image'),
(4, 'docx', 'Word Document'),
(5, 'txt', 'Text File');

-- =========================================
-- 2. SEED USERS & SETTINGS
-- =========================================
INSERT INTO dbo.Users (Username, Email, PasswordHash, CreatedAt) VALUES 
('HunterZ', 'hunterz@student.edu.vn', 'hash_123456', DATEADD(DAY, -30, GETDATE())),
('JohnDoe', 'john@gmail.com', 'hash_abcdef', DATEADD(DAY, -15, GETDATE()));

INSERT INTO dbo.UserSettings (UserId, Theme, DailyFocusGoalMinutes, Language) VALUES 
(1, 'DARK', 180, 'VI'),
(2, 'LIGHT', 120, 'EN');

-- Cài đặt Pomodoro mặc định của hệ thống
INSERT INTO dbo.PomodoroPresets (UserId, Name, FocusTimeMinutes, ShortBreakMinutes, LongBreakMinutes, LongBreakInterval) VALUES 
(NULL, 'Classic 25/5', 25, 5, 15, 4),
(NULL, 'Deep Work 50/10', 50, 10, 20, 3),
(1, 'My Custom 45/5', 45, 5, 20, 4); -- Custom của HunterZ

-- =========================================
-- 3. SEED SUBJECTS & TAGS CHO HUNTERZ (USER 1)
-- =========================================
INSERT INTO dbo.Subjects (UserId, Name, ColorHex) VALUES 
(1, 'Java Programming', '#F87171'),
(1, 'Database Systems', '#FBBF24'),
(1, 'Computer Networks', '#34D399'),
(1, 'AI Basics', '#A78BFA');

INSERT INTO dbo.Tags (UserId, Name, ColorHex) VALUES 
(1, 'Exam', '#EF4444'),
(1, 'Homework', '#F59E0B'),
(1, 'Important', '#DC2626'),
(1, 'CodeSnippet', '#10B981'),
(1, 'Theory', '#3B82F6');

-- =========================================
-- 4. SEED NOTES (CÁC GHI CHÚ ĐỒ ÁN)
-- =========================================
INSERT INTO dbo.Notes (UserId, SubjectId, Title, Content, CreatedAt) VALUES 
(1, 1, 'Java Socket Basics', 'TCP vs UDP. TCP guarantees delivery, UDP does not. Use ServerSocket and Socket for TCP.', DATEADD(DAY, -20, GETDATE())),
(1, 1, 'Multithreading in Java', 'Runnable vs Thread class. ExecutorService is preferred for thread pooling. synchronized keyword avoids race conditions.', DATEADD(DAY, -18, GETDATE())),
(1, 2, 'SQL Normalization 3NF', '1NF: Atomic values. 2NF: No partial dependency. 3NF: No transitive dependency. Essential for clean database design.', DATEADD(DAY, -15, GETDATE())),
(1, 3, 'OSI Model Layers', '7 Layers: Physical, Data Link, Network, Transport, Session, Presentation, Application.', DATEADD(DAY, -10, GETDATE())),
(1, 4, 'Intro to Neural Networks', 'Nodes, Weights, Biases. Activation functions: ReLU, Sigmoid, Tanh. Forward propagation & Backpropagation.', DATEADD(DAY, -5, GETDATE())),
(1, 1, 'JavaFX MVC Architecture', 'Model-View-Controller. FXML for UI, Controller for logic. Use fx:id to link FXML with Controller fields.', DATEADD(DAY, -2, GETDATE()));

-- =========================================
-- 5. SEED TASKS 
-- =========================================
INSERT INTO dbo.Tasks (UserId, Title, Description, DueDate, EstimatedMinutes, ActualMinutes, StatusId, PriorityId, CreatedAt) VALUES 
(1, 'Build LAN Chat Module', 'Write Server and Client classes using Java Sockets', DATEADD(DAY, 2, GETDATE()), 120, 45, 2, 3, DATEADD(DAY, -5, GETDATE())),
(1, 'Design SQL Server Schema', 'Apply 3NF for Focus-Node database', DATEADD(DAY, -1, GETDATE()), 60, 60, 3, 4, DATEADD(DAY, -3, GETDATE())),
(1, 'Read Chapter 4 on Networking', 'Read TCP/UDP differences', DATEADD(DAY, 1, GETDATE()), 45, 0, 1, 2, DATEADD(DAY, -1, GETDATE())),
(1, 'Create NoteCard FXML', 'Design the card UI for recent notes list', DATEADD(DAY, 0, GETDATE()), 30, 35, 3, 2, DATEADD(DAY, -1, GETDATE())),
(1, 'Prepare for Midterm Exam', 'Review Java multi-threading and SQL queries', DATEADD(DAY, 5, GETDATE()), 180, 0, 1, 4, GETDATE());

-- =========================================
-- 6. MAPPING N-N (GẮN TAG, LINK NOTE VÀ TASK)
-- =========================================
-- NoteTags: (NoteId, TagId)
INSERT INTO dbo.NoteTags (NoteId, TagId) VALUES 
(1, 4), (1, 5),   -- Java Socket: CodeSnippet, Theory
(2, 3), (2, 4),   -- Multithreading: Important, CodeSnippet
(3, 1), (3, 5),   -- SQL Normalization: Exam, Theory
(6, 4);           -- JavaFX: CodeSnippet

-- TaskTags: (TaskId, TagId)
INSERT INTO dbo.TaskTags (TaskId, TagId) VALUES 
(1, 2), (1, 3),   -- Build LAN: Homework, Important
(2, 2),           -- Design Schema: Homework
(5, 1), (5, 3);   -- Midterm: Exam, Important

-- TaskNotes: (TaskId, NoteId) - Liên kết Task với Note để tham khảo khi làm
INSERT INTO dbo.TaskNotes (TaskId, NoteId) VALUES 
(1, 1), (1, 2),   -- Build LAN tham khảo Java Socket & Multithreading
(2, 3),           -- Design Schema tham khảo Normalization
(4, 6);           -- Create NoteCard tham khảo JavaFX MVC

-- =========================================
-- 7. SEED FOCUS SESSIONS (DỮ LIỆU ĐỂ THỐNG KÊ ANALYTICS)
-- =========================================
-- Giả lập học tập trong 7 ngày qua
INSERT INTO dbo.FocusSessions (UserId, TaskId, NoteId, PresetId, StartedAt, EndedAt, PlannedMinutes, ActualMinutes, IsCompleted) VALUES 
(1, 2, 3, 1, DATEADD(MINUTE, -120, DATEADD(DAY, -3, GETDATE())), DATEADD(MINUTE, -95, DATEADD(DAY, -3, GETDATE())), 25, 25, 1),
(1, 2, 3, 1, DATEADD(MINUTE, -90, DATEADD(DAY, -3, GETDATE())), DATEADD(MINUTE, -65, DATEADD(DAY, -3, GETDATE())), 25, 25, 1),
(1, NULL, 1, 2, DATEADD(MINUTE, -200, DATEADD(DAY, -2, GETDATE())), DATEADD(MINUTE, -150, DATEADD(DAY, -2, GETDATE())), 50, 50, 1),
(1, 1, 2, 3, DATEADD(MINUTE, -100, DATEADD(DAY, -1, GETDATE())), DATEADD(MINUTE, -55, DATEADD(DAY, -1, GETDATE())), 45, 45, 1),
(1, 4, 6, 1, DATEADD(MINUTE, -30, GETDATE()), DATEADD(MINUTE, -5, GETDATE()), 25, 25, 1),
-- Session bị bỏ dở
(1, 3, NULL, 1, DATEADD(MINUTE, -400, GETDATE()), DATEADD(MINUTE, -390, GETDATE()), 25, 10, 0);

-- =========================================
-- HOÀN TẤT SEED DỮ LIỆU
-- =========================================
PRINT 'TẠO SCHEMA VÀ SEED DỮ LIỆU MẪU THÀNH CÔNG!';
