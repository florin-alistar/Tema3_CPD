import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Collection;

/*
 * Clasa Worker pentru a raspunde la o singura cerere a unui client
 * Este creata de thread-ul parinte CityLevelRequestHandler dintr-un oras anume
 * Practic, ea trateaza o *singura* cerere pentru un oras anume (cel din thread-ul parinte)
 */
public class IndividualRequestHandler implements Runnable {

	private Socket socket;
	private InputStream inputStream;
	private OutputStream outputStream;
	private CityLevelRequestHandler parent;
	
	// SynchronizedList trimisa de thread-ul parinte
	private Collection<Journey> availableJourneys;
	
	public void setParent(CityLevelRequestHandler parent) {
		this.parent = parent;
	}
	
	public void setAvailableJourneys(Collection<Journey> availableJrs) {
		this.availableJourneys = availableJrs;
	}
	
	public IndividualRequestHandler(Socket socket) {
		this.socket = socket;
		try {
			this.inputStream = socket.getInputStream();
			this.outputStream = socket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Adaugare destinatie -> functie pentru gazde
	// Datele vin de pe client sub forma de String, cu informatiile separate prin spatiu
	private void handleAddNewDestinationRequest(String req) {
		String[] parts = req.split(" ");
		String locationName = parts[1];
		int startMonth = Integer.parseInt(parts[2]);
		int endMonth = Integer.parseInt(parts[3]);
		int freePositions = Integer.parseInt(parts[4]);
		synchronized (this.availableJourneys) {
			Journey journey = new Journey(locationName, startMonth, endMonth, freePositions);
			this.availableJourneys.add(journey);
			PrintWriter writer = new PrintWriter(this.outputStream, true);
			writer.println("Added " + locationName + " for city " + parent.getCityName() + " with " + freePositions + " free positions");
			writer.println("EOF");
		}
	}
	
	/*
	 * Functie de tratare a cererii de rezervare
	 * Pasi:
	 * 		1. Se trimit clientului locatiile (ca String-uri, o locatie pe linie)
	 *      2. Clientul primeste locatiile si alege una la intamplare
	 *      3. Clientul trimite cererea de rezervare efectiva
	 *      4. Serverul ii aloca Journey si decrementeaza numarul de locatii libere (poate si o eliminam daca chiar vrem)
	 */
	private void handleJourneyBookingRequest(BufferedReader reader) {
		PrintWriter writer = new PrintWriter(this.outputStream, true);
		
		// Atunci cand se vrea sa se faca rezervare, trimitem mai intai clientului locatiile disponibile
		//		ca el sa poata rezerva ceva valid (o locatie care exista)
		//      si el isi va alege una la intamplare (codul e in aplicatia client)
		// Desigur, se poate ca lista aceasta sa fie goala
		//		ea nu va fi goala de la inceput pentru ca o sa punem cateva valori initiale,
		//		dar pe parcurs ar putea sa ajunga goala
		
		
		// Sincronizam aici pentru a trimite doar locatiile care erau la inceputul cererii,
		//		nu si altele care ar putea aparea in timpul for (adaugare din alta parte)
		synchronized(this.availableJourneys) {
			if (this.availableJourneys.isEmpty()) {
				writer.println("Nothing in " + parent.getCityName());
				writer.println("EOF");
				return;
			}
			for (Journey available: this.availableJourneys) {
				writer.println(available.simpleRep());
			}
			writer.println("EOF");
		}
		
		// acum rezervam *efectiv*
		String bookRequest = null;
		try {
		    bookRequest = reader.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		synchronized(this.availableJourneys) {
			// Cererea de rezervare va fi sub forma      nume_locatie start_luna durata_in_luni
			String[] reqParts = bookRequest.split(" ");
			String name = reqParts[0];
			Journey foundJourney = null;
			int initialFree = 0;
			int finalFree = 0;
			// Gasim Journey al carei nume se potriveste cu ce a trimis clientul
			//		SI *care poate fi booked*
			for (Journey j: this.availableJourneys) {
				if (j.getExactLocation().equals(name)) {
					initialFree = j.getFreeSpots();
					if (j.book()) {
						foundJourney = j;
						finalFree = j.getFreeSpots();
					}
				}
			}
			
			// daca locatia dorita de client este disponibila
			if (foundJourney != null) {
				writer.println("Booked " + name + " from " + parent.getCityName() + 
						" from month " + reqParts[1] + " with duration " + reqParts[2] + " months! --> "
						+ "     Initial: " + initialFree + ", Final: " + finalFree);
				
				// Aici am putea eventual si elimina locatia din lista daca am ajuns la 0
				//		dar nu facem asta, ca sa avem mai multe mesaje de tipul "Sorry, but ... is not available anymore"
				if (finalFree == 0) {
					//this.availableJourneys.remove(foundJourney);
				}
			}else {
				// Cand clientul a facut cererea era deja prea tarziu (nu mai erau locuri libere pentru acea destinatie)
				writer.println("Sorry, but " + name + " from " + parent.getCityName() + " is not available anymore...");
			}
			writer.println("EOF");
		}
	}
	
	// Cerere de get_all pentru clienti
	// Scriem o linie "EOF" ca sa stie clientul cand sa iasa din bucla while pe
	//		care o face ca sa citeasca mai multe linii (verificarea cu != null nu merge, am incercat)
	private void handleGetAllRequest() {
		PrintWriter writer = new PrintWriter(this.outputStream, true);
		boolean any = !this.availableJourneys.isEmpty();
		writer.println("   " + this.availableJourneys.size() + " journeys available in " + parent.getCityName());
		synchronized(this.availableJourneys) {
			int i = 1;
			for (Journey available: this.availableJourneys) {
				writer.println("        " + i + ". " + parent.getCityName() + " -> " + available.toString());
				i++;
			}
			if (!any) {
				writer.println("No locations available for " + parent.getCityName());
			}
			writer.println("EOF");
		}
	}
	
	@Override
	public void run() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String request = null;
		try {
			request = reader.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Server got request " + request + " on port " + this.parent.getPort());
		
		// In functie de comanda primita, executam o functie anume...
		if (request.equals("stop")) {
			// Thread-ul parinte este scos din bucla while prin inchiderea fortata a ServerSocket-ului
			//		(daca nu am face asa, parintele ar ramane blocat in while pentru ca nu ar avea acces la InputStream
			//			ca sa stie cand sa iasa -> nu are acces pentru ca thread-ul copil se ocupa de cereri efective, nu el)
			PrintWriter writer = new PrintWriter(this.outputStream, true);
			writer.println("OVER");
			writer.println("EOF");
			parent.closeSocket();
		}else if (request.equals("get all")) {
			this.handleGetAllRequest();
		}else if (request.startsWith("add")) {
			this.handleAddNewDestinationRequest(request);
		}else {
			this.handleJourneyBookingRequest(reader);
		}
		
		try {
			this.inputStream.close();
			this.outputStream.close();
			this.socket.close();
		}
		catch (Exception ex){
			System.out.println(ex.getMessage() + " at closing socket at server side handler...");
		}
	}
}
