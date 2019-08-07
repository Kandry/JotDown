package com.kozyrev.jotdown_room;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputEditText;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.kozyrev.jotdown_room.DB.Note;
import com.kozyrev.jotdown_room.DB.NoteDB;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;

import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;

public class DetailNoteActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String EXTRA_NOTE_ID = "uid";
    static final int REQUEST_GALLERY = 100;
    final static int RQS_TIME = 1;

    NoteDB db;

    private ShareActionProvider shareActionProvider;

    private TextInputEditText title;
    private TextInputEditText description;
    private ImageView imageView;
    private TextView textView;
    private Calendar calendar = Calendar.getInstance();

    private int noteId;
    private String imageUriString;
    private boolean notImageAdding = true;
    Uri originalUri;

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("noteId", noteId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_note);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.nav_open_drawer, R.string.nav_close_drawer);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        db = Room.databaseBuilder(getApplicationContext(), NoteDB.class, "notedatabase")
                .allowMainThreadQueries()
                .build();

        if (savedInstanceState != null){
            noteId = savedInstanceState.getInt("noteId");
        } else if(getIntent().getExtras() != null){
            noteId = (int) getIntent().getExtras().get(EXTRA_NOTE_ID);
        } else {
            noteId = -1;
        }

        title = (TextInputEditText) findViewById(R.id.textTitle);
        description = (TextInputEditText) findViewById(R.id.textDescription);
        imageView = (ImageView) findViewById(R.id.imageNote);

        Single<Note> noteSingle = db.getNoteDAO().getSingleNoteById(noteId);

        SingleObserver<Note> observer = new SingleObserver<Note>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onSuccess(Note note) {
                title.setText(note.getName());
                description.setText(note.getDescription());
                if (note.getImageResourceUri() != null) {
                    Uri imageUri = Uri.parse(note.getImageResourceUri());
                    imageUriString = imageUri.toString();
                    imageView.getLayoutParams().height = (int) getResources().getDimension(R.dimen.imageview_height);
                    Picasso.get()
                            .load(imageUri)
                            .resize(800, 450)
                            .centerCrop()
                            .into(imageView);
                    //imageView.setImageURI(null);
                    //imageView.setImageURI(imageUri);
                    imageView.setContentDescription(note.getName());

                }
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }
        };

        noteSingle.subscribe(observer);

        textView = (TextView) findViewById(R.id.textViewAlarmPrompt);

        Button openTimeDialogButton = (Button) findViewById(R.id.buttonShowTimeDialog);
        openTimeDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText("");
                openDatePickerDialog();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }

    private void setShareActionIntent(String text){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        shareActionProvider.setShareIntent(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (notImageAdding){
            if (noteId < 0) {
                addNote();
            } else {
                updateNote();
            }
        }
    }

    public void addImage(View v){
        notImageAdding = false;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        notImageAdding = true;

        switch (requestCode){
            case REQUEST_GALLERY:
                if(resultCode == RESULT_OK){
                    originalUri = data.getData();
                    imageUriString = originalUri.toString();
                    imageView.getLayoutParams().height = (int) getResources().getDimension(R.dimen.imageview_height);
                    Picasso.get()
                            .load(originalUri)
                            .resize(533, 300)
                            .centerCrop()
                            .into(imageView);
                }
                break;
        }
    }

    private void addNote(){
        Note note = new Note(title.getText().toString(), description.getText().toString(), imageUriString);
        db.getNoteDAO().insert(note);
    }

    private void updateNote(){
        Note note = db.getNoteDAO().getNoteById(noteId);
        note.setName(title.getText().toString());
        note.setDescription(description.getText().toString());
        note.setImageResourceUri(imageUriString);
        db.getNoteDAO().update(note);
    }

    DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            openTimePickerDialog(true);
        }
    };

    TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            setAlarm(calendar);
        }
    };

    private void openDatePickerDialog(){
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, onDateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.setTitle("Выберите дату");
        datePickerDialog.show();
    }


    private void openTimePickerDialog(boolean is24hr){
        Calendar calendar = Calendar.getInstance();

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, onTimeSetListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE), is24hr);
        timePickerDialog.setTitle("Выберите время");
        timePickerDialog.show();
    }

    private void setAlarm(Calendar targetCal){
        textView.setText("Сигнализация установлена на ");
        textView.append(String.valueOf(targetCal.getTime()));

        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        intent.putExtra(AlarmReceiver.EXTRA_NOTE_ID, noteId);
        intent.putExtra(AlarmReceiver.EXTRA_TITLE, title.getText().toString());
        intent.putExtra(AlarmReceiver.EXTRA_CONTENT_TEXT, description.getText().toString());
        intent.putExtra(AlarmReceiver.EXTRA_URI, imageUriString);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), RQS_TIME, intent, FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, targetCal.getTimeInMillis(), pendingIntent);
    }
}
