ALTER TABLE project_finance MODIFY COLUMN `viability` ENUM('PENDING', 'APPROVED', 'NOT_APPLICABLE') NOT NULL DEFAULT 'PENDING';