CREATE TABLE IF NOT EXISTS SOA_ESPS.SERVER_BILLS (
  ID SERIAL PRIMARY KEY,
  creation_time TIMESTAMP(6) without time zone NOT NULL DEFAULT now(),
  modification_time TIMESTAMP(6) without time zone,
  uuid uuid DEFAULT uuid_generate_v4(),
  owner_id BIGINT NOT NULL,
  account_balance NUMERIC(5, 2),
  owner_public_key Bytea,
  public_key Bytea,
  private_key Bytea,
  cipher_key Bytea,
  bill_signature Bytea,
  shared_secret VARCHAR(400),
  indentation VARCHAR(400)
);
/