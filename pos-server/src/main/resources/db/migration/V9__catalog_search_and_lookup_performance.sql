ALTER TABLE product
    ADD COLUMN IF NOT EXISTS sku_normalized VARCHAR(80);

ALTER TABLE product
    ADD COLUMN IF NOT EXISTS name_normalized VARCHAR(160);

UPDATE product
SET sku_normalized = UPPER(TRIM(sku)),
    name_normalized = UPPER(TRIM(name))
WHERE sku_normalized IS NULL
   OR name_normalized IS NULL;

ALTER TABLE product
    ALTER COLUMN sku_normalized SET NOT NULL;

ALTER TABLE product
    ALTER COLUMN name_normalized SET NOT NULL;

ALTER TABLE product_barcode
    ADD COLUMN IF NOT EXISTS barcode_normalized VARCHAR(64);

UPDATE product_barcode
SET barcode_normalized = UPPER(TRIM(barcode))
WHERE barcode_normalized IS NULL;

ALTER TABLE product_barcode
    ALTER COLUMN barcode_normalized SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_product_search_merchant_active_sku
    ON product (merchant_id, is_active, sku_normalized, id);

CREATE INDEX IF NOT EXISTS idx_product_search_merchant_name
    ON product (merchant_id, name_normalized, id);

CREATE INDEX IF NOT EXISTS idx_product_barcode_lookup_normalized
    ON product_barcode (barcode_normalized);
