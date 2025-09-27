ALTER TABLE product
    ADD CONSTRAINT uq_product_name UNIQUE (name);