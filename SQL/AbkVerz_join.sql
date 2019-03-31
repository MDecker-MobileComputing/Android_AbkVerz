
-- Inner Join in impliziter Schreibweise
SELECT abkuerzungen.abk_id, abkuerzung, bedeutung 
    FROM abkuerzungen, bedeutungen
    WHERE abkuerzungen.abk_id = bedeutungen.abk_id
    ORDER BY abkuerzungen.abkuerzung ASC;
    