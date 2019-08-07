package com.kozyrev.jotdown_room;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.kozyrev.jotdown_room.DB.Note;
import com.kozyrev.jotdown_room.DB.NoteDB;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subscribers.DisposableSubscriber;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    RecyclerView notesRecycler;

    private List<Note> notesList = null;
    boolean isCard = true;

    NoteDB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        notesRecycler = (RecyclerView) findViewById(R.id.notes_recycler);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, 1);
        notesRecycler.setLayoutManager(layoutManager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_create_note:
                addNewNote();
                return true;
            case R.id.action_search:
                return true;
            case R.id.action_choose_list_view:
                showPopupMenu(findViewById(R.id.action_choose_list_view));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }

    private void showPopupMenu(View v){
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.inflate(R.menu.menu_choose_listview);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.menu_list_view:
                        if(isCard) {
                            isCard = !isCard;
                            createAdapter();
                        }
                        Log.i("RxTest", "isCard " + isCard);
                        break;
                    case R.id.menu_cardlist_view:
                        if(!isCard) {
                            isCard = !isCard;
                            createAdapter();
                        }
                        Log.i("RxTest", "isCard " + isCard);
                        break;
                }
                return true;
            }
        });
        popupMenu.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Flowable<List<Note>> notesList1 = db.getNoteDAO().getAllNotesFlowable();
        notesList1
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSubscriber<List<Note>>() {

                    @Override
                    public void onNext(List<Note> listNote) {
                        Log.i("RxTest", "Next");
                        notesList = listNote;
                        createAdapter();
                    }

                    @Override
                    public void onError(Throwable t) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onClickAdd(View view) {
        /*String imageUri = "android.resource://com.kozyrev.jotdown_room/" + R.drawable.note_example;
        db.getNoteDAO().insert(new Note("Example note", "This an example note", imageUri));*/
        addNewNote();
    }

    private void addNewNote(){
        Intent intent = new Intent(MainActivity.this, DetailNoteActivity.class);
        startActivity(intent);
    }

    private void createAdapter(){
        CaptionedImagesAdapter adapter = new CaptionedImagesAdapter(notesList, isCard);
        notesRecycler.setAdapter(adapter);

        adapter.setListener(new CaptionedImagesAdapter.Listener() {
            @Override
            public void onClick(int position) {
                Intent intent = new Intent(MainActivity.this, DetailNoteActivity.class);
                intent.putExtra(DetailNoteActivity.EXTRA_NOTE_ID, notesList.get(position).getUid());
                startActivity(intent);
            }

            @Override
            public void onLongClick(int position) {
                Note note = db.getNoteDAO().getNoteById(notesList.get(position).getUid());
                db.getNoteDAO().delete(note);
                Toast.makeText(MainActivity.this, "Note deleted", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
