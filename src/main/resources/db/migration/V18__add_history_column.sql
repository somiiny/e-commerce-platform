ALTER TABLE history
    ADD COLUMN creator_type VARCHAR(10) NOT NULL
        AFTER description;

ALTER TABLE history
    MODIFY old_status VARCHAR(50) NULL;