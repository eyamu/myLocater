package com.wpr.mylocator;

public class Journey {
	private String from;
	private String to;
	private String longtudes;
	private String latitudes;
	
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	
	public String getLatitudes() {
		return latitudes;
	}
	public void setLatitudes(String latitudes) {
		this.latitudes = latitudes;
	}
	public String getLongtudes() {
		return longtudes;
	}
	public void setLongtudes(String longtudes) {
		this.longtudes = longtudes;
	}
	
	public String toString(){
		return "Your journey from "+getFrom()+" to "+getTo()+" has started";
	}

}
