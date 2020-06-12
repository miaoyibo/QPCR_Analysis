package com.huoyan.convert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.huoyan.model.DailyModel;
import com.huoyan.util.FileChooseUtil;
import com.huoyan.util.POIUtil;

/***
 * 空白质控：vic<=32失控；fam<=40让步接受；
 * @author miaoyibo
 *
 */

public class DailyReport {
	static Pattern p = Pattern.compile("\\d{4}\\d{1,2}\\d{1,2}+");
	public static void main(String[] args) throws Exception {
		String files = FileChooseUtil.chooseFiles("请选择文件夹：",JFileChooser.DIRECTORIES_ONLY);
		List<File> csvFile=new ArrayList<>();
		List<File> excelFile=new ArrayList<>();
		if(files!=null) {
			File file=new File(files);
			File[] listFiles = file.listFiles();
			for (File file2 : listFiles) {
				if(file2.getName().endsWith("csv")) {
					csvFile.add(file2);
				}else if(file2.getName().endsWith("xls")) {
					excelFile.add(file2);
				}else {
					//TODO
				}
			}
		}
		List<DailyModel> list=new ArrayList<>();
		List<DailyModel> csvlist = readQpcrResultFromCSV(csvFile);
		List<DailyModel> excellist = readQpcrResultFromEXCEL(excelFile);
		if(csvlist==null&&excellist==null) {
			JOptionPane.showMessageDialog(null, "没有有效的csv或excel文件.");
			return;
		}
		if(csvlist!=null) {
			list.addAll(csvlist);
		}
		if(excellist!=null) {
			list.addAll(excellist);
		}
		Map<String, List<DailyModel>> map = list.stream().collect(Collectors.groupingBy(DailyModel::getDate));
		writeDaily(map);
		JOptionPane.showMessageDialog(null, "work is done.");
	}
	
	public static List<DailyModel> readQpcrResultFromEXCEL(List<File> qpcrfiles) throws IOException {
		if(qpcrfiles==null||qpcrfiles.isEmpty()) return null;
		List<DailyModel> list=new ArrayList<DailyModel>();
		for (File file : qpcrfiles) {
			Workbook workbook = POIUtil.getWorkBook(file);
			if (workbook != null) {
				Sheet sheet = workbook.getSheetAt(0);
				DailyModel ss1=getValue(file,sheet,33,new NCHandle(1));
				DailyModel ss2=getValue(file,sheet,53,new NCHandle(2));
				DailyModel ss3=getValue(file,sheet,191,new NCHandle(3));
				DailyModel pc=getValue(file,sheet,167,new PCHandle());
				list.add(ss1);
				list.add(ss2);
				list.add(ss3);
				list.add(pc);
				workbook.close();
			}
		}
		return list;
	}
	public static List<DailyModel> readQpcrResultFromCSV(List<File> qpcrfiles) throws IOException {
		if(qpcrfiles==null||qpcrfiles.isEmpty()) return null;
		List<DailyModel> list=new ArrayList<>();
		for (File file : qpcrfiles) {	
			BufferedReader reader=new BufferedReader(new FileReader(file));
			int count=0;
			String line;
			while((line=reader.readLine())!=null) {
				if(count==33) {
					DailyModel value = getCSVValue(file,line,reader.readLine(),new NCHandle(1));
					list.add(value);
					count++;
				}
				if(count==53) {
					DailyModel value =getCSVValue(file,line,reader.readLine(),new NCHandle(2));
					list.add(value);
					count++;
				}
				if(count==191) {
					DailyModel value =getCSVValue(file,line,reader.readLine(),new NCHandle(3));
					list.add(value);
					count++;
				}
				if(count==167) {
					DailyModel value =getCSVValue(file,line,reader.readLine(),new PCHandle());
					list.add(value);
					count++;
				}
				count++;
			}
			reader.close();
		}
		return list;
	}

	private static void writeDaily(Map<String, List<DailyModel>> map) throws Exception {
		if(map==null)return;
		int rowNum=4;
		for(Entry<String, List<DailyModel>> en:map.entrySet()) {
			List<DailyModel> value = en.getValue();
			String newname=en.getKey()+".xlsx";
			File file = new File("model.xlsx");
			if(!file.exists()) {
				JOptionPane.showMessageDialog(null, "找不到模板文件:model.xlsx！请将model.xlsx和本程序放置在同一目录下");
				return;
			}
			Workbook book = POIUtil.getWorkBook(file);
			Row daterow = book.getSheetAt(0).getRow(2);
			Cell datecell = daterow.getCell(0);
			datecell.setCellValue("日期： "+en.getKey());
			for (int i=0;i<value.size();i++) {
				DailyModel ss = value.get(i);
				Row row = book.getSheetAt(0).getRow(rowNum);
				if(row==null)row=book.getSheetAt(0).createRow(rowNum);			
				for (int cellNum = 0; cellNum < 5; cellNum++) {
					Cell cell = row.getCell(cellNum);
					if(cell==null) {
						cell = row.createCell(cellNum);
					}
					if(cell.getStringCellValue()!=null&&!cell.getStringCellValue().isEmpty())break;
					cell.setCellValue(ss.getValue(cellNum));
				}
				rowNum++;
			}
			FileOutputStream out=new FileOutputStream(newname);
			book.write(out);
			out.close();
			book.close();
			rowNum=4;
		}
	}
	
	public static DailyModel getValue(File file,Sheet sheet,int row,Handle h) {
		DailyModel model=new DailyModel();
		model.setVersion(file.getName().substring(0,file.getName().lastIndexOf('.')));
		Matcher m = p.matcher(file.getName());
		String date="na";
		if(m.find()) {
			date=m.group();
		}
		model.setDate(date);
		model.setName(h.getHandleName());
		model.setExpect(h.getCriteria());
		String value1 =null;
		try {
			value1 = sheet.getRow(row).getCell(7).getStringCellValue();
		} catch (Exception e) {
			value1=sheet.getRow(row).getCell(7).getNumericCellValue()+"";
		}
		String value2 =null;
		try {
			value2 = sheet.getRow(row).getCell(7).getStringCellValue();
		} catch (Exception e) {
			value2=sheet.getRow(row+1).getCell(7).getNumericCellValue()+"";
		}
		model.setReal((value1.equals(" - ")?"NoCt":value1)+"/"+(value2.equals(" - ")?"NoCt":value2));
		model.setCharge(h.chargeResult(value1, value2));
		return model;
	}

	public static DailyModel getCSVValue(File file,String line,String line1,Handle h) {
		DailyModel model=new DailyModel();
		String[] s = line.split(",");
		String[] s2 = line1.split(",");
		model.setVersion(file.getName().substring(0,file.getName().lastIndexOf('.')));
		Matcher m = p.matcher(file.getName());
		String date="na";
		if(m.find()) {
			date=m.group();
		}
		model.setDate(date);
		model.setName(h.getHandleName());
		model.setExpect(h.getCriteria());
		model.setReal((s[7].equals(" - ")?"NoCt":s[7])+"/"+(s2[7].equals(" - ")?"NoCt":s2[7]));
		model.setCharge(h.chargeResult(s[7], s2[7]));
		return model;
	}
	public static void copeExcel(String fromexcel, String newexcel) throws Exception{
		XSSFWorkbook wb = null;
        FileInputStream fis =null;
        FileOutputStream fos = null;
        try {  
        	fis = new FileInputStream(fromexcel);
        	fos = new FileOutputStream(newexcel);
            wb = new XSSFWorkbook(fis);  
            wb.write(fos);
            fis.close();
            fos.close();
            
        } catch (Exception e) {  
            e.printStackTrace();  
        }finally{
			try {
				if(fis != null)
					fis.close();
				if(fos != null)
					fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
