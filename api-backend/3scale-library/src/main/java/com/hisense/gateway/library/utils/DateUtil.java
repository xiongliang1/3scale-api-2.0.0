package com.hisense.gateway.library.utils;

import com.hisense.api.library.utils.MiscUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
public class DateUtil {
    /**
     * 获取起止时间之间的月份
     * @param startTime
     * @param endTime
     * @return
     */
    public static List<String> getMonths(String startTime, String endTime) {

        // 返回的日期集合
        List<String> months = new ArrayList<String>();

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        DateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM");
        try {
            Date start = dateFormat.parse(startTime);
            Date end = dateFormat.parse(endTime);

            Calendar tempStart = Calendar.getInstance();
            tempStart.setTime(start);

            Calendar tempEnd = Calendar.getInstance();
            tempEnd.setTime(end);
//            tempEnd.add(Calendar.MONTH, +1);// 日期加1(包含结束)
            while (tempStart.before(tempEnd)) {
                months.add(dateFormat2.format(tempStart.getTime()));
                tempStart.add(Calendar.MONTH, 1);
            }
        } catch (Exception e) {
            log.error("getMonths exception:",e);
        }
        return months;
    }
    /**
     * 获取起止时间之间的日期
     * @param startTime
     * @param endTime
     * @return
     */
    public static List<String> getDays(String startTime, String endTime) {

        // 返回的日期集合
        List<String> days = new ArrayList<String>();

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        DateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date start = dateFormat.parse(startTime);
            Date end = dateFormat.parse(endTime);

            Calendar tempStart = Calendar.getInstance();
            tempStart.setTime(start);

            Calendar tempEnd = Calendar.getInstance();
            tempEnd.setTime(end);
//            tempEnd.add(Calendar.DATE, +1);// 日期加1(包含结束)
            while (tempStart.before(tempEnd)) {
                days.add(dateFormat2.format(tempStart.getTime()));
                tempStart.add(Calendar.DAY_OF_YEAR, 1);
            }
        } catch (Exception e) {
            log.error("getDays",e);
        }
        return days;
    }

    /**
     * 获取起止时间之间的小时
     * @param startTime
     * @param endTime
     * @return
     */
    public static List<String> getHours(String startTime, String endTime) {

        // 返回的日期集合
        List<String> hours = new ArrayList<String>();

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        DateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH");
        try {
            Date start = dateFormat.parse(startTime);
            Date end = dateFormat.parse(endTime);

            Calendar tempStart = Calendar.getInstance();
            tempStart.setTime(start);

            Calendar tempEnd = Calendar.getInstance();
            tempEnd.setTime(end);
            while (tempStart.before(tempEnd)) {
                hours.add(dateFormat2.format(tempStart.getTime()));
                tempStart.add(Calendar.HOUR_OF_DAY, 1);
            }
        } catch (Exception e) {
            log.error("getHours exception:",e);
        }
        return hours;
    }


    /**
     * 将标砖的UTC时间转换为正常年月日格式
     * @param time exm：2017-11-18T07:12:06.615Z
     * @return
     */
    public static String formatUtcTime(String time){
        //此方法是将UTC格式的时间转化为秒为单位的Long类型。
        time = time.replace("Z", " UTC");//UTC是本地时间
        SimpleDateFormat format =new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss Z");
        format.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        Date d = null;
        try {
            d = format.parse(time);
        } catch (Exception e) {
            log.error("formatUtcTime exception:",e);
        }
        //此处是将date类型装换为字符串类型，比如：Sat Nov 18 15:12:06 CST 2017转换为2017-11-18 15:12:06
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sf.format(d);
    }


    /**
     * 时间转换
     * @param date
     */
    public static Date formatTimeZone(Date date){
        try{
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH");
            format.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            date =  format.parse(format.format(date));
        }catch (Exception e){
            log.error("时间转换异常",e);
        }
        return date;
    }

    public static Date getStartTime(int days) {
        Calendar dayStart = Calendar.getInstance();
        dayStart.add(Calendar.DATE, days);
        dayStart.set(Calendar.HOUR_OF_DAY, 0);
        dayStart.set(Calendar.MINUTE, 0);
        dayStart.set(Calendar.SECOND, 0);
        dayStart.set(Calendar.MILLISECOND, 0);
        return dayStart.getTime();
    }

    public static Date getEndTime(int days) {
        Calendar dayEnd = Calendar.getInstance();
        dayEnd.add(Calendar.DATE, days);
        dayEnd.set(Calendar.HOUR_OF_DAY, 23);
        dayEnd.set(Calendar.MINUTE, 59);
        dayEnd.set(Calendar.SECOND, 59);
        dayEnd.set(Calendar.MILLISECOND, 999);
        return dayEnd.getTime();
    }

    /**
     * 获取两个日期间隔的所有日期
     * @param start 格式必须为'2018-01-25'
     * @param end 格式必须为'2018-01-25'
     * @return
     */
    public static List<String> getBetweenDate(String start, String end) {
        List<String> list = new ArrayList<>();
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);

        long distance = ChronoUnit.DAYS.between(startDate, endDate);
        if (distance == 0) {
            list.add(startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            return list;
        }else if(distance<0){
            LocalDate temp = endDate;
            endDate = startDate;
            startDate = temp;
            distance = ChronoUnit.DAYS.between(startDate, endDate);
        }
        Stream.iterate(startDate, d -> {
            return d.plusDays(1);
        }).limit(distance + 1).forEach(f -> {
            list.add(f.toString());
        });
        return list;
    }

    public static Date asDate(String str){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.parse(str, dtf);
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDateTime.atZone(zone).toInstant();
        Date date = Date.from(instant);
        return date;
    }

    /**
     * Date转换为格式化时间
     * @param date date
     * @param pattern 格式
     * @return
     */
    public static String formatDate(Date date, String pattern){
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        return dateFormat.format(date);
    }
}
