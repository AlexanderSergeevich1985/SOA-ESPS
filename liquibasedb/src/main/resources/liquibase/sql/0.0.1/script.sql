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

CREATE TABLE IF NOT EXISTS SOA_ESPS.EXECUTOR_NODE (
  ID SERIAL PRIMARY KEY,
  url VARCHAR NOT NULL,
  ip_address VARCHAR(35) NOT NULL,
  description VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS SOA_ESPS.NODE_STATISTIC (
  NODE_ID SERIAL PRIMARY KEY REFERENCES EXECUTOR_NODE (ID),
  CREATION_TIME TIMESTAMP(6) without time zone NOT NULL DEFAULT now(),
  MODIFICATION_TIME TIMESTAMP(6) without time zone,
  --NODE_ID SERIAL PRIMARY KEY,
  PERFORMANCE_INDEX DOUBLE PRECISION,
  FAILURE_RATE DOUBLE PRECISION,
  FAILURE_PROBABILITY DOUBLE PRECISION
);

CREATE TABLE IF NOT EXISTS SOA_ESPS.T_USER_DETAILS (
  ID BIGSERIAL PRIMARY KEY,
  CREATION_TIME TIMESTAMP(6) without time zone NOT NULL DEFAULT now(),
  MODIFICATION_TIME TIMESTAMP(6) without time zone,
  PASSWORD VARCHAR(400),
  USER_NAME VARCHAR(35) NOT NULL,
  IS_ACCOUNT_EXPIRED BOOLEAN NOT NULL DEFAULT FALSE,
  IS_LOCKED BOOLEAN NOT NULL DEFAULT FALSE,
  IS_CREDENTIALS_EXPIRED BOOLEAN NOT NULL DEFAULT FALSE,
  IS_ENABLED BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS SOA_ESPS.oauth2_registered_client (
  id varchar(100) NOT NULL,
  client_id varchar(100) NOT NULL,
  client_id_issued_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
  client_secret varchar(200) DEFAULT NULL,
  client_secret_expires_at timestamp DEFAULT NULL,
  client_name varchar(200) NOT NULL,
  client_authentication_methods varchar(1000) NOT NULL,
  authorization_grant_types varchar(1000) NOT NULL,
  redirect_uris varchar(1000) DEFAULT NULL,
  scopes varchar(1000) NOT NULL,
  client_settings varchar(2000) NOT NULL,
  token_settings varchar(2000) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS SOA_ESPS.oauth2_authorization (
  id varchar(100) NOT NULL,
  registered_client_id varchar(100) NOT NULL,
  principal_name varchar(200) NOT NULL,
  authorization_grant_type varchar(100) NOT NULL,
  attributes varchar(4000) DEFAULT NULL,
  state varchar(500) DEFAULT NULL,
  authorization_code_value BYTEA DEFAULT NULL,
  authorization_code_issued_at timestamp DEFAULT NULL,
  authorization_code_expires_at timestamp DEFAULT NULL,
  authorization_code_metadata varchar(2000) DEFAULT NULL,
  access_token_value BYTEA DEFAULT NULL,
  access_token_issued_at timestamp DEFAULT NULL,
  access_token_expires_at timestamp DEFAULT NULL,
  access_token_metadata varchar(2000) DEFAULT NULL,
  access_token_type varchar(100) DEFAULT NULL,
  access_token_scopes varchar(1000) DEFAULT NULL,
  oidc_id_token_value BYTEA DEFAULT NULL,
  oidc_id_token_issued_at timestamp DEFAULT NULL,
  oidc_id_token_expires_at timestamp DEFAULT NULL,
  oidc_id_token_metadata varchar(2000) DEFAULT NULL,
  refresh_token_value BYTEA DEFAULT NULL,
  refresh_token_issued_at timestamp DEFAULT NULL,
  refresh_token_expires_at timestamp DEFAULT NULL,
  refresh_token_metadata varchar(2000) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS SOA_ESPS.oauth2_authorization_consent (
  registered_client_id varchar(100) NOT NULL,
  principal_name varchar(200) NOT NULL,
  authorities varchar(1000) NOT NULL,
  PRIMARY KEY (registered_client_id, principal_name)
);