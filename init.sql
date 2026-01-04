-- 기본 설정
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

INSERT INTO user (user_id, user_name, user_email, user_password, user_role, use_yn, created_date)
VALUES ('admin', '관리자', 'admin@khshop.com', 'rhdxhd12', 'ADMIN', 'Y', NOW());