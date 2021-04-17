
/*
 * O locatie particulara unui oras
 * Campul de oras nu apare aici pentru ca el se afla in CityLevelRequestHandler,
 *    care contine o lista List<Journey> sincronizata (din biblioteca Java)
 */
public class Journey {

	private String exactLocation;
	private int startMonth;
	private int endMonth;
	private int freeSpots;
	
	public Journey(String exactLocation, int startMonth, int endMonth, int free) {
		this.startMonth = startMonth;
		this.endMonth = endMonth;
		this.exactLocation = exactLocation;
		this.freeSpots = free;
	}

	/*
	 * Returneaza false daca nu poate fi booked
	 * Listele de <Journey> sunt partajate intre thread-uri, si deci si obiectele dinauntrul lor (de tip Journey)
	 * Aceasta functie trebuie executata de un singur thread la un moment dat (ca acest Journey sa fie rezervat doar de clientul care a 
	 *    trimis primul cererea) -> de aici, synchronized
	 */
	public synchronized boolean book() {
		if (freeSpots == 0) {
			return false;
		}
		
		this.freeSpots--;
		return true;
	}
	
	public String simpleRep() {
		return exactLocation + " " + startMonth + " " + endMonth + " " + freeSpots;
	}
	
	@Override
	public String toString() {
		return exactLocation + " from month " + startMonth + " to " + endMonth + " with " + freeSpots + " free spots...";
	}
	
	public String getExactLocation() {
		return exactLocation;
	}

	public int getStartMonth() {
		return startMonth;
	}

	public int getEndMonth() {
		return endMonth;
	}

	public int getFreeSpots() {
		return freeSpots;
	}
}
