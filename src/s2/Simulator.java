package s2;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import s2.Event.EventType;
import javax.swing.*;

public class Simulator {
	private int row;// packet sequence number
	private int mst;// max simulation time
	// private LinkedList<Event> pg;
	private Queue<Event> queue;// Queue Structure
	private Queue<Result> resultList;// to save the results
	private int numberOfServers;// number of sink or servers
	private Event nextDeparture;
	private Event nextArrival;
	private double simclock;
	private double mu;
	private int npa;// number of packet arrival
	private int npd;// number of packet departures
	private int tpl;// total packet loss
	private int tdl;// total delay
	private int maxqsize;// Max Q size
	private int tqs;
	private Result currentResult;
	private double lamda;// is the expected (average) number of arrivals per hour,
							// or day, or whatever units t is measured in.
	private int id;// event id

	enum ServerStatus {
		IDEL, BUSY
	};

	private ServerStatus ss;// server status
	/////// metrices////////////
	double avgDelay = 0;// averageDelay=total delay/npd
	double plr;// packetLossRate;//total packet loss/npa
	double avgBuffer;// averageBuffer;// total Q size / time it is equalent to Queue size or buffer size
	private double dt;


	//////////////////////////////
	public Simulator(double lamda, double mu, int endTime, int queueSize) {
		//////////// initializer////////////
		this.mu = mu;
		this.id = 1;
		this.lamda = lamda;
		this.mst = endTime;
		// pg = new LinkedList<Event>();
		this.numberOfServers = 1;
		maxqsize = queueSize;
		queue = new LinkedList<Event>();
		resultList = new LinkedList<Result>();
	}

	public void init() {
		// this.load+=x;// X axis on the chart
		ss = ServerStatus.IDEL;
		double sum = 0;
		/*
		 * for (int i = 0; i <=100000*mst; i++) { double iat =
		 * poissonExponential(lamda*x); sum += iat; Event e = new Event(sum,
		 * Event.EventType.IN); e.setId(i); pg.add(e); //npd=1; }
		 */
		// pg.sort((e1,e2) -> Double.compare(e1.getTime(),e2.getTime()));

		////////// SCHEDULING PARAMETER////////////////
		simclock = 0;
		dt = 0;
		nextArrival = new Event(0);
		nextDeparture = new Event(Double.POSITIVE_INFINITY, Event.EventType.OUT);
		nextDeparture.setDepartureTime(Double.POSITIVE_INFINITY);
		currentResult = new Result();
		////////// LOCAL VAR//////////
		// load=0;
		npa = 0;
		npd = 0;
		tpl = 0;
		tdl = 0;
		tqs = 0;

		/////////////// reset performance metrics ////////////////////
		avgDelay = 0;// averageDelay=total delay/npd
		plr = 0;// packetLossRate;//total packet loss/npa
		avgBuffer = 0;// averageBuffer;// total buffer / time

	}

	public Event scheduler(scheduling_policy s) {

		if (s == scheduling_policy.FIFO) {

			double iat = exponential(lamda);
			// System.out.println("iat:"+iat);
			nextArrival = new Event(simclock + iat, Event.EventType.IN);
			nextArrival.setId(id++);

			// nextArrival = pg.element();
			if (nextArrival.getTime() < nextDeparture.getDepartureTime()) {
				// pg.remove();
				return nextArrival;
			} else {
				return nextDeparture;
			}

		}
		return null;
	}

	public void pg() {
		npa++;
		// System.out.println(getLog(EventType.IN));
		// nextDeparture.setTime(nextArrival.getTime());

		if (ServerStatus.IDEL == ss) {
			currentResult.otype = EventType.Srv;

			ss = ServerStatus.BUSY;
			nextDeparture = (Event) nextArrival.clone();
			nextDeparture.setServiceTime(exponential(mu));
			nextDeparture.setDepartureTime(simclock + nextDeparture.getServiceTime());
			nextDeparture.setWaitingTime(0);
			// nextDeparture.setTime(simclock + serviceExponential(1.0 / mu));
			nextDeparture.setType(EventType.OUT);
		}

		else {
			if (queue.size() == maxqsize) {
				currentResult.otype = EventType.Dropped;
				tpl++;
				// nextDeparture.setWaitingTime(0);
				// nextDeparture.setDepartureTime(Double.MAX_VALUE);
				// System.out.println(String.format("%12s%s%s", " ", nextArrival.toString(),
				// "Droped!"));
			} else {
				currentResult.otype = EventType.Q;
				tqs += queue.size() * (nextDeparture.getTime() - simclock);
				queue.add(nextArrival);
			}
		}

	}

	public void pd() {
		npd++;
		// System.out.println(getLog(EventType.OUT));
		currentResult.otype = EventType.OUT;
		if (!queue.isEmpty()) {

			/////////////// update performance metrices/////////
			tqs += queue.size() * (nextDeparture.getTime() - simclock);
			tdl += simclock - queue.peek().getTime();

			nextDeparture = queue.remove();

			nextDeparture.setServiceTime(exponential(mu));
			nextDeparture.setDepartureTime(simclock + nextDeparture.getServiceTime());
			nextDeparture.setWaitingTime(simclock - nextDeparture.getTime());

			nextDeparture.setType(EventType.OUT);

		} else if (queue.isEmpty()) {
			ss = ServerStatus.IDEL;
			nextDeparture.setDepartureTime(Double.POSITIVE_INFINITY);
			// nextDeparture.setTime(Double.POSITIVE_INFINITY);
		}

	}

	public void updateResults(Event e) {
		currentResult.clock = simclock;
		// currentResult.otype=e.getType();
		currentResult.npd = npd;
		currentResult.npa = npa;
		currentResult.ndp = tpl;
		currentResult.qLength = queue.size();
		currentResult.ss = ss;
		currentResult.st = e.getServiceTime();
		currentResult.wt = e.getWaitingTime();

		avgDelay += e.getWaitingTime();
		// plr=(double)tpl;
		avgBuffer += (queue.size() * dt);

		currentResult.lost_ratio = (double) tpl / npa;
		currentResult.avgQLength = avgBuffer / simclock;
		if (npd != 0)
			currentResult.avgDelay = avgDelay / npd;

		/*
		 * if (e.getType() == EventType.IN) { if (ss == ServerStatus.IDEL)
		 * currentResult.otype = EventType.Srv; else { if (queue.size() == maxqsize) {
		 * currentResult.otype = EventType.Dropped; } else { currentResult.otype =
		 * EventType.Q; } }
		 * 
		 * } else { currentResult.otype = EventType.OUT; }
		 */
		Result r = new Result();
		r = (Result) currentResult.clone();
		resultList.add(r);

		/*
		 * avgDelay += tdl / npd;// averageDelay=total delay/npd plr += tpl / npa;//
		 * packetLossRate;//total packet loss/npa avgBuffer += tqs / simclock;//
		 * averageBuffer;// total buffer / time
		 */

	}

	public void updateClock(Event e) {
		dt = simclock;
		if (e.getType() == EventType.OUT)
			simclock = e.getDepartureTime();
		else
			simclock = e.getTime();

		dt = simclock - dt;

	}

	public String getLog(EventType e) {
		return String.format("%-3d) ;t=%-10.2f;%s-;%-4d;%s", row++, simclock,
				(e == EventType.IN) ? nextArrival : nextDeparture, npd, queue);

	}

	public static double exponential(double lambda) {
		Random r = new Random();
		double x = Math.log(1 - r.nextDouble()) / (-lambda);
		return x;
	}

/*	private static double getPoissonRandom(double mean) {
		Random r = new Random();
		double L = Math.exp(-mean);
		double k = 1;
		double p = 1.0;
		do {
			p = p * r.nextDouble();
			k++;
		} while (p > L);
		return (k - 1);
	}

	private static double NonZeroPoissonRandom(double mean) {
		int k = 1;
		double t = Math.exp(-mean) / ((1 - Math.exp(-mean)) * mean);
		double s = t;
		double u = Math.random();
		while (s < u) {
			k = k + 1;
			t = t * mean / k;
			s = s + t;
		}
		return k;
	}
*/
	public enum scheduling_policy {
		FIFO, SRTM, STF, RR
	};

	public static void main(String[] args) throws IOException {
		PrintWriter out = null;

		try {
			double sum=0;
			  for (int i = 0; i < 100; i++) { 
				  //double x=exponential(1/10.0);
				  double x=exponential(10); System.out.println(x+"\tavg="+((sum+=x)/i)); }
			 
			/*
			 * String m =
			 * JOptionPane.showInputDialog("Enter lambda,mu,simulation time,and queue size:"
			 * + "\ne.g: 3,4,100,10"); String [] r=m.split(","); double
			 * lambda=Double.parseDouble(r[0]); double mu=Double.parseDouble(r[1]); int
			 * mst=Integer.parseInt(r[2]); int qsize=Integer.parseInt(r[3]);
			 */

			// Simulator s = new Simulator(lambda,mu,mst, qsize);
			File dir = new File("logs");
			if (!dir.exists()) {
				dir.mkdir();
			}

			File file = new File("logs/out.csv");
			out = new PrintWriter(file);

			for (int i = 1; i <= 1; i++) {
				int load = i*10;
				Simulator s = new Simulator(load,12, 1000, 10);
				
				s.init();
				s.run();
				s.saveLog(load,out);

			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} finally {
			out.flush();
			out.close();
		}

		/*
		 * File f=s.saveLog(); JOptionPane.showMessageDialog(null,
		 * "Simulation complete.\nLog: logs/out.csv"); Desktop desktop =
		 * Desktop.getDesktop(); desktop.open(f);
		 */

	}

	private void saveLog(int load, PrintWriter out) {

			String header = String.format("%s\t%-6s\t%-5s\t%4s\t%4s\t%4s\t%4s\t%4s\t" + "%-4s\t%-4s\t%s\t%s\t%-1s",
					"load", "clock", "otype", "ss", "qLength", "npa", "npd", "dropped", "wt", "st", "lost%", "avgDelay",
					"avgQLength");
			out.println(header);
			 System.out.println(header);
			Result lastResult = new Result();
			for (; resultList.iterator().hasNext();) {

				lastResult = resultList.remove();
				 System.out.println(lastResult.toString());
				out.println(load + "\t" + lastResult.toString());
			}
			System.out.println("Ended\t" + load + "\t" + lastResult.toString());
			out.println("Ended\t" + load + "\t" + lastResult.toString());
			// System.out.println("Simulation complete.");
			System.out.println("Log: logs/out.csv");
		
	}

	public void run() {
		while (simclock <= mst) {// for
			Event e = scheduler(Simulator.scheduling_policy.FIFO);
			updateClock(e);

			if (e.getType() == Event.EventType.OUT) {
				pd();
			} else if (e.getType() == Event.EventType.IN) {
				pg();
			}

			updateResults(e);


		}
	}
}
