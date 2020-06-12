package com.huoyan.convert;

import org.apache.commons.lang3.math.NumberUtils;

public class NCHandle implements Handle{
	
	private int num;
	

	public NCHandle(int num) {
		super();
		this.num = num;
	}

	@Override
	public String getHandleName() {
		return "空白质控"+num;
	}

	@Override
	public String chargeResult(String fam, String vic) {
		String result=null;
		if(NumberUtils.isCreatable(vic)&&Double.parseDouble(vic)<=32) {
			result="失控";
		}else if(NumberUtils.isCreatable(fam)&&Double.parseDouble(fam)<=40) {
			result="让步接受";
		}else {
			result="正常在控";
		}
		return result;
	}

	@Override
	public String getCriteria() {
		return "NoCt/NoCt";
	}

}
