-- Competition Table update

ALTER TABLE `ifs`.`competition` 
CHANGE COLUMN `co_funders` `funder` VARCHAR(255) NULL DEFAULT NULL ,
CHANGE COLUMN `co_funders_budget` `funder_budget` DECIMAL(10,2) NULL DEFAULT NULL ;


-- Competition Co-Funders table.

CREATE TABLE `competition_co_funder` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `co_funder` varchar(255) DEFAULT NULL,
  `co_funder_budget` decimal(10,2) DEFAULT '0.00',
  `competition_id` bigint(20) NOT NULL,
  `created_timestamp` timestamp NULL DEFAULT NULL,
  `updated_timestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `competition_co_funders_compitions_id_idx` (`competition_id`),
  CONSTRAINT `competition_co_funders_compitions_id` FOREIGN KEY (`competition_id`) REFERENCES `competition` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=177 DEFAULT CHARSET=utf8;

