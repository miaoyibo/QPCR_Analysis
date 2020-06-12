package com.huoyan.convert;

import org.apache.commons.lang3.math.NumberUtils;

public class PCHandle implements Handle{

	@Override
	public String getHandleName() {
		return "�����ʿ�";
	}

	@Override
	public String chargeResult(String fam, String vic) {
		String result=null;
		if(NumberUtils.isCreatable(fam)&&NumberUtils.isCreatable(vic)&&Double.parseDouble(fam)<=32&&Double.parseDouble(vic)<=32) {
			result="�����ڿ�";
		}else {
			result="ʧ��";
		}
		return result;
	}

	@Override
	public String getCriteria() {
		return "Ctֵ<=32";
	}

}
