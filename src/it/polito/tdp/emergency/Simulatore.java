package it.polito.tdp.emergency;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class Simulatore {

	// Parametri
	private int NS = 3; // numero di studi medici
	private int NP = 50; // numero di pazienti in arrivo
	private int T_ARRIVAL = 15; // intervallo di tempo tra i pazienti (in minuti)

	private LocalTime T_inizio = LocalTime.of(8, 0);
	private LocalTime T_fine = LocalTime.of(20, 0);

	private int DURATION_TRIAGE = 5;
	private int DURATION_WHITE = 10;
	private int DURATION_YELLOW = 15;
	private int DURATION_RED = 30;
	private int TIMEOUT_WHITE = 120;
	private int TIMEOUT_YELLOW = 60;
	private int TIMEOUT_RED = 90;

	// Modello del mondo
	List<Paziente> pazienti;
	StatoPaziente statoTriage; // il prossimo stato da assegnare

	// Valori in output
	private int paz_curati;
	private int paz_abbandonati;
	private int paz_morti;

	// Coda degli eventi
	PriorityQueue<Event> queue = new PriorityQueue<>();

	public void init() {
		this.pazienti = new ArrayList<>();
		LocalTime ora = T_inizio;

		for (int i = 0; i < NP; i++) {
			Paziente p = new Paziente(i + 1, StatoPaziente.NEW, ora);
			Event e = new Event(ora, EventType.ARRIVA, p);
			ora = ora.plusMinutes(T_ARRIVAL);
		}

		statoTriage = StatoPaziente.WHITE;

		paz_curati = 0;
		paz_abbandonati = 0;
		paz_morti = 0;
	}

	public void run() {
		Event e;
		while ((e = queue.poll()) != null) {
			if (e.getOra().isAfter(T_fine))
				break;

			processEvent(e);
		}
	}

	private void processEvent(Event e) {
		switch(e.getTipo()) {
		case ARRIVA:
			Event e2 = new Event(e.getOra().plusMinutes(DURATION_TRIAGE),
					EventType.TRIAGE, e.getPaziente()) ;
			queue.add(e2) ;
			break ;

		case TRIAGE:
			e.getPaziente().setStato(statoTriage);
			
			if(statoTriage==StatoPaziente.WHITE) {
				queue.add(new Event(e.getOra().plusMinutes(TIMEOUT_WHITE),
						EventType.TIMEOUT_WHITE, e.getPaziente())) ;
			} else if (statoTriage==StatoPaziente.YELLOW) {
				queue.add(new Event(e.getOra().plusMinutes(TIMEOUT_YELLOW),
						EventType.TIMEOUT_YELLOW, e.getPaziente())) ;
			} else if (statoTriage==StatoPaziente.RED) {
				queue.add(new Event(e.getOra().plusMinutes(TIMEOUT_RED),
						EventType.TIMEOUT_RED, e.getPaziente())) ;
			}
			
			// cambiaStatoTriage
			if(statoTriage==StatoPaziente.WHITE)
				statoTriage=StatoPaziente.YELLOW ;
			else if(statoTriage==StatoPaziente.YELLOW)
				statoTriage=StatoPaziente.RED ;
			else if(statoTriage==StatoPaziente.RED)
				statoTriage=StatoPaziente.WHITE ;

			break;
			
		case CHIAMATA:
			break;
			
			
		case USCITA:
			// Registrare l'uscita di e.getPaziente()
			
			// Decidere chi deve essere chiamato (sulla base di colore e ora di arrivo)
			// tra i pazienti attesa
			
			// Schedulare per ADESSO la CHIAMATA del paziente
			break;
			
		case TIMEOUT_WHITE:
			break;
		case TIMEOUT_YELLOW:
			break;
		case TIMEOUT_RED:
			break;
		}
		
	}

}
