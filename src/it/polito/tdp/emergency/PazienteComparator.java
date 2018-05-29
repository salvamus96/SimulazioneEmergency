package it.polito.tdp.emergency;

import java.util.Comparator;

public class PazienteComparator implements Comparator<Paziente> {

	@Override
	public int compare(Paziente paz1, Paziente paz2) {
		if (paz1.getStato() == paz2.getStato()) {
			return paz1.getOraArrivo().compareTo(paz2.getOraArrivo());
		}
		if (paz1.getStato() == StatoPaziente.RED)
			return -1;
		if (paz2.getStato() == StatoPaziente.RED)
			return +1;
		if (paz1.getStato() == StatoPaziente.YELLOW)
			return -1;
		if (paz2.getStato() == StatoPaziente.YELLOW)
			return +1;

		throw new IllegalArgumentException(
				"Stato paziente non accettabile: " + paz1.getStato() + "-" + paz2.getStato());
	}

}
