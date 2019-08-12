package com.kozyrev.jotdown_room.DB;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

public class DateConverter {
    @TypeConverter
    public Long dateToTimestamp(Date date){
        if(date == null){
            return null;
        } else {
            return date.getTime();
        }
    }
}
