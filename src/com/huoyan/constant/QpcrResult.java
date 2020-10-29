package com.huoyan.constant;

public enum QpcrResult {

	Negative("Negative","summary",null),
	Positive("Positive","summary",null),
	ReRq("Positive  pending","summary","RE"),
	Re("gray area","summary","RE"),
	InvalidVic("VIC lost control","summary","RE"),
	InvalidFamVic("FAM,VIC no value","summary","RE"),
	Unknow("Unknown","summary",null);
	
	private String value;
	private String group;
	private String next;

	private QpcrResult(String value,String group,String next) {
		this.value = value;
		this.group=group;
		this.next=next;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getNext() {
		return next;
	}

	public void setNext(String next) {
		this.next = next;
	}
	
}
