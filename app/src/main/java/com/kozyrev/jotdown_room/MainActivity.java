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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.kozyrev.jotdown_room.DB.Note;
import com.kozyrev.jotdown_room.DB.NoteDB;
import com.kozyrev.jotdown_room.RowTypes.ImageRowType;
import com.kozyrev.jotdown_room.RowTypes.RowType;
import com.kozyrev.jotdown_room.RowTypes.TextRowType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.MaybeObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.subscribers.DisposableSubscriber;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    RecyclerView notesRecycler;
    EditText searchEditText;
    Toolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView navigationView;

    private List<Note> notesList = null;
    boolean isCard = true;
    boolean isSearch = false;
    List<RowType> items = new ArrayList<>();

    NoteDB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initDB();
    }

    private void initViews(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.nav_open_drawer, R.string.nav_close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        notesRecycler = (RecyclerView) findViewById(R.id.notes_recycler);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, 1);
        notesRecycler.setLayoutManager(layoutManager);

        searchEditText = (EditText) findViewById(R.id.searchEditText);
    }

    private void initDB(){
        db = Room.databaseBuilder(getApplicationContext(), NoteDB.class, "notedatabase")
                .allowMainThreadQueries()
                .build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_search:
                createSearch();
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

        popupMenu.setOnMenuItemClickListener(menuItem -> {
                switch(menuItem.getItemId()) {
                    case R.id.menu_list_view:
                        if(isCard) {
                            isCard = false;
                            updateAdapter();
                        }
                        break;
                    case R.id.menu_cardlist_view:
                        if(!isCard) {
                            isCard = true;
                            updateAdapter();
                        }
                        break;
                }
                return true;
        });
        popupMenu.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        flowableAllNotes();
    }

    private void flowableAllNotes(){
        Flowable<List<Note>> notesAllList = db.getNoteDAO().getAllNotesFlowable();
        notesAllList
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSubscriber<List<Note>>() {
                    @Override
                    public void onNext(List<Note> listNote) {
                        Log.i("RxTest", "Next");
                        if (!isSearch) {
                            notesList = listNote;
                            if(notesRecycler.getAdapter() != null){
                                updateAdapter();
                            } else {
                                createAdapter();
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable t) {}

                    @Override
                    public void onComplete() {}
                });
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
        setAdapterDataSet();

        CaptionedImagesAdapter adapter = new CaptionedImagesAdapter(items);
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
                int noteId = notesList.get(position).getUid();
                Note note = db.getNoteDAO().getNoteById(noteId);

                // РАЗРЕШЕНИЯ СПРОСИТЬ
                File root = android.os.Environment.getExternalStorageDirectory();
                String path = root.getAbsolutePath() + "/VoiceRecords/Note" + noteId;
                File directory = new File(path);
                File[] files = directory.listFiles();
                for (File file : files){
                    file.delete();
                }
                directory.delete();

                db.getNoteDAO().delete(note);
                Toast.makeText(MainActivity.this, "Note deleted", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateAdapter(){
        setAdapterDataSet();

        CaptionedImagesAdapter adapter = (CaptionedImagesAdapter) notesRecycler.getAdapter();
        adapter.updateNotesList(items);
    }

    private void setAdapterDataSet(){
        if (items.size() > 0) items.clear();

        for (Note note : notesList){
            if ((note.getImageResourceUri() != null) && isCard){
                items.add(new ImageRowType(note));
            } else {
                items.add(new TextRowType(note));
            }
        }
    }

    private void createSearch(){
        isSearch = !isSearch;

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) searchEditText.getLayoutParams();

        if(isSearch) {
            setEditTextLayoutParams(params, (int) getResources().getDimension(R.dimen.searchEditText_height), (int) getResources().getDimension(R.dimen.margin_top));
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    String searchText = searchEditText.getText().toString();
                    maybeSearchNotes(searchText);
                }
            });
        } else {
            int nullDimen = (int) getResources().getDimension(R.dimen.height_null);
            setEditTextLayoutParams(params, nullDimen, nullDimen);
            searchEditText.setText("");
            flowableAllNotes();
        }

        searchEditText.setLayoutParams(params);
    }

    private void setEditTextLayoutParams(LinearLayout.LayoutParams params, int height, int topMargin){
        params.height = height;
        params.topMargin = topMargin;
    }

    private void maybeSearchNotes(String searchText){
        Maybe<List<Note>> notesMaybe = db.getNoteDAO().getAllNotesBySearchText(searchText);

        MaybeObserver<List<Note>> observer = new MaybeObserver<List<Note>>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {}

            @Override
            public void onSuccess(@NonNull List<Note> listNote) {
                notesList = listNote;
                updateAdapter();
            }

            @Override
            public void onError(@NonNull Throwable e) {}

            @Override
            public void onComplete() {}
        };
        notesMaybe.subscribe(observer);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(isSearch) createSearch();
    }
}