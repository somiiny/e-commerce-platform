CREATE TABLE product_option (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    color VARCHAR(10) NOT NULL,
    size VARCHAR(10) NOT NULL,
    stock INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME DEFAULT NULL
);

ALTER TABLE product_option
    ADD CONSTRAINT uq_product_option UNIQUE (product_id, color, size);