-- liquibase formatted sql

-- changeset starasov:1
CREATE TABLE notification_task (
    id SERIAL,
    chat_id BIGINT,
    send_message TEXT,
    send_time TIMESTAMP
)