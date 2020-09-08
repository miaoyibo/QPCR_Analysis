package com.huoyan.analysis;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import com.huoyan.constant.QpcrResult;
import com.huoyan.model.ControlStatus;
import com.huoyan.model.QpcrModel;
import com.huoyan.rule.QpcrRuleHandle;
import com.huoyan.util.POIUtil;

/***
 * 应该改用策略模式
 * 
 * @author miaoyibo
 *
 */
public class MixQpcrHandle extends WriteHandle {
	static String pattern = "[a-zA-Z][0-9]{1,}";
	private List<String> blankList = new ArrayList<>();
	private List<String> positiveList = new ArrayList<>();
	{
		blankList.add("阴性对照");
		blankList.add("空白对照");
		blankList.add("only mix");
		blankList.add("blank control");
		blankList.add("NF WATER");
		blankList.add("NF water");

		positiveList.add("阳性对照");
		positiveList.add("PC in Kit");
	}
	// FAM＜38→提示空白异常
	private double blank_fam = 38d;
	// 两个阳参FAM＞29,提示阳参数值偏高。两个阳参FAM无数值→提示阳参异常，整版重提
	private double positive_fam = 29d;
	// FAM无数值，VIC≤32→Negative
	private double sample_negative_vic = 32d;
	// FAM≤34，VIC≤32→Positive
	private double sample_positive_fam = 34d;
	private double sample_positive_vic = 32d;
	// 34＜FAM≤38（阳待），VIC≤32→阳待，下一步操作RE
	private double sample_re_fam_low = 34d;
	private double sample_re_fam_high = 38d;
	private double sample_re_vic = 32d;
	// FAM＞38（灰区），VIC≤32→灰区，下一步操作RE
	private double sample_grey_fam = 38d;
	private double sample_grey_vic = 32d;
	// VIC＞32，或者VIC无数值(VIC失控)→VIC失控，下一步操作RE
	private double sample_invalid_vic = 32d;

	public MixQpcrHandle(PoolHandle poolHandle, File qpcr, File task) {
		super(poolHandle, qpcr, task);
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws Exception {
		MixQpcrHandle qa = new MixQpcrHandle(new TianjinPoolHandle(), null, null);
		qa.apply();

	}

	@Override
	public List<QpcrModel> readQpcrResult(File file, File task) throws Exception {
		List<QpcrModel> results = new ArrayList<>();
		Workbook workbook = POIUtil.getWorkBook(file);
		if (workbook != null) {
			parserConfig();
			Sheet sheet = null;
			if ((sheet = workbook.getSheet("定量结果")) != null) {
				Sheet samplesheet = workbook.getSheet("样本信息");
				Map<String, String> map = parseSampleId(samplesheet);
				parseQpcr1(sheet, results, map);
			} else if ((sheet = workbook.getSheet("Quan. Result")) != null) {
				Sheet samplesheet = workbook.getSheet("样本信息");
				Map<String, String> map = parseSampleId(samplesheet);
				parseQpcr1(sheet, results, map);
			} else if ((sheet = workbook.getSheet("实验数据")) != null) {
				// ct,sample,样品类型
				int[] index = { 12, 2, 9 };
				parseQpcr2(sheet, results, index);
			} else if ((sheet = workbook.getSheet("Data")) != null) {
				// ct,sample,样品类型
				int[] index = { 5, 3, 7 };
				parseQpcr2(sheet, results, index);
			} else if ((sheet = workbook.getSheet("Sheet1")) != null) {
				parseQpcr(sheet, results);
			}
			try {
				workbook.close();
			} catch (IOException e) {
			}
		}
		String version = file.getName().substring(0, file.getName().lastIndexOf("."));
		results.forEach(q -> {
			q.setVersion(version);
		});
		return results;
	}

	private Map<String, String> parseSampleId(Sheet samplesheet) {
		Map<String, String> map = new HashMap<String, String>();
		if (samplesheet != null) {
			int lastRowNum = samplesheet.getLastRowNum();
			for (int rowNum = 1; rowNum <= lastRowNum; rowNum++) {
				Row row = samplesheet.getRow(rowNum);
				String index = POIUtil.getCellValue(row.getCell(0));
				String id = POIUtil.getCellValue(row.getCell(1));
				if (StringUtils.isNotEmpty(index)) {
					map.put(index, id);
				}
			}
		}
		return map;
	}

	private void parseQpcr(Sheet sheet, List<QpcrModel> results) {
		int lastRowNum = sheet.getLastRowNum();
		ControlStatus cs = new ControlStatus(false, 0, 0, 0);
		for (int rowNum = 1; rowNum <= lastRowNum; rowNum = rowNum + 2) {
			Row famrow = sheet.getRow(rowNum);
			Row vicrow = sheet.getRow(rowNum + 1);
			if (famrow == null || vicrow == null) {
				continue;
			}
			Cell famcell = famrow.getCell(7);
			Cell viccell = vicrow.getCell(7);
			QpcrModel qpcr = new QpcrModel();
			String sample = POIUtil.getCellValue(famrow.getCell(3));
			if (StringUtils.isEmpty(sample)) {
				continue;
			}
			// 孔位检查
			Cell cell = famrow.getCell(2);
			if (cell == null || !POIUtil.getCellValue(cell).matches(pattern)) {
				continue;
			}
			results.add(qpcr);
			qpcr.setDate(POIUtil.getCellValue(famrow.getCell(0)));
			qpcr.setFam(QpcrRuleHandle.formatFam(POIUtil.getCellValue(famcell)));
			qpcr.setVic(POIUtil.getCellValue(viccell));
			qpcr.setLoc(POIUtil.getCellValue(famrow.getCell(2)));
			qpcr.setSampleId(sample);
			qpcr.setSampleType(POIUtil.getCellValue(famrow.getCell(5)));
			if (chargeControlStatus(qpcr, cs)) {
				continue;
			}
			chargeSample(qpcr);
		}
		checkControllStatus(results, cs);
	}

	/***
	 * 
	 * @param sheet
	 * @param results
	 * @param criteria
	 * @param index    0:ct,1:sample,2:样品类型
	 */
	private void parseQpcr2(Sheet sheet, List<QpcrModel> results, int[] index) {
		ControlStatus cs = new ControlStatus(false, 0, 0, 0);
		Row daterow = sheet.getRow(2);
		String date = POIUtil.getCellValue(daterow.getCell(1));
		int lastRowNum = sheet.getLastRowNum();
		int startNum = 0;
		for (int rowNum = 3; rowNum <= lastRowNum; rowNum++) {
			Row row = sheet.getRow(rowNum);
			if (row != null && row.getCell(0) != null && "反应孔".equals(POIUtil.getCellValue(row.getCell(0)))) {
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
			// 孔位检查
			Cell cell = famrow.getCell(0);
			if (cell == null || !POIUtil.getCellValue(cell).matches(pattern)) {
				continue;
			}
			Cell famcell = famrow.getCell(index[0]);
			Cell viccell = vicrow.getCell(index[0]);
			QpcrModel qpcr = new QpcrModel();
			String sample = POIUtil.getCellValue(famrow.getCell(index[1]));
			if (StringUtils.isEmpty(sample)) {
				continue;
			}
			String sampleType = POIUtil.getCellValue(famrow.getCell(index[2]));
			results.add(qpcr);
			qpcr.setDate(date);
			qpcr.setFam(QpcrRuleHandle.formatFam(POIUtil.getCellValue(famcell)));
			qpcr.setVic(POIUtil.getCellValue(viccell));
			qpcr.setLoc(POIUtil.getCellValue(famrow.getCell(0)));
			qpcr.setSampleId(sample);
			qpcr.setSampleType(sampleType);
			if (chargeControlStatus(qpcr, cs)) {
				continue;
			}
			chargeSample(qpcr);
		}
		checkControllStatus(results, cs);
	}

	private void parseQpcr1(Sheet sheet, List<QpcrModel> results, Map<String, String> samplemap) {
		ControlStatus cs = new ControlStatus(false, 0, 0, 0);
		int lastRowNum = sheet.getLastRowNum();
		for (int rowNum = 1; rowNum <= lastRowNum; rowNum = rowNum + 2) {
			Row famrow = sheet.getRow(rowNum);
			Row vicrow = sheet.getRow(rowNum + 1);
			if (famrow == null || vicrow == null) {
				continue;
			}
			// 孔位检查
			Cell cell = famrow.getCell(0);
			if (cell == null || !POIUtil.getCellValue(cell).matches(pattern)) {
				continue;
			}
			Cell famcell = famrow.getCell(4);
			Cell viccell = vicrow.getCell(4);
			QpcrModel qpcr = new QpcrModel();
			String index = POIUtil.getCellValue(famrow.getCell(10));
			String sample = StringUtils.isEmpty(samplemap.get(index)) ? index : samplemap.get(index);
			if (StringUtils.isEmpty(sample)) {
				continue;
			}
			results.add(qpcr);
			// qpcr.setDate( POIUtil.getCellValue(famrow.getCell(0)));
			qpcr.setFam(QpcrRuleHandle.formatFam(POIUtil.getCellValue(famcell)));
			qpcr.setVic(QpcrRuleHandle.formatVic(POIUtil.getCellValue(viccell)));
			qpcr.setLoc(POIUtil.getCellValue(famrow.getCell(0)));
			qpcr.setSampleId(sample);
			qpcr.setSampleType(sample);
			if (chargeControlStatus(qpcr, cs)) {
				continue;
			}
			chargeSample(qpcr);
		}
		checkControllStatus(results, cs);
	}

	private void checkControllStatus(List<QpcrModel> results, ControlStatus cs) {
		if (cs.isBlankFlag()) {
			results.forEach(r -> r.setRemark("空白对照异常"));
			return;
		}
		if (cs.getPositiveNum() > 0) {
			if (cs.getPositiveNum() == cs.getErrorPositiveFlag()) {
				results.forEach(r -> {
					r.setRemark("阳性对照异常");
					r.setResult("整板重提");
					r.setNextStep(null);
				});
			} else if (cs.getPositiveNum() == cs.getWarningPositiveFlag()) {
				results.forEach(q -> {
					if (q.getType().equals("质控数据")) {
						q.setRemark("阳参数值偏高");
					}
				});
			}
		}
	}

	/***
	 * 解析配置文件
	 * 
	 * @return
	 * @throws Exception
	 */
	private void parserConfig() throws Exception {
		Properties pro = null;
		File file = new File("config.properties");
		if (file.exists()) {
			pro = new Properties();
			FileInputStream in = new FileInputStream(file);
			pro.load(in);
			in.close();
			blank_fam = Double.parseDouble(pro.getProperty("blank_fam"));
			positive_fam = Double.parseDouble(pro.getProperty("positive_fam"));
			sample_negative_vic = Double.parseDouble(pro.getProperty("sample_negative_vic"));
			sample_positive_fam = Double.parseDouble(pro.getProperty("sample_positive_fam"));
			sample_positive_vic = Double.parseDouble(pro.getProperty("sample_positive_vic"));
			sample_re_fam_low = Double.parseDouble(pro.getProperty("sample_re_fam_low"));
			sample_re_fam_high = Double.parseDouble(pro.getProperty("sample_re_fam_high"));
			sample_re_vic = Double.parseDouble(pro.getProperty("sample_re_vic"));
			sample_grey_fam = Double.parseDouble(pro.getProperty("sample_grey_fam"));
			sample_grey_vic = Double.parseDouble(pro.getProperty("sample_grey_vic"));
			sample_invalid_vic = Double.parseDouble(pro.getProperty("sample_invalid_vic"));

		}
	}

	private void chargeSample(QpcrModel qpcr) {
		boolean famempyt = StringUtils.isEmpty(qpcr.getFam()) || "NoCt".equals(qpcr.getFam());
		boolean vicv = NumberUtils.isCreatable(qpcr.getVic())
				&& Double.parseDouble(qpcr.getVic()) <= sample_negative_vic;
		if (famempyt && vicv) {
			qpcr.setResult(QpcrResult.Negative.getValue());
			qpcr.setType(QpcrResult.Negative.getGroup());
			return;
		}
		boolean pfamv = NumberUtils.isCreatable(qpcr.getFam())
				&& Double.parseDouble(qpcr.getFam()) <= sample_positive_fam;
		boolean pvicv = NumberUtils.isCreatable(qpcr.getVic())
				&& Double.parseDouble(qpcr.getVic()) <= sample_positive_vic;
		if (pfamv && pvicv) {
			qpcr.setResult(QpcrResult.Positive.getValue());
			qpcr.setType(QpcrResult.Positive.getGroup());
			qpcr.setRemark(QpcrResult.Positive.getValue());
			return;
		}
		boolean rfamvv = NumberUtils.isCreatable(qpcr.getFam())
				&& Double.parseDouble(qpcr.getFam()) <= sample_re_fam_high
				&& Double.parseDouble(qpcr.getFam()) > sample_re_fam_low;
		boolean rvicv = NumberUtils.isCreatable(qpcr.getVic()) && Double.parseDouble(qpcr.getVic()) <= sample_re_vic;
		if (rfamvv && rvicv) {
			qpcr.setResult(QpcrResult.ReRq.getValue());
			qpcr.setType(QpcrResult.ReRq.getGroup());
			qpcr.setNextStep(QpcrResult.ReRq.getNext());
			qpcr.setRemark(QpcrResult.ReRq.getValue());
			return;
		}
		boolean gfam = NumberUtils.isCreatable(qpcr.getFam()) && Double.parseDouble(qpcr.getFam()) > sample_grey_fam;
		boolean gvic = NumberUtils.isCreatable(qpcr.getVic()) && Double.parseDouble(qpcr.getVic()) <= sample_grey_vic;
		if (gfam && gvic) {
			qpcr.setResult(QpcrResult.Re.getValue());
			qpcr.setType(QpcrResult.Re.getGroup());
			qpcr.setNextStep(QpcrResult.Re.getNext());
			return;
		}
		boolean vicempyt = StringUtils.isEmpty(qpcr.getVic()) || "NoCt".equals(qpcr.getVic());
		boolean invalidvic = NumberUtils.isCreatable(qpcr.getVic())
				&& Double.parseDouble(qpcr.getVic()) > sample_invalid_vic;
		if (vicempyt || invalidvic) {
			qpcr.setResult(QpcrResult.InvalidVic.getValue());
			qpcr.setType(QpcrResult.InvalidVic.getGroup());
			qpcr.setNextStep(QpcrResult.InvalidVic.getNext());
			return;
		}
		qpcr.setResult(QpcrResult.Unknow.getValue());
		qpcr.setType(QpcrResult.Unknow.getGroup());
		qpcr.setRemark(QpcrResult.Unknow.getValue());
	}

	private boolean chargeControlStatus(QpcrModel qpcr, ControlStatus cs) {
		if (findBlankControl(qpcr)) {
			qpcr.setType("质控数据");
			if (checkBlankControl(qpcr)) {
				cs.setBlankFlag(true);
			}
			return true;
		}
		if (findPositiveControl(qpcr)) {
			qpcr.setType("质控数据");
			cs.setPositiveNum(cs.getPositiveNum() + 1);
			if (checkErrorPositiveControl(qpcr)) {
				cs.setErrorPositiveFlag(cs.getErrorPositiveFlag() + 1);
			} else if (checkWarningPositiveControl(qpcr)) {
				cs.setWarningPositiveFlag(cs.getWarningPositiveFlag() + 1);
			}
			return true;
		}
		String sample = qpcr.getSampleId();
		if (sample.contains("质控") || sample.contains("对照") || sample.contains("作废")) {
			qpcr.setType("质控数据");
			return true;
		}
		return false;
	}

	private boolean findBlankControl(QpcrModel qpcr) {
		String sample = qpcr.getSampleId();
		String sampleType = qpcr.getSampleType() == null ? "" : qpcr.getSampleType();
		Optional<String> findAny = blankList.stream().filter(b -> sample.contains(b) || sampleType.contains(b))
				.findAny();
		return findAny.isPresent();

	}

	private boolean findPositiveControl(QpcrModel qpcr) {
		String sample = qpcr.getSampleId();
		String sampleType = qpcr.getSampleType() == null ? "" : qpcr.getSampleType();
		Optional<String> findAny = positiveList.stream().filter(b -> sample.contains(b) || sampleType.contains(b))
				.findAny();
		return findAny.isPresent();

	}

	private boolean checkBlankControl(QpcrModel qpcr) {
		return NumberUtils.isCreatable(qpcr.getFam()) && Double.parseDouble(qpcr.getFam()) < blank_fam;
	}

	private boolean checkErrorPositiveControl(QpcrModel qpcr) {
		return StringUtils.isEmpty(qpcr.getFam()) || "NoCt".equals(qpcr.getFam());
	}

	private boolean checkWarningPositiveControl(QpcrModel qpcr) {
		return NumberUtils.isCreatable(qpcr.getFam()) && Double.parseDouble(qpcr.getFam()) > positive_fam;
	}

}