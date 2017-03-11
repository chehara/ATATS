public class Pair<Long,Double> {
	public long m_hash;
	public double m_time;
	
	public Pair(long hash, double time){
		this.m_hash = hash;
		this.m_time = time;
	}
	public Pair(){
		
	}	
	
	public long getHash(){
		return m_hash;
	}
	
	public double getTime() {
		return m_time;
	}

}
