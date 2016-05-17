-- Fix bug 1586501, assume no data, no FK issues
ALTER TABLE CM_CONTAINER_URL DROP CONSTRAINT CM_CONTAINER_URL_KEY;
ALTER TABLE CM_CONTAINER_URL DROP COLUMN CM_CONTAINER_URL_ID;
ALTER TABLE CM_CONTAINER_URL ADD (CM_CONTAINER_URL_ID NUMBER(10) NOT NULL);
ALTER TABLE CM_CONTAINER_URL ADD CONSTRAINT CM_CONTAINER_URL_KEY PRIMARY KEY (CM_CONTAINER_URL_ID);

