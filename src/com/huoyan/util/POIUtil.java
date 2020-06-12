package com.huoyan.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class POIUtil {

	private final static String xls = "xls";
	private final static String xlsx = "xlsx";

	private final static String sql = "INSERT INTO course_tab ( name, grade, term, english_name, picture, img, college_id, created_date )VALUES(";

	public static void main(String[] args) throws IOException {
		File file =new File("D:\\user_info(1).xlsx");
		readExcel(file);
	}

	/**
	 * 读入excel文件，解析后返回
	 * 
	 * @param file
	 * @throws IOException
	 */
	public static List<String[]> readExcel(File file) throws IOException {
		
		// 检查文件
		checkFile(file);
		// 获得Workbook工作薄对象
		Workbook workbook = getWorkBook(file);
		// 创建返回对象，把每行中的值作为一个数组，所有行作为一个集合返回
		List<String[]> list = new ArrayList<String[]>();
		FileWriter writer=new FileWriter("D://est.txt");
		//FileWriter writer2=new FileWriter("D://course.txt");
		if (workbook != null) {
			for (int sheetNum = 0; sheetNum < workbook.getNumberOfSheets(); sheetNum++) {
				// 获得当前sheet工作表
				Sheet sheet = workbook.getSheetAt(sheetNum);
				if (sheet == null) {
					continue;
				}
				//String sql = "INSERT INTO course_tab ( name, grade, term, english_name, picture, img, college_id, created_date )VALUES(";
				// 获得当前sheet的开始行
				int firstRowNum = sheet.getFirstRowNum();
				// 获得当前sheet的结束行
				int lastRowNum = sheet.getLastRowNum();
				// 循环除了第一行的所有行
				int i=318;
				for (int rowNum = firstRowNum + 1; rowNum <= lastRowNum; rowNum++) {
					String sql = "INSERT INTO user_info(user_id, user_name, user_password, user_email, user_type, role_id, user_token) VALUES (";
					// 获得当前行
					Row row = sheet.getRow(rowNum);
					if (row == null) {
						continue;
					}
					// 获得当前行的开始列
					int firstCellNum = row.getFirstCellNum();
					// 获得当前行的列数
					int lastCellNum = row.getPhysicalNumberOfCells();
					String[] cells = new String[row.getPhysicalNumberOfCells()];
					// 循环当前行
					for (int cellNum = 0; cellNum < 7; cellNum++) {
						Cell cell = row.getCell(cellNum);
						// cells[cellNum] = getCellValue(cell);
						String cellValue = getCellValue(cell);
						/*if (cellValue.equals("经济学院")) {
							cellValue="5";
						}else if (cellValue.equals("计算机与软件学院")) {
							cellValue="6";
						}else if(cellValue.equals("物理电气信息学院")){
							cellValue="7";
						}else if(cellValue.equals("数学统计学院")){
							cellValue="8";
						}else if(cellValue.equals("农学院")){
							cellValue="9";
						}*/
							
						if (cellNum==6) {
							sql=sql+"'"+cellValue+"'"+");";
						}else {
							sql=sql+"'"+cellValue+"'"+",";
						}
						

					}
					//System.out.println(sql);
					writer.write(sql);
					writer.write("\r\n");
					/*String sql2="INSERT INTO course_add_tab(profile_num,view_num,course_id)values(0,0,"+i+");";
					writer2.write(sql2);
					writer2.write("\r\n");*/
					i++;	
					list.add(cells);
				}
			}
			writer.close();
			//writer2.close();
			workbook.close();
		}
		return list;
	}

	public static void checkFile(File file) throws IOException {
		// 判断文件是否存在
		if (null == file) {
			throw new FileNotFoundException("文件不存在！");
		}
		// 获得文件名
		String fileName = file.getName();
		// 判断文件是否是excel文件
		if (!fileName.endsWith(xls) && !fileName.endsWith(xlsx)) {
			throw new IOException(fileName + "不是excel文件");
		}
	}

	public static Workbook getWorkBook(File file) {
		// 获得文件名
		String fileName = file.getName();
		// 创建Workbook工作薄对象，表示整个excel
		Workbook workbook = null;
		try {
			// 获取excel文件的io流
			InputStream is = new FileInputStream(file);
			// 根据文件后缀名不同(xls和xlsx)获得不同的Workbook实现类对象
			if (fileName.endsWith(xls)) {
				// 2003
				workbook = new HSSFWorkbook(is);
			} else if (fileName.endsWith(xlsx)) {
				// 2007
				workbook = new XSSFWorkbook(is);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return workbook;
	}

	public static String getCellValue(Cell cell) {
		String cellValue = "";
		if (cell == null) {
			return cellValue;
		}
		// 把数字当成String来读，避免出现1读成1.0的情况
		if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
			cell.setCellType(Cell.CELL_TYPE_STRING);
		}
		// 判断数据的类型
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_NUMERIC: // 数字
			cellValue = String.valueOf(cell.getNumericCellValue());
			break;
		case Cell.CELL_TYPE_STRING: // 字符串
			cellValue = String.valueOf(cell.getStringCellValue());
			break;
		case Cell.CELL_TYPE_BOOLEAN: // Boolean
			cellValue = String.valueOf(cell.getBooleanCellValue());
			break;
		case Cell.CELL_TYPE_FORMULA: // 公式
			cellValue = String.valueOf(cell.getCellFormula());
			break;
		case Cell.CELL_TYPE_BLANK: // 空值
			cellValue = "";
			break;
		case Cell.CELL_TYPE_ERROR: // 故障
			cellValue = "非法字符";
			break;
		default:
			cellValue = "未知类型";
			break;
		}
		return cellValue;
	}

}
