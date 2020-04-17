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

CREATE TABLE IF NOT EXISTS SOA_ESPS.USER_PROFILES (
  ID SERIAL PRIMARY KEY,
  creation_time TIMESTAMP(6) without time zone NOT NULL DEFAULT now(),
  modification_time TIMESTAMP(6) without time zone,
  login VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS SOA_ESPS.DEVICES_INFO (
  ID SERIAL PRIMARY KEY,
  creation_time TIMESTAMP(6) without time zone NOT NULL DEFAULT now(),
  modification_time TIMESTAMP(6) without time zone,
  device_uuid uuid,
  device_type VARCHAR(40) NOT NULL,
  device_soft_model VARCHAR(100) NOT NULL,
  device_key_hash VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS SOA_ESPS.USER_DEVICES (
  user_profile_id INTEGER references USER_PROFILES(ID),
  device_id INTEGER references DEVICES_INFO(ID)
);

CREATE TABLE IF NOT EXISTS SOA_ESPS.USERS_INFO (
  ID SERIAL PRIMARY KEY,
  creation_time TIMESTAMP(6) without time zone NOT NULL DEFAULT now(),
  modification_time TIMESTAMP(6) without time zone,
  first_name VARCHAR(40) NOT NULL,
  middle_name VARCHAR(50) NOT NULL,
  last_name VARCHAR(70) NOT NULL,
  birthday TIMESTAMP(6) without time zone,
  email VARCHAR NOT NULL,
  telephone VARCHAR(35) NOT NULL,
  user_profile_id INTEGER references USER_PROFILES(ID)
);