import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Start {

	private static int step = 1;
	
	// Sa vedem cand anume se face o anumita cerere
	//	-> avand o ordine, putem verifica corectitudinea
	public static synchronized int getCurrentStep() {
		step++;
		return step - 1;
	}
	
	public static void main(String[] args) {
		
		// Serverul asculta pe 5 porturi -> unul pentru fiecare oras -> un thread per port
		List<Integer> ports = Arrays.asList(50, 60, 70, 80, 90);
		List<Thread> threads = new ArrayList<>();
		
		//Pentru simulare: 
		// 85 de threaduri de comenzi efective
		//					-> 10 de adaugare (ajung atatea) => 2 pe fiecare port (oras)
		//					-> 70 de booking
		//					-> 5 de afisare a tuturor locatiilor disponibile
		// 5 threaduri (comenzi) pentru STOP
		
		List<ClientRequester> requesters = new ArrayList<ClientRequester>();
		
		// adauga 10 cereri de add pentru gazde, 2 pe fiecare din cele 5 porturi (orase)
		for (int i = 0; i < 10; i++) {
			int p = ports.get(i / 2);
			String req = getNewRequest(1, p);
			requesters.add(new ClientRequester(p, req));
		}
		
		// adauga 70 de cereri de rezervare pentru clienti, 14 pe fiecare din cele 5 porturi (orase)
		//		(desi s-ar putea face si aleatoriu)
		for (int i = 0; i < 70; i++) {
			//int p = ports.get(Math.abs(rand.nextInt()) % ports.size());
			int p = ports.get(i / 14);
			String req = getNewRequest(2, p);
			requesters.add(new ClientRequester(p, req));
		}
		
		// 5 cereri de get all, una pe oras
		for (int i = 0; i < 5; i++) {
			int p = ports.get(i);
			String req = getNewRequest(0, p);
			requesters.add(new ClientRequester(p, req));
		}
		
		// Cream cele 85 de threaduri efective si le dam start
		for (ClientRequester clReq: requesters) {
			Thread th = new Thread(clReq);
			threads.add(th);
			// "fire and forget"
			th.start();
		}
		
		
		// asteptam dupa aceste 85 de threaduri...
		for (Thread th: threads) {
			try {
				th.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// Si cream acum 5 threaduri de STOP la final (ca sa oprim SocketServer-ele pe server)
		threads.clear();
		for (Integer p: ports) {
			Thread th = new Thread(new ClientRequester(p, "stop"));
			threads.add(th);
			th.start();
		}
		
		// si le facem join...
		for (Thread th: threads) {
			try {
				th.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		System.out.println("All done...");
	}
	
	private static Random rand = new Random();
	private static String letters = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM";
	
	private static String randLocationName() {
		StringBuilder sb = new StringBuilder();
		int len = 5 + Math.abs(rand.nextInt()) % 10;
		for (int i = 1; i <= len; i++) {
			sb.append(letters.charAt(Math.abs(rand.nextInt()) % letters.length()));
		}
		return sb.toString();
	}
	
	// fara stop -> vor fi 5 din acelea la final
	// pentru a opri thread-urile dedicate celor 5 orase
	// Cereri pentru gazde -> "add" (adaugare locatie) si "stop"
	// Cereri pentru clienti -> "get all", "book"
	public static String getNewRequest(int typeOfReq, int port) {
		String msg = null;
		if (typeOfReq == 0) {
			msg = "get all";
		}else if (typeOfReq == 1) {
			
			// pentru gazde, care adauga locatii *noi* (nume la intamplare)
			String location = randLocationName();
			int startMonth = 1 + Math.abs(rand.nextInt()) % 11;
			int endMonth = Math.min(12, startMonth + Math.abs(rand.nextInt()) % (12 - startMonth + 1));
			int freePos = 1 + Math.abs(rand.nextInt()) % 5;
			msg = "add " + location + " " + startMonth + " " + endMonth + " " + freePos;
		}else {
			msg = "book";
		}
		return msg;
	}

}
