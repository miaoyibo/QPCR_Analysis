package com.huoyan.analysis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import com.huoyan.model.CriteriaModel;
import com.huoyan.model.QpcrModel;
import com.huoyan.rule.QpcrRuleHandle;
import com.huoyan.util.POIUtil;

public class HongShiQpcrHandle extends QpcrHandle{
	
	private static String sheetName = "实验数据";
	static String pattern = "[a-zA-Z][0-9]{1,}";
	public HongShiQpcrHandle(PoolHandle poolHandle,File qpcr,File task) {
		super(poolHandle,qpcr,task);
		
	}

	public static void main(String[] args) throws Exception {
		HongShiQpcrHandle qa=new HongShiQpcrHandle(new TianjinPoolHandle(),null,null);
		qa.apply();

	}

	@Override
	public List<QpcrModel> readQpcrResult(File file, Map<String, CriteriaModel> criteria,File task) {
		List<QpcrModel> results = new ArrayList<>();
		Workbook workbook = POIUtil.getWorkBook(file);
		if (workbook != null) {
			Sheet sheet = workbook.getSheet(sheetName);
			if (sheet == null) {
				return results;
			}
			int lastRowNum = sheet.getLastRowNum();
			String version = POIUtil.getCellValue(sheet.getRow(0).getCell(1));
			String date = POIUtil.getCellValue(sheet.getRow(2).getCell(1));
			int startNum = 0;
			for (int rowNum = 3; rowNum <= lastRowNum; rowNum++) {
				Row row = sheet.getRow(rowNum);
				if (row != null && row.getCell(0) != null && "反应孔".equals(POIUtil.getCellValue(row.getCell(0)))) {
					startNum = rowNum + 1;
					break;
				}
			}
			for (int rowNum = startNum; rowNum <= lastRowNum; rowNum = rowNum + 3) {
				Row famrow = sheet.getRow(rowNum);
				Row vicrow = sheet.getRow(rowNum + 1);
				Row controlrow = sheet.getRow(rowNum + 2);
				if (famrow == null || vicrow == null) {
					continue;
				}
				Cell cell = famrow.getCell(0);
				if (cell == null || !POIUtil.getCellValue(cell).matches(pattern)) {
					continue;
				}
				Cell famcell = famrow.getCell(12);
				Cell viccell = vicrow.getCell(12);
				QpcrModel qpcr = new QpcrModel();				
				String sample = POIUtil.getCellValue(famrow.getCell(2));
				if(StringUtils.isEmpty(sample)) {
					continue;
				}
			    results.add(qpcr);
				qpcr.setDate(date);
				qpcr.setFam(POIUtil.getCellValue(famcell));
				qpcr.setVic(POIUtil.getCellValue(viccell));
				qpcr.setLoc(POIUtil.getCellValue(famrow.getCell(0)));
				qpcr.setVersion(version);
				qpcr.setSampleId(sample);
				qpcr.setSelfControl(POIUtil.getCellValue(controlrow.getCell(12)));
				if (sample.contains("质控")|| sample.contains("对照")) {
					qpcr.setType("质控");
					qpcr.setSampleId(sample);
					continue;
				}
				if ("NoCt".equals(qpcr.getFam())&&"NoCt".equals(qpcr.getVic())&&NumberUtils.isCreatable(qpcr.getSelfControl())) {
					qpcr.setType("阴性");
					qpcr.setResult("阴性");
				}else {
					qpcr.setRemark("重Q");
					qpcr.setType("异常");
				}
			/*	if (QpcrRuleHandle.charge(criteria.get("重提"), qpcr)) {
					qpcr.setRemark("重提");
					qpcr.setType("异常");
					continue;
				}
				if (QpcrRuleHandle.charge(criteria.get("重Q"), qpcr)) {
					qpcr.setRemark("重Q");
					qpcr.setType("异常");
					continue;
				}
				// 阴性
*/				
			}
			try {
				workbook.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return results;
	}

	@Override
	public void setCriteria(Map<String, CriteriaModel> map, Properties pro) {
		CriteriaModel c1 = new CriteriaModel();
		String fam = pro.getProperty("sample.fam");
		String vic=pro.getProperty("sample.vic");
		c1.setFam(Double.parseDouble(fam.substring(1,fam.length()-1)));
		if (fam.startsWith("[")) {
			c1.setFamSymbol("<=");
		}else {
			c1.setFamSymbol("<");
		}
		c1.setVic(Double.parseDouble(vic.substring(1,vic.length()-1)));
		if (vic.startsWith("[")) {
			c1.setVicSymbol(">=");
		}else {
			c1.setVicSymbol(">");
		}
		c1.setSpecialVicValue("NoCt");
		map.put("重Q", c1);
		CriteriaModel c2 = new CriteriaModel();
		String fam2 = pro.getProperty("sample.fam2");
		c2.setFam(Double.parseDouble(fam2.substring(1,fam2.length()-1)));
		if (fam2.startsWith("[")) {
			c2.setFamSymbol("<=");
		}else {
			c2.setFamSymbol("<");
		}
		map.put("重提", c2);
		
	}

}
