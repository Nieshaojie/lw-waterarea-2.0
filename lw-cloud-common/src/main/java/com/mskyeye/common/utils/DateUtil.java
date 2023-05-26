package com.mskyeye.common.utils;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @ProjectName syhf
 * @Package: com.suncreate.syhf.core.util
 * @ClassName: 类名称
 * @Description: java类
 * @Author: R.Gong
 * @CreateDate: 2018/6/21 15:20
 * @Version: 1.0
 */
public class DateUtil {
    /**
     * 根据oracle的Timestamp获取字符串日期时间
     * @param obj Timestamp时间
     * @param formatStr 格式化字符串，如果是null默认yyyy-MM-dd hh:mm:ss
     * @return 格式化后的字符串
     */
    public String getDateBySqlTimestamp(Timestamp obj, String formatStr) {
        try {
            if (formatStr == null || formatStr.equals("")) {
                formatStr = "yyyy-MM-dd hh:mm:ss";
            }
            SimpleDateFormat sf = new SimpleDateFormat(formatStr);
            Date date = new Date(obj.getTime());
            return sf.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getFormatByDate(Date date,String formatStr){
        try{
            if (formatStr == null || formatStr.equals("")) {
                formatStr = "yyyy/MM/dd HH:mm:ss";
            }
            SimpleDateFormat sf = new SimpleDateFormat(formatStr);
            return sf.format(date);
        } catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    public static String getCurDateBySqlDate(String formatStr){
        try{
            if (formatStr == null || formatStr.equals("")) {
                formatStr = "yyyy/MM/dd HH:mm:ss";
            }
            SimpleDateFormat sf = new SimpleDateFormat(formatStr);
            Date date = new Date();
            return sf.format(date);
        } catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    /**
     *将时间选择器的值转换成yyyy-MM-dd hh:mm:ss格式
     */
    public String setDateByTimeChoice(String strTimeChoice){
        try{
            String[] strBase = strTimeChoice.split(" ");
            String[] strDateArray = strBase[0].split("-");
            String[] strTimeArray = strBase[1].split(":");

            String strYear = strDateArray[0];
            String strMonth = strDateArray[1];
            String strDay = strDateArray[2];

            String strHour = strTimeArray[0];
            String strMinute = strTimeArray[1];
            String strSecond = strTimeArray[2];

            if(Integer.parseInt(strMonth) < 10){
                strMonth = "0" + strMonth;
            }
            if(Integer.parseInt(strDay) < 10){
                strDay = "0" + strDay;
            }

            return strYear + "-" + strMonth + "-" + strDay + " " + strHour + ":" + strMinute + ":" + strSecond;
        } catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    /**
     *将String日期格式化成标准格式数据，必须是yyyy-MM-dd hh:mm:ss格式
     */
    public String setStringFormat(String strTime){
        try{
            String[] strBase = strTime.split(" ");
            String[] strDateArray = strBase[0].split("-");
            String[] strTimeArray = strBase[1].split(":");

            String strYear = strDateArray[0];
            String strMonth = strDateArray[1];
            String strDay = strDateArray[2];

            String strHour = strTimeArray[0];
            String strMinute = strTimeArray[1];
            String strSecond = strTimeArray[2];

            if(strMonth.charAt(0) == '0'){
                strMonth = strMonth.substring(1);
            }
            if(strDay.charAt(0) == '0'){
                strDay = strDay.substring(1);
            }
            if(strHour.charAt(0) == '0'){
                strHour = strHour.substring(1);
            }
            if(strMinute.charAt(0) == '0'){
                strMinute = strMinute.substring(1);
            }
            if(strMinute.charAt(0) == '0'){
                strMinute = strMinute.substring(1);
            }
            return strYear + "-" + strMonth + "-" + strDay + " " + strHour + ":" + strMinute + ":" + strSecond;
        } catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 计算时间差
     * @param date1
     * @param date2
     * @return
     */
    public static int secondsBetween(Date date1, Date date2) {
        Calendar cal = Calendar.getInstance();
        if (date1 == null || date2 == null) {
            return 0;
        }
        cal.setTime(date1);
        long time1 = cal.getTimeInMillis();
        cal.setTime(date2);
        long time2 = cal.getTimeInMillis();
        return Math.abs(Integer.parseInt(String.valueOf((time2 - time1) / 1000L)));
    }

    /**
     * 获取当前存储航迹和报警的时间戳
     * @return
     */
    public static String getCurDataForStorage(){
        Calendar now = Calendar.getInstance();
        String strYear = String.valueOf(now.get(Calendar.YEAR));
        String strMonth = String.valueOf(now.get(Calendar.MONTH)+  1);
        String strDay = String.valueOf(now.get(Calendar.DAY_OF_MONTH));
        if(Integer.parseInt(strMonth) < 10){
            strMonth = "0" + strMonth;
        }
        if(Integer.parseInt(strDay) < 10){
            strDay = "0" + strDay;
        }
        return strYear + strMonth + strDay;
    }

}
