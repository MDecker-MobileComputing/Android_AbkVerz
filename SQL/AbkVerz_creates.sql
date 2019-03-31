
CREATE TABLE abkuerzungen (
  abk_id     INTEGER PRIMARY KEY,
  abkuerzung TEXT    NOT NULL
);

CREATE INDEX mein_index_1 ON abkuerzungen(abkuerzung);


CREATE TABLE bedeutungen (
  abk_id    INTEGER         ,
  bedeutung TEXT    NOT NULL,
  FOREIGN KEY (abk_id) REFERENCES abkuerzungen(abk_id)
);

CREATE INDEX mein_index_2 ON bedeutungen(abk_id);
