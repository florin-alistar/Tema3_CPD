import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Start {

	public static void main(String[] args) {
		
		List<String> cities = Arrays.asList("Berlin", "Barcelona", "Atena", "Istanbul", "Oslo");
		List<Integer> ports = Arrays.asList(50, 60,  70, 80, 90);
		List<Thread> threads = new ArrayList<>();
		
		// Punem cateva locatii la inceput
		
		Map<String, List<Journey>> initialJourneys = new HashMap<String, List<Journey>>();
		List<Journey> berlin = new ArrayList<Journey>();
		berlin.add(new Journey("Brandenburg", 5, 7, 2));
		berlin.add(new Journey("Parliament", 6, 9, 2));
		
		List<Journey> barcelona = new ArrayList<Journey>();
		barcelona.add(new Journey("CampNou", 2, 10, 2));
		barcelona.add(new Journey("CasaMila", 5, 11, 3));
		
		List<Journey> atena = new ArrayList<Journey>();
		atena.add(new Journey("Acropole", 1, 12, 3));
		atena.add(new Journey("Olympieion", 2, 9, 2));
		
		List<Journey> istanbul = new ArrayList<Journey>();
		istanbul.add(new Journey("Topkapi", 1, 12, 4));
		istanbul.add(new Journey("Dolmabahce", 1, 5, 3));
		
		List<Journey> oslo = new ArrayList<Journey>();
		oslo.add(new Journey("Vigeland", 5, 12, 1));
		oslo.add(new Journey("Akershus", 8, 10, 2));
		
		initialJourneys.put("Berlin", berlin);
		initialJourneys.put("Barcelona", barcelona);
		initialJourneys.put("Atena", atena);
		initialJourneys.put("Istanbul", istanbul);
		initialJourneys.put("Oslo", oslo);
		
		// Un thread per oras (si per port 50, 60, 70, 80, 90)
		for (int i = 0; i < cities.size(); i++) {
			String city = cities.get(i);
			int port = ports.get(i);
			Collection<Journey> availableJourneys = Collections.synchronizedList(initialJourneys.get(city));
			CityLevelRequestHandler cityLevelRequestHandler = new CityLevelRequestHandler(port, city, availableJourneys);
			Thread th = new Thread(cityLevelRequestHandler);
			threads.add(th);
			th.start();
		}
		
		for (Thread t: threads) {
			try {
				t.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		System.out.println("Inchis");
	}

}
