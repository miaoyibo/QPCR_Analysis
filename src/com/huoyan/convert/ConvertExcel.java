package com.huoyan.convert;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.huoyan.model.CriteriaModel;
import com.huoyan.model.QpcrModel;
import com.huoyan.util.DateUtil;
import com.huoyan.util.FileChooseUtil;
import com.huoyan.util.POIUtil;
public class ConvertExcel {	
	private static int nc1fam=33;
	private static int nc1vic=34; 
	private static int nc2fam=53;
	private static int nc2vic=54; 
	private static int nc3fam=191;
	private static int nc3vic=192; 
	private static int pcfam=167;
	private static int pcvic=168; 
	private static String[] headers= {"版号","反应孔","样本名称","FAM Ct值","VIC","结果","备注","QPCR下机时间"};
	public static void main(String[] args) throws Exception {
		Map<String, CriteriaModel> criteria =parserConfig();
		String qpcr = FileChooseUtil.chooseFiles("请选择qpcr文件：",JFileChooser.FILES_ONLY);
		if (qpcr==null) {
			return;
		}
		String typefile = FileChooseUtil.chooseFiles("请选择排版单：",JFileChooser.FILES_ONLY);
		if (typefile==null) {
			return;
		}
		Map<String, String> typeset=parseTypeSet(typefile);
		List<QpcrModel> qpcrResult = readQpcrResult(qpcr,criteria);
		Workbook workbook = new XSSFWorkbook();
		writeExcel(workbook,typeset,qpcrResult);
		FileOutputStream out = new FileOutputStream("result.xlsx");
		workbook.write(out);
		out.close();
		workbook.close();
		JOptionPane.showMessageDialog(null, "done.");
	}	
	/***
	 * 生成结果
	 * @param workbook
	 * @param typeset
	 * @param qpcrResult
	 */
	private static void writeExcel(Workbook workbook,Map<String, String> typeset, List<QpcrModel> qpcrResult) {
		Map<String, List<QpcrModel>> map = qpcrResult.stream().collect(Collectors.groupingBy(QpcrModel::getType));		
		for (Entry<String, List<QpcrModel>> entry : map.entrySet()) {
			Sheet sheet = workbook.createSheet(entry.getKey());
			writeHeader(sheet,headers);
			List<QpcrModel> value = entry.getValue();
			int rownum=1;
			for (QpcrModel qpcrModel : value) {
				if (typeset.get(qpcrModel.getLoc())==null) {
					continue;
				}
				Row row = sheet.createRow(rownum);
				//json is better
				Cell cell = row.createCell(0);
				cell.setCellValue(qpcrModel.getVersion());
				Cell cell1 = row.createCell(1);
				cell1.setCellValue(qpcrModel.getLoc());
				Cell cell2 = row.createCell(2);
				cell2.setCellValue(typeset.get(qpcrModel.getLoc()));
				Cell cell3 = row.createCell(3);
				cell3.setCellValue(qpcrModel.getFam());
				Cell cell4 = row.createCell(4);
				cell4.setCellValue(qpcrModel.getVic());
				Cell cell5 = row.createCell(5);
				cell5.setCellValue(qpcrModel.getResult());
				Cell cell6 = row.createCell(6);
				cell6.setCellValue(qpcrModel.getRemark());
				Cell cell7 = row.createCell(7);
				cell7.setCellValue(qpcrModel.getDate());
				rownum++;
			}
			
		}
		
	}
/***
 * 表头
 * @param sheet
 * @param headers
 */
	private static void writeHeader(Sheet sheet, String[] headers) {
		Row row = sheet.createRow(0);
		for (int i=0;i<headers.length;i++) {
			Cell cell = row.createCell(i);
			cell.setCellValue(headers[i]);
		}
		
	}
/***
 * 解析qpcr文件
 * @param qpcrfile
 * @param criteria
 * @return
 */
	public static List<QpcrModel> readQpcrResult(String qpcrfile,Map<String, CriteriaModel> criteria) {
		File file=new File(qpcrfile);
		List<QpcrModel> results=new ArrayList<>();
		Workbook workbook = POIUtil.getWorkBook(file);
		if (workbook != null) {
			Sheet sheet = workbook.getSheetAt(0);
			int lastRowNum = sheet.getLastRowNum();
			QpcrModel nc1=new QpcrModel();
			nc1.setFam(formatFam(parseSheetValue(sheet, nc1fam, 7)));
			nc1.setVic(parseSheetValue(sheet, nc1vic, 7));
			QpcrModel nc2=new QpcrModel();
			nc2.setFam(formatFam(parseSheetValue(sheet, nc2fam, 7)));
			nc2.setVic(parseSheetValue(sheet, nc2vic, 7));
			QpcrModel nc3=new QpcrModel();
			nc3.setFam(formatFam(parseSheetValue(sheet, nc3fam, 7)));
			nc3.setVic(parseSheetValue(sheet, nc3vic, 7));
			QpcrModel pc=new QpcrModel();
			pc.setFam(formatFam(parseSheetValue(sheet, pcfam, 7)));
			pc.setVic(parseSheetValue(sheet, pcvic, 7));
			List<QpcrModel> nclist =new ArrayList<QpcrModel>();
			nclist.add(nc1);
			nclist.add(nc2);
			nclist.add(nc3);
			String remark="";
			if (charge(criteria.get("整版重提"), nclist)) {
				remark="整版重提";
			}
			if (charge(criteria.get("整版重Q"), pc)) {
				remark="整版重Q";
			}		
			for (int rowNum = 1; rowNum <= lastRowNum; rowNum=rowNum+2) {
				Row famrow = sheet.getRow(rowNum);
				Row vicrow = sheet.getRow(rowNum+1);
				Cell famcell = famrow.getCell(7);
				Cell viccell = vicrow.getCell(7);
				QpcrModel qpcr=new QpcrModel();
				results.add(qpcr);
				qpcr.setDate(DateUtil.formatDate(file.lastModified()));
				qpcr.setFam(formatFam(POIUtil.getCellValue(famcell)));
				qpcr.setVic(POIUtil.getCellValue(viccell));
				qpcr.setLoc(POIUtil.getCellValue(famrow.getCell(2)));							
				qpcr.setVersion(file.getName().substring(0,file.getName().lastIndexOf("-")));
				qpcr.setSampleId("");
				qpcr.setRemark(remark);
				qpcr.setType("异常");
				if (rowNum==nc1fam||rowNum==nc2fam||rowNum==nc3fam||rowNum==pcfam) {
					qpcr.setType("质控");
					continue;
				}
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
				qpcr.setType("阴性");
				qpcr.setResult(result);
			}
		}
		return results;
	}
	private static String formatFam(String s) {
		if (s!=null) {
			s=s.trim().equals("-")?"NoCt":s;
		}
		return s;
	}
	private static boolean charge(CriteriaModel criteriaModel, List<QpcrModel> qpcrlist) {
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
	public static String parseSheetValue(Sheet sheet,int rownum,int colnum){
		Row row = sheet.getRow(rownum);
		if (row==null) {
			return null;
		}
		return POIUtil.getCellValue(sheet.getRow(rownum).getCell(colnum));
	}
	
	private static Map<String, String> parseTypeSet(String typefile) {
		Map<String, String> map=new HashMap<>();
		File file=new File(typefile);
		Workbook workbook = POIUtil.getWorkBook(file);
		if (workbook != null) {
			Sheet sheet = workbook.getSheetAt(0);
			for (int rowNum = 0; rowNum <= sheet.getLastRowNum(); rowNum++) {
				Row row=sheet.getRow(rowNum);
				Cell cell = row.getCell(0);
				Cell cell1 = row.getCell(1);
				if (cell!=null&&cell1!=null&&StringUtils.isNotEmpty(cell.getStringCellValue())&&StringUtils.isNotEmpty(cell1.getStringCellValue())) {
					map.put(cell.getStringCellValue(), cell1.getStringCellValue());
				}
			}
		}
		return map;
	}
	private static Map<String, CriteriaModel> parserConfig() throws Exception {
		Map<String, CriteriaModel> map=new HashMap<String, CriteriaModel>();
		Properties pro = new Properties();
		File file=new File("config.properties");
		if (file.exists()) {
			pro.load(new FileInputStream(file));
		}else {
			pro.put("nc.vic", "32");
			pro.put("pc.fam", "32");
			pro.put("sample.vic", "32");
			pro.put("sample.fam1", "40");
			pro.put("sample.fam2", "38");
		}
		//整版重提
		CriteriaModel c1=new CriteriaModel();
		c1.setVic(Double.parseDouble(pro.getProperty("nc.vic")));
		c1.setVicSymbol("<");
		map.put("整版重提", c1);
		//整版重Q
		CriteriaModel c2=new CriteriaModel();
		c2.setFam(Double.parseDouble(pro.getProperty("pc.fam")));
		c2.setFamSymbol(">");
		c2.setSpecialFamValue("-");
		map.put("整版重Q", c2);
		//重Q
		CriteriaModel c3=new CriteriaModel();
		c3.setFam(Double.parseDouble(pro.getProperty("sample.fam1")));
		c3.setFamSymbol("<=");
		map.put("重Q", c3);
		//重提
		CriteriaModel c4=new CriteriaModel();
		c4.setVic(Double.parseDouble(pro.getProperty("sample.vic")));
		c4.setVicSymbol(">=");
		c4.setSpecialVicValue("-");
		map.put("重提", c4);
		//人工判读
		CriteriaModel c5=new CriteriaModel();
		c5.setFam(Double.parseDouble(pro.getProperty("sample.fam2")));
		c5.setFamSymbol("<");
		map.put("人工判读", c5);	
		return map;
	}
}
