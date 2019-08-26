package com.kozyrev.jotdown_room.NoteDetail;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.kozyrev.jotdown_room.AlarmReceiver;

import java.util.Calendar;
import java.util.Date;

import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;

public class NoteAlarm {

    private static final int RQS_TIME = 10;

    private Context appContext;
    private Context activityContext;

    private TextView alarmTextView;

    private Calendar calendar;
    private Date alarmTime;

    private int alarmTextViewHeight;
    private boolean isAlarmUpdating = false;

    public NoteAlarm(Context appContext, Context activityContext, Calendar calendar, Date alarmTime, TextView alarmTextView, int alarmTextViewHeight){
        this.appContext = appContext;
        this.activityContext = activityContext;

        this.calendar = calendar;
        this.alarmTime = alarmTime;

        this.alarmTextView = alarmTextView;
        this.alarmTextViewHeight = alarmTextViewHeight;
    }

    private DatePickerDialog.OnDateSetListener onDateSetListener = (DatePicker datePicker, int year, int month, int dayOfMonth) -> {
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        openTimePickerDialog();
    };

    private TimePickerDialog.OnTimeSetListener onTimeSetListener = (TimePicker timePicker, int hourOfDay, int minute) -> {
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        setAlarmTextViewParams(alarmTextViewHeight);
        alarmTextView.setText(calendar.getTime().toString());
    };

    public void openDatePickerDialog(Date updateDate){
        if (updateDate != null) calendar.setTime(updateDate);

        DatePickerDialog datePickerDialog = new DatePickerDialog(activityContext, onDateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.setTitle("Выберите дату");
        datePickerDialog.show();
    }

    private void openTimePickerDialog(){
        TimePickerDialog timePickerDialog = new TimePickerDialog(activityContext, onTimeSetListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE), true);
        timePickerDialog.setTitle("Выберите время");
        timePickerDialog.show();
    }

    public void setAlarm(Calendar targetCal, AlarmManager alarmSystemService, int noteId, String title, String description, String imageUriString){
        alarmTime = targetCal.getTime();
        String alarmText = alarmTime.toString();

        if (isAlarmUpdating) cancelAlarm(alarmText, alarmSystemService);

        newAlarmIntent(alarmText, targetCal, alarmSystemService, noteId, title, description, imageUriString);
    }

    private void newAlarmIntent(String alarmText, Calendar targetCal, AlarmManager alarmSystemService, int noteId, String title, String description, String imageUriString){
        Intent intent = new Intent(appContext, AlarmReceiver.class);
        intent.putExtra(AlarmReceiver.EXTRA_NOTE_ID, noteId);
        intent.putExtra(AlarmReceiver.EXTRA_TITLE, title);
        intent.putExtra(AlarmReceiver.EXTRA_CONTENT_TEXT, description);
        intent.putExtra(AlarmReceiver.EXTRA_URI, imageUriString);
        intent.setAction(alarmText);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(appContext, RQS_TIME, intent, FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = alarmSystemService;
        alarmManager.set(AlarmManager.RTC_WAKEUP, targetCal.getTimeInMillis(), pendingIntent);
    }

    public void updateAlarm(){
        isAlarmUpdating = true;
        openDatePickerDialog(alarmTime);
    }

    public void cancelAlarm(String alarmText, AlarmManager alarmSystemService){
        if (isAlarmUpdating) isAlarmUpdating = false;
        else setAlarmTextViewParams(0);

        Intent intent = new Intent(appContext,  AlarmReceiver.class);
        intent.setAction(alarmText);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(appContext, RQS_TIME, intent, FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = alarmSystemService;
        alarmManager.cancel(pendingIntent);
    }

    public void setAlarmTextViewParams(int height){
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) alarmTextView.getLayoutParams();
        params.height = height;
        alarmTextView.setLayoutParams(params);
    }
}
