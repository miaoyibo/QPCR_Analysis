package com.huoyan.analysis;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import com.huoyan.model.CriteriaModel;
import com.huoyan.model.QpcrModel;
import com.huoyan.rule.QpcrRuleHandle;
import com.huoyan.util.POIUtil;

public class YaRuiQpcrHandle extends QpcrHandle {

	static String pattern = "[a-zA-Z][0-9]{1,}";
	private static String sampleExcel = "任务单";
	private static String colName = "样例编号";
	private Map<String, String> map;

	public YaRuiQpcrHandle(PoolHandle poolHandle,File qpcr,File task) {
		super(poolHandle,qpcr,task);

	}

	public static void main(String[] args) throws Exception {

		YaRuiQpcrHandle qa = new YaRuiQpcrHandle(new TianjinPoolHandle(),null,null);
		qa.apply();

	}

	@Override
	public List<QpcrModel> readQpcrResult(File file, Map<String, CriteriaModel> criteria,File task) {
	/*	if (map == null) {
			map = parseSampleExcel(file,task);
		}*/
		List<QpcrModel> results = new ArrayList<>();
		Workbook workbook = POIUtil.getWorkBook(file);
		if (workbook != null) {
			Sheet sheet = workbook.getSheet("Data&Graph");
			if (sheet == null) {
				return results;
			}
			Sheet dsheet = workbook.getSheet("Protocol&Plate");
			int lastRowNum = sheet.getLastRowNum();
			String version = getVersionByFilename(file.getName(), ".");
			String date = "";
			if (dsheet!=null) {
				date = POIUtil.getCellValue(dsheet.getRow(2).getCell(1));
			}
			for (int rowNum = 2; rowNum <= lastRowNum; rowNum = rowNum + 2) {
				Row famrow = sheet.getRow(rowNum);
				Row vicrow = sheet.getRow(rowNum + 1);
				if (famrow == null || vicrow == null) {
					continue;
				}
				Cell cell = famrow.getCell(0);
				if (cell == null || !POIUtil.getCellValue(cell).matches(pattern)) {
					continue;
				}
				Cell famcell = famrow.getCell(5);
				Cell viccell = vicrow.getCell(5);
				QpcrModel qpcr = new QpcrModel();
				String sample = POIUtil.getCellValue(famrow.getCell(3));
				if (StringUtils.isEmpty(sample)) {
					continue;
				}
				results.add(qpcr);
				qpcr.setDate(date);
				qpcr.setFam(QpcrRuleHandle.formatFam(POIUtil.getCellValue(famcell)));
				qpcr.setVic(POIUtil.getCellValue(viccell));
				qpcr.setLoc(POIUtil.getCellValue(cell));
				qpcr.setVersion(version);
				qpcr.setSampleId(sample);
				if (sample.contains("质控") || sample.contains("对照")) {
					qpcr.setType("质控");
					qpcr.setSampleId(sample);
					continue;
				}
				if (QpcrRuleHandle.charge(criteria.get("复测"), qpcr)) {
					qpcr.setRemark("复测");
					qpcr.setType("异常");
					continue;
				}
				// 阴性
				qpcr.setType("阴性");
				qpcr.setResult("阴性");
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

	/***
	 * 解析任务单
	 */
	public Map<String, String> parseSampleExcel(File file,File task) {
		if (task==null) {
			task=new File(file.getParent(), sampleExcel);
		}
		Map<String, String> map = new HashMap<String, String>();
		if (task!=null&&task.exists() && !task.isFile()) {
			File[] files = task.listFiles();
			for (File file2 : files) {
				String version = getVersionByFilename(file2.getName(), ".");
				Workbook workbook = POIUtil.getWorkBook(file2);
				if (workbook != null) {
					Sheet sheet = workbook.getSheetAt(0);
					if (sheet == null) {
						continue;
					}
					int targetNum = getTargetCol(sheet);
					for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
						Row row = sheet.getRow(rowNum);
						String key = POIUtil.getCellValue(row.getCell(targetNum-1));
						String value = POIUtil.getCellValue(row.getCell(targetNum));
						if (StringUtils.isNotEmpty(key)&&StringUtils.isNotEmpty(value)) {
							map.put(version + key, value);
						}
					}
				}
				try {
					workbook.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return map;
	}

	private int getTargetCol(Sheet sheet) {
		int targetNum = 16;
		Row row = sheet.getRow(0);
		for (int i = 0; i <= row.getLastCellNum(); i++) {
			String value = POIUtil.getCellValue(row.getCell(i));
			if (value != null && colName.equals(value.trim())) {
				targetNum = i;
			}
		}
		return targetNum;
	}

	private static String getVersionByFilename(String name, String split) {
		int indexOf = name.indexOf("(");
		if (indexOf < 0) {
			indexOf = name.indexOf("（");
		}
		indexOf = indexOf < 0 ? name.lastIndexOf(split) : indexOf;
		return name.substring(0, indexOf);
	}

}
