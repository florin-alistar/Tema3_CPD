import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/*
*   Clasa Worker de thread folosita pentru
*      a simula situatia in care avem multi clienti (mai multe threaduri)
 */
public class ClientRequester implements Runnable {

	private int port;
	private String msgRequest;
	
	public ClientRequester(int port, String request) {
		this.port = port;
		this.msgRequest = request;
	}
	
	@Override
	public void run() {
		try (Socket socket = new Socket("127.0.0.1", port)){
			
			InputStream inputStream = socket.getInputStream();
			OutputStream outputStream = socket.getOutputStream();
			PrintWriter writer = new PrintWriter(outputStream, true);
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			
			// Cerere->raspuns unic (chiar si pe mai multe linii) pentru add, get_all si stop
			// EOF este un terminator "custom" pe care il definim noi
			if (!this.msgRequest.equals("book")) {
				writer.println(this.msgRequest);
	            String answer = null;
	            while (!(answer = reader.readLine()).equals("EOF")) {
	            	System.out.println(answer + " from port " + port + " AT STEP " + Start.getCurrentStep() + " (somewhat)");
	            }
			}else {
				// pentru booking in schimb, trebuie mai intai cerute locatiile disponibile,
				//		apoi analizat raspunsul pentru a alege una la intamplare, iar apoi cerere din nou
				writer.println(msgRequest);
				String str = null;
				// libere sau nu (daca trimitem cerere la una nelibera, atunci vom primi mesaj ca nu e libera)
				List<String> answers = new ArrayList<String>();
				while (!(str = reader.readLine()).equals("EOF")) {
					answers.add(str);
				}
				
				// nu avem nicio locatie libera
				if (answers.get(0).contains("Nothing")) {
					System.out.println(answers.get(0));
				}else {
					// alegem una la intamplare (indiferent daca e libera sau nu; oricum, daca nu e, nu se va modifica lista si vom primi mesaj)
					Random rand = new Random();
					int index = Math.abs(rand.nextInt()) % answers.size();
					// nume_locatie luna_de_start luna_de_final locuri_libere
					String[] locationData = answers.get(index).split(" ");
					String locName = locationData[0];
					int startMonth = Integer.parseInt(locationData[1]);
					int endMonth = Integer.parseInt(locationData[2]);
					int startBookingMonth = startMonth + Math.abs(rand.nextInt() % (endMonth - startMonth + 1));
					int bookingDuration = 1 + Math.abs(rand.nextInt()) % (endMonth - startBookingMonth + 1);
					writer.println(locName + " " + startMonth + " " + bookingDuration);
					String answer = reader.readLine();
					System.out.println(answer + " from port " + port + " AT STEP " + Start.getCurrentStep());
				}
			}
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
