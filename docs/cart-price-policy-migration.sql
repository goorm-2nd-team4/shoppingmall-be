-- Cart pricing policy migration
-- Date: 2026-04-27
-- Target DB: PostgreSQL
--
-- Change summary
-- 1. cart_items no longer stores product_price
-- 2. cart price is always calculated from products.product_price
--
-- Preconditions
-- - Deploy application code that no longer reads/writes cart_items.product_price first
-- - Then run this migration

BEGIN;

ALTER TABLE cart_items
DROP COLUMN IF EXISTS product_price;

COMMIT;
