CREATE TABLE IF NOT EXISTS SOA_ESPS.JOB_DESC (
  ID SERIAL PRIMARY KEY,
  START_TIME timestamp(6) without time zone NOT NULL,
  END_TIME timestamp(6) without time zone,
  ADDITIONAL_INFO VARCHAR(400) NOT NULL,
  SCHEDULER_TASK_ID INTEGER
);

CREATE TABLE IF NOT EXISTS SOA_ESPS.FAILED_EVENT (
  ID SERIAL PRIMARY KEY,
  EVENT_STATUS VARCHAR(400) NOT NULL,
  LAST_UPDATED timestamp(6) without time zone,
  JOB_DESC_ID INTEGER,
  FOREIGN KEY (JOB_DESC_ID) REFERENCES SOA_ESPS.JOB_DESC (ID)
);