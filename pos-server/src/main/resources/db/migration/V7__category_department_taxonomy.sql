ALTER TABLE category
    ADD COLUMN IF NOT EXISTS parent_id BIGINT NULL;

ALTER TABLE category
    ADD CONSTRAINT fk_category_parent FOREIGN KEY (parent_id) REFERENCES category (id) ON DELETE SET NULL;

ALTER TABLE category
    ADD CONSTRAINT chk_category_parent_not_self CHECK (parent_id IS NULL OR parent_id <> id);

CREATE INDEX IF NOT EXISTS idx_category_parent ON category (parent_id);
CREATE INDEX IF NOT EXISTS idx_category_merchant_parent ON category (merchant_id, parent_id);
