-- 외래키 없이 테이블 생성
-- User 테이블
CREATE TABLE IF NOT EXISTS `users` (
                                       `id` INT AUTO_INCREMENT PRIMARY KEY,
                                       `login_id` VARCHAR(100) UNIQUE,
    `username` VARCHAR(100) NOT NULL,
    `email` VARCHAR(255) NOT NULL UNIQUE,
    `password` VARCHAR(255),
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at` TIMESTAMP,
    `auth_provider` VARCHAR(50) NOT NULL DEFAULT 'local',
    `provider_id` varchar(1024),
    `profile_image_url` VARCHAR(1024)
    );

-- File 테이블
CREATE TABLE IF NOT EXISTS `file` (
                                      `id` INT AUTO_INCREMENT PRIMARY KEY,
                                      `url` VARCHAR(1024) NOT NULL,
    `stored_name` VARCHAR(255) NOT NULL,
    `original_name` VARCHAR(255) NOT NULL,
    `file_type` VARCHAR(100) NOT NULL,
    `size` INT NOT NULL,
    `usage_type` VARCHAR(50) NOT NULL,
    `uploaded_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted_at` TIMESTAMP,
    `comment_id` INT NULL,
    `issue_id` INT NULL
    );

-- Milestone 테이블
CREATE TABLE IF NOT EXISTS `milestone` (
                                           `id` INT AUTO_INCREMENT PRIMARY KEY,
                                           `title` VARCHAR(500) NOT NULL UNIQUE,
    `description` TEXT,
    `due_date` DATE,
    `state` VARCHAR(10) NOT NULL DEFAULT 'OPEN',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at` TIMESTAMP
    );

-- Label 테이블
CREATE TABLE IF NOT EXISTS `label` (
                                       `id` INT AUTO_INCREMENT PRIMARY KEY,
                                       `name` VARCHAR(100) NOT NULL UNIQUE,
    `description` TEXT,
    `color` CHAR(7) NOT NULL DEFAULT '#0075ca',
    `text_color` VARCHAR(10) NOT NULL DEFAULT 'BLACK',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at` TIMESTAMP
    );

-- Issue 테이블
CREATE TABLE IF NOT EXISTS `issue` (
                                       `id` INT AUTO_INCREMENT PRIMARY KEY,
                                       `title` VARCHAR(500) NOT NULL,
    `content` TEXT,
    `state` VARCHAR(10) NOT NULL DEFAULT 'OPEN',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at` TIMESTAMP,
    `closed_at` TIMESTAMP,
    `writer_id` INT NOT NULL,
    `milestone_id` INT
    );

-- Comment 테이블
CREATE TABLE IF NOT EXISTS `comment` (
                                         `id` INT AUTO_INCREMENT PRIMARY KEY,
                                         `content` TEXT NOT NULL,
                                         `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                         `deleted_at` TIMESTAMP,
                                         `writer_id` INT NOT NULL,
                                         `issue_id` INT NOT NULL
);

-- IssueAssignee 테이블
CREATE TABLE IF NOT EXISTS `issue_assignee` (
                                                `issue_id` INT NOT NULL,
                                                `user_id` INT NOT NULL,
                                                PRIMARY KEY (`issue_id`, `user_id`)
    );

-- IssueLabel 테이블
CREATE TABLE IF NOT EXISTS `issue_label` (
                                             `issue_id` INT NOT NULL,
                                             `label_id` INT NOT NULL,
                                             PRIMARY KEY (`issue_id`, `label_id`)
    );

-- 외래키 제약조건 추가
ALTER TABLE `file`
    ADD CONSTRAINT `fk_file_issue`
        FOREIGN KEY (`issue_id`) REFERENCES `issue`(`id`),
    ADD CONSTRAINT `fk_file_comment`
        FOREIGN KEY (`comment_id`) REFERENCES `comment`(`id`),
    ADD CONSTRAINT `chk_file_reference`
        CHECK ((issue_id IS NULL AND comment_id IS NOT NULL) OR
              (issue_id IS NOT NULL AND comment_id IS NULL) OR
              (issue_id IS NULL AND comment_id IS NULL));

ALTER TABLE `issue`
    ADD CONSTRAINT `fk_issue_writer`
        FOREIGN KEY (`writer_id`) REFERENCES `users`(`id`),
    ADD CONSTRAINT `fk_issue_milestone`
        FOREIGN KEY (`milestone_id`) REFERENCES `milestone`(`id`);

ALTER TABLE `comment`
    ADD CONSTRAINT `fk_comment_writer`
        FOREIGN KEY (`writer_id`) REFERENCES `users`(`id`),
    ADD CONSTRAINT `fk_comment_issue`
        FOREIGN KEY (`issue_id`) REFERENCES `issue`(`id`);

ALTER TABLE `issue_assignee`
    ADD CONSTRAINT `fk_issue_assignee_issue`
        FOREIGN KEY (`issue_id`) REFERENCES `issue`(`id`),
    ADD CONSTRAINT `fk_issue_assignee_user`
        FOREIGN KEY (`user_id`) REFERENCES `users`(`id`);

ALTER TABLE `issue_label`
    ADD CONSTRAINT `fk_issue_label_issue`
        FOREIGN KEY (`issue_id`) REFERENCES `issue`(`id`),
    ADD CONSTRAINT `fk_issue_label_label`
        FOREIGN KEY (`label_id`) REFERENCES `label`(`id`);

CREATE TABLE IF NOT EXISTS `refresh_token` (
      `id` INTEGER AUTO_INCREMENT PRIMARY KEY,
      `user_id` INT UNIQUE,
      `token` VARCHAR(1024) NOT NULL
);
