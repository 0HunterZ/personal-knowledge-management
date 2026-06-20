-- =========================================
-- SCRIPT TẠO BẢNG LỊCH SỬ CHUYỂN FILE LAN
-- =========================================

CREATE TABLE dbo.LanTransferHistory (
    TransferId NVARCHAR(36) PRIMARY KEY,
    UserId INT NOT NULL,
    FileName NVARCHAR(255) NOT NULL,
    TargetName NVARCHAR(255) NOT NULL,
    SizeBytes BIGINT NOT NULL,
    Status NVARCHAR(50) NOT NULL,
    CreatedAt DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (UserId) REFERENCES dbo.Users(UserId) ON DELETE CASCADE
);
