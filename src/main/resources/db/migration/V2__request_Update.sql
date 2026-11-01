ALTER TABLE friend_requests
    ADD COLUMN answer_time TIMESTAMPTZ;
ALTER TABLE match_requests
    ADD COLUMN answer_time TIMESTAMPTZ;
