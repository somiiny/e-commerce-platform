ALTER TABLE purchase
    ADD COLUMN receiver_name VARCHAR(30) NOT NULL,
    ADD COLUMN zip_code VARCHAR(10) NOT NULL,
    ADD COLUMN shipping_address VARCHAR(255) NOT NULL,
    ADD COLUMN shipping_detail_address VARCHAR(255) NULL,
    ADD COLUMN phone_number VARCHAR(30) NOT NULL;