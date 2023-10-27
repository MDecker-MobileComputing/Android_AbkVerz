package de.mide.abkverz;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;


/**
 * In dieser Klasse kapseln wir alle Datenbank-Zugriffe.
 * Da diese Klasse von {@link android.database.sqlite.SQLiteOpenHelper} erbt,
 * beinhaltet dies auch das Erzeugen des Datenbank-Schemas, falls es noch nicht
 * vorhanden sein sollte.
 * Es wird davon ausgegangen, dass alle Abkürzungen nur aus Großbuchstaben
 * <br><br>
 *
 * Pfad der Datenbank-Datei auf dem Android-Gerät/Emulator:
 * <pre>
 *     /data/data/de.mide.abkverz/databases/abkverz.db
 * </pre>
 * Diese Datei mit adb.exe von Emulator-Instanz auf den PC herunterladen:
 * <pre>
 *     adb pull /data/data/de.mide.abkverz/databases/abkverz.db
 * </pre>
 * Wenn man hierbei einen Fehler wegen mangelnden Berechtigungen bekommt, dann
 * den ADB-Dämon mit Root-Rechten neu starten und den pull-Befehl nochmal probieren:
 * <pre>
 *     adb root
 * </pre>
 * <br><br>
 *
 * This project is licensed under the terms of the BSD 3-Clause License.
 */
public class DatenbankManager extends SQLiteOpenHelper implements IGlobalConstants {

    /**
     * Prepared Statement, um eine in der DB bisher noch nicht bekannt Abkürzung
     * samt Bedeutung einzufügen.
     */
    protected SQLiteStatement _statementInsertNeuAbk = null;

    /**
     * Prepared Statement, um zu einer bereits in der DB eingetragenen
     * Abkürzung die erste oder eine weitere Bedeutung einzutragen.
     */
    protected SQLiteStatement _statementInsertBedeutung = null;


    /**
     * Konstruktor, ruft den Super-Konstruktor auf und erzeugt die Prepared Statements.
     *
     * @param context  Selbstreferenz auf Activity, die dieses Objekt erzeugt hat
     */
    public DatenbankManager(Context context) {

        super(context,
                "abkverz.db",  // Name der DB-Datei
                null,          // Default-CursorFactory verwenden
                1 );           // Versions-Nummer des Datenbank-Schemas


        // Prepared Statements erzeugen
        SQLiteDatabase db = getReadableDatabase();

        _statementInsertNeuAbk =
                db.compileStatement(
                        "INSERT INTO abkuerzungen (abkuerzung) VALUES ( ? )" // "?" = Platzhalter
                );

        _statementInsertBedeutung =
                db.compileStatement(
                        "INSERT INTO bedeutungen (abkuerzung, bedeutung) " +
                                "  SELECT abk_id, ? FROM abkuerzungen WHERE abkuerzung=?"
                );
    }


    /**
     * Abstrakte Methode aus {@link android.database.sqlite.SQLiteOpenHelper}, muss also
     * überschrieben werden damit die vorliegende Klasse nicht auch wieder abstrakt ist.
     *
     * Legt mit "CREATE TABLE" und "CREATE INDEX" das Datenbankschema an, wenn noch
     * nicht verhanden (z.B. beim ersten Start der App nach Installation).
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        try {

            db.execSQL( "CREATE TABLE abkuerzungen ( "                   +
                    "abk_id     INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "abkuerzung TEXT NOT NULL ) "
            );
            db.execSQL( "CREATE INDEX abkuerzung_index_1 ON abkuerzungen(abkuerzung)" );

            db.execSQL( "CREATE TABLE bedeutungen (                         " +
                    "bedeutung_id INTEGER PRIMARY KEY,                      " +
                    "bedeutung    TEXT NOT NULL,                            " +
                    "abkuerzung   INTEGER,                                  " +
                    "FOREIGN KEY (abkuerzung) REFERENCES abkuerzungen(abk_id) ) "
            );
            db.execSQL( "CREATE INDEX bedeutungen_index_1 ON abkuerzungen(abkuerzung)" );

            Log.i(TAG4LOGGING, "Datenbankschema angelegt.");

            // Wir fügen noch ein paar Beispiel-Daten in die neuen Tabellen ein
            db.execSQL( "INSERT INTO abkuerzungen (abk_id, abkuerzung) VALUES( 1,'ADB')" );
            db.execSQL( "INSERT INTO abkuerzungen (abk_id, abkuerzung) VALUES( 2,'KSC')" );
            db.execSQL( "INSERT INTO abkuerzungen (abk_id, abkuerzung) VALUES( 3,'HCI')" );

            db.execSQL( "INSERT INTO bedeutungen (abkuerzung, bedeutung) VALUES( 1, 'Android Debug Bridge'      )" );
            db.execSQL( "INSERT INTO bedeutungen (abkuerzung, bedeutung) VALUES( 2, 'Karlsruher Sport-Club'     )" );
            db.execSQL( "INSERT INTO bedeutungen (abkuerzung, bedeutung) VALUES( 2, 'Kennedy Space Center'      )" );
            db.execSQL( "INSERT INTO bedeutungen (abkuerzung, bedeutung) VALUES( 3, 'Human-Computer Interaction')" );
            db.execSQL( "INSERT INTO bedeutungen (abkuerzung, bedeutung) VALUES( 3, 'Hash Collision Index'      )" );

            Log.v(TAG4LOGGING, "Beispiel-Datensätze eingefügt.");

        } catch (SQLException ex) {

            Log.e(TAG4LOGGING, "Exception beim Anlegen von DB-Schema aufgetreten: " + ex);
        }
    }


    /**
     * Zweite abstrakte Methode aus {@link android.database.sqlite.SQLiteOpenHelper},
     * lassen wir aber leer. In dieser Methode könnte man z.B. für eine neue Version
     * der App zu einer bestehenden Tabelle neue Spalte hinzufügen, oder ganz neue
     * Tabellen für ganz neue Funktionen.
     *
     * @param db  Referenz auf Datenbank-Objekt.
     *
     * @param oldVersion  Alte Version des Datenbank-Schemas.
     *
     * @param newVersion  Neue Version des Datenbank-Schemas, auf hergestellt werden soll.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.w(TAG4LOGGING, "Leere Implementierung der onUpdate()-Methode in DatenbankManager aufgerufen.");
    }


    /**
     * Such nach Bedeutungen für die als Argument {@code abk} übergebene Abkürzung.
     *
     * @param abk  Die Abkürzung, nach der gesucht werden soll; darf nicht leer sein
     *             und darf keine Leerzeichen enthalten.
     *
     * @return  Array der gefundenen Bedeutungen; ist Array der Länge 0, wenn nichts
     *          gefunden, aber nicht {@code null}.
     */
    @SuppressLint("DefaultLocale")
    public String[] sucheNachAbk(String abk) throws SQLException {

        abk = abk.toUpperCase();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery( "SELECT bedeutung " +
                        "  FROM abkuerzungen, bedeutungen " +
                        "  WHERE abkuerzungen.abk_id = bedeutungen.abkuerzung " +
                        "   AND abkuerzungen.abkuerzung = '" + abk + "'" +
                        "  ORDER BY abkuerzungen.abkuerzung ASC, bedeutung ASC",
                null ); // die "selectionArgs" brauchen wir hier nicht

        // Ergebnis der Query auswerten
        int anzahlErgebnisZeilen = cursor.getCount();
        if (anzahlErgebnisZeilen == 0) {

            cursor.close();
            return new String[]{};
        }

        String[] resultStrings = new String[anzahlErgebnisZeilen];
        int counter = 0;
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

            resultStrings[counter] = cursor.getString(0);
            counter++;
        }

        cursor.close();

        return resultStrings;
    }


    /**
     * Bedeutung für bisher in der Datenbank noch nicht vorhandene Abkürzung
     * hinzufügen.
     *
     * @param abkString  Die neue Abkürzung, darf nicht leer sein.
     *
     * @param bedeutung  Die neue Bedeutung, darf nicht leer sein.
     */
    public void insertGanzNeueAbk(String abkString, String bedeutung) throws SQLException {

        abkString = abkString.toUpperCase();

        _statementInsertNeuAbk.bindString(1, abkString); // Wert für Platzhalter "?"

        long idOfNewRow = _statementInsertNeuAbk.executeInsert();
        if (idOfNewRow == -1) {

            throw new SQLException("Einfügen der neuen Abkürzung '" + abkString +
                    "' ist fehlgeschlagen.");
        }

        insertBedeutung(abkString, bedeutung);
    }


    /**
     * Fügt für eine bereits in der Datenbank stehende Abkürzung die erste oder eine weitere
     * Bedeutung hinzu.
     *
     * @param abkString  Abkürzung, muss schon in Datenbank gespeichert sein.
     *
     * @param bedeutung  Die erste oder eine weitere Bedeutung für diese Abkürzung.
     */
    public void insertBedeutung(String abkString, String bedeutung) throws SQLException {

        abkString = abkString.toUpperCase();

        _statementInsertBedeutung.bindString(1, bedeutung);
        _statementInsertBedeutung.bindString(2, abkString);

        long idOfNewRow = _statementInsertBedeutung.executeInsert();
        if (idOfNewRow == -1 ) {

            throw new SQLException("Einfügen der neuen Bedeutung '" + bedeutung +
                    "' ist fehlgeschlagen.");
        }
    }

};
