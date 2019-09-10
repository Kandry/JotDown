package com.kozyrev.jotdown_room;

import android.Manifest;
import android.app.AlarmManager;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputEditText;
import android.support.transition.TransitionManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kozyrev.jotdown_room.Adapter.DetailNotePagerAdapter;
import com.kozyrev.jotdown_room.CustomViews.WrapContentHeightViewPager;
import com.kozyrev.jotdown_room.DB.Note;
import com.kozyrev.jotdown_room.DB.NoteDB;
import com.kozyrev.jotdown_room.NoteDetail.NoteAlarm;
import com.kozyrev.jotdown_room.NoteDetail.NoteCamera;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Date;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;

public class DetailNoteActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 100;
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 101;
    private static final int REQUEST_RECORD_AUDIO = 102;

    private static final int REQUEST_GALLERY = 200;
    private static final int START_CAMERA_APP = 201;
    private static final int REQUEST_PROVIDER = 202;

    public static final String EXTRA_NOTE_ID = "uid";

    NoteDB db;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private NestedScrollView nestedScrollView;
    private ImageView imageView;
    private TextInputEditText title, description;
    private TextView alarmTextView;
    private LinearLayout buttonsLayout;
    private ImageButton cameraButton, imageButton, audioRecordingButton, fileButton;

    private Calendar calendar = Calendar.getInstance();
    private ShareActionProvider shareActionProvider;
    private Chronometer chronometer;

    private NoteAlarm noteAlarm;
    private NoteCamera noteCamera;

    private int noteId;
    private String imageUriString, fileUriString = "";
    private boolean notImageAdding = true;
    private boolean isRecord = false;
    Uri originalUri;
    Uri fileUri;
    private Date alarmTime = new Date();

    private TabLayout tabLayout;

    private DetailNotePagerAdapter viewPagerAdapter;

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

        noteAlarm = new NoteAlarm(getApplicationContext(), this, calendar, alarmTime, alarmTextView, (int) getResources().getDimension(R.dimen.searchEditText_height),
                (AlarmManager) getSystemService(Context.ALARM_SERVICE), drawerLayout, noteId);
        noteAlarm.initAlarmListeners();

        alarmTextView.setOnClickListener(v -> {
            noteAlarm.updateAlarm(title.getText().toString(), description.getText().toString(), imageUriString);
        });

        downloadData();

        if (noteId < 0) noteId = (int) addNote();

        noteCamera = new NoteCamera(getApplicationContext(), this, imageView);

        WrapContentHeightViewPager wrapContentViewPager = findViewById(R.id.viewpager);

        tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(wrapContentViewPager);

        viewPagerAdapter = new DetailNotePagerAdapter(getSupportFragmentManager(), noteId, fileUriString);
        wrapContentViewPager.setAdapter(viewPagerAdapter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (notImageAdding){
            updateNote();
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

        nestedScrollView = (NestedScrollView) findViewById(R.id.nextedScrollView);
        nestedScrollView.setFillViewport(true);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        title = (TextInputEditText) findViewById(R.id.textTitle);
        description = (TextInputEditText) findViewById(R.id.textDescription);
        imageView = (ImageView) findViewById(R.id.imageNote);
        alarmTextView = (TextView) findViewById(R.id.textViewAlarmPrompt);

        buttonsLayout = (LinearLayout) findViewById(R.id.buttonsLayout);
        cameraButton = (ImageButton) findViewById(R.id.cameraButton);
        imageButton = (ImageButton) findViewById(R.id.imageButton);
        audioRecordingButton = (ImageButton) findViewById(R.id.audioRecordingButton);
        fileButton = findViewById(R.id.fileButton);
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
                    noteAlarm.setAlarmTime(alarmTime);
                    if (alarmTime.compareTo(new Date()) < 1) {
                        noteAlarm.cancelAlarm(alarmTime.toString());
                    } else {
                        calendar.setTime(alarmTime);
                        noteAlarm.setAlarmTextViewParams((int) getResources().getDimension(R.dimen.searchEditText_height));
                        alarmTextView.setText(alarmTime.toString());
                    }
                }
                if (!note.getFilesUri().equals("")){
                    fileUriString = note.getFilesUri();
                }
            }

            @Override
            public void onError(Throwable e) {e.printStackTrace();}
        };
        noteSingle.subscribe(observer);
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
                noteAlarm.openDatePickerDialog(null, title.getText().toString(), description.getText().toString(), imageUriString);
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
                    imageUriString = noteCamera.getPhotoFromCamera((int) getResources().getDimension(R.dimen.imageview_height));
                }
                break;

            case REQUEST_PROVIDER:
                if (resultCode == RESULT_OK){

                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        switch (requestCode){
            case (REQUEST_READ_EXTERNAL_STORAGE):
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    callCameraApp();
                } else {
                    Toast.makeText(getApplicationContext(), "Нет разрешения на запись, фото не сохранено", Toast.LENGTH_LONG).show();
                    finishAffinity();
                }
                break;
            case (REQUEST_WRITE_EXTERNAL_STORAGE):
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    fileButtonClick();
                } else {
                    Toast.makeText(getApplicationContext(), "Нет разрешения на чтение, файл не прикреплен", Toast.LENGTH_LONG).show();
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
    public void addImage(View view){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_GALLERY);
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
    /* -------------------------------------- Конец взаимодействий с изображениями -------------------------------------- */

    /* PHOTO -------------------------------------- Взаимодействия с камерой -------------------------------------------- */
    public void addPhoto(View view){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (getNeedPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE)){
                callCameraApp();
            }
        } else {
            callCameraApp();
        }
    }

    private void callCameraApp(){
        Intent cameraAppIntent = noteCamera.callCameraApp();
        startActivityForResult(cameraAppIntent, START_CAMERA_APP);
    }
    /* ----------------------------------------- Конец взаимодействий с камерой ----------------------------------------- */

    /* DATABASE ------------------------------------- Взаимодействия с БД ----------------------------------------------- */
    private long addNote(){
        Note note = new Note(title.getText().toString(), description.getText().toString(), imageUriString);
        return db.getNoteDAO().insert(note);
    }

    private void updateNote(){
        Note note = db.getNoteDAO().getNoteById(noteId);
        note.setName(title.getText().toString());
        note.setDescription(description.getText().toString());
        note.setImageResourceUri(imageUriString);
        note.setFilesUri(viewPagerAdapter.getNotesFileFragment().noteDetailFile.getFileUriString());
        setAlarmText(note);
        db.getNoteDAO().update(note);
    }
    /* ------------------------------------------- Конец взаимодействий с БД -------------------------------------------- */

    /* ALARM ------------------------------------ Взаимодействия с будильником ------------------------------------------ */
    private void setAlarmText(Note note){
        if (noteAlarm.getAlarmTime() != null) {
            note.setAlarmTime(noteAlarm.getAlarmTime().getTime());
        }
        else {
            note.setAlarmTime(0);
        }
    }
    /* --------------------------------------- Конец взаимодействий с будильником --------------------------------------- */

    /* AUDIO ----------------------------------- Взаимодействия с аудиозаписями ----------------------------------------- */
    public void addRecord(View view){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getNeedPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO)){
                recordButtonClick();
            }
        } else {
            recordButtonClick();
        }
    }

    private void recordButtonClick(){
        isRecord = !isRecord;
        if (isRecord) {
            prepareForRecording();
            viewPagerAdapter.getRecordingFragment().noteAudioRecord.startRecording();
        } else {
            prepareForStop();
            viewPagerAdapter.getRecordingFragment().noteAudioRecord.stopRecording();
        }
    }

    private void prepareForRecording(){
        TransitionManager.beginDelayedTransition(buttonsLayout);
        cameraButton.setEnabled(false);
        imageButton.setEnabled(false);
        fileButton.setEnabled(false);
        int audioButtonColor = getResources().getColor(R.color.colorAccent);
        audioRecordingButton.setBackgroundColor(audioButtonColor);
        // Ограничить доступность остальных действий
    }

    private void prepareForStop(){
        TransitionManager.beginDelayedTransition(buttonsLayout);
        cameraButton.setEnabled(true);
        imageButton.setEnabled(true);
        fileButton.setEnabled(true);
        int audioButtonColor = getResources().getColor(R.color.selected_background);
        audioRecordingButton.setBackgroundColor(audioButtonColor);
        // Снять ограничение доступности остальных действий
    }
    /* -------------------------------------- Конец взаимодействий с аудиозаписями -------------------------------------- */

    /* FILES -------------------------------------- Взаимодействия с файлами -------------------------------------------- */
    public void addFile(View view){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getNeedPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, REQUEST_READ_EXTERNAL_STORAGE)){
                fileButtonClick();
            }
        } else {
            fileButtonClick();
        }
    }

    private void fileButtonClick(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        startActivityForResult(Intent.createChooser(intent, "Select file"), REQUEST_PROVIDER);
    }

    private void getFileFromProvider(Intent data){
        fileUri = data.getData();
        int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        getContentResolver().takePersistableUriPermission(fileUri, takeFlags);

        viewPagerAdapter.getNotesFileFragment().noteDetailFile.addFileUri(fileUri.toString());
    }
    /* ----------------------------------------- Конец взаимодействий с файлами ----------------------------------------- */
}