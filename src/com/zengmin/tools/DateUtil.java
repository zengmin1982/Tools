package com.zengmin.tools;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DateUtil {
    public static final String FMT_K_11 = "yyyy年MM月dd日";
    public static final String FMT_K_6 = "MM月dd日";
    public static final String FMT_8 = "yyyyMMdd";
    public static final String FMT_10 = "yyyy-MM-dd";
    public static final String FMT_12 = "yyyyMMddHHmm";
    public static final String FMT_14 = "yyyyMMddHHmmss";
    public static final String FMT_16 = "yyyy-MM-dd HH:mm";
    public static final String FMT_17 = "yyyyMMddHHmmssSSS";
    public static final String FMT_19 = "yyyy-MM-dd HH:mm:ss";
    public static final String FMT_23 = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String FMT_T_6 = "HHmmss";
    public static final String FMT_T_8 = "HH:mm:ss";
    public static final String FMT_YM = "yyyy-MM";
    public static final String FMT_HM = "HH:mm";
    public static final String FMT_MDHMS = "MMddHHmmss";


    /**
     * 格式化日期（日期型->字符串）
     * 
     * @param date 日期型
     * @param fmt 格式
     * @return 字符串
     */
    public static String formatDate(Date date, String fmt) {
        DateFormat formatter = new SimpleDateFormat(fmt);
        return formatter.format(date);
    }

    /**
     * 格式化日期（字符串->日期型）
     * 
     * @param date 字符串
     * @param fmt 格式
     * @return 日期型
     */
    public static Date formatDate(String date, String fmt) {
        Date v = null;
        DateFormat formatter = new SimpleDateFormat(fmt);
        formatter.setLenient(false);
        try {
            v = formatter.parse(date);
        } catch (Exception e) {
            v = null;
        }
        return v;
    }

    /**
     * 格式化日期（字符串->日期型）
     * 
     * @param date 字符串
     * @param fmt 格式
     * @return 日期型
     */
    public static String formatDate(String date, String fmtFrom, String fmtTo) {
        String sDate = null;
        Date v = formatDate(date, fmtFrom);
        if (v != null) {
            sDate = formatDate(v, fmtTo);
        }
        return sDate;
    }

    /**
     * 获取指定日期N天之后的日期并格式化成指定格式
     * 
     * @param date 指定日期（日期型）
     * @param afterDays 日数
     * @param fmt 格式
     * @return 新日期
     */
    public static String getAfterDate(Date date, int afterDays, String fmt) {
        return formatDate(getNextDate(date, afterDays), fmt);
    }

    /**
     * 获取指定日期N天之后的日期
     * 
     * @param date 指定日期（字符串）
     * @param afterDays 日数
     * @param fmt 格式
     * @return 新日期
     */
    public static String getAfterDate(String date, int afterDays, String fmt) {
        return formatDate(getNextDate(formatDate(date, fmt), afterDays), fmt);
    }

    /**
     * 获取系统时间
     * 
     * @return 系统时间
     */
    public static Date getSysDate() {
        return new Date();
    }

    /**
     * 获取指定格式的系统时间
     * 
     * @param fmt 格式
     * @return 系统时间
     */
    public static String getSysDate(String fmt) {
        return formatDate(getSysDate(), fmt);
    }

    /**
     * 获取指定日期的显示字符串
     * 
     * @param date 指定日期
     * @return 显示字符串
     */
    public static String getShowDate(Date date) {
        String show = null;
        if (getSysDate(FMT_8).equals(formatDate(date, FMT_8))) {
            show = formatDate(date, FMT_HM);
        } else {
            show = formatDate(date, FMT_K_6);
        }
        return show;
    }

    /**
     * 当天的第一秒
     * 
     * @return
     */
    public static Date getCurrentdayBegin() {
        return getDateBegin(getSysDate());
    }

    /**
     * 当天的最后一秒
     * 
     * @return
     */
    public static Date getCurrentdayEnd() {
        return getDateEnd(getSysDate());
    }

    /**
     * 获取指定日期的开始时间(00:00:00)
     * 
     * @param d 指定日期
     * @return 指定日期的开始时间
     */
    public static Date getDateBegin(Date d) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 获取指定日期的结束时间(23:59:59)
     * 
     * @param d 指定日期
     * @return 指定日期的结束时间
     */
    public static Date getDateEnd(Date d) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        return cal.getTime();
    }

    /**
     * 获取指定日期所在月的第一天的日期
     * 
     * @param date 指定日期
     * @return 所在月的第一天的日期
     */
    public static Date getMonthBegin(Date d) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.set(Calendar.DATE, 1);
        return getDateBegin(cal.getTime());
    }

    /**
     * 获取指定日期所在月的最后一天的日期
     * 
     * @param date 指定日期
     * @return 所在月的最后一天的日期
     */
    public static Date getMonthEnd(Date d) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.add(Calendar.MONTH, 1);
        cal.set(Calendar.DATE, 1);
        cal.add(Calendar.DATE, -1);
        return getDateEnd(cal.getTime());
    }

    /**
     * 获取指定日期所在周的第一天的日期
     * 
     * @param date 指定日期
     * @return 所在周的第一天的日期
     */
    public static Date getWeekBegin(Date d) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return getDateBegin(cal.getTime());
    }

    /**
     * 获取指定日期所在周的最后一天的日期
     * 
     * @param date 指定日期
     * @return 所在周的最后一天的日期
     */
    public static Date getWeekEnd(Date d) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        return getDateEnd(cal.getTime());
    }

    /**
     * 获取指定年指定周的第一天的日期
     * 
     * @param year 指定年
     * @param week 指定周
     * @return 该周第一天的日期
     */
    public static Date getWeekBegin(int year, int week) {
        int afterDays = (week - 1) * 7;
        // 由于每年头几天被调整到上年，所以这里要加上偏移
        Calendar cal = Calendar.getInstance();
        cal.setTime(DateUtil.formatDate(year + "0101", DateUtil.FMT_8));
        int day = cal.get(Calendar.DAY_OF_WEEK);
        if (day == Calendar.MONDAY) {
            // do nothing
        } else if (day == Calendar.SUNDAY) {
            afterDays += 1;
        } else {
            afterDays += 9 - day;
        }
        cal.add(Calendar.DATE, afterDays);
        return cal.getTime();
    }

    /**
     * 获取指定日期是所在年的第几周
     * 
     * @param date 指定日期
     * @return int[] 所在年，所在周
     */
    public static int[] getWeekOfYear(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.setMinimalDaysInFirstWeek(7); // 每年第一周至少7天，意味着只有1号是周一时才算第一周，否则都要调整到上年
        int year = cal.get(Calendar.YEAR);
        int week = cal.get(Calendar.WEEK_OF_YEAR);
        if (cal.get(Calendar.MONTH) == Calendar.JANUARY && week > 5) {
            year -= 1; // 每年头几天调整到上年
        }
        return new int[] { year, week };
    }

    /**
     * 获取指定日期的N日之后的日期
     * 
     * @param date 指定日期
     * @param afterDays 日数
     * @return 新日期
     */
    public static Date getNextDate(Date date, int afterDays) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, afterDays);
        return cal.getTime();
    }

    /**
     * 获取指定日期的N月之后的日期
     * 
     * @param date 指定日期
     * @param afterMonths 月数
     * @return 新日期
     */
    public static Date getNextMonth(Date date, int afterMonths) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, afterMonths);
        return cal.getTime();
    }

    /**
     * 获取指定日期的N周之后的日期
     * 
     * @param date 指定日期
     * @param afterWeeks 周数
     * @return 新日期
     */
    public static Date getNextWeek(Date date, int afterWeeks) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, 7 * afterWeeks);
        return cal.getTime();
    }

    /**
     * 获取2个日期相差的天数
     * 
     * @param from
     * @param to
     * @return
     */
    public static int getDaysBetween2Date(Date from, Date to) {
        long sub = to.getTime() - from.getTime();
        int days = (int) (sub / (24 * 60 * 60 * 1000));
        if (sub % (24 * 60 * 60 * 1000) != 0) {
            days++;
        }
        return days;
    }

    /**
     * 获取两个日期之间的日期
     * 
     * @param startDate
     * @param endDate
     * @return
     */
    public static List<Date> getDatesBetween2Date(Date startDate, Date endDate) {
        List<Date> list = new ArrayList<Date>();
        Calendar calendarTemp = Calendar.getInstance();
        calendarTemp.setTime(startDate);
        while (calendarTemp.getTime().compareTo(endDate) <= 0) {
            list.add(calendarTemp.getTime());
            calendarTemp.add(Calendar.DATE, 1);
        }
        return list;
    }
}
