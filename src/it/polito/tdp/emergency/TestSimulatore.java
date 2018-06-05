package it.polito.tdp.emergency;

public class TestSimulatore {

	public static void main(String[] args) {
		Simulatore sim = new Simulatore() ;
		
		sim.setNP(250);
		sim.setT_ARRIVAL(3);
		sim.setNS(3);
		sim.init(); 
		
		sim.run(); 
		
		System.out.println("Pazienti curati: "+sim.getPaz_curati());
		System.out.println("Pazienti abbandonati: "+sim.getPaz_abbandonati());
		System.out.println("Pazienti morti: "+sim.getPaz_morti());

	}

}
