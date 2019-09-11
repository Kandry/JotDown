package com.kozyrev.jotdown_room;

import android.app.SearchManager;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.graphics.Color;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.support.v7.widget.SearchView;

import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.OnDragInitiatedListener;
import androidx.recyclerview.selection.OnItemActivatedListener;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;

import com.kozyrev.jotdown_room.Adapter.CaptionedImagesAdapter;
import com.kozyrev.jotdown_room.Adapter.NoteItemKeyProvider;
import com.kozyrev.jotdown_room.Adapter.NoteItemLookup;
import com.kozyrev.jotdown_room.DB.Note;
import com.kozyrev.jotdown_room.DB.NoteDB;
import com.kozyrev.jotdown_room.RowTypes.ImageRowType;
import com.kozyrev.jotdown_room.RowTypes.RowType;
import com.kozyrev.jotdown_room.RowTypes.TextRowType;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.MaybeObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.subscribers.DisposableSubscriber;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SearchView.OnQueryTextListener{

    RecyclerView notesRecycler;
    Toolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    SearchView searchView;

    SelectionTracker selectionTracker;
    ActionMode actionMode;
    Menu menu;

    MenuItem itemSearch, itemChooseListView, itemSelectCount, itemClear, itemDelete;

    private List<Note> notesList = null;
    boolean isCard = true, isSearch = false;
    List<RowType> items = new ArrayList<>();
    ArrayMap<Integer, Note> removedNotes = new ArrayMap<>();

    NoteDB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initDB();
/*
        if (savedInstanceState != null) {
            selectionTracker.onRestoreInstanceState(savedInstanceState);
        }*/
    }

    private void initViews(){
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.nav_open_drawer, R.string.nav_close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        notesRecycler = findViewById(R.id.notes_recycler);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, 1);
        notesRecycler.setLayoutManager(layoutManager);
        notesRecycler.setItemAnimator(new DefaultItemAnimator());
        notesRecycler.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
    }

    private void initDB(){
        db = Room.databaseBuilder(getApplicationContext(), NoteDB.class, "notedatabase")
                .allowMainThreadQueries()
                .build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main_toolbar, menu);
        this.menu = menu;
        itemSearch = menu.findItem(R.id.action_search);
        itemChooseListView = menu.findItem(R.id.action_choose_list_view);
        itemSelectCount = menu.findItem(R.id.action_item_count);
        itemClear = menu.findItem(R.id.action_clear);
        itemDelete = menu.findItem(R.id.action_delete);

        //SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) itemSearch.getActionView();
        searchView.setOnQueryTextListener(this);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_choose_list_view:
                showPopupMenu(findViewById(R.id.action_choose_list_view));
                return true;
            case R.id.action_clear:
                selectionTracker.clearSelection();
                return true;
            case R.id.action_delete:
                deleteNotes();
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

    @Override
    protected void onResume() {
        super.onResume();
        if (searchView != null) {
            if (TextUtils.isEmpty(searchView.getQuery().toString())) {
                deleteSearch();
            } else {
                createSearch(searchView.getQuery().toString());
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        selectionTracker.onSaveInstanceState(outState);
    }

    private void flowableAllNotes(){
        Flowable<List<Note>> notesAllList = db.getNoteDAO().getAllNotesFlowable();
        notesAllList
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSubscriber<List<Note>>() {
                    @Override
                    public void onNext(List<Note> listNote) {
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
        Intent intent = new Intent(MainActivity.this, DetailNoteActivity.class);
        startActivity(intent);
    }

    private void createAdapter(){
        setAdapterDataSet();

        CaptionedImagesAdapter adapter = new CaptionedImagesAdapter(items);
        notesRecycler.setAdapter(adapter);

        buildSelectionTracker();
        adapter.setSelectionTracker(selectionTracker);
        addObserverSelectionTracker();
    }

    private void buildSelectionTracker(){
        selectionTracker = new SelectionTracker.Builder<>(
                "noteSelectionId",
                notesRecycler,
                new NoteItemKeyProvider(1, items),
                new NoteItemLookup(notesRecycler),
                StorageStrategy.createLongStorage()
        )
                .withOnItemActivatedListener(new OnItemActivatedListener<Long>() {
                    @Override
                    public boolean onItemActivated(@NonNull ItemDetailsLookup.ItemDetails<Long> item, @NonNull MotionEvent motionEvent) {
                        Intent intent = new Intent(MainActivity.this, DetailNoteActivity.class);
                        intent.putExtra(DetailNoteActivity.EXTRA_NOTE_ID, notesList.get(item.getPosition()).getUid());
                        startActivity(intent);
                        return true;
                    }
                })
                .withOnDragInitiatedListener(new OnDragInitiatedListener() {
                    @Override
                    public boolean onDragInitiated(@NonNull MotionEvent motionEvent) {
                        return true;
                    }
                })
                .build();
    }

    private void addObserverSelectionTracker(){
        selectionTracker.addObserver(new SelectionTracker.SelectionObserver() {
            @Override
            public void onItemStateChanged(@NonNull Object key, boolean selected) {
                super.onItemStateChanged(key, selected);
            }

            @Override
            public void onSelectionRefresh() {
                super.onSelectionRefresh();
            }

            @Override
            public void onSelectionChanged() {
                super.onSelectionChanged();
                if(selectionTracker.hasSelection() && actionMode == null){
                    actionMode = startSupportActionMode(new ActionModeController(MainActivity.this, selectionTracker));
                    setMenuVisisble(true);
                    setMenuItemTitle(selectionTracker.getSelection().size());
                } else if (!selectionTracker.hasSelection() && actionMode != null){
                    actionMode.finish();
                    actionMode = null;
                } else {
                    setMenuVisisble(false);
                    setMenuItemTitle(selectionTracker.getSelection().size());
                    selectionTracker.clearSelection();
                }

                Iterator<RowType> itemIterable = selectionTracker.getSelection().iterator();
                while (itemIterable.hasNext()){
                    Log.d("select+++", itemIterable.next().toString());
                    Log.d("select-", "Click4");
                }
            }

            @Override
            public void onSelectionRestored() {
                super.onSelectionRestored();
            }
        });
    }

    private void deleteNotes(){
        int sizeSelection = selectionTracker.getSelection().size();
        selectionTracker.clearSelection();

        List<Integer> positions = ((CaptionedImagesAdapter) notesRecycler.getAdapter()).positions;
        removedNotes.clear();

        for (int position : positions){
            removedNotes.put(position, notesList.get(position));
        }
        for (int position : positions){
            notesList.remove(removedNotes.get(position));
        }
        updateAdapter();

        Snackbar snackbar = Snackbar
                .make(drawerLayout, sizeSelection + " notes deleted", Snackbar.LENGTH_LONG);
        snackbar.addCallback(new Snackbar.Callback(){
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                switch(event) {
                    case DISMISS_EVENT_TIMEOUT:
                    case DISMISS_EVENT_CONSECUTIVE:
                    case DISMISS_EVENT_MANUAL:
                        for (int i = 0; i < removedNotes.size(); i++){
                            deleteNote(removedNotes.keyAt(i));
                        }
                        break;
                }
            }
        });
        snackbar.setAction("UNDO", view -> {
            for(int i = 0; i < removedNotes.size(); i++){
                notesList.add(removedNotes.keyAt(i), removedNotes.valueAt(i));
                updateAdapter();
            }
        });
        snackbar.setActionTextColor(Color.YELLOW);
        snackbar.show();
    }

    private void deleteNote(int position){
        int noteId =  removedNotes.get(position).getUid();
        Note note = db.getNoteDAO().getNoteById(noteId);

        // РАЗРЕШЕНИЯ СПРОСИТЬ
        File root = android.os.Environment.getExternalStorageDirectory();
        String path = root.getAbsolutePath() + "/VoiceRecords/Note" + noteId;
        File directory = new File(path);
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
        directory.delete();

        db.getNoteDAO().delete(note);
    }


    public void setMenuItemTitle(int selectedItemSize) {
        itemSelectCount.setTitle("" + selectedItemSize);
    }

    private void setMenuVisisble(boolean isSelect){
        itemSearch.setVisible(!isSelect);
        itemChooseListView.setVisible(!isSelect);
        itemSelectCount.setVisible(isSelect);
        itemClear.setVisible(isSelect);
        itemDelete.setVisible(isSelect);
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

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (TextUtils.isEmpty(newText)) {
            deleteSearch();
        } else {
            createSearch(newText);
        }
        return true;
    }
    
    private void createSearch(String searchText){
        isSearch = true;
        maybeSearchNotes(searchText);
    }

    private void deleteSearch(){
        isSearch = false;
        flowableAllNotes();
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
}