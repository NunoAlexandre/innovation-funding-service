ALTER TABLE `address`
ADD COLUMN `address_line3` VARCHAR(255) NULL DEFAULT NULL COMMENT '' AFTER `address_line2`;
