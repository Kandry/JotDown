package com.kozyrev.jotdown_room.NoteDetail;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.kozyrev.jotdown_room.AlarmReceiver;
import com.kozyrev.jotdown_room.R;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;

public class NoteAlarm {

    private static final int RQS_TIME = 10;

    private Context appContext;
    private Context activityContext;

    private TextView alarmTextView;
    private View rootView;
    Snackbar snackbar;

    private Calendar calendar;
    private Date alarmTime;
    private AlarmManager alarmService;

    private int alarmTextViewHeight;
    private boolean isAlarmUpdating = false;

    public NoteAlarm(Context appContext, Context activityContext, Calendar calendar, Date alarmTime, TextView alarmTextView, int alarmTextViewHeight, AlarmManager alarmService, View rootView){
        this.appContext = appContext;
        this.activityContext = activityContext;

        this.calendar = calendar;
        this.alarmTime = alarmTime;

        this.alarmTextView = alarmTextView;
        this.alarmTextViewHeight = alarmTextViewHeight;

        this.alarmService = alarmService;

        this.rootView = rootView;
    }

    public void initAlarmListeners() {
        alarmTextView.setOnClickListener(v -> {
            updateAlarm();
        });

        alarmTextView.setOnLongClickListener(v -> {
            alarmTextView.setText("");
            setAlarmTextViewParams(0);

            snackbar = Snackbar
                    .make(rootView, R.string.alarm_canceled_message, Snackbar.LENGTH_LONG);
            snackbar.addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar snackbar, int event) {
                    switch (event) {
                        case DISMISS_EVENT_TIMEOUT:
                        case DISMISS_EVENT_CONSECUTIVE:
                        case DISMISS_EVENT_MANUAL:
                            cancelAlarm(alarmTime.toString());
                            break;
                    }
                }
            });
            snackbar.setAction("UNDO", view -> {
                alarmTextView.setText(alarmTime.toString());
                setAlarmTextViewParams(alarmTextViewHeight);
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
            return true;
        });
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

        alarmTime = calendar.getTime();
        if (alarmTime.compareTo(new Date()) < 1) {
            Toast.makeText(appContext, "Установите дату и время больше текущей", Toast.LENGTH_LONG).show();
        } else {
            setAlarmTextViewParams(alarmTextViewHeight);
            alarmTextView.setText(alarmTime.toString());
        }
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

    public void setAlarm(Calendar targetCal, int noteId, String title, String description, String imageUriString){
        alarmTime = targetCal.getTime();
        String alarmText = alarmTime.toString();
        newAlarmIntent(alarmText, targetCal, noteId, title, description, imageUriString);
    }

    private void newAlarmIntent(String alarmText, Calendar targetCal, int noteId, String title, String description, String imageUriString){
        Intent intent = new Intent(appContext, AlarmReceiver.class);
        intent.putExtra(AlarmReceiver.EXTRA_NOTE_ID, noteId);
        intent.putExtra(AlarmReceiver.EXTRA_TITLE, title);
        intent.putExtra(AlarmReceiver.EXTRA_CONTENT_TEXT, description);
        intent.putExtra(AlarmReceiver.EXTRA_URI, imageUriString);
        intent.setAction(alarmText);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(appContext, RQS_TIME, intent, FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = alarmService;
        alarmManager.set(AlarmManager.RTC_WAKEUP, targetCal.getTimeInMillis(), pendingIntent);
    }

    public void updateAlarm(){
        isAlarmUpdating = true;
        cancelAlarm(alarmTime.toString());
        openDatePickerDialog(alarmTime);
    }

    public void cancelAlarm(String alarmText){
        if (isAlarmUpdating) isAlarmUpdating = false;

        Intent intent = new Intent(appContext,  AlarmReceiver.class);
        intent.setAction(alarmText);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(appContext, RQS_TIME, intent, FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = alarmService;
        alarmManager.cancel(pendingIntent);
        alarmTime = null;
    }

    public void setAlarmTextViewParams(int height){
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) alarmTextView.getLayoutParams();
        params.height = height;
        alarmTextView.setLayoutParams(params);
    }

    public Date getAlarmTime(){
        return alarmTime;
    }

    public void setAlarmTime(Date alarmTime) {
        this.alarmTime = alarmTime;
    }
}
