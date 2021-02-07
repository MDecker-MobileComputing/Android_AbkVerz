package de.mide.abkverz;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Beispiel-App zur Nutzung der in Android eingebauten SQLite-Datenbank. 
 * Mit der App können Bedeutungen für Abkürzungen abgespeichert und gesucht werden,
 * wobei eine Abkürzung auch mehrere Bedeutungen haben kann.
 * <br><br>
 *
 * This project is licensed under the terms of the BSD 3-Clause License.
 */
public class MainActivity extends Activity 
		          implements IGlobalConstants, OnClickListener {
 
	/** Hilfs-Objekt für Zugriffe auf Datenbank. */
	protected DatenbankManager _datenbankManager = null;
	
	/** Eingabefeld mit Abkürzung, nach der gesucht werden soll. */
	protected EditText _textEditAbkZumSuchen = null;
	
	/** Button, mit dem die Suche nach einer Abkürzung gestartet wird. */
	protected Button _buttonAbkSuche = null;
	
	/** Button, mit dem Activity zum Anlegen eines neuen Eintrags aufgerufen wird. */
	protected Button _buttonNeuerEintrag = null;
		
	/** TextView-Element, mit dem die für eine Abkürzung gefundene Bedeutungen angezeigt werden. */
	protected TextView _textViewBedeutungen = null;
	
	
       /**
        * Lifecycle-Methode zur Initialisierung des Activity-Objekts.
        */
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// *** DB-Helper-Objekt erzeugen ***
		_datenbankManager = new DatenbankManager(this);
		
		
		// *** Referenzen auf UI-Elemente abfragen ***
		_textEditAbkZumSuchen = findViewById(R.id.textEditFuerAbkZumSuchen       );
		_buttonAbkSuche       = findViewById(R.id.buttonStartAbkSuche            );
		_buttonNeuerEintrag   = findViewById(R.id.buttonNeueAbkEintragen         );
		_textViewBedeutungen  = findViewById(R.id.textViewFuerAnzeigeBedeutungen );
				
		
		// *** Event-Handler für Buttons setzen ***
		_buttonAbkSuche.setOnClickListener(this);
		_buttonNeuerEintrag.setOnClickListener(this);
	}


	/**
	 * Event-Handler-Methode für die beiden Buttons.
	 * 
	 * @param view  UI-Element, das Event erzeugt hat, sollte ein Button sein
	 */
	@Override
	public void onClick(View view) {
		
		if (view == _buttonAbkSuche) {

			sucheNachAbk();

		} else if (view == _buttonNeuerEintrag) {		

			Intent intent = new Intent(this, NeuerEintragActivity.class);
			startActivity(intent);

		} else {

			String errorMsg = "Unerwartetes View-Element hat onClick-Event ausgelöst: " + view;
			Log.w(TAG4LOGGING, errorMsg);
			showToast(errorMsg);
		}
	}
	
	
	/**
	 * Suche nach Bedeutungen von eine eingegebene Abkürzung.
	 */
	protected void sucheNachAbk() {

		String   errorMsg    = null;
		String[] bedeutungen = null;
				
		String suchString = _textEditAbkZumSuchen.getText().toString().trim();
		if (suchString.length() == 0) {

			showToast("Bitte zulässige Abkürzung zum Suchen eingeben!");
			return;
		}
		
		// *** Eigentliche DB-Query ausführen ***
		try {
			bedeutungen = _datenbankManager.sucheNachAbk(suchString);
		}
		catch (Exception ex) {

			errorMsg = "Exception bei suchNachAbk() aufgetreten: " + ex;
			showToast(errorMsg);
			Log.e(TAG4LOGGING, errorMsg);
			return;
		}
		
		  
		if (bedeutungen == null || bedeutungen.length == 0) {

			_textViewBedeutungen.setText("");
			showToast("Abkürzung '" + suchString + "' nicht gefunden.");
			return;
		}
	
		// *** Ergebnis-Treffer darstellen ***
		StringBuffer sb = new StringBuffer();
		for(String bedeutung: bedeutungen) {

			sb.append(bedeutung).append("\n");
		}
		_textViewBedeutungen.setText(sb.toString());
	}

	
	/**
	 * Hilfemethode, um Toast-Texte anzuzeigen (Dauer: Lang)
	 * 
	 * @param message Anzuzeigende Nachricht
	 */
	protected void showToast(String message) {

		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}

};
