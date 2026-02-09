ALTER TABLE inventory_movement
    ALTER COLUMN sale_id DROP NOT NULL;

ALTER TABLE inventory_movement
    ALTER COLUMN sale_line_id DROP NOT NULL;

ALTER TABLE inventory_movement
    ADD COLUMN IF NOT EXISTS reference_type VARCHAR(40);

UPDATE inventory_movement
SET reference_type = 'SALE_RECEIPT'
WHERE reference_type IS NULL;

ALTER TABLE inventory_movement
    ALTER COLUMN reference_type SET NOT NULL;

ALTER TABLE inventory_movement DROP CONSTRAINT IF EXISTS ck_inventory_movement_type;
ALTER TABLE inventory_movement DROP CONSTRAINT IF EXISTS ck_inventory_movement_sale_quantity_negative;
ALTER TABLE inventory_movement DROP CONSTRAINT IF EXISTS ck_inventory_movement_reference_type;
ALTER TABLE inventory_movement DROP CONSTRAINT IF EXISTS ck_inventory_movement_reference_non_blank;
ALTER TABLE inventory_movement DROP CONSTRAINT IF EXISTS ck_inventory_movement_source_document;

ALTER TABLE inventory_movement
    ADD CONSTRAINT ck_inventory_movement_type
        CHECK (movement_type IN ('SALE', 'RETURN', 'ADJUSTMENT'));

ALTER TABLE inventory_movement
    ADD CONSTRAINT ck_inventory_movement_reference_type
        CHECK (reference_type IN ('SALE_RECEIPT', 'SALE_RETURN', 'STOCK_ADJUSTMENT'));

ALTER TABLE inventory_movement
    ADD CONSTRAINT ck_inventory_movement_reference_non_blank
        CHECK (length(trim(reference_number)) > 0);

ALTER TABLE inventory_movement
    ADD CONSTRAINT ck_inventory_movement_source_document
        CHECK (
            (movement_type = 'SALE'
                AND sale_id IS NOT NULL
                AND sale_line_id IS NOT NULL
                AND quantity_delta < 0
                AND reference_type = 'SALE_RECEIPT')
            OR
            (movement_type = 'RETURN'
                AND sale_id IS NULL
                AND sale_line_id IS NULL
                AND quantity_delta > 0
                AND reference_type = 'SALE_RETURN')
            OR
            (movement_type = 'ADJUSTMENT'
                AND sale_id IS NULL
                AND sale_line_id IS NULL
                AND reference_type = 'STOCK_ADJUSTMENT')
        );

CREATE INDEX IF NOT EXISTS idx_inventory_movement_reference
    ON inventory_movement (reference_type, reference_number, created_at, id);

CREATE OR REPLACE VIEW stock_balance AS
SELECT store_location_id,
       product_id,
       SUM(quantity_delta) AS quantity_on_hand
FROM inventory_movement
GROUP BY store_location_id, product_id;
