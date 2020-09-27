
INSERT INTO abkuerzungen (abkuerzung) VALUES ('ARM');


-- Mit dem folgenden Insert-Statement kann die erste oder eine weitere Bedeutung 
-- für eine Abkürzung hinzugefügt werden
INSERT INTO bedeutungen (abkuerzung, bedeutung) 
            SELECT abk_id, 'Advanced RISC Machines' 
              FROM abkuerzungen 
             WHERE abkuerzung='ARM';


