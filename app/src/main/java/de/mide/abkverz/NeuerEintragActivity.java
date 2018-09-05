package de.mide.abkverz;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


/**
 * Activity, um eine neue Abkürzung in die DB einzutragen.
 * <br><br>
 *
 * This project is licensed under the terms of the BSD 3-Clause License.
 */
public class NeuerEintragActivity extends Activity 
								  implements IGlobalConstants, OnClickListener {
	
	/** Hilfs-Objekt für Zugriffe auf Datenbank. */
	protected DatenbankManager _datenbankManager = null;
	
	/** Button zum Einfügen eines neuen Datensatzes. */
	protected Button _buttonEinfuegen = null;
	
	/** Button für "Abbrechen", wir schließen die Activity */
	protected Button _buttonZurueck = null;
	
	/** UI-Element für die Eingabe der neuen Abkürzung. */
	protected EditText _editTextAbk = null;
	
	/** UI-Element für die Eingabe der neuen Bedeutung. */
	protected EditText _editTextBedeutung = null;
	
	
	/**
	 * Lifecycle-Methode. Referenzen von UI-Element in Member-Variablen speichern
	 * und Event-Handler für Buttons definieren. Erzeugt auf Objekt vom
	 * Datenbank-Manager.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_neuer_eintrag);
		
		// *** Datenbank-Manager-Objekt erzeugen ***
		_datenbankManager = new DatenbankManager(this);
		
		
		// *** Referenzen von UI-Element in Member-Variablen speichern ***
		_buttonEinfuegen   = (Button)   findViewById(R.id.buttonEintragEinfuegen );
		_buttonZurueck     = (Button)   findViewById(R.id.buttonZurueck          );
		_editTextAbk       = (EditText) findViewById(R.id.editTextNeueAbk        );
		_editTextBedeutung = (EditText) findViewById(R.id.editTextNeueBedeutung  );

		
		// *** Event-Handler für Buttons festlegen ***
		_buttonEinfuegen.setOnClickListener(this);
		_buttonZurueck.setOnClickListener(this);
	}

	
	/**
	 * Event-Handler für Buttons.
	 * 
	 * @param view Einer der beiden Buttons, der das Event ausgelöst hat.
	 */
	@Override
	public void onClick(View view) {
		String errorMessage = null;
		
		if (view == _buttonZurueck) {
			
			finish();
			
		} else if (view == _buttonEinfuegen) { 
					
			neuenEintragEinfuegen();
			
		} else {
			
			errorMessage = "Event-Handler-Methode für unerwartetes View-Objekt aufgerufen: " + view;
			showToast(errorMessage);
			Log.w(TAG4LOGGING, errorMessage);
			
		}
	}
	

	/**
	 * Methode um Eingaben des Nutzers auszulesen und
	 * den neuen Eintrag in der DB zu erzeugen
	 */
	protected void neuenEintragEinfuegen () {
		String errorMsg     = null;
		String[] resultStrs = null;
		
		String abkString    = _editTextAbk.getText().toString().trim();
		if (abkString.length() == 0) {
			showToast("Bitte Abkürzung eingeben!");
			return;
		}				
		String bedeutString = _editTextBedeutung.getText().toString().trim();
		if (bedeutString.length() == 0) {
			showToast("Bitte auch Bedeutung für die Abkürzung eingeben!");
			return;
		}
		
		
		try {
			
			// Zuerst schauen, ob es diese Abk. in der DB schon gibt
			resultStrs = _datenbankManager.sucheNachAbk(abkString);
			if (resultStrs.length > 0) {
				
				_datenbankManager.insertBedeutung(abkString, bedeutString);
				showToast("Neue Bedeutung in Datenbank eingefügt.");
				
			} else { // Die Abk. ist noch nicht in der Datenbank
			
				_datenbankManager.insertGanzNeueAbk(abkString, bedeutString);
				showToast("Neue Abkürzung in Datenbank eingefügt.");
				
			}
			
			
			// *** Nach erfolgreicher Einfüge-Operation ***
			_editTextAbk.setText      ("");
			_editTextBedeutung.setText("");
			
		}
		catch (Exception ex) {
			errorMsg = "Exception beim Einfügen neuer Abk+Bedeutung aufgetreten: " + ex;
			Log.e(TAG4LOGGING, errorMsg );
		}
		
	}
	
	
	/**
	 * Hilfsmethode, um Toast-Texte anzuzeigen (Dauer: Lang)
	 * 
	 * @param message Anzuzeigende Nachricht
	 */
	protected void showToast(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}

		 
};
