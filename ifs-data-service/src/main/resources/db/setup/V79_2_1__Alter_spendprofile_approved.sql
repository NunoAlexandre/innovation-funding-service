ALTER TABLE `spend_profile`
ADD COLUMN `approval` ENUM('UNSET', 'APPROVED', 'REJECTED') NULL DEFAULT 'UNSET' AFTER `marked_as_complete`;

