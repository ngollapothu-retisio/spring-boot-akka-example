
DROP TABLE IF EXISTS BRAND;

CREATE TABLE IF NOT EXISTS BRAND
(
    ID VARCHAR(255) NOT NULL,
    NAME TEXT NOT NULL,
    ACTIVE BOOLEAN DEFAULT FALSE,
    LOGO VARCHAR(500) DEFAULT NULL,
    CREATED_TMST TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    LAST_MODIFIED_TMST TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY(ID)
);