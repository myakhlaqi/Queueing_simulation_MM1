package s2;


class Event implements Cloneable{
	enum EventType {IN,Q,Srv,OUT, Dropped};//0 generation and 1 departuere
	//private static int count;
	private int id;
	
	public void setId(int id) {
		this.id = id;
	}
	public int getId() {
		return this.id;
	}
	//private PacketType type;
	//enum PacketType {PACKET_GENERATION,PACKET_DEPARTUER};
	private double ArrivalTime;
	private double ServiceTime;
	private double DepartureTime;
	private double waitingTimeGlobal;
	private double waitingTimeMLFQ1;//higher priority
	private double waitingTimeMLFQ2;
	private double waitingTimeMLFQ3;//lower priority
	public void setWaitingTime(double waitingTime) {
		this.waitingTimeGlobal = waitingTime;
	}
	public double getWaitingTime() {
		return waitingTimeGlobal;
	}
	//public double stime;
	private EventType type;//0 means generation and 1 means departurer

	public Event(double time,EventType type) {
		this.type = type;
		this.ArrivalTime = time;
	}
	public Event(double time) {
		this.type = EventType.IN;
		this.ArrivalTime = time;
	}


	public EventType getType() {
		return type;
	}
	public void setType(EventType type) {
		this.type = type;
	}
	public double getTime() {
		return ArrivalTime;
	}
	public void setTime(double time) {
		this.ArrivalTime = time;
	}
	public String toString() {

		/*return String.format(" <p%-3d,%-4.2f,%-4.2f,%-4.2f,%-4.2f,%3s>",id,this.ArrivalTime,
				this.getWaitingTime(),this.ServiceTime,this.DepartureTime,type);*/
		return String.format(" <p%-3d,%-4.2f,%3s>",id,this.ArrivalTime,
		type);

				
		/*return String.format(" <p%-3d,%-4.2f,%3s>",id,this.ArrivalTime,
				
				(this.type==EventType.GEN)?"IN":"OUT");*/
	}
	public double getServiceTime() {
		return ServiceTime;
	}
	public void setServiceTime(double serviceTime) {
		ServiceTime = serviceTime;
	}
	public double getDepartureTime() {
		return DepartureTime;
	}
	public void setDepartureTime(double departureTime) {
		DepartureTime = departureTime;
	}
	@Override
	public Object clone() {
	    Event e = null;
	        e = new Event(this.getTime());
	        e.setId(this.id);
	        //e.setServiceTime(this.getServiceTime());
	        //e.setWaitingTime(this.getWaitingTime());
	        //e.setDepartureTime(this.getDepartureTime());
	    return e;
	}	
}

