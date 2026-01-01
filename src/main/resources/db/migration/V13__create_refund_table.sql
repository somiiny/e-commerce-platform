CREATE TABLE `refund` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    refund_type VARCHAR(30) NOT NULL,
    purchase_id BIGINT NOT NULL,
    payment_id BIGINT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    reason VARCHAR(255) NOT NULL,
    status VARCHAR(30) NOT NULL,
    requested_by BIGINT NOT NULL,
    approved_by BIGINT NULL,
    approved_at DATETIME NULL,
    refund_key VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP
);