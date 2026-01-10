ALTER TABLE purchase
    ADD COLUMN purchase_no VARCHAR(30) NOT NULL UNIQUE
        AFTER user_id;