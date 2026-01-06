-- =============================================================================
-- UPDATE SCRIPT: Update existing users with BCrypt hashed passwords
-- Run this AFTER running ecommerce_schema.sql if users exist with plain text passwords
-- =============================================================================

-- BCrypt hash for 'password123': $2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.V4pVWBWVHxkd0L
-- BCrypt hash for 'securepass': $2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi

UPDATE app_user
SET password = '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.V4pVWBWVHxkd0L'
WHERE email_address = 'john.doe@example.com';

UPDATE app_user
SET password = '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi'
WHERE email_address = 'jane.smith@example.com';

-- Verify the updates
SELECT email_address, first_name, last_name,
       CASE WHEN password LIKE '$2a$%' THEN 'BCrypt (OK)' ELSE 'NOT BCrypt' END as password_type
FROM app_user;

