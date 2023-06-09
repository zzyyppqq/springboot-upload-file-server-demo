package com.example.uploadingfiles.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * 描述：
 * 作者：@author alex
 * 创建时间：2023/3/14 3:17 PM
 */
public class TimeUtils {
    /**
     * 将时间转换成日期
     *
     * @param timeInMillis
     * @param dateFormat
     * @return
     */
    public static String formatDateToStr(long timeInMillis, String dateFormat) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        Date date = calendar.getTime();
        return formatDateToStr(date, dateFormat);
    }

    /**
     * Date转换成字符串日期
     *
     * @param date
     * @param dateFormat
     * @return
     */
    public static String formatDateToStr(Date date, String dateFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(date);
    }
}
