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
import com.huoyan.util.FileChooseUtil;
import com.huoyan.util.POIUtil;
public class TianJinConvertExcel {	
	private static String[] headers= {"版号","反应孔","样本名称","FAM Ct值","VIC","结果","备注","QPCR下机时间"};
	public static void main(String[] args) throws Exception {
		Map<String, CriteriaModel> criteria =parserConfig();
		String qpcr = FileChooseUtil.chooseFiles("请选择qpcr文件：",JFileChooser.DIRECTORIES_ONLY);
		if (qpcr==null) {
			return;
		}
		File file=new File(qpcr);
		File[] files = file.listFiles();
		List<QpcrModel> qpcrResult=new ArrayList<>();
		for (File file2 : files) {
			List<QpcrModel> list = readQpcrResult(file2,criteria);
			if (list!=null&&!list.isEmpty()) {
				qpcrResult.addAll(list);
			}
		}		
		Workbook workbook = new XSSFWorkbook();
		writeExcel(workbook,qpcrResult);
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
	private static void writeExcel(Workbook workbook, List<QpcrModel> qpcrResult) {
		Map<String, List<QpcrModel>> map = qpcrResult.stream().collect(Collectors.groupingBy(QpcrModel::getType));	
		List<QpcrModel> list = map.get("阴性");
		if (list!=null) {
			Sheet sheet = workbook.createSheet("结果上传");
			String[] header= {"样本编号","检测结果（阴性/阳性/检测失败"};
			writeHeader(sheet, header);
			int rownum=1;
			for (QpcrModel q : list) {
				Row row = sheet.createRow(rownum);
				Cell cell = row.createCell(0);
				Cell cell1 = row.createCell(1);
				cell.setCellValue(q.getSampleId());
				cell1.setCellValue(q.getResult());
				rownum++;
			}
		}
		for (Entry<String, List<QpcrModel>> entry : map.entrySet()) {
			Sheet sheet = workbook.createSheet(entry.getKey());
			writeHeader(sheet,headers);
			List<QpcrModel> value = entry.getValue();
			int rownum=1;
			for (QpcrModel qpcrModel : value) {
				Row row = sheet.createRow(rownum);
				//json is better
				Cell cell = row.createCell(0);
				cell.setCellValue(qpcrModel.getVersion());
				Cell cell1 = row.createCell(1);
				cell1.setCellValue(qpcrModel.getLoc());
				Cell cell2 = row.createCell(2);
				cell2.setCellValue(qpcrModel.getSampleId());
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
	private  static void writeHeader(Sheet sheet, String[] headers) {
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
	public static List<QpcrModel> readQpcrResult(File file,Map<String, CriteriaModel> criteria) {
		List<QpcrModel> results=new ArrayList<>();
		Workbook workbook = POIUtil.getWorkBook(file);
		if (workbook != null) {
			Sheet sheet = workbook.getSheet("实验数据");
			if (sheet==null) {
				return results;
			}
			int lastRowNum = sheet.getLastRowNum();
			String version=POIUtil.getCellValue(sheet.getRow(0).getCell(1));
			String date=POIUtil.getCellValue(sheet.getRow(2).getCell(1));
			int startNum=0;
			for(int rowNum = 3; rowNum <= lastRowNum; rowNum++) {
				Row row = sheet.getRow(rowNum);
				if (row!=null&&row.getCell(0)!=null&&"反应孔".equals(POIUtil.getCellValue(row.getCell(0)))) {
					startNum=rowNum+1;
					break;
				}
			}
			for (int rowNum = startNum; rowNum <= lastRowNum; rowNum=rowNum+2) {
				Row famrow = sheet.getRow(rowNum);
				Row vicrow = sheet.getRow(rowNum+1);
				if (famrow==null||vicrow==null) {
					break;
				}
				Cell famcell = famrow.getCell(12);
				Cell viccell = vicrow.getCell(12);				
				QpcrModel qpcr=new QpcrModel();
				results.add(qpcr);
				String sample=POIUtil.getCellValue(famrow.getCell(2));
				qpcr.setDate(date);
				qpcr.setFam(formatFam(POIUtil.getCellValue(famcell)));
				qpcr.setVic(POIUtil.getCellValue(viccell));
				qpcr.setLoc(POIUtil.getCellValue(famrow.getCell(0)));							
				qpcr.setVersion(version);
				qpcr.setSampleId(sample);
				if (StringUtils.isEmpty(sample)||sample.contains("质控")) {
					qpcr.setType("质控");
					continue;
				}
				if(charge(criteria.get("复测"),qpcr)) {
					qpcr.setRemark("复测");
					qpcr.setType("异常");
					continue;
				}
				//阴性
				qpcr.setType("阴性");
				qpcr.setResult("阴性");
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
			pro.put("sample.fam1", "39");
			pro.put("sample.fam2", "38");
		}
	
		//复测
		CriteriaModel c1=new CriteriaModel();
		c1.setFam(Double.parseDouble(pro.getProperty("sample.fam1")));
		c1.setFamSymbol("<");
		c1.setVic(Double.parseDouble(pro.getProperty("sample.vic")));
		c1.setVicSymbol(">");
		c1.setSpecialVicValue("NoCt");
		map.put("复测", c1);
		return map;
	}
}
