ALTER TABLE user_workout_logs
    MODIFY COLUMN activity_type ENUM (
        'INDOOR_RUNNING',
        'OTHER',
        'OUTDOOR_CYCLING',
        'CYCLING',
        'OUTDOOR_RUNNING',
        'OUTDOOR_WALKING'
    ) NULL;
