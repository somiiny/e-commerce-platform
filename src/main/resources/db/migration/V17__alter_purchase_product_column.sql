ALTER TABLE purchase_product
DROP COLUMN options,
    ADD COLUMN product_option_id BIGINT NOT NULL AFTER product_id;