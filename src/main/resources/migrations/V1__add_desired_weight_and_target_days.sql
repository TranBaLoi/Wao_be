-- Migration: Add desired weight and target days to UserHealthProfile
-- Author: AI Assistant
-- Date: 2026-03-26
-- Description: Thêm trường cân nặng mong muốn, số ngày target, và calo hàng ngày

ALTER TABLE user_health_profiles
ADD COLUMN desired_weight_kg DOUBLE PRECISION,
ADD COLUMN target_days INT,
ADD COLUMN daily_calories DOUBLE PRECISION;

-- Thêm comment cho các column mới
COMMENT ON COLUMN user_health_profiles.desired_weight_kg IS 'Cân nặng mong muốn sau khi hoàn thành mục tiêu (kg)';
COMMENT ON COLUMN user_health_profiles.target_days IS 'Số ngày để đạt được mục tiêu (ngày)';
COMMENT ON COLUMN user_health_profiles.daily_calories IS 'Calo cần hấp thụ mỗi ngày = target_calories / target_days (kcal)';

