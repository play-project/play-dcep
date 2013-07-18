package de.s_node.bsc.borealis;

public class BorealisScoreLoad implements Comparable<BorealisScoreLoad>{

	private String  name;
	private double load;
	
	public BorealisScoreLoad(String name, double load) {
		super();
		this.name = name;
		this.load = load;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getLoad() {
		return load;
	}
	public void setLoad(double load) {
		this.load = load;
	}
	
	@Override
	public int compareTo(BorealisScoreLoad o) {
		if(load < o.getLoad()){
			return -1;
		}else if(this.load == o.getLoad()){
			return 0;
		}else{
			return 1;
		}
	}
}
