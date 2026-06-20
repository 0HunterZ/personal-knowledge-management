-- =========================================
-- REVIEW ITEMS TABLE FOR SPACED REPETITION
-- =========================================

IF OBJECT_ID('dbo.review_items', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.review_items (
        id INT IDENTITY(1,1) PRIMARY KEY,
        note_id INT NULL,
        question NVARCHAR(500) NOT NULL,
        answer NVARCHAR(MAX) NOT NULL,
        difficulty NVARCHAR(20) NOT NULL DEFAULT 'Good',
        next_review_date DATE NOT NULL,
        repetitions INT NOT NULL DEFAULT 0,
        ease_factor FLOAT NOT NULL DEFAULT 2.5,
        interval INT NOT NULL DEFAULT 0,
        FOREIGN KEY (note_id) REFERENCES dbo.Notes(NoteId)
    );
END
