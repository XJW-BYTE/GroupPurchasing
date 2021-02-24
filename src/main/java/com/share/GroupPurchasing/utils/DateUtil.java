package com.share.GroupPurchasing.utils;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class DateUtil {

    public static Timestamp getCurrentDt() {

        Long startTime = getStartTime();

        return new Timestamp(startTime);
    }


    public static Timestamp addDays(int num) {

        Calendar todayStart = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
        todayStart.set(Calendar.HOUR_OF_DAY, 0);
        todayStart.set(Calendar.MINUTE, 0);
        todayStart.set(Calendar.SECOND, 40);
        todayStart.set(Calendar.MILLISECOND, 0);

        todayStart.add(Calendar.MONTH,num);

        return new Timestamp(todayStart.getTime().getTime());
    }



    private static Long getStartTime(){
        Calendar todayStart = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
        todayStart.set(Calendar.HOUR_OF_DAY, 0);
        todayStart.set(Calendar.MINUTE, 0);
        todayStart.set(Calendar.SECOND, 40);
        todayStart.set(Calendar.MILLISECOND, 0);
        return todayStart.getTime().getTime();
    }



    public static Timestamp addDays(Timestamp timestamp,int num) {

        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTime(new Date(timestamp.getTime()));
        gregorianCalendar.add(gregorianCalendar.MONTH,num);

        gregorianCalendar.getTime();
        Timestamp newTimestamp = new Timestamp(gregorianCalendar.getTime().getTime());

        return newTimestamp;
    }

}
