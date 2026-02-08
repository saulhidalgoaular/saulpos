-- Initial baseline table to verify Flyway is working
CREATE TABLE IF NOT EXISTS flyway_test_table (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
