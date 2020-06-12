package com.huoyan.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
	
	
	public static String formatDate(long l) {
		Date date=new Date(l);

        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:MM");

        return sdf.format(date);
	}

}
