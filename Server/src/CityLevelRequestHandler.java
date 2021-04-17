import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * 
 * @author Florin
 * Exista 5 orase (Berlin, Barcelona, Atena, Istanbul, Oslo)
 * Pentru fiecare cream un thread separat care sa asculte pentru cereri legate numai de acel oras
 * 		-> porturile sunt, in ordinea asta, 50, 60, 70, 80, 90
 * Aceasta clasa reprezinta acele threaduri
 * 		-> ce fac ele: 1. asculta in continuu dupa cereri (*doar pentru un oras -> cityName*)
 * 					   2. cand primesc una, creeaza un thread IndividualRequestHandler care trateaza cererea
 * 					   3. ca sa le scoatem din while(true), 
 * 							thread-urile copil IndividualRequestHandler vor apela metoda "closeSocket" a acestei clase (la final,
 * 																dupa cererile utile, asa cum se vede in clasa Start din apl Client)
 *				
 */
public class CityLevelRequestHandler implements Runnable {

	private String cityName;
	private int port;
	private ServerSocket serverSocket;
	
	// Aceasta lista e, bineinteles, SynchronizedList
	private Collection<Journey> availableJourneys;
	
	public CityLevelRequestHandler(int port, String cityName, Collection<Journey> journeysInCity) {
		this.port = port;
		this.cityName = cityName;
		this.availableJourneys = journeysInCity;
	}
	
	public String getCityName() {
		return this.cityName;
	}
	
	public void closeSocket() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		try {
			// Creare ServerSocket
		    serverSocket = new ServerSocket(port);
			while (true) {
				// Acceptare conexiune
				Socket socket = serverSocket.accept();
				
				// Dam unui thread copil responsabilitatea de a gestiona cererea
				//		pentru ca acest thread (CityLevelRequestHandler) sa poate asculta in continuare
				//		in timp ce copiii lucreaza
				IndividualRequestHandler reqHandler = new 
						IndividualRequestHandler(socket);
				
				// Setam parintele thread-ului copil si ii trimitem si lista de locatii disponibile pentru orasul curent
				//		(transmitere prin referinta -> lista partajata)
				reqHandler.setParent(this);
				reqHandler.setAvailableJourneys(this.availableJourneys);
				Thread th = new Thread(reqHandler);
				th.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
