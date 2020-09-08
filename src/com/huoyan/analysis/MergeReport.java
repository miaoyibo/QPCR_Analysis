package com.huoyan.analysis;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.huoyan.model.MergeReportModel;
import com.huoyan.util.FileChooseUtil;
import com.huoyan.util.POIUtil;

public class MergeReport {
	static String pattern = "[a-zA-Z][0-9]{1,}";
	private static String[] headers = { "地区", "板号", "样品编号", "箱号+社区", "备注" };
	public static void main(String[] args) throws Exception {
		MergeReport mergeReport=new MergeReport();
		mergeReport.apply();

	}
	
	public void apply() throws Exception {
		File[] files = FileChooseUtil.getFiles("请选择qpcr文件：", JFileChooser.FILES_AND_DIRECTORIES);
		if (files == null) {
			return;
		}
		List<MergeReportModel> data=new ArrayList<>();
		for (File file : files) {
			if (file.isFile()) {
				List<MergeReportModel> list = parseExcel(file);
				data.addAll(list);
			}
		}
		Workbook workbook = new XSSFWorkbook();
		writeExcel(workbook, data);
		File result=new File("汇总报表.xlsx");
		FileOutputStream out = new FileOutputStream(result);
		workbook.write(out);
		out.close();
		workbook.close();
		JOptionPane.showMessageDialog(null, "done.");
	}
	
	public List<MergeReportModel> parseExcel(File file){
		List<MergeReportModel> results=new ArrayList<MergeReportModel>();
		Workbook workbook = POIUtil.getWorkBook(file);
		if (workbook != null) {
			Sheet sheet = workbook.getSheet("Sheet1");
			if (sheet == null) {
				return results;
			}
			Row versionrow = sheet.getRow(2);
			String version = POIUtil.getCellValue(versionrow.getCell(0));
			if (version.startsWith("板号：")) {
				version=version.substring(version.indexOf("：")+1);
			}
			int lastRowNum = sheet.getLastRowNum();
			for (int rowNum = 1; rowNum <= lastRowNum; rowNum++) {
				Row row = sheet.getRow(rowNum);
				Cell cell = row.getCell(15);
				if (cell == null || !POIUtil.getCellValue(cell).matches(pattern)) {
					continue;
				}
				MergeReportModel model=new MergeReportModel();
				String sampleId=POIUtil.getCellValue(row.getCell(16));
				if (StringUtils.isEmpty(sampleId)||sampleId.contains("对照")) {
					continue;
				}
				model.setSampleId(sampleId);
				model.setVersion(version);
				String boxArea=POIUtil.getCellValue(row.getCell(17));
				model.setBoxArea(boxArea);
				model.setArea(getArea(boxArea));
				results.add(model);
			}
			try {
				workbook.close();
			} catch (IOException e) {
			}
		}
		
		return results;
	}
	
	private String getArea(String boxAres) {
		if (StringUtils.isEmpty(boxAres)) {
			return boxAres;
		}
		String s="0123456789-_——";
		char[] charArray = boxAres.toCharArray();
		int num=0;
		for(int i=0;i<charArray.length;i++) {
			char c = charArray[i];
			if (s.indexOf(c)!=-1) {
				continue;
			}else {
				num=i;
				break;
			}
			
		}	
		 return boxAres.substring(num);
	}
	
	private static void writeExcel(Workbook workbook, List<MergeReportModel> list) {
		Sheet sheet = workbook.createSheet("汇总信息");
		POIUtil.writeHeader(sheet, headers);
		int rownum = 1;
		for (MergeReportModel model : list) {
			Row row = sheet.createRow(rownum);
			Cell cell = row.createCell(0);
			cell.setCellValue(model.getArea());
			Cell cell1 = row.createCell(1);
			cell1.setCellValue(model.getVersion());
			Cell cell2 = row.createCell(2);
			cell2.setCellValue(model.getSampleId());
			Cell cell3 = row.createCell(3);
			cell3.setCellValue(model.getBoxArea());
			Cell cell4 = row.createCell(4);
			cell4.setCellValue(model.getRemark());
			rownum++;
		}
	}

}
