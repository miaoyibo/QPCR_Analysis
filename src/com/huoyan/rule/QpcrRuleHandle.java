package com.huoyan.rule;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.huoyan.model.CriteriaModel;
import com.huoyan.model.QpcrModel;

public class QpcrRuleHandle {
	
	
	public static boolean charge(CriteriaModel criteriaModel, List<QpcrModel> qpcrlist) {
		for (QpcrModel qpcrModel : qpcrlist) {
			if (criteriaModel.getFam()!=null&&qpcrModel.getFam()!=null&&NumberUtils.isCreatable(qpcrModel.getFam())&&compare(Double.parseDouble(qpcrModel.getFam()),criteriaModel.getFam(),criteriaModel.getFamSymbol())) {
				return true;
			}
			if (criteriaModel.getSpecialFamValue()!=null&&qpcrModel.getFam()!=null&&criteriaModel.getSpecialFamValue().equals(qpcrModel.getFam().trim())) {
				return true;
			}
			if (criteriaModel.getVic()!=null&&qpcrModel.getVic()!=null&&NumberUtils.isCreatable(qpcrModel.getVic())&&compare(Double.parseDouble(qpcrModel.getVic()),criteriaModel.getVic(),criteriaModel.getVicSymbol())) {
				return true;
			}
			if (criteriaModel.getSpecialVicValue()!=null&&qpcrModel.getVic()!=null&&criteriaModel.getSpecialVicValue().equals(qpcrModel.getVic().trim())) {
				return true;
			}
		}
		return false;
	}
	public static boolean charge(CriteriaModel criteriaModel, QpcrModel q) {
		 List<QpcrModel> qpcrlist=new ArrayList<>();
		 qpcrlist.add(q);
		 return charge(criteriaModel,qpcrlist);
	}
	private static boolean compare(double n1, double n2,String sign) {
		switch (sign) {
		case ">":
			return n1>n2;
		case ">=":
			return n1>=n2;
		case "<":
			return n1<n2;
		case "<=":
			return n1<=n2;
		default:
			return false;
		}
	}
	
	public static String formatFam(String s) {
		if (s!=null) {
			s=s.trim().equals("-")?"NoCt":s;
		}
		return s;
	}
	public static String formatVic(String s) {
		if (StringUtils.isEmpty(s)||s.trim().equals("-")) {
			return "NoCt";
		}
		return s;
	}

}
