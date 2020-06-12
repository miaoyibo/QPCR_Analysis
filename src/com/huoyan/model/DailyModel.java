package com.huoyan.model;

public class DailyModel {
	private String date;
	private String version;
	
	private String name;
	
	private String expect;
	
	private String real;
	
	private String charge;

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getExpect() {
		return expect;
	}

	public void setExpect(String expect) {
		this.expect = expect;
	}

	public String getReal() {
		return real;
	}

	public void setReal(String real) {
		this.real = real;
	}

	public String getCharge() {
		return charge;
	}

	public void setCharge(String charge) {
		this.charge = charge;
	}
	
	public String getValue(int num) {
		if(num==0) {
			return version;
		}
		if(num==1) {
			return name;
		}
		if(num==2) {
			return expect;
		}
		if(num==3) {
			return real;
		}
		if(num==4) {
			return charge;
		}
		return "";
	}
	

}
