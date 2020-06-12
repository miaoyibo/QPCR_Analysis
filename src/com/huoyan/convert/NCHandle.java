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
		return "�հ��ʿ�"+num;
	}

	@Override
	public String chargeResult(String fam, String vic) {
		String result=null;
		if(NumberUtils.isCreatable(vic)&&Double.parseDouble(vic)<=32) {
			result="ʧ��";
		}else if(NumberUtils.isCreatable(fam)&&Double.parseDouble(fam)<=40) {
			result="�ò�����";
		}else {
			result="�����ڿ�";
		}
		return result;
	}

	@Override
	public String getCriteria() {
		return "NoCt/NoCt";
	}

}
