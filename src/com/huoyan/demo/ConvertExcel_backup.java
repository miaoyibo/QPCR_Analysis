package com.huoyan.demo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.huoyan.model.CriteriaModel;
import com.huoyan.model.QpcrModel;
import com.huoyan.util.DateUtil;
import com.huoyan.util.POIUtil;


public class ConvertExcel_backup {
     
	public static void main(String[] args) {
		

	}	
	public static List<QpcrModel> readQpcrResult(String qpcrfile,Map<String, CriteriaModel> criteria) {
		File file=new File(qpcrfile);
		List<QpcrModel> results=new ArrayList<>();
		Workbook workbook = POIUtil.getWorkBook(file);
		if (workbook != null) {
			Sheet sheet = workbook.getSheetAt(0);
			int lastRowNum = sheet.getLastRowNum();
			QpcrModel nc1=new QpcrModel();
			nc1.setFam(parseSheetValue(sheet, 33, 7));
			nc1.setVic(parseSheetValue(sheet, 34, 7));
			QpcrModel nc2=new QpcrModel();
			nc2.setFam(parseSheetValue(sheet, 53, 7));
			nc2.setVic(parseSheetValue(sheet, 54, 7));
			QpcrModel nc3=new QpcrModel();
			nc3.setFam(parseSheetValue(sheet, 192, 7));
			nc3.setVic(parseSheetValue(sheet, 193, 7));
			QpcrModel pc=new QpcrModel();
			pc.setFam(parseSheetValue(sheet, 167, 7));
			pc.setVic(parseSheetValue(sheet, 168, 7));
			List<QpcrModel> nclist =new ArrayList<QpcrModel>();
			nclist.add(nc1);
			nclist.add(nc2);
			nclist.add(nc3);			
			/*if(charge(nc1VicRow,32,"<")||chargeNC(nC2VicRow,32,"<")||chargeNC(nC3VicRow,32,"<")) {
				//整版重提
			}
			if(chargePC(pcFamRow,32,">")) {
				//整版重Q
			}
			boolean positive=false;
			if(chargeNC(nc1FamRow,38,"<=")||chargeNC(nC2famRow,38,"<=")||chargeNC(nC3FamRow,38,"<=")) {
				positive=true;
			}*/
			String remark="";			
			for (int rowNum = 1; rowNum <= lastRowNum; rowNum=rowNum+2) {
				Row famrow = sheet.getRow(rowNum);
				Row vicrow = sheet.getRow(rowNum+1);
				Cell famcell = famrow.getCell(7);
				Cell viccell = vicrow.getCell(7);
				QpcrModel qpcr=new QpcrModel();
				qpcr.setDate(DateUtil.formatDate(file.lastModified()));
				qpcr.setFam(POIUtil.getCellValue(famcell));
				qpcr.setVic(POIUtil.getCellValue(viccell));
				qpcr.setLoc(POIUtil.getCellValue(famrow.getCell(2)));							
				qpcr.setVersion(file.getName().substring(0,file.getName().lastIndexOf("-")));
				qpcr.setSampleId("");
				qpcr.setRemark(remark);
				if (StringUtils.isNotEmpty(remark)) {
					continue;
				}
				if(charge(criteria.get("重提"),qpcr)) {
					qpcr.setRemark("重提");
					continue;
				}
				if(charge(criteria.get("人工判读"),qpcr)) {
					//人工判读
					qpcr.setRemark("人工判读");
					continue;
				}
				if(charge(criteria.get("重Q"),qpcr)) {
					//重Q
					qpcr.setRemark("重Q");
					continue;
				}
				//阴性
				String result="阴性";
				qpcr.setResult(result);
			}
		}
		return results;
	}
	private static boolean charge(CriteriaModel criteriaModel, List<QpcrModel> qpcrlist) {
		for (QpcrModel qpcrModel : qpcrlist) {
			if (criteriaModel.getFam()!=null&&NumberUtils.isCreatable(qpcrModel.getFam())&&compare(Double.parseDouble(qpcrModel.getFam()),criteriaModel.getFam(),criteriaModel.getFamSymbol())) {
				return true;
			}
			if (criteriaModel.getSpecialFamValue()!=null&&criteriaModel.getSpecialFamValue().equals(qpcrModel.getFam())) {
				return true;
			}
			if (criteriaModel.getVic()!=null&&NumberUtils.isCreatable(qpcrModel.getVic())&&compare(Double.parseDouble(qpcrModel.getVic()),criteriaModel.getVic(),criteriaModel.getVicSymbol())) {
				return true;
			}
			if (criteriaModel.getSpecialVicValue()!=null&&criteriaModel.getSpecialVicValue().equals(qpcrModel.getVic())) {
				return true;
			}
		}
		return false;
	}
	private static boolean charge(CriteriaModel criteriaModel, QpcrModel q) {
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
	public static boolean chargeNC(Cell cell,double n,String sign) {
		if(cell.getStringCellValue().equals(" - ")) {
			return false;
		}else {
			if(sign.equals("<")) {
				return cell.getNumericCellValue()<n;
			}
			if(sign.equals("<=")) {
				return cell.getNumericCellValue()<=n;
			}
			if(sign.equals(">")) {
				return cell.getNumericCellValue()>n;
			}
			return false;
		}
		
	}
	public static boolean chargePC(Cell cell,double n,String sign) {
		if(!cell.getStringCellValue().equals(" - ")) {
			if(sign.equals(">")) {
				return cell.getNumericCellValue()>n;
			}
			if(sign.equals(">=")) {
				return cell.getNumericCellValue()>=n;
			}
			
		}
		return true;
	}
	
	public static String parseSheetValue(Sheet sheet,int rownum,int colnum){
		return POIUtil.getCellValue(sheet.getRow(rownum).getCell(colnum));
	}
}
