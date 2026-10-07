SET @user_coupon_query_index_exists = (
    SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'user_coupon'
      AND index_name = 'idx_user_coupon_user_status_expire'
);
SET @user_coupon_query_index_sql = IF(
    @user_coupon_query_index_exists = 0,
    'ALTER TABLE user_coupon ADD KEY idx_user_coupon_user_status_expire (user_id, status, expire_at, id)',
    'SELECT 1'
);
PREPARE user_coupon_query_index_statement FROM @user_coupon_query_index_sql;
EXECUTE user_coupon_query_index_statement;
DEALLOCATE PREPARE user_coupon_query_index_statement;
