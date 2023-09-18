package s2;
 
import s2.Event.EventType;

public class Result implements Cloneable {
	public double clock;
	public EventType otype;
	public Simulator.ServerStatus ss;
	public int qLength;
	public int npa ;
	public int npd;
	public int ndp; // represents the number of droped packet
	public double wt;
	public double st;
	public double lost_ratio;
	public double avgDelay;
	public double avgQLength;
	
	public String toString() {
		return String.format("%5.4f\t%s\t%5s\t%5d\t%5d\t%5d\t%5d\t"+
	"%4.3f\t%4.3f\t%6.2f\t%6.3f\t%6.3f",clock,otype,ss,qLength,
	npa,npd,ndp,wt,st,lost_ratio,avgDelay,avgQLength);
	}

	@Override
	public Object clone() {
	    Result user = null;
	    try {
		    
	        user = (Result) super.clone();
	    } catch (CloneNotSupportedException e) {
	        user = new Result();
	    }
  	    return user;
	}
}
 
