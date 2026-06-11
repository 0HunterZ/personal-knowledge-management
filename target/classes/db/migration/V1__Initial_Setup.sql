-- =========================================
-- SCRIPT TẠO CẤU TRÚC DATABASE FOCUS-NODE
-- HỆ QUẢN TRỊ: SQL SERVER
-- CHUẨN: 3NF, SỬ DỤNG INT IDENTITY(1,1), SCHEMA: dbo
-- =========================================

-- =========================================
-- 1. XÓA BẢNG CŨ NẾU TỒN TẠI (ĐỂ REBUILD)
-- BẢNG PHỤ THUỘC (CON) XÓA TRƯỚC, BẢNG ĐỘC LẬP (CHA) XÓA SAU
-- =========================================
IF OBJECT_ID('dbo.vw_DailyFocusTime', 'V') IS NOT NULL DROP VIEW dbo.vw_DailyFocusTime;
IF OBJECT_ID('dbo.vw_TaskCompletionStats', 'V') IS NOT NULL DROP VIEW dbo.vw_TaskCompletionStats;

IF OBJECT_ID('dbo.NoteFiles', 'U') IS NOT NULL DROP TABLE dbo.NoteFiles;
IF OBJECT_ID('dbo.FileResources', 'U') IS NOT NULL DROP TABLE dbo.FileResources;
IF OBJECT_ID('dbo.FileTypes', 'U') IS NOT NULL DROP TABLE dbo.FileTypes;
IF OBJECT_ID('dbo.FocusSessions', 'U') IS NOT NULL DROP TABLE dbo.FocusSessions;
IF OBJECT_ID('dbo.PomodoroPresets', 'U') IS NOT NULL DROP TABLE dbo.PomodoroPresets;
IF OBJECT_ID('dbo.TaskTags', 'U') IS NOT NULL DROP TABLE dbo.TaskTags;
IF OBJECT_ID('dbo.TaskNotes', 'U') IS NOT NULL DROP TABLE dbo.TaskNotes;
IF OBJECT_ID('dbo.Tasks', 'U') IS NOT NULL DROP TABLE dbo.Tasks;
IF OBJECT_ID('dbo.TaskStatuses', 'U') IS NOT NULL DROP TABLE dbo.TaskStatuses;
IF OBJECT_ID('dbo.TaskPriorities', 'U') IS NOT NULL DROP TABLE dbo.TaskPriorities;
IF OBJECT_ID('dbo.NoteTags', 'U') IS NOT NULL DROP TABLE dbo.NoteTags;
IF OBJECT_ID('dbo.QuickNotes', 'U') IS NOT NULL DROP TABLE dbo.QuickNotes;
IF OBJECT_ID('dbo.Notes', 'U') IS NOT NULL DROP TABLE dbo.Notes;
IF OBJECT_ID('dbo.Folders', 'U') IS NOT NULL DROP TABLE dbo.Folders;
IF OBJECT_ID('dbo.Tags', 'U') IS NOT NULL DROP TABLE dbo.Tags;
IF OBJECT_ID('dbo.Subjects', 'U') IS NOT NULL DROP TABLE dbo.Subjects;
IF OBJECT_ID('dbo.UserSettings', 'U') IS NOT NULL DROP TABLE dbo.UserSettings;
IF OBJECT_ID('dbo.Users', 'U') IS NOT NULL DROP TABLE dbo.Users;

-- =========================================
-- 2. TẠO BẢNG CHÍNH VÀ LOOKUP TABLES
-- =========================================

-- 2.1 USERS
CREATE TABLE dbo.Users (
    UserId INT IDENTITY(1,1) PRIMARY KEY,
    Username NVARCHAR(50) NOT NULL UNIQUE,
    Email NVARCHAR(100) NOT NULL UNIQUE,
    PasswordHash NVARCHAR(255) NOT NULL,
    CreatedAt DATETIME2 DEFAULT GETDATE(),
    IsDeleted BIT DEFAULT 0
);

-- 2.2 USER SETTINGS
CREATE TABLE dbo.UserSettings (
    UserId INT PRIMARY KEY,
    Theme NVARCHAR(20) DEFAULT 'LIGHT',
    DailyFocusGoalMinutes INT DEFAULT 120,
    Language NVARCHAR(10) DEFAULT 'VI',
    FOREIGN KEY (UserId) REFERENCES dbo.Users(UserId) ON DELETE CASCADE
);

-- 2.3 SUBJECTS (MÔN HỌC)
CREATE TABLE dbo.Subjects (
    SubjectId INT IDENTITY(1,1) PRIMARY KEY,
    UserId INT NOT NULL,
    Name NVARCHAR(100) NOT NULL,
    ColorHex NVARCHAR(7) DEFAULT '#3B82F6',
    CreatedAt DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (UserId) REFERENCES dbo.Users(UserId) ON DELETE CASCADE
);

-- 2.4 TAGS
CREATE TABLE dbo.Tags (
    TagId INT IDENTITY(1,1) PRIMARY KEY,
    UserId INT NOT NULL,
    Name NVARCHAR(50) NOT NULL,
    ColorHex NVARCHAR(7) DEFAULT '#10B981',
    CreatedAt DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (UserId) REFERENCES dbo.Users(UserId) ON DELETE CASCADE
);

-- 2.5 FOLDERS
CREATE TABLE dbo.Folders (
    FolderId INT IDENTITY(1,1) PRIMARY KEY,
    UserId INT NOT NULL,
    Name NVARCHAR(100) NOT NULL,
    ParentId INT NULL,
    CreatedAt DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (UserId) REFERENCES dbo.Users(UserId) ON DELETE CASCADE,
    FOREIGN KEY (ParentId) REFERENCES dbo.Folders(FolderId)
);

-- 2.5 TASK STATUSES (LOOKUP)
CREATE TABLE dbo.TaskStatuses (
    StatusId INT PRIMARY KEY,
    StatusName NVARCHAR(50) NOT NULL
);

-- 2.6 TASK PRIORITIES (LOOKUP)
CREATE TABLE dbo.TaskPriorities (
    PriorityId INT PRIMARY KEY,
    PriorityName NVARCHAR(50) NOT NULL
);

-- 2.7 FILE TYPES (LOOKUP)
CREATE TABLE dbo.FileTypes (
    FileTypeId INT PRIMARY KEY,
    Extension NVARCHAR(10) NOT NULL,
    Description NVARCHAR(100)
);

-- =========================================
-- 3. TẠO BẢNG NGHIỆP VỤ CHÍNH
-- =========================================

-- 3.1 NOTES (GHI CHÚ)
CREATE TABLE dbo.Notes (
    NoteId INT IDENTITY(1,1) PRIMARY KEY,
    UserId INT NOT NULL,
    SubjectId INT NULL,
    FolderId INT NULL,
    Title NVARCHAR(200) NOT NULL,
    Content NVARCHAR(MAX) NULL,
    CreatedAt DATETIME2 DEFAULT GETDATE(),
    UpdatedAt DATETIME2 DEFAULT GETDATE(),
    IsDeleted BIT DEFAULT 0,
    FOREIGN KEY (UserId) REFERENCES dbo.Users(UserId),
    -- SubjectId set NULL nếu môn học bị xóa (không dùng cascade delete trên subject)
    FOREIGN KEY (SubjectId) REFERENCES dbo.Subjects(SubjectId) ON DELETE SET NULL,
    FOREIGN KEY (FolderId) REFERENCES dbo.Folders(FolderId)
);

-- 3.2 QUICK NOTES (GHI CHÚ NHANH)
CREATE TABLE dbo.QuickNotes (
    QuickNoteId INT IDENTITY(1,1) PRIMARY KEY,
    UserId INT NOT NULL,
    Content NVARCHAR(MAX) NOT NULL,
    CreatedAt DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (UserId) REFERENCES dbo.Users(UserId) ON DELETE CASCADE
);

-- 3.3 TASKS (NHIỆM VỤ)
CREATE TABLE dbo.Tasks (
    TaskId INT IDENTITY(1,1) PRIMARY KEY,
    UserId INT NOT NULL,
    Title NVARCHAR(200) NOT NULL,
    Description NVARCHAR(MAX) NULL,
    DueDate DATETIME2 NULL,
    EstimatedMinutes INT DEFAULT 0,
    ActualMinutes INT DEFAULT 0,
    StatusId INT NOT NULL DEFAULT 1,
    PriorityId INT NOT NULL DEFAULT 2,
    CreatedAt DATETIME2 DEFAULT GETDATE(),
    IsDeleted BIT DEFAULT 0,
    FOREIGN KEY (UserId) REFERENCES dbo.Users(UserId),
    FOREIGN KEY (StatusId) REFERENCES dbo.TaskStatuses(StatusId),
    FOREIGN KEY (PriorityId) REFERENCES dbo.TaskPriorities(PriorityId)
);

-- 3.4 POMODORO PRESETS (CÀI ĐẶT POMODORO)
CREATE TABLE dbo.PomodoroPresets (
    PresetId INT IDENTITY(1,1) PRIMARY KEY,
    UserId INT NULL, -- NULL nghĩa là preset mặc định của hệ thống
    Name NVARCHAR(50) NOT NULL,
    FocusTimeMinutes INT DEFAULT 25,
    ShortBreakMinutes INT DEFAULT 5,
    LongBreakMinutes INT DEFAULT 15,
    LongBreakInterval INT DEFAULT 4,
    FOREIGN KEY (UserId) REFERENCES dbo.Users(UserId) ON DELETE CASCADE
);

-- 3.5 FOCUS SESSIONS (PHIÊN TẬP TRUNG)
CREATE TABLE dbo.FocusSessions (
    SessionId INT IDENTITY(1,1) PRIMARY KEY,
    UserId INT NOT NULL,
    TaskId INT NULL,
    NoteId INT NULL,
    PresetId INT NULL,
    StartedAt DATETIME2 NOT NULL,
    EndedAt DATETIME2 NULL,
    PlannedMinutes INT NOT NULL,
    ActualMinutes INT NOT NULL DEFAULT 0,
    IsCompleted BIT DEFAULT 0,
    FOREIGN KEY (UserId) REFERENCES dbo.Users(UserId) ON DELETE CASCADE,
    FOREIGN KEY (TaskId) REFERENCES dbo.Tasks(TaskId),
    FOREIGN KEY (NoteId) REFERENCES dbo.Notes(NoteId),
    FOREIGN KEY (PresetId) REFERENCES dbo.PomodoroPresets(PresetId)
);

-- 3.6 FILE RESOURCES (TÀI LIỆU/ẢNH ĐÍNH KÈM)
CREATE TABLE dbo.FileResources (
    FileId INT IDENTITY(1,1) PRIMARY KEY,
    UserId INT NOT NULL,
    FolderId INT NULL,
    FileName NVARCHAR(255) NOT NULL,
    FilePath NVARCHAR(500) NOT NULL,
    FileTypeId INT NOT NULL,
    SizeBytes BIGINT DEFAULT 0,
    UploadedAt DATETIME2 DEFAULT GETDATE(),
    IsDeleted BIT DEFAULT 0,
    FOREIGN KEY (UserId) REFERENCES dbo.Users(UserId) ON DELETE CASCADE,
    FOREIGN KEY (FileTypeId) REFERENCES dbo.FileTypes(FileTypeId),
    FOREIGN KEY (FolderId) REFERENCES dbo.Folders(FolderId)
);

-- =========================================
-- 4. TẠO BẢNG TRUNG GIAN (N-N RELATIONSHIPS)
-- =========================================

-- 4.1 NOTE - TAGS
CREATE TABLE dbo.NoteTags (
    NoteId INT NOT NULL,
    TagId INT NOT NULL,
    PRIMARY KEY (NoteId, TagId),
    FOREIGN KEY (NoteId) REFERENCES dbo.Notes(NoteId) ON DELETE CASCADE,
    FOREIGN KEY (TagId) REFERENCES dbo.Tags(TagId) ON DELETE CASCADE
);

-- 4.2 TASK - NOTES
CREATE TABLE dbo.TaskNotes (
    TaskId INT NOT NULL,
    NoteId INT NOT NULL,
    PRIMARY KEY (TaskId, NoteId),
    FOREIGN KEY (TaskId) REFERENCES dbo.Tasks(TaskId) ON DELETE CASCADE,
    FOREIGN KEY (NoteId) REFERENCES dbo.Notes(NoteId) ON DELETE CASCADE
);

-- 4.3 TASK - TAGS
CREATE TABLE dbo.TaskTags (
    TaskId INT NOT NULL,
    TagId INT NOT NULL,
    PRIMARY KEY (TaskId, TagId),
    FOREIGN KEY (TaskId) REFERENCES dbo.Tasks(TaskId) ON DELETE CASCADE,
    FOREIGN KEY (TagId) REFERENCES dbo.Tags(TagId) ON DELETE CASCADE
);

-- 4.4 NOTE - FILES
CREATE TABLE dbo.NoteFiles (
    NoteId INT NOT NULL,
    FileId INT NOT NULL,
    PRIMARY KEY (NoteId, FileId),
    FOREIGN KEY (NoteId) REFERENCES dbo.Notes(NoteId) ON DELETE CASCADE,
    FOREIGN KEY (FileId) REFERENCES dbo.FileResources(FileId) ON DELETE CASCADE
);

-- =========================================
-- 5. TẠO VIEWS THỐNG KÊ (GIỮ CHUẨN 3NF)
-- =========================================
GO

CREATE VIEW dbo.vw_DailyFocusTime AS
SELECT 
    UserId,
    CAST(StartedAt AS DATE) AS StudyDate,
    SUM(ActualMinutes) AS TotalFocusMinutes,
    COUNT(SessionId) AS TotalSessions,
    SUM(CASE WHEN IsCompleted = 1 THEN 1 ELSE 0 END) AS CompletedSessions
FROM dbo.FocusSessions
GROUP BY UserId, CAST(StartedAt AS DATE);

GO

CREATE VIEW dbo.vw_TaskCompletionStats AS
SELECT 
    UserId,
    COUNT(TaskId) AS TotalTasks,
    SUM(CASE WHEN StatusId = 3 THEN 1 ELSE 0 END) AS CompletedTasks, -- Assuming Status 3 = Done
    SUM(EstimatedMinutes) AS TotalEstimatedMinutes,
    SUM(ActualMinutes) AS TotalActualMinutes
FROM dbo.Tasks
WHERE IsDeleted = 0
GROUP BY UserId;

GO
