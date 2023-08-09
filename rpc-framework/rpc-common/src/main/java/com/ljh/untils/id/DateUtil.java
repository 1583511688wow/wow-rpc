package com.ljh.untils.id;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 时间工具类
 * @author ljh
 */
public class DateUtil {

    public static Date get(String date){

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date parse = dateFormat.parse(date);
            return parse;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }


}
