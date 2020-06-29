package com.huoyan.analysis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.huoyan.model.QpcrModel;
import com.huoyan.util.POIUtil;

public abstract class PoolHandle {

	private static final String prefix = "新冠任务单-";
	private static final String sheetname = "QPCR上机";

	public List<QpcrModel> addPoolSample(List<QpcrModel> qpcrResult, File file) {
		//Map<String, List<QpcrModel>> map = qpcrResult.stream().collect(Collectors.groupingBy(QpcrModel::getVersion));
		List<String> versionList = qpcrResult.stream().map(QpcrModel::getVersion).collect(Collectors.toList());
		Map<String, List<String>> pollmap = parseSheet(file.listFiles(),versionList);
		if (!pollmap.isEmpty()) {
			List<QpcrModel> newResult=new ArrayList<>();
			for(QpcrModel model:qpcrResult) {
				String key=model.getVersion()+model.getSampleId();
				List<String> list = pollmap.get(key);
				if (list!=null&&!list.isEmpty()) {
					for (String id : list) {
						String[] ss = id.split("&");
						QpcrModel clone = model.clone();
						clone.setSampleId(ss[0]);
						clone.setVersion(ss[1]);
						newResult.add(clone);
					}
				}else {
					newResult.add(model);
				}
			}
			return newResult;
		}
		
		return qpcrResult;
	}

	private Map<String, List<String>> parseSheet(File[] files,List<String> versionList) {
		Map<String, List<String>> pollmap = new HashMap<String, List<String>>();
		for (File file2 : files) {
			String name = file2.getName();
			if (name.startsWith(prefix)) {
				name = name.substring(name.indexOf(prefix) + prefix.length(), name.lastIndexOf("."));
			}
			if (!versionList.contains(name)) {
				continue;
			}
			Workbook workbook = POIUtil.getWorkBook(file2);
			if (workbook == null || workbook.getSheet(sheetname) == null) {
				continue;
			}
			Sheet sheet = workbook.getSheet(sheetname);
			Row header = sheet.getRow(0);
			for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
				Row row = sheet.getRow(rowNum);
				// Cell cell = row.getCell(6);
				List<String> list = new ArrayList<String>();
				for (int i = 1; i < 6; i++) {
					Cell cell = row.getCell(i);
					Cell headerCell = header.getCell(i);
					String value = POIUtil.getCellValue(cell);
					if (StringUtils.isNotEmpty(value)) {
						list.add(value+"&"+POIUtil.getCellValue(headerCell));
					}
				}
				Cell cell = row.getCell(6);
				String cellValue = POIUtil.getCellValue(cell);
				if (!list.isEmpty() && StringUtils.isNoneEmpty(cellValue)) {
					pollmap.put(name + cellValue, list);
				}

			}
			try {
				workbook.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return pollmap;
	}
}
