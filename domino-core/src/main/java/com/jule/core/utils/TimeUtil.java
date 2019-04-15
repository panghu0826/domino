package com.jule.core.utils;

import lombok.extern.slf4j.Slf4j;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by xujian on 2017/5/24 0024.
 */
@Slf4j
public class TimeUtil {

    /**
     * 获取一天0 点时刻的毫秒
     * @return
     */
    public static long getDayBeginTimeInMs(){
        Calendar c1 = Calendar.getInstance();
        c1.set(Calendar.HOUR_OF_DAY, 0);
        c1.set(Calendar.MINUTE, 0);
        c1.set(Calendar.SECOND, 0);
        return c1.getTime().getTime();
    }

    public static int getCurrentMonth(){
        return Calendar.getInstance().get(Calendar.MONTH) + 1;
    }

    public static int getCurrentDay(){
        return Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 获取某年某年第几周
     * @return
     */
    public static String getWeek(){
        return Calendar.getInstance().get(Calendar.YEAR) + "-" + String.format("%02d",getCurrentMonth()) + "-" + String.format("%02d",Calendar.getInstance().get(Calendar.WEEK_OF_MONTH));
    }

    /**
     * 获取某年某月
     * @return
     */
    public static String getMonth(){
        return Calendar.getInstance().get(Calendar.YEAR) + "-" + String.format("%02d",getCurrentMonth());
    }

    public static void main(String[] args){
        System.out.println(getWeek());
        System.out.println(getMonth());
    }

    public static int getCurrentTime() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    public static boolean checkToday(int time) {
        return new SimpleDateFormat(
                "yyyy:MM:dd").format(new Date(time * 1000l)).equals(
                new SimpleDateFormat(
                        "yyyy:MM:dd").format(new Date()));
    }

    /**
     * 得到一个距离当天 时间00：00的秒数
     *
     * @return
     */
    public static int getNowTime() {
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.HOUR_OF_DAY) * 60 * 60 + c.get(Calendar.MINUTE)
                * 60 + c.get(Calendar.SECOND);
    }

    static final int DAY_SECONDS = 86400;// 一天的秒数

    /**
     * 判断是否为系统时间的昨天
     *
     * @param time
     *            秒为单位
     * @return
     */
    public static boolean isYesterday(int time) {
        return new SimpleDateFormat(
                "yyyy:MM:dd").format(new Date((time + DAY_SECONDS) * 1000l))
                .equals(new SimpleDateFormat(
                        "yyyy:MM:dd").format(new Date()));
    }

    public static int[] getOverDay(int startTime, int endTime) {
        int result[] = new int[3];
        return result;
    }

    /**
     * 获取系统距1970年1月1日总毫秒
     *
     * @return
     */
    public static long getSysCurTimeMillis() {
        return getCalendar().getTimeInMillis();
    }

    /**
     * 获取系统距1970年1月1日总秒
     *
     * @return
     */
    public static long getSysCurSeconds() {
        return getCalendar().getTimeInMillis() / 1000;
    }

    /**
     * 获取系统当前时间
     *
     * @return
     */
    public static Timestamp getSysteCurTime() {
        Timestamp ts = new Timestamp(getCalendar().getTimeInMillis());
        return ts;
    }

    public static Timestamp getSysMonth() {
        java.util.Calendar now = getCalendar();
        now.set(Calendar.DAY_OF_MONTH, 1);
        now.set(Calendar.HOUR_OF_DAY, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        formatter.format(now.getTime());
        return new Timestamp(now.getTimeInMillis());
    }

    /**
     * 获取指定日期距1970年1月1日总秒
     *
     * @param date
     * @return
     */
    public static long getDateToSeconds(Date date) {
        return getCalendar(date).getTimeInMillis() / 1000;
    }

    /**
     * 获取时间的秒
     *
     * @param time
     * @return
     */
    @SuppressWarnings("deprecation")
    public static int getTimeToSeconds(Time time) {
        if (time != null)
            return time.getHours() * 3600 + time.getMinutes() * 60
                    + time.getSeconds();
        return 0;
    }

    @SuppressWarnings("deprecation")
    public static Time getSecondsToTime(int seconds) {
        Time time = new Time(seconds / 3600, seconds % 3600 / 60,
                seconds % 3600 % 60);
        return time;
    }

    /**
     * 获取当前时间的秒
     *
     * @return
     */
    public static int getSysTimeSeconds() {
        Calendar cal = getCalendar();
        return cal.get(Calendar.HOUR_OF_DAY) * 3600 + cal.get(Calendar.MINUTE)
                * 60 + cal.get(Calendar.SECOND);
    }

    /**
     * 获取指定日期距1970年1月1日总毫秒
     *
     * @param date
     * @return
     */
    public static long getDateToMillis(Date date) {
        return getCalendar(date).getTimeInMillis();
    }

    /**
     * 获取当前小时
     *
     * @return
     */
    public static int getCurrentHour() {
        return getCalendar().get(Calendar.HOUR_OF_DAY);
    }

    /**
     * 获 取当前分钟
     *
     * @return
     */
    public static int getCurrentMinute() {
        return getCalendar().get(Calendar.MINUTE);
    }

    /**
     * 获取当前分钟
     *
     * @return
     */
    public static int getCurrentSecond() {
        return getCalendar().get(Calendar.SECOND);
    }

    /**
     * 指定的毫秒long值转成Timestamp类型
     *
     * @param value
     * @return
     */
    public static java.sql.Timestamp getMillisToDate(long value) {
        return new java.sql.Timestamp(value);
    }

    /**
     * 当前系统时间增加值
     *
     * @param type
     * @param value
     * @return
     */
    public static java.util.Date addSystemCurTime(int type, int value) {
        Calendar cal = getCalendar();
        switch (type) {
            case Calendar.DATE:// 增加天数
                cal.add(Calendar.DATE, value);
                break;
            case Calendar.HOUR:// 增加小时
                cal.add(Calendar.HOUR, value);
                break;
            case Calendar.MINUTE:// 增加分钟
                cal.add(Calendar.MINUTE, value);
                break;
            case Calendar.SECOND:// 增加秒
                cal.add(Calendar.SECOND, value);
                break;
            case Calendar.MILLISECOND:// 增加毫秒
                cal.add(Calendar.MILLISECOND, value);
                break;
            default:
                break;
        }
        return new java.util.Date(cal.getTimeInMillis());
    }

    public static Date getNextDate() {
        Calendar cal = getCalendar();
        cal.add(Calendar.DATE, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.add(Calendar.MILLISECOND, 0);
        return new Date(cal.getTimeInMillis());
    }

    public static long toNextDate() {
        long now = System.currentTimeMillis();
        Calendar cal = getCalendar();
        cal.add(Calendar.DATE, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.add(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis() - now;
    }

    /**
     * 格式化日期
     *
     * @param date
     * @return
     */
    public static String getDateFormat(java.util.Date date) {
        SimpleDateFormat formatter;
        formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String ctime = formatter.format(date);
        return ctime;
    }

    /**
     * 获取默认日期2000-01-01
     *
     * @return 返回默认起始时间
     */
    public static java.sql.Timestamp getDefaultDate() {
        java.util.Date defaultDate = null;
        try {
            defaultDate = (java.util.Date) new SimpleDateFormat(
                    "yyyy-MM-dd hh:mm:ss").parseObject("2000-01-01 00:00:00");

        } catch (ParseException e) {
            log.error(e.getMessage(),e);
        }
        return new java.sql.Timestamp(defaultDate.getTime());
    }

    /**
     * 获取默认目上限日期2999-01-01
     *
     * @return 返回默认上限时间
     */
    public static java.sql.Timestamp getDefaultMaxDate() {
        java.util.Date defaultDate = null;
        try {
            defaultDate = (java.util.Date) new SimpleDateFormat(
                    "yyyy-MM-dd hh:mm:ss").parseObject("2999-01-01 00:00:00");
        } catch (ParseException e) {
            log.error(e.getMessage(),e);
        }
        return new java.sql.Timestamp(defaultDate.getTime());
    }

    /**
     * <pre>
     * 比较日期是否同一天(注意：分界线为晚上 12 点)
     * </pre>
     *
     * @param date
     * @return
     */
    public static boolean dateCompare(Date date) {
        if (date == null)
            return false;
        java.util.Calendar now = getCalendar();
        java.util.Calendar other = getCalendar(date);
        return dateCompare(now, other) == 0 ? true : false;
    }

    /**
     * <pre>
     * 比较日期是否同一天(注意：分界线为晚上 12 点)
     * </pre>
     *
     * @return
     */
    public static boolean dateCompare(long date) {
        java.util.Calendar now = getCalendar();
        java.util.Calendar other = getCalendar(getMillisToDate(date));
        return dateCompare(now, other) == 0 ? true : false;
    }

    /**
     * <pre>
     * 比较是否为同一天(注意：分界线为凌晨 5 点)
     * </pre>
     *
     * @param date
     * @return
     */
    public static boolean dataCompare5(Date date) {
        if (date == null)
            return false;
        java.util.Calendar now = getCalendar();
        now.add(Calendar.HOUR_OF_DAY, -5);
        java.util.Calendar other = getCalendar(date);
        other.add(Calendar.HOUR_OF_DAY, -5);
        if (dateCompare(now, other) == 0) {
            return true;
        }
        return false;
    }

    /**
     * <pre>
     * 比较日期是否同一天(注意：分界线为晚上 12 点)
     * </pre>
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean dataCompare(Date date1, Date date2) {
        if (date1 == null || date2 == null)
            return false;
        java.util.Calendar c1 = getCalendar(date1);
        java.util.Calendar c2 = getCalendar(date2);
        return dateCompare(c1, c2) == 0 ? true : false;
    }

    /**
     * 返回两个日期相差天数
     *
     * @param startDate
     *            开始日期
     * @param endDate
     *            结束日期
     * @return
     */
    public static int dateCompare(java.util.Calendar startDate,
                                  java.util.Calendar endDate) {
        startDate.set(Calendar.HOUR_OF_DAY, 0);
        startDate.set(Calendar.MINUTE, 0);
        startDate.set(Calendar.SECOND, 0);
        startDate.set(Calendar.MILLISECOND, 0);

        endDate.set(Calendar.HOUR_OF_DAY, 0);
        endDate.set(Calendar.MINUTE, 0);
        endDate.set(Calendar.SECOND, 0);
        endDate.set(Calendar.MILLISECOND, 0);

        int day = (int) (endDate.getTimeInMillis() / 1000 / 60 / 60 / 24 - startDate
                .getTimeInMillis() / 1000 / 60 / 60 / 24);
        return day;
    }

    /**
     * <pre>
     * 比较日期是否同一天(注意：分界线为晚上 12 点)
     * </pre>
     *
     * @param startDate
     * @param endDate
     * @return
     * @throws Exception
     */
    public static int dateCompare(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        java.util.Calendar c1 = getCalendar(startDate);
        java.util.Calendar c2 = getCalendar(endDate);
        return dateCompare(c1, c2);
    }

    /**
     * <pre>
     * 返回两个日期相差天数(注意：分界线为凌晨 5 点)
     * </pre>
     *
     * @return
     */
    public static int dateCompare5(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        java.util.Calendar c1 = getCalendar(startDate);
        c1.add(Calendar.HOUR_OF_DAY, -5);
        java.util.Calendar c2 = getCalendar(endDate);
        c2.add(Calendar.HOUR_OF_DAY, -5);
        return dateCompare(c1, c2);
    }

    /**
     * 比较日期是否是同一个月份
     *
     * @param date
     *            被比较的日期
     * @return
     */
    public static boolean monthCompare(Date date) {// 一年之内是否是同一个月
        if (date == null)
            return false;
        java.util.Calendar now = getCalendar();
        java.util.Calendar other = getCalendar(date);
        int nowMonth = now.get(Calendar.MONTH) + 1;
        int otherMonth = other.get(Calendar.MONTH) + 1;
        return (otherMonth - nowMonth) == 0 ? true : false;
    }

    /**
     * 获取该月的天数
     *
     * @return
     */
    public static int monthDays() {// 返回当前月份的天数
        java.util.Calendar now = getCalendar();
        return now.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    /**
     * 获取当前是该月的第几天
     *
     * @return
     */
    public static int monthDay() {
        java.util.Calendar now = getCalendar();
        return now.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 重置防沉迷刷新时间
     *
     * @param hour
     *            刷新时间点
     * @param refreshTime
     *            刷新时间引用
     */
    public static void setAASRefreshTime(int hour, Calendar refreshTime) {
        refreshTime.setTime(getSysteCurTime());
        refreshTime.set(Calendar.HOUR_OF_DAY, hour);
        refreshTime.set(Calendar.MINUTE, 0);
        refreshTime.set(Calendar.SECOND, 0);
    }

    public static long calcDistanceMillis(Date startTime, Date endTime) {
        long startSecond = getDateToSeconds(startTime);
        long endSecond = getDateToSeconds(endTime);
        return (endSecond - startSecond) * 1000;
    }

    /**
     * 间隔时间以小时为单位
     *
     * @param startDate
     * @param interval
     * @return
     */
    public static boolean isInterval(Date startDate, int interval) {
        return dataCompare5(startDate);
    }

    public static int timeToFrame(int secondTime) {
        return (secondTime * 25) / 1000;
    }

    /**
     * 获取系统时间
     *
     * @return
     */
    private static java.util.Calendar getCalendar() {
        java.util.Calendar nowCalendar = java.util.Calendar.getInstance();
        nowCalendar.setTime(new java.util.Date());
        return nowCalendar;
    }

    /**
     * 获取指定的时间
     *
     * @param date
     * @return
     */
    public static java.util.Calendar getCalendar(Date date) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        return calendar;
    }

    public static Timestamp getCalendarToDate(java.util.Calendar calendar) {
        if (calendar != null)
            return new Timestamp(getCalendar().getTimeInMillis());
        return null;
    }

    public static Date addDate(Date date, long value) {
        long time = date.getTime() + value;
        return new Date(time);
    }

    /**
     * 把日期类型转换为字节数组
     *
     * @param date
     * @return
     */
    public static byte[] dateToBytes(Date date) {
        Calendar calendar = Calendar.getInstance();
        byte[] byteArray = new byte[7];
        calendar.setTime(date);
        short year = (short) calendar.get(Calendar.YEAR);
        byteArray[0] = (byte) ((year >>> 8) & 0xFF);
        byteArray[1] = (byte) (year & 0xFF);
        byteArray[2] = (byte) (calendar.get(Calendar.MONTH) + 1);
        byteArray[3] = (byte) calendar.get(Calendar.DATE);
        byteArray[4] = (byte) calendar.get(Calendar.HOUR_OF_DAY);
        byteArray[5] = (byte) calendar.get(Calendar.MINUTE);
        byteArray[6] = (byte) calendar.get(Calendar.SECOND);
        return byteArray;
    }

    public static Date getSunday() {
        int mondayPlus = getMondayPlus();
        GregorianCalendar currentDate = new GregorianCalendar();
        currentDate.add(GregorianCalendar.DATE, mondayPlus + 6);
        currentDate.set(Calendar.HOUR_OF_DAY, 0);
        currentDate.set(Calendar.MINUTE, 0);
        currentDate.set(Calendar.SECOND, 0);
        Date monday = currentDate.getTime();
        return monday;
    }

    public static Date getNextMonday() {
        int mondayPlus = getMondayPlus();
        GregorianCalendar currentDate = new GregorianCalendar();
        currentDate.add(GregorianCalendar.DATE, mondayPlus + 7);
        currentDate.set(Calendar.HOUR_OF_DAY, 0);
        currentDate.set(Calendar.MINUTE, 0);
        currentDate.set(Calendar.SECOND, 0);
        Date monday = currentDate.getTime();
        return monday;
    }

    private static int getMondayPlus() {
        Calendar cd = Calendar.getInstance();
        // 获得今天是一周的第几天，星期日是第一天，星期一是第二天......
        int dayOfWeek = cd.get(Calendar.DAY_OF_WEEK) - 1; // 因为按中国礼拜一作为第一天所以这里减1
        if (dayOfWeek == 1) {
            return 0;
        } else {
            return 1 - dayOfWeek;
        }
    }

    public static int getDayOfWeekIndex() {
        Calendar calendar = Calendar.getInstance();
        int index = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (index == 0) {
            index = 7;
        }
        return index;
    }

    public static boolean isTimeOut(Date expDate) {
        Calendar curentDate = Calendar.getInstance();
        Calendar expirtDate = Calendar.getInstance();
        expirtDate.setTime(expDate);

        long intervalMillis = expirtDate.getTimeInMillis()
                - curentDate.getTimeInMillis();
        return intervalMillis <= 0;
    }

    public static Date getSaturday(int nextWeek) {
        int mondayPlus = getMondayPlus();
        if (nextWeek > 0) {
            mondayPlus = mondayPlus + (nextWeek * 7);
        }
        GregorianCalendar currentDate = new GregorianCalendar();
        currentDate.add(GregorianCalendar.DATE, mondayPlus + 5);
        currentDate.set(Calendar.HOUR_OF_DAY, 5);
        currentDate.set(Calendar.MINUTE, 0);
        currentDate.set(Calendar.SECOND, 0);
        Date saturday = currentDate.getTime();
        return saturday;
    }

    public static boolean isSaturday() {
        int dayIndex = getDayOfWeekIndex();
        if (6 == dayIndex) {
            return true;
        }
        return false;
    }

    public static Date parseDate(String dateStr) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date;
        try {
            date = df.parse(dateStr);
            return date;
        } catch (ParseException e) {
            log.error(e.getMessage(),e);
        }
        return null;
    }

    public static boolean isInDate(long curTimeMillis, Date openDate,
                                   Date stopDate) {
        if (openDate == null || stopDate == null) {
            return false;
        }
        long openMillis = getDateToMillis(openDate);
        long stopMillis = getDateToMillis(stopDate);
        return curTimeMillis >= openMillis && curTimeMillis <= stopMillis;
    }

    public static boolean isAfter(long currentMillis, Date date) {
        if (date == null || date == null) {
            return false;
        }
        long openMillis = getDateToMillis(date);
        return currentMillis >= openMillis;
    }

    public static Date addTime(Date current, int type, int value) {
        Calendar cal = getCalendar(current);
        switch (type) {
            case Calendar.DATE:// 增加天数
                cal.add(Calendar.DATE, value);
                break;
            case Calendar.HOUR:// 增加小时
                cal.add(Calendar.HOUR, value);
                break;
            case Calendar.MINUTE:// 增加分钟
                cal.add(Calendar.MINUTE, value);
                break;
            case Calendar.SECOND:// 增加秒
                cal.add(Calendar.SECOND, value);
                break;
            case Calendar.MILLISECOND:// 增加毫秒
                cal.add(Calendar.MILLISECOND, value);
                break;
            default:
                break;
        }
        return new java.util.Date(cal.getTimeInMillis());
    }

    public static boolean isSaturday(Date date) {
        int dayIndex = getDayOfWeekIndex(date);
        if (6 == dayIndex) {
            return true;
        }
        return false;
    }

    public static int getDayOfWeekIndex(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int index = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (index == 0) {
            index = 7;
        }
        return index;
    }

    /** 上周一 00:00:01，譬如：当前时间 2013-05-03 返回：2013-04-29 00:00:30 */
    public static Date getSunday(int nextWeek) {
        int mondayPlus = getMondayPlus();
        if (nextWeek > 0) {
            mondayPlus = mondayPlus + (nextWeek * 7);
        }
        GregorianCalendar currentDate = new GregorianCalendar();
        currentDate.add(GregorianCalendar.DATE, mondayPlus);
        currentDate.set(Calendar.HOUR_OF_DAY, 0);
        currentDate.set(Calendar.MINUTE, 0);
        currentDate.set(Calendar.SECOND, 30);
        Date saturday = currentDate.getTime();
        return saturday;
    }

    /**
     * <pre>
     * 获取 date 所在周的周日日期
     * </pre>
     *
     * @param date
     * @return
     */
    public static Date getCurrentWeekEndDate(Date date) {
        Calendar aCalendar = Calendar.getInstance();
        aCalendar.setTime(date);

        // 取当前日期是星期几(week:星期几)
        int week = aCalendar.get(Calendar.DAY_OF_WEEK) - 1;
        int count = 0;
        if (week == 1) {
            count = 6;
        } else if (week == 2) {
            count = 5;
        } else if (week == 3) {
            count = 4;
        } else if (week == 4) {
            count = 3;
        } else if (week == 5) {
            count = 2;
        } else if (week == 6) {
            count = 1;
        }

        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_WEEK, count);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 0);
        return c.getTime();
    }
}
