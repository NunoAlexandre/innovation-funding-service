INSERT INTO role (`name`, url) VALUES('system_registrar', '');

ALTER TABLE `user` ADD COLUMN system_user TINYINT(1) DEFAULT 0;

INSERT INTO `user` (email, first_name, last_name, status, uid, system_user) VALUES('ifs_web_user@innovateuk.org', 'IFS Web', 'System User', 'ACTIVE', '8394d970-b250-4b15-9621-3534325691b4', 1);