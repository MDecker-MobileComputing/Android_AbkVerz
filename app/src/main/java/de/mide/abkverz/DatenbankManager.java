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

	/** Prepared SQL-Statement, um eine in der DB bisher noch nicht bekannt Abkürzung
	 *  samt Bedeutung einzufügen. */
	protected SQLiteStatement _statementInsertNeuAbk = null;
	
	/** Prepared SQL-Statement, um zu einer bereits in der DB eingetragenen
	 *  Abkürzung die erste oder eine weitere Bedeutung einzutragen.
	 */
	protected SQLiteStatement _statementInsertBedeutung = null;
	
	
	/**
	 * Konstruktor, ruft den Super-Konstruktor auf und erzeugt die PreparedStatements.
	 * 
	 * @param context Selbstreferenz auf Activity, die dieses Objekt erzeugt hat
	 */
	public DatenbankManager(Context context) {

		super(context,
			  "abkverz.db",  // Name der DB 
			  null,          // Default-CursorFactory verwenden
			  1);            // Versions-Nummer als int


		// *** Prepared Statements erzeugen ***
		SQLiteDatabase db = getReadableDatabase();
		
		_statementInsertNeuAbk = 
			db.compileStatement(
				"INSERT INTO abkuerzungen (abkuerzung) VALUES ( ? )" // "?" = Platzhalter 
			);
			
		_statementInsertBedeutung =
			db.compileStatement(
				"INSERT INTO bedeutungen (abk_id, bedeutung) " + 
				"  SELECT abk_id, ? FROM abkuerzungen WHERE abkuerzung=?;"
			);
	}
	
	
	/**
	 * Abstrakte Methode aus <i>SQLiteOpenHelper</i>, muss also überschrieben werden.
	 *
	 * Legt mit "CREATE TABLE" und "CREATE INDEX" das Datenbankschema an, wenn noch
	 * nicht verhanden (z.B. beim ersten Start der App nach Installation).
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		
		try {
			
			// *** Erste Tabelle mit Index anlegen *** 
			db.execSQL(
			  "CREATE TABLE abkuerzungen (        " +
	  	      "abk_id     INTEGER PRIMARY KEY AUTOINCREMENT, " +
	  	      "abkuerzung TEXT NOT NULL        ); "   		
			);
			db.execSQL("CREATE INDEX mein_index_1 ON abkuerzungen (abkuerzung);");
			
			// *** Zweite Tabelle mit Index anlegen *** 
			db.execSQL(
					"CREATE TABLE bedeutungen (                              " +
					"abk_id    INTEGER,                                      " +
					"bedeutung TEXT NOT NULL,                                " +   								  
					"FOREIGN KEY (abk_id) REFERENCES abkuerzungen(abk_id) ); "
			);				
	
			db.execSQL("CREATE INDEX mein_index_2 ON bedeutungen (abk_id)");
			
			Log.i(TAG4LOGGING, "Datenbankschema angelegt.");
			
			// *** Wir fügen noch ein paar Beispiel-Daten in die neuen Tabellen ein ***
			db.execSQL("INSERT INTO abkuerzungen VALUES(1,'ADB');");
			db.execSQL("INSERT INTO abkuerzungen VALUES(2,'KSC');");
			db.execSQL("INSERT INTO abkuerzungen VALUES(3,'HCI');");
			
			db.execSQL("INSERT INTO bedeutungen VALUES(1,'Android Debug Bridge'      );");
			db.execSQL("INSERT INTO bedeutungen VALUES(2,'Karlsruher Sport-Club'     );");
			db.execSQL("INSERT INTO bedeutungen VALUES(2,'Kennedy Space Center'      );");
			db.execSQL("INSERT INTO bedeutungen VALUES(3,'Human-Computer Interaction');");
			db.execSQL("INSERT INTO bedeutungen VALUES(3,'Hash Collision Index'      );");
			
			Log.v(TAG4LOGGING, "Beispiel-Datensätze eingefügt.");
			
		} catch (SQLException ex) {
			Log.e(TAG4LOGGING, "Datenbank-Exception beim Anlegen von Schema aufgetreten: " + ex);
		}
	}


	/**
	 * Zweite abstrakte Methode aus <i>SQLiteOpenHelper</i>, 
	 * lassen wir aber leer. In dieser Methode könnte man
	 * z.B. für eine neue Version nicht mehr benötigte Tabelle
	 * mit "DROP TABLE" löschen.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		Log.w(TAG4LOGGING, 
			  "Leere Implementierung der onUpdate()-Methode in DatenbankManager aufgerufen.");
	}
	
	
	/**
	 * Such nach Bedeutungen für die als Parameter übergebene Abkürzung
	 * 
	 * @param abk Die Abkürzung, nach der gesucht werden soll; darf nicht leer sein
	 *            und darf keine Leerzeichen enthalten
     *
	 * @return Array der gefundenen Bedeutungen; ist Array der Länge 0, wenn nichts
	 *         gefunden
	 */	
	@SuppressLint("DefaultLocale")
	public String[] sucheNachAbk(String abk) throws SQLException {
		
		abk = abk.toUpperCase();
		
		SQLiteDatabase db = getReadableDatabase();						
		Cursor cursor = 
				db.rawQuery(
						"SELECT bedeutung     " + 
						"  FROM abkuerzungen, bedeutungen " +
						"  WHERE abkuerzungen.abk_id = bedeutungen.abk_id " +
						"	 AND abkuerzung = '" + abk + "'" +
						"  ORDER BY bedeutungen.bedeutung ASC", 
						null); // die "selectionArgs" brauchen wir hier nicht
		
		
		// *** Ergebnis der Query auswerten ***
		int resultRows = cursor.getCount();
		if (resultRows == 0)
			return new String[]{};
		
		String[] resultStrings = new String[resultRows];
				
		int counter = 0;
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			resultStrings[counter] = cursor.getString(0);
			counter++;
		}
		
		return resultStrings;
	}
	
	
	/**
	 * Bedeutung für bisher in der Datenbank noch nicht vorhandene Abkürzung
	 * hinzufügen
	 *
	 * @param abkString Die neue Abkürzung, darf nicht leer sein.
     *
	 * @param bedeutung Die neue Bedeutung, darf nicht leer sein.
	 */
	public void insertGanzNeueAbk(String abkString, String bedeutung) throws SQLException {
		
		abkString = abkString.toUpperCase();
		
		_statementInsertNeuAbk.bindString(1, abkString); // Wert für Platzhalter "?"
		long idOfNewRow = _statementInsertNeuAbk.executeInsert();
		if (idOfNewRow == -1 ) 
			throw new SQLException("Einfügen der neuen Abkürzung '" + abkString +
					                "' ist fehlgeschlagen.");
				
		insertBedeutung(abkString, bedeutung);
	}
	
	
	/**
	 * Fügt für eine bereits in der Datenbank stehende Abkürzung die erste oder eine weitere
	 * Bedeutung hinzu
	 *  
	 * @param abkString Abkürzung, muss schon in Datenbank gespeichert sein
     *
	 * @param bedeutung Die erste oder eine weitere Bedeutung für diese Abkürzung
	 */
	public void insertBedeutung(String abkString, String bedeutung) throws SQLException {
		
		abkString = abkString.toUpperCase();
		
		_statementInsertBedeutung.bindString(1, bedeutung);
		_statementInsertBedeutung.bindString(2, abkString);
		long idOfNewRow = _statementInsertBedeutung.executeInsert();
		if (idOfNewRow == -1 ) 
			throw new SQLException("Einfügen der neuen Bedeutung '" + bedeutung +
					               "' ist fehlgeschlagen.");
	}
	
};
