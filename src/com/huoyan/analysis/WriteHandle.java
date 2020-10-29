package com.huoyan.analysis;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.huoyan.model.QpcrModel;
import com.huoyan.util.FileChooseUtil;

public abstract class WriteHandle {
	// private static String[] headers = { "版号", "反应孔", "样本名称", "FAM_CT", "HEX_CT",
	// "检测结果", "下一步操作", "备注" };
	private static String[] headers = { "到样日期&时间\r\nArrival Date&Time", "下单时间\r\n Order Time", "原始编号",
			"样品编号\r\nSamplenumber", "PCR孔板号\r\nplate NO.", "孔位well position", "QPCR仪器号", "统筹填写备注", "QPCR备注",
			"第一次\r\nFAM", "第一次\r\nVIC", "第一次\r\n检测结果", "下一步\r\n操作", "第一次\r\n判定人", "第一次检测结果备注" };
	private static String poolfile = "pool任务单";

	private PoolHandle poolHandle;

	private File qpcr;
	private File task;

	public WriteHandle(PoolHandle poolHandle, File qpcr, File task) {
		this.poolHandle = poolHandle;
		this.qpcr = qpcr;
		this.task = task;
	}

	public String apply() throws Exception {
		File[] files = FileChooseUtil.getFiles("请选择qpcr文件：", JFileChooser.FILES_AND_DIRECTORIES);
		if (files == null) {
			return null;
		}
		List<QpcrModel> qpcrResult = new ArrayList<>();
		for (File file2 : files) {
			if (file2.isFile()) {
				List<QpcrModel> list = readQpcrResult(file2, task);
				if (list != null && !list.isEmpty()) {
					qpcrResult.addAll(list);
				}
			}
		}
		if (qpcrResult.isEmpty()) {
			JOptionPane.showMessageDialog(null, "no data.");
			return null;
		}
		File pool = new File(qpcr + File.separator + poolfile);
		if (pool.exists()) {
			qpcrResult = poolHandle.addPoolSample(qpcrResult, pool);
		}

		Workbook workbook = new XSSFWorkbook();
		writeExcel(workbook, qpcrResult);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
		String dateTime = LocalDateTime.now(ZoneOffset.of("+8")).format(formatter);
		String filenname = "result-" + dateTime + ".xlsx";
		Path p = Paths.get(filenname);
		if (Files.exists(p)) {
			Files.delete(p);
		}
		Files.createFile(p);
		OutputStream out = Files.newOutputStream(p);
		workbook.write(out);
		out.close();
		workbook.close();
		JOptionPane.showMessageDialog(null, "done.");
		return p.toAbsolutePath().toString();
	}

	/***
	 * 生成结果
	 * 
	 * @param workbook
	 * @param typeset
	 * @param qpcrResult
	 */
	private static void writeExcel(Workbook workbook, List<QpcrModel> qpcrResult) {
		Map<String, List<QpcrModel>> map = qpcrResult.stream().collect(Collectors.groupingBy(QpcrModel::getType));
		List<QpcrModel> list = map.getOrDefault("summary", new ArrayList<>());
		writeValue(workbook, "summary", list);
		List<QpcrModel> list2 = map.getOrDefault("control", new ArrayList<>());
		writeValue(workbook, "control", list2);

	}

	private static void writeValue(Workbook workbook, String sheetname, List<QpcrModel> value) {
		Sheet sheet = workbook.createSheet(sheetname);
		writeHeader(sheet, headers);
		int rownum = 1;
		for (QpcrModel qpcrModel : value) {
			Row row = sheet.createRow(rownum);
			// json is better
			Cell cell = row.createCell(3);
			cell.setCellValue(qpcrModel.getSampleId());
			Cell cell1 = row.createCell(4);
			cell1.setCellValue(qpcrModel.getVersion());
			Cell cell2 = row.createCell(5);
			cell2.setCellValue(qpcrModel.getLoc());
			Cell cell3 = row.createCell(9);
			cell3.setCellValue(qpcrModel.getFam());
			Cell cell4 = row.createCell(10);
			cell4.setCellValue(qpcrModel.getVic());
			Cell cell5 = row.createCell(11);
			cell5.setCellValue(qpcrModel.getResult());
			Cell cell6 = row.createCell(12);
			cell6.setCellValue(qpcrModel.getNextStep());
			Cell cell7 = row.createCell(14);
			cell7.setCellValue(qpcrModel.getRemark());
			rownum++;
		}
	}

	/***
	 * 表头
	 * 
	 * @param sheet
	 * @param headers
	 */
	private static void writeHeader(Sheet sheet, String[] headers) {
		Row row = sheet.createRow(0);
		sheet.setColumnWidth(0, 3600);
		for (int i = 0; i < headers.length; i++) {
			Cell cell = row.createCell(i);
			cell.setCellValue(headers[i]);
			if (i < 9) {
				cell.setCellStyle(getStyle(sheet.getWorkbook(), IndexedColors.GREEN.getIndex()));
			} else {
				cell.setCellStyle(getStyle(sheet.getWorkbook(), IndexedColors.SKY_BLUE.getIndex()));
			}
		}

	}

	/***
	 * 解析qpcr文件
	 * 
	 * @param qpcrfile
	 * @param criteria
	 * @return
	 */
	public abstract List<QpcrModel> readQpcrResult(File file, File task) throws Exception;

	private static CellStyle getStyle(Workbook workbook, short color) {
		CellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
		cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
		cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		cellStyle.setFillForegroundColor(color);
		cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		cellStyle.setWrapText(true);
		Font font = workbook.createFont();
		font.setFontHeightInPoints((short) 10);// 设置字体大小
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);// 粗体显示
		cellStyle.setFont(font);
		return cellStyle;
	}

}
