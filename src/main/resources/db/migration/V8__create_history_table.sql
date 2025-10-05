CREATE TABLE `history` (
     id BIGINT AUTO_INCREMENT PRIMARY KEY,
     history_type VARCHAR(50) NOT NULL,
     purchase_id BIGINT NOT NULL,
     payment_id BIGINT NULL,
     old_status VARCHAR(50) NOT NULL,
     new_status VARCHAR(50) NOT NULL,
     description TEXT NULL,
     created_by BIGINT NOT NULL,
     created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);