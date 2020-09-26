
CREATE TABLE abkuerzungen (
  abk_id     INTEGER PRIMARY KEY,
  abkuerzung TEXT    NOT NULL
);

CREATE INDEX abkuerzung_index_1 ON abkuerzungen(abkuerzung);


CREATE TABLE bedeutungen (
  bedeutung_id INTEGER PRIMARY KEY,
  bedeutung    TEXT    NOT NULL   ,  
  abkuerzung   INTEGER            ,

  FOREIGN KEY (abkuerzung) REFERENCES abkuerzungen(abk_id)
);

CREATE INDEX bedeutungen_index_1 ON bedeutungen(abkuerzung);
