package com.kozyrev.jotdown_room;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputEditText;
import android.support.transition.Transition;
import android.support.transition.TransitionManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.kozyrev.jotdown_room.DB.Note;
import com.kozyrev.jotdown_room.DB.NoteDB;
import com.kozyrev.jotdown_room.Entities.Recording;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;

import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;

public class DetailNoteActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    static final int RQS_TIME = 10;

    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 100;
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 101;
    private static final int REQUEST_RECORD_AUDIO = 102;

    static final int REQUEST_GALLERY = 200;
    private static final int START_CAMERA_APP = 201;

    public static final String EXTRA_NOTE_ID = "uid";

    NoteDB db;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ImageView imageView;
    private TextInputEditText title;
    private TextInputEditText description;
    private TextView alarmTextView;
    private RecyclerView recyclerViewRecordings;
    private TextView textViewNoRecordings;
    private LinearLayout buttonsLayout;
    private ImageButton cameraButton;
    private ImageButton imageButton;
    private ImageButton audioRecordingButton;

    private Calendar calendar = Calendar.getInstance();
    private ShareActionProvider shareActionProvider;
    private MediaRecorder mediaRecorder;
    private Chronometer chronometer;
    private RecordingAdapter recordingAdapter;

    private int noteId;
    private String imageUriString;
    private boolean notImageAdding = true;
    private boolean isAlarmUpdating = false;
    private Uri cameraImageUri;
    Uri originalUri;
    private Date alarmTime = null;
    private String audioRecordFileName = null;
    private ArrayList<Recording> recordingArraylist;

    /* ACTIVITY_LIFE_CYCLE --------------- Взаимодействия с жизненным циклом активности ---------------------------------- */
    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("noteId", noteId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_note);

        initViews();
        initDB();

        noteId = (savedInstanceState != null)
                    ? savedInstanceState.getInt("noteId")
                    : (
                        (getIntent().getExtras() != null)
                                ? (int) getIntent().getExtras().get(EXTRA_NOTE_ID)
                                : -1);

        downloadData();
        initViewsListeners();
        // RxAndroid
        // Загружаем записи. Нужно переделать под RxAndroid, шоб ассинхронно и налету
        //fetchRecordings();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (notImageAdding){
            if (noteId < 0) addNote();
            else updateNote();
        }
    }
    /* ------------------------------- Конец взаимодействий с жизненным циклом активности ------------------------------- */

    private void initViews(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.nav_open_drawer, R.string.nav_close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        title = (TextInputEditText) findViewById(R.id.textTitle);
        description = (TextInputEditText) findViewById(R.id.textDescription);
        imageView = (ImageView) findViewById(R.id.imageNote);
        alarmTextView = (TextView) findViewById(R.id.textViewAlarmPrompt);

        recyclerViewRecordings = (RecyclerView) findViewById(R.id.recyclerViewRecordings);
        recyclerViewRecordings.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerViewRecordings.setHasFixedSize(true);
        textViewNoRecordings = (TextView) findViewById(R.id.textViewNoRecordings);

        buttonsLayout = (LinearLayout) findViewById(R.id.buttonsLayout);
        cameraButton = (ImageButton) findViewById(R.id.cameraButton);
        imageButton = (ImageButton) findViewById(R.id.imageButton);
        audioRecordingButton = (ImageButton) findViewById(R.id.audioRecordingButton);
    }

    private void initDB(){
        db = Room.databaseBuilder(getApplicationContext(), NoteDB.class, "notedatabase")
                .allowMainThreadQueries()
                .build();
    }

    private void downloadData(){
        Single<Note> noteSingle = db.getNoteDAO().getSingleNoteById(noteId);
        SingleObserver<Note> observer = new SingleObserver<Note>() {
            @Override
            public void onSubscribe(Disposable d) {}

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
                    imageView.setContentDescription(note.getName());
                }
                if (note.getAlarmTime() != 0){
                    alarmTime = new Date(note.getAlarmTime());
                    if (alarmTime.compareTo(new Date()) < 1) {
                        cancelAlarm(alarmTime.toString());
                    } else {
                        setAlarmTextViewParams((int) getResources().getDimension(R.dimen.searchEditText_height));
                        alarmTextView.setText(alarmTime.toString());
                    }
                }
            }

            @Override
            public void onError(Throwable e) {e.printStackTrace();}
        };
        noteSingle.subscribe(observer);
    }

    private void initViewsListeners(){
        alarmTextView.setOnClickListener(v -> {
            updateAlarm();
        });

        alarmTextView.setOnLongClickListener(v -> {
            cancelAlarm(alarmTime.toString());
            alarmTextView.setText("");
            Toast.makeText(getApplicationContext(), R.string.alarm_canceled_message, Toast.LENGTH_LONG).show();
            return true;
        });

        audioRecordingButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (getNeedPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO)){
                        audioRecordingButtonDownUp(event);
                    }
                } else {
                    audioRecordingButtonDownUp(event);
                }
                return true;
            }
        });
    }

    private void audioRecordingButtonDownUp(MotionEvent event){
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                prepareForRecording();
                startRecording();
                break;
            case MotionEvent.ACTION_UP:
                prepareForStop();
                stopRecording();
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean getNeedPermissions(String[] permissions, int requestCode){
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                if (shouldShowRequestPermissionRationale(permission)){
                    Toast.makeText(this, "Для корректной работы приложения предоставьте необходимые разрешения", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(permissions, requestCode);
                return false;
            }
        }
        return true;
    }

    /* TOOLBAR ------------------------------- Взаимодействия с панелью приложения -------------------------------------- */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_detail_note, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_create_notify:
                alarmTextView.setText("");
                openDatePickerDialog(null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }
    /* ------------------------------------ Конец взаимодействий с панелью приложения ----------------------------------- */

    /* ACTIVITY_CHOOSE ----------------------- Взаимодействия с активностью выбора -------------------------------------- */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        notImageAdding = true;

        switch (requestCode){
            case REQUEST_GALLERY:
                if(resultCode == RESULT_OK){
                    getImageFromGallery(data);
                }
                break;

            case START_CAMERA_APP:
                if (resultCode == RESULT_OK){
                    getPhotoFromCamera();
                }
                break;
        }
    }

    private void getImageFromGallery(Intent data){
        originalUri = data.getData();

        int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        getContentResolver().takePersistableUriPermission(originalUri, takeFlags);

        imageUriString = originalUri.toString();
        imageView.getLayoutParams().height = (int) getResources().getDimension(R.dimen.imageview_height);
        Picasso.get()
                .load(originalUri)
                .resize(533, 300)
                .centerCrop()
                .into(imageView);
    }

    private void getPhotoFromCamera(){
        imageUriString = cameraImageUri.toString();
        imageView.getLayoutParams().height = (int) getResources().getDimension(R.dimen.imageview_height);
        Picasso.get()
                .load(cameraImageUri)
                .resize(533, 300)
                .centerCrop()
                .into(imageView);
    }
    /* ------------------------------------ Конец взаимодействий с активностью выбора ----------------------------------- */

    /* SHARE --------------------------------- Взаимодействия с отправкой заметки --------------------------------------- */
    private void setShareActionIntent(String text){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        shareActionProvider.setShareIntent(intent);
    }
    /* ------------------------------------ Конец взаимодействий с отправкой заметки ------------------------------------ */

    /* IMAGE ----------------------------------- Взаимодействия с изображениями ----------------------------------------- */
    public void addImage(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_GALLERY);
    }
    /* -------------------------------------- Конец взаимодействий с изображениями -------------------------------------- */

    public void addPicture(View view){
        notImageAdding = false;
        if (view == cameraButton){
            addPhoto();
        } else if (view == imageButton) {
            addImage();
        }
    }

    /* PHOTO -------------------------------------- Взаимодействия с камерой -------------------------------------------- */
    public void addPhoto(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (getNeedPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE)){
                callCameraApp();
            }
        } else {
            callCameraApp();
        }
    }

    private void callCameraApp(){
        Intent cameraAppIntent = new Intent();
        cameraAppIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex){
            ex.printStackTrace();
        }

        String authorities = getApplicationContext().getPackageName() + ".fileprovider";
        cameraImageUri = FileProvider.getUriForFile(this, authorities, photoFile);

        cameraAppIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
        startActivityForResult(cameraAppIntent, START_CAMERA_APP);
    }

    private File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMAGE_" + timeStamp + "_";
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        return File.createTempFile(imageFileName, ".jpg", storageDirectory);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        switch (requestCode){
            case (REQUEST_WRITE_EXTERNAL_STORAGE):
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    callCameraApp();
                } else {
                    Toast.makeText(getApplicationContext(), "Нет разрешения на запись, фото не сохранено", Toast.LENGTH_LONG).show();
                    finishAffinity();
                }
                break;
            case (REQUEST_RECORD_AUDIO):
                if (!(grantResults.length == 3 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED)){
                    Toast.makeText(this, "Нет разрешений на работу с аудио. Аудозаметка не сохранена", Toast.LENGTH_SHORT).show();
                    finishAffinity();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }
    /* ----------------------------------------- Конец взаимодействий с камерой ----------------------------------------- */

    /* DATABASE ------------------------------------- Взаимодействия с БД ----------------------------------------------- */
    private void addNote(){
        Note note = new Note(title.getText().toString(), description.getText().toString(), imageUriString);
        setAlarmText(note);
        db.getNoteDAO().insert(note);
    }

    private void updateNote(){
        Note note = db.getNoteDAO().getNoteById(noteId);
        note.setName(title.getText().toString());
        note.setDescription(description.getText().toString());
        note.setImageResourceUri(imageUriString);
        setAlarmText(note);
        db.getNoteDAO().update(note);
    }

    private void setAlarmText(Note note){
        if (alarmTime != null) note.setAlarmTime(alarmTime.getTime());
    }
    /* ------------------------------------------- Конец взаимодействий с БД -------------------------------------------- */

    /* ALARM ------------------------------------ Взаимодействия с будильником ------------------------------------------ */
    DatePickerDialog.OnDateSetListener onDateSetListener = (DatePicker datePicker, int year, int month, int dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            openTimePickerDialog();
    };

    TimePickerDialog.OnTimeSetListener onTimeSetListener = (TimePicker timePicker, int hourOfDay, int minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            setAlarm(calendar);
    };

    private void openDatePickerDialog(Date updateDate){
        if (updateDate != null) calendar.setTime(updateDate);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, onDateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.setTitle("Выберите дату");
        datePickerDialog.show();
    }

    private void openTimePickerDialog(){
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, onTimeSetListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE), true);
        timePickerDialog.setTitle("Выберите время");
        timePickerDialog.show();
    }

    private void setAlarm(Calendar targetCal){
        alarmTime = targetCal.getTime();
        String alarmText = alarmTime.toString();

        if (isAlarmUpdating) cancelAlarm(alarmText);

        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        intent.putExtra(AlarmReceiver.EXTRA_NOTE_ID, noteId);
        intent.putExtra(AlarmReceiver.EXTRA_TITLE, title.getText().toString());
        intent.putExtra(AlarmReceiver.EXTRA_CONTENT_TEXT, description.getText().toString());
        intent.putExtra(AlarmReceiver.EXTRA_URI, imageUriString);
        intent.setAction(alarmText);

        setAlarmTextViewParams((int) getResources().getDimension(R.dimen.searchEditText_height));
        alarmTextView.setText(alarmText);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), RQS_TIME, intent, FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, targetCal.getTimeInMillis(), pendingIntent);
    }

    private void updateAlarm(){
        isAlarmUpdating = true;
        openDatePickerDialog(alarmTime);
    }

    private void cancelAlarm(String alarmText){
        if (isAlarmUpdating) isAlarmUpdating = false;
        else setAlarmTextViewParams((int) getResources().getDimension(R.dimen.height_null));

        Intent intent = new Intent(getApplicationContext(),  AlarmReceiver.class);
        intent.setAction(alarmText);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), RQS_TIME, intent, FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    private void setAlarmTextViewParams(int height){
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) alarmTextView.getLayoutParams();
        params.height = height;
        alarmTextView.setLayoutParams(params);
    }

    /* --------------------------------------- Конец взаимодействий с будильником --------------------------------------- */

    /* AUDIO ----------------------------------- Взаимодействия с аудиозаписями ----------------------------------------- */
   /* private void addAudioRecord() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (getNeedPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO)){
                prepareForRecording();
            }
        } else {
            prepareForRecording();
        }
    }*/

    private void prepareForRecording(){
        TransitionManager.beginDelayedTransition(buttonsLayout);
        startRecording();
    }

    private void startRecording(){
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

        File root = android.os.Environment.getExternalStorageDirectory();
        File file = new File(root.getAbsolutePath() + "/VoiceAudioRecords/Note" + noteId);
        if (!file.exists()){
            file.mkdirs();
        }

        audioRecordFileName = root.getAbsolutePath() + "/VoiceAudioRecords/Note" + noteId + "/" + String.valueOf(System.currentTimeMillis() + ".mp3");
        mediaRecorder.setOutputFile(audioRecordFileName);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        recordingAdapter.notifyStopPlaying();

        try{
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }

    private void prepareForStop(){
        TransitionManager.beginDelayedTransition(buttonsLayout);
    }

    private void stopRecording(){
        try {
            mediaRecorder.stop();
            mediaRecorder.release();
        } catch(Exception ex){
            ex.printStackTrace();
        }

        mediaRecorder = null;

        Toast.makeText(this, "Recording saved successfully.", Toast.LENGTH_SHORT).show();
    }

    private void fetchRecordings() {
        File root = android.os.Environment.getExternalStorageDirectory();
        String path = root.getAbsolutePath() + "/VoiceAudioRecords/Note" + noteId;
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        //Log.d("Files", "Size: " + files.length);
        if (files != null) {

            for (int i = 0; i < files.length; i++) {
                Log.d("Files", "FileName:" + files[i].getName());
                String fileName = files[i].getName();
                String recordingUri = root.getAbsolutePath() + "/VoiceAudioRecords/Note" + noteId + "/" + fileName;

                Recording recording = new Recording(recordingUri, fileName, false);
                recordingArraylist.add(recording);
            }

            textViewNoRecordings.setVisibility(View.GONE);
            recyclerViewRecordings.setVisibility(View.VISIBLE);
            setAdapterToRecyclerView();

        } else {
            textViewNoRecordings.setVisibility(View.VISIBLE);
            recyclerViewRecordings.setVisibility(View.GONE);
        }
    }

    private void setAdapterToRecyclerView(){
        recordingAdapter = new RecordingAdapter(this,recordingArraylist);
        recyclerViewRecordings.setAdapter(recordingAdapter);
    }
    /* -------------------------------------- Конец взаимодействий с аудиозаписями -------------------------------------- */
}