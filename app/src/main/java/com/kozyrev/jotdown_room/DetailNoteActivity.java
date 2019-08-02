package com.kozyrev.jotdown_room;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.kozyrev.jotdown_room.DB.Note;
import com.kozyrev.jotdown_room.DB.NoteDB;

import org.reactivestreams.Subscription;

import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.subscribers.DisposableSubscriber;

public class DetailNoteActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String EXTRA_NOTE_ID = "uid";
    static final int REQUEST_GALLERY = 100;

    NoteDB db;

    private ShareActionProvider shareActionProvider;

    private TextInputEditText title;
    private TextInputEditText description;
    private ImageView imageView;

    private Drawable drawable;

    private int noteId;
    private String imageUriString;

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
                    /*drawable = ContextCompat.getDrawable(DetailNoteActivity.this, note.getImageResourceId());
                    imageView.setImageDrawable(drawable);*/
                    Uri imageUri = Uri.parse(note.getImageResourceUri());
                    imageUriString = imageUri.toString();
                    imageView.setImageURI(null);
                    imageView.setImageURI(imageUri);
                    imageView.setContentDescription(note.getName());
                    imageView.getLayoutParams().height = (int) getResources().getDimension(R.dimen.imageview_height);
                }
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }
        };

        noteSingle.subscribe(observer);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        super.onPause();/*
        title = (TextInputEditText) findViewById(R.id.textTitle);
        description = (TextInputEditText) findViewById(R.id.textDescription);
        imageView = (ImageView) findViewById(R.id.imageNote);*/

        if (noteId > -1) {
            updateNote();
        } else {
            addNote();
        }
    }

    public void addImage(View v){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case REQUEST_GALLERY:
                if(resultCode == RESULT_OK){
                    Uri selectedImageUri = data.getData();
                    imageUriString = selectedImageUri.toString();
                    imageView.setImageURI(null);
                    imageView.setImageURI(selectedImageUri);
                    imageView.getLayoutParams().height = (int) getResources().getDimension(R.dimen.imageview_height);
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
}
