package com.hisense.gateway.library.utils.api;

import com.hisense.gateway.library.model.base.TimeQuery;
import com.hisense.gateway.library.model.base.analytics.ApiTrafficStatQuery;
import lombok.extern.slf4j.Slf4j;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.hisense.gateway.library.constant.AnalyticsConstant.*;
import static com.hisense.gateway.library.constant.BaseConstants.TAG;

@Slf4j
public class AnalyticsUtil {

    public static String formatTime(Date date) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        return format.format(date).replace(" ", "+");
    }
    public static void fixTimeQuery(ApiTrafficStatQuery statQuery, boolean sub) {
        TimeQuery timeQuery = statQuery.getTimeQuery();
        StatGranularity granularity = statQuery.getGranularity();

        Date current = new Date();

        if (timeQuery == null) {
            timeQuery = new TimeQuery();
            statQuery.setTimeQuery(timeQuery);
        }

        if (timeQuery.getStart() == null) {
            timeQuery.setStart(current);
        }

        if (timeQuery.getEnd() == null) {
            timeQuery.setEnd(current);
        }

        Calendar cal = Calendar.getInstance();
        if (sub) {
            cal.setTime(timeQuery.getStart());
            cal.add(Calendar.HOUR, -8);
            timeQuery.setStart(cal.getTime());

            cal.setTime(timeQuery.getEnd());
            cal.add(Calendar.HOUR, -8);
            timeQuery.setEnd(cal.getTime());
        }

        cal.setTime(timeQuery.getStart());
        cal.add(Calendar.HOUR, -1 * granularity.getHours());
        timeQuery.setStart(cal.getTime());

        log.info("{}amendTimeQuery(sub={}),statQuery={}", TAG, sub, statQuery);
    }
}
