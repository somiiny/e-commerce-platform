ALTER TABLE purchase_product
    DROP COLUMN deleted_at;

ALTER TABLE purchase_product
    ADD COLUMN refunded_quantity BIGINT NULL
    AFTER quantity;