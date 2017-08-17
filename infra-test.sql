
USE `promotion`;

CREATE TABLE `promotion`.`infra_test_user` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'PK for infra_test_user table',
  `nm` VARCHAR(255) NULL COMMENT 'name of user',
  PRIMARY KEY (`id`),
  INDEX `infra_test_user_x01` (`nm` ASC)
)