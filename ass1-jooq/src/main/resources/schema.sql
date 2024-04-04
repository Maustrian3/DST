/*
 In the initialize phase this script will be called to delete and recreate your tables.

 Afterwards in the generate-sources phase jooq will scan your existing tables in your database and
 generate sources from it.
 */

-- This will delete the tables every time the sql script is called
DROP TABLE IF EXISTS preference, rider_preference;

-- Add here your sql statements to create the tables "preference" and "rider_preference"

CREATE TABLE RIDER_PREFERENCE
(
    rider_id      BIGINT PRIMARY KEY NOT NULL,
    vehicle_class VARCHAR,
    area          VARCHAR
);

CREATE TABLE preference
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    rider_id   BIGINT NOT NULL,
    pref_key   VARCHAR,
    pref_value VARCHAR,
    FOREIGN KEY (rider_id) REFERENCES rider_Preference (rider_id)
);
