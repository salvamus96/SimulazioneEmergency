package it.polito.tdp.emergency;

public enum EventType {
	ARRIVA, // arriva nuovo paziente all'ingresso
	TRIAGE, // al paziente viene assegnato un codice
	CHIAMATA, // il paziente entra dal medico
	USCITA, // il paziente esce dallo studio medico
	
	TIMEOUT_WHITE,
	TIMEOUT_YELLOW,
	TIMEOUT_RED,
}
