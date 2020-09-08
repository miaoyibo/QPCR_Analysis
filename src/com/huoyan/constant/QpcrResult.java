package com.huoyan.constant;

public enum QpcrResult {

	Negative("Negative","数据汇总",null),
	Positive("Positive","数据汇总",null),
	ReRq("RE+RQ","数据汇总","RE"),
	Re("灰区","数据汇总","RE"),
	InvalidVic("VIC失控","数据汇总","RE"),
	Unknow("Unknown","数据汇总",null);
	
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
