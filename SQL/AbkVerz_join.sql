
-- Inner Join in impliziter Schreibweise um alle Abkürzungen und zugehörige Bedeutungen auszugeben
SELECT abkuerzungen.abkuerzung, bedeutung 
    FROM abkuerzungen, bedeutungen
    WHERE abkuerzungen.abk_id = bedeutungen.abkuerzung
    ORDER BY abkuerzungen.abkuerzung ASC, bedeutung ASC;

    
-- Inner Join in impliziter Schreibweise um alle Bedeutungen für eine bestimmte Abkürzung auszugeben
SELECT abkuerzungen.abkuerzung, bedeutung 
    FROM abkuerzungen, bedeutungen
    WHERE abkuerzungen.abk_id = bedeutungen.abkuerzung
      AND abkuerzungen.abkuerzung="KSC"
    ORDER BY abkuerzungen.abkuerzung ASC, bedeutung ASC;
