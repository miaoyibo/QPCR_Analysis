package com.huoyan.analysis;

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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.huoyan.model.CriteriaModel;
import com.huoyan.model.QpcrModel;
import com.huoyan.util.FileChooseUtil;

public abstract class QpcrHandle {
	private static String[] headers = { "���", "��Ӧ��", "��������", "FAM Ctֵ", "VIC", "���", "��ע", "QPCR�»�ʱ��" };
	private static String poolfile = "����";
	static String pattern = "[a-zA-Z][0-9]{1,}";
	
	private PoolHandle poolHandle;

	public QpcrHandle(PoolHandle poolHandle) {
		System.out.println("�вι���");
		this.poolHandle = poolHandle;
		System.out.println(poolHandle);
	}

	public void apply() throws Exception {
		Map<String, CriteriaModel> criteria = parserConfig();
		String qpcr = FileChooseUtil.chooseFiles("��ѡ��qpcr�ļ���", JFileChooser.DIRECTORIES_ONLY);
		if (qpcr == null) {
			return;
		}
		File file = new File(qpcr);
		File[] files = file.listFiles();
		List<QpcrModel> qpcrResult = new ArrayList<>();
		for (File file2 : files) {
			if (file2.isFile()) {
				List<QpcrModel> list = readQpcrResult(file2, criteria);
				if (list != null && !list.isEmpty()) {
					qpcrResult.addAll(list);
				}
			}
		}
		if (qpcrResult.isEmpty()) {
			JOptionPane.showMessageDialog(null, "no data.");
			return;
		}
		File pool = new File(qpcr + File.separator + poolfile);
		if (pool.exists()) {
			qpcrResult = poolHandle.addPoolSample(qpcrResult, pool);
		}

		Workbook workbook = new XSSFWorkbook();
		writeExcel(workbook, qpcrResult);
		FileOutputStream out = new FileOutputStream("result.xlsx");
		workbook.write(out);
		out.close();
		workbook.close();
		JOptionPane.showMessageDialog(null, "done.");
	}

	/***
	 * ���ɽ��
	 * 
	 * @param workbook
	 * @param typeset
	 * @param qpcrResult
	 */
	private static void writeExcel(Workbook workbook, List<QpcrModel> qpcrResult) {
		Map<String, List<QpcrModel>> map = qpcrResult.stream().collect(Collectors.groupingBy(QpcrModel::getType));
		List<QpcrModel> list = map.get("����");
		if (list != null) {
			Sheet sheet = workbook.createSheet("����ϴ�");
			sheet.setColumnWidth(1, 6000);
			sheet.setColumnWidth(2, 6000);
			sheet.setColumnWidth(3, 6000);
			String[] header = { "�������", "�����������/����/���ʧ�ܣ�", "���֤���Ǳ��", "���գ��Ǳ��" };
			writeHeader(sheet, header);
			int rownum = 1;
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
			writeHeader(sheet, headers);
			List<QpcrModel> value = entry.getValue();
			int rownum = 1;
			for (QpcrModel qpcrModel : value) {
				Row row = sheet.createRow(rownum);
				// json is better
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
	 * ��ͷ
	 * 
	 * @param sheet
	 * @param headers
	 */
	private static void writeHeader(Sheet sheet, String[] headers) {
		Row row = sheet.createRow(0);
		for (int i = 0; i < headers.length; i++) {
			Cell cell = row.createCell(i);
			cell.setCellValue(headers[i]);
		}

	}

	/***
	 * ����qpcr�ļ�
	 * 
	 * @param qpcrfile
	 * @param criteria
	 * @return
	 */
	public abstract List<QpcrModel> readQpcrResult(File file, Map<String, CriteriaModel> criteria);
/***
 * ���������ļ�
 * @return
 * @throws Exception
 */
	private static Map<String, CriteriaModel> parserConfig() throws Exception {
		Map<String, CriteriaModel> map = new HashMap<String, CriteriaModel>();
		Properties pro = new Properties();
		File file = new File("config.properties");
		if (file.exists()) {
			FileInputStream in = new FileInputStream(file);
			pro.load(in);
			in.close();
		} else {
			pro.put("nc.vic", "32");
			pro.put("pc.fam", "32");
			pro.put("sample.vic", "32");
			pro.put("sample.fam1", "38");
			pro.put("sample.fam2", "38");
		}

		// ����
		CriteriaModel c1 = new CriteriaModel();
		c1.setFam(Double.parseDouble(pro.getProperty("sample.fam1")));
		c1.setFamSymbol("<");
		c1.setVic(Double.parseDouble(pro.getProperty("sample.vic")));
		c1.setVicSymbol(">");
		c1.setSpecialVicValue("NoCt");
		map.put("����", c1);
		return map;
	}
}
