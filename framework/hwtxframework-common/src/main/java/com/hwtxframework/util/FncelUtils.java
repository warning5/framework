package com.hwtxframework.util;

import com.jfinal.core.Controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by panye on 2014/9/21.
 */
public class FncelUtils {

    public static Date getDate(String date) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy");
        return simpleDateFormat.parse(date);
    }

    public static String getDate(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy");
        return simpleDateFormat.format(date);
    }

    public static boolean getStatistic(Controller controller) {
        boolean statistic = false;
        if (controller.getPara("statistic") != null) {
            statistic = controller.getParaToBoolean("statistic");
        }
        return statistic;
    }

    public static Date getYearDate(Date date) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return getDate(Integer.toString(calendar.get(Calendar.YEAR)));
    }
}
