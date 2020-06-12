package com.huoyan.convert;

import org.apache.commons.lang3.math.NumberUtils;

public class PCHandle implements Handle{

	@Override
	public String getHandleName() {
		return "阳性质控";
	}

	@Override
	public String chargeResult(String fam, String vic) {
		String result=null;
		if(NumberUtils.isCreatable(fam)&&NumberUtils.isCreatable(vic)&&Double.parseDouble(fam)<=32&&Double.parseDouble(vic)<=32) {
			result="正常在控";
		}else {
			result="失控";
		}
		return result;
	}

	@Override
	public String getCriteria() {
		return "Ct值<=32";
	}

}
