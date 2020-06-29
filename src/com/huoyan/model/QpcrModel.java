package com.huoyan.model;

public class QpcrModel implements Cloneable{
	
	

	private String version;
	
	private String date;
	
	private String loc;
	
	private String fam;
	
	private String vic;
	
	private String result;
	
	private String remark;
	
	private String sampleId;
	
	private String type;

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getLoc() {
		return loc;
	}

	public void setLoc(String loc) {
		this.loc = loc;
	}

	public String getFam() {
		return fam;
	}

	public void setFam(String fam) {
		this.fam = fam;
	}

	public String getVic() {
		return vic;
	}

	public void setVic(String vic) {
		this.vic = vic;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getSampleId() {
		return sampleId;
	}

	public void setSampleId(String sampleId) {
		this.sampleId = sampleId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	@Override
	public QpcrModel clone()  {
		QpcrModel qpcrModel=null;
		try {
			qpcrModel=(QpcrModel) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return qpcrModel;
	}

}
