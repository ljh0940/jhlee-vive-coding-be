-- Fix existing provider constraint issue
-- This migration handles the case where the constraint already exists but is incorrect

DO $$
BEGIN
    -- Drop the existing provider check constraint
    IF EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'users_provider_check'
    ) THEN
        ALTER TABLE users DROP CONSTRAINT users_provider_check;
        RAISE NOTICE 'Dropped existing users_provider_check constraint';
    END IF;

    -- Add the correct constraint
    ALTER TABLE users ADD CONSTRAINT users_provider_check CHECK (provider IN ('KAKAO', 'LOCAL'));
    RAISE NOTICE 'Added correct users_provider_check constraint';

    -- Drop the existing role check constraint if needed
    IF EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'users_role_check'
    ) THEN
        ALTER TABLE users DROP CONSTRAINT users_role_check;
        RAISE NOTICE 'Dropped existing users_role_check constraint';
    END IF;

    -- Add the correct role constraint
    ALTER TABLE users ADD CONSTRAINT users_role_check CHECK (role IN ('USER', 'ADMIN'));
    RAISE NOTICE 'Added correct users_role_check constraint';

EXCEPTION
    WHEN OTHERS THEN
        RAISE NOTICE 'Error occurred: %', SQLERRM;
        RAISE;
END $$;
