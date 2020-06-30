package com.huoyan.analysis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

public class HongShiQpcrHandle extends QpcrHandle{
	
	private static String sheetName = "ʵ������";
	static String pattern = "[a-zA-Z][0-9]{1,}";
	public HongShiQpcrHandle(PoolHandle poolHandle) {
		super(poolHandle);
		
	}

	public static void main(String[] args) throws Exception {
		HongShiQpcrHandle qa=new HongShiQpcrHandle(new TianjinPoolHandle());
		qa.apply();

	}

	@Override
	public List<QpcrModel> readQpcrResult(File file, Map<String, CriteriaModel> criteria) {
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
				if (row != null && row.getCell(0) != null && "��Ӧ��".equals(POIUtil.getCellValue(row.getCell(0)))) {
					startNum = rowNum + 1;
					break;
				}
			}
			for (int rowNum = startNum; rowNum <= lastRowNum; rowNum = rowNum + 2) {
				Row famrow = sheet.getRow(rowNum);
				Row vicrow = sheet.getRow(rowNum + 1);
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
				results.add(qpcr);
				String sample = POIUtil.getCellValue(famrow.getCell(2));
				qpcr.setDate(date);
				qpcr.setFam(QpcrRuleHandle.formatFam(POIUtil.getCellValue(famcell)));
				qpcr.setVic(POIUtil.getCellValue(viccell));
				qpcr.setLoc(POIUtil.getCellValue(famrow.getCell(0)));
				qpcr.setVersion(version);
				qpcr.setSampleId(sample);
				String sampletype = POIUtil.getCellValue(famrow.getCell(9));
				if ((StringUtils.isEmpty(sample) && !"������Ʒ".equals(sampletype.trim())) || sample.contains("�ʿ�")) {
					qpcr.setType("�ʿ�");
					qpcr.setSampleId(sample);
					continue;
				}
				if (QpcrRuleHandle.charge(criteria.get("����"), qpcr)) {
					qpcr.setRemark("����");
					qpcr.setType("�쳣");
					continue;
				}
				// ����
				qpcr.setType("����");
				qpcr.setResult("����");
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

}
