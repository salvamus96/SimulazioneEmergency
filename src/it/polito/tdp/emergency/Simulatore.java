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
	private int T_POLLING = 5;

	// Modello del mondo
	private List<Paziente> pazienti;
	private StatoPaziente statoTriage; // il prossimo stato da assegnare
	private int studi_occupati;
	private PriorityQueue<Paziente> attesa;

	// Valori in output
	private int paz_curati;
	private int paz_abbandonati;
	private int paz_morti;

	// Coda degli eventi
	private PriorityQueue<Event> queue = new PriorityQueue<>();

	public void init() {
		this.pazienti = new ArrayList<>();
		this.attesa = new PriorityQueue<>(new PazienteComparator());

		LocalTime ora = T_inizio;

		for (int i = 0; i < NP; i++) {
			Paziente p = new Paziente(i + 1, StatoPaziente.NEW, ora);
			Event e = new Event(ora, EventType.ARRIVA, p);
			ora = ora.plusMinutes(T_ARRIVAL);
			queue.add(e) ;
		}
		queue.add(new Event(T_inizio, EventType.POLLING, null));

		this.studi_occupati = 0;

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
		switch (e.getTipo()) {
		case ARRIVA:
			Event e2 = new Event(e.getOra().plusMinutes(DURATION_TRIAGE), EventType.TRIAGE, e.getPaziente());
			queue.add(e2);
			break;

		case TRIAGE:
			e.getPaziente().setStato(statoTriage);

			if (statoTriage == StatoPaziente.WHITE) {
				queue.add(new Event(e.getOra().plusMinutes(TIMEOUT_WHITE), EventType.TIMEOUT_WHITE, e.getPaziente()));
			} else if (statoTriage == StatoPaziente.YELLOW) {
				queue.add(new Event(e.getOra().plusMinutes(TIMEOUT_YELLOW), EventType.TIMEOUT_YELLOW, e.getPaziente()));
			} else if (statoTriage == StatoPaziente.RED) {
				queue.add(new Event(e.getOra().plusMinutes(TIMEOUT_RED), EventType.TIMEOUT_RED, e.getPaziente()));
			}
			attesa.add(e.getPaziente());

			// cambiaStatoTriage
			if (statoTriage == StatoPaziente.WHITE)
				statoTriage = StatoPaziente.YELLOW;
			else if (statoTriage == StatoPaziente.YELLOW)
				statoTriage = StatoPaziente.RED;
			else if (statoTriage == StatoPaziente.RED)
				statoTriage = StatoPaziente.WHITE;

			break;

		case CHIAMATA:
			attesa.remove(e.getPaziente());
			studi_occupati++;

			switch (e.getPaziente().getStato()) {
			case WHITE:
				queue.add(new Event(e.getOra().plusMinutes(DURATION_WHITE), EventType.USCITA, e.getPaziente()));
				break;
			case YELLOW:
				queue.add(new Event(e.getOra().plusMinutes(DURATION_YELLOW), EventType.USCITA, e.getPaziente()));
				break;
			case RED:
				queue.add(new Event(e.getOra().plusMinutes(DURATION_RED), EventType.USCITA, e.getPaziente()));
				break;
			}

			e.getPaziente().setStato(StatoPaziente.TREATING);
			break;

		case USCITA:
			// Registrare l'uscita di e.getPaziente()
			e.getPaziente().setStato(StatoPaziente.OUT);
			paz_curati++;
			studi_occupati--;

			// Decidere chi deve essere chiamato (sulla base di colore e ora di arrivo)
			// tra i pazienti attesa
			Paziente paz = attesa.peek();
			if (paz != null) {
				// Schedulare per ADESSO la CHIAMATA del paziente
				queue.add(new Event(e.getOra(), EventType.CHIAMATA, paz));
			}

			break;

		case TIMEOUT_WHITE:
			if (e.getPaziente().getStato() == StatoPaziente.WHITE) {
				attesa.remove(e.getPaziente());
				paz_abbandonati++;
				e.getPaziente().setStato(StatoPaziente.OUT);
			}
			break;

		case TIMEOUT_YELLOW:
			if (e.getPaziente().getStato() == StatoPaziente.YELLOW) {

				attesa.remove(e.getPaziente());
				e.getPaziente().setStato(StatoPaziente.RED);
				queue.add(new Event(e.getOra().plusMinutes(TIMEOUT_RED), EventType.TIMEOUT_RED, e.getPaziente()));
				attesa.add(e.getPaziente()); // reinseriscilo in una posizione più privilegiata

			}
			break;

		case TIMEOUT_RED:
			if (e.getPaziente().getStato() == StatoPaziente.RED) {
				attesa.remove(e.getPaziente());
				paz_morti++;
				e.getPaziente().setStato(StatoPaziente.DEAD);
			}
			break;

		case POLLING:
			if(studi_occupati<NS && !attesa.isEmpty()) {
				Paziente paz2 = attesa.peek();
				queue.add(new Event(e.getOra(), EventType.CHIAMATA, paz2));
			}
			queue.add(new Event(e.getOra().plusMinutes(T_POLLING),
					EventType.POLLING, null));
			break;

		}

	}

	public int getPaz_curati() {
		return paz_curati;
	}

	public int getPaz_abbandonati() {
		return paz_abbandonati;
	}

	public int getPaz_morti() {
		return paz_morti;
	}

	public void setNS(int nS) {
		NS = nS;
	}

	public void setNP(int nP) {
		NP = nP;
	}

	public void setT_ARRIVAL(int t_ARRIVAL) {
		T_ARRIVAL = t_ARRIVAL;
	}

	public void setT_inizio(LocalTime t_inizio) {
		T_inizio = t_inizio;
	}

	public void setT_fine(LocalTime t_fine) {
		T_fine = t_fine;
	}

	public void setDURATION_TRIAGE(int dURATION_TRIAGE) {
		DURATION_TRIAGE = dURATION_TRIAGE;
	}

	public void setDURATION_WHITE(int dURATION_WHITE) {
		DURATION_WHITE = dURATION_WHITE;
	}

	public void setDURATION_YELLOW(int dURATION_YELLOW) {
		DURATION_YELLOW = dURATION_YELLOW;
	}

	public void setDURATION_RED(int dURATION_RED) {
		DURATION_RED = dURATION_RED;
	}

	public void setTIMEOUT_WHITE(int tIMEOUT_WHITE) {
		TIMEOUT_WHITE = tIMEOUT_WHITE;
	}

	public void setTIMEOUT_YELLOW(int tIMEOUT_YELLOW) {
		TIMEOUT_YELLOW = tIMEOUT_YELLOW;
	}

	public void setTIMEOUT_RED(int tIMEOUT_RED) {
		TIMEOUT_RED = tIMEOUT_RED;
	}

	public void setT_POLLING(int t_POLLING) {
		T_POLLING = t_POLLING;
	}

}
