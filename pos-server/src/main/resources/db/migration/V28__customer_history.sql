ALTER TABLE sale
    ADD COLUMN customer_id BIGINT;

ALTER TABLE sale
    ADD CONSTRAINT fk_sale_customer
        FOREIGN KEY (customer_id) REFERENCES customer (id) ON DELETE RESTRICT;

CREATE INDEX IF NOT EXISTS idx_sale_customer_created
    ON sale (customer_id, created_at, id);
