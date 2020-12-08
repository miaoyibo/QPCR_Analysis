package com.huoyan.model;

import java.util.ArrayList;
import java.util.List;

public class ControlStatus {
	boolean blankFlag;
	int positiveNum;
	int errorPositiveFlag;
	int warningPositiveFlag;
	
	List<QpcrModel> positiveControls=new ArrayList<QpcrModel>();
	
	public ControlStatus(boolean blankFlag, int positiveNum, int errorPositiveFlag, int warningPositiveFlag) {
		super();
		this.blankFlag = blankFlag;
		this.positiveNum = positiveNum;
		this.errorPositiveFlag = errorPositiveFlag;
		this.warningPositiveFlag = warningPositiveFlag;
	}
	public boolean isBlankFlag() {
		return blankFlag;
	}
	public void setBlankFlag(boolean blankFlag) {
		this.blankFlag = blankFlag;
	}
	public int getPositiveNum() {
		return positiveNum;
	}
	public void setPositiveNum(int positiveNum) {
		this.positiveNum = positiveNum;
	}
	public int getErrorPositiveFlag() {
		return errorPositiveFlag;
	}
	public void setErrorPositiveFlag(int errorPositiveFlag) {
		this.errorPositiveFlag = errorPositiveFlag;
	}
	public int getWarningPositiveFlag() {
		return warningPositiveFlag;
	}
	public void setWarningPositiveFlag(int warningPositiveFlag) {
		this.warningPositiveFlag = warningPositiveFlag;
	}
	public List<QpcrModel> getPositiveControls() {
		return positiveControls;
	}
	public void setPositiveControls(List<QpcrModel> positiveControls) {
		this.positiveControls = positiveControls;
	}
	
	


}
