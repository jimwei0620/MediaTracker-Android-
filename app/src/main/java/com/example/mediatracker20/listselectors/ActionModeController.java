package com.example.mediatracker20.listselectors;

import android.content.Context;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.recyclerview.selection.SelectionTracker;

import com.example.mediatracker20.R;
import com.example.mediatracker20.adapters.MediaItemAdapter;
import com.example.mediatracker20.adapters.MediaListAdapter;
import com.google.android.material.snackbar.Snackbar;

import java.util.Iterator;
import java.util.List;


import model.exceptions.ItemNotFoundException;
import model.google.Database;
import model.model.ListManager;
import model.model.MediaItem;
import model.model.MediaList;

//Controls action mode for selection trackers
public class ActionModeController implements ActionMode.Callback {

    private Context context;
    private SelectionTracker selectionTracker;
    private List<MediaList> currentList = null;
    private List<MediaItem> currentItems = null;
    private ListManager listManager;
    private String listName;
    private boolean isList;
    private MediaListAdapter mediaListAdapter;
    private MediaItemAdapter mediaItemAdapter;
    private MediaList listChosen = null;

    //List action mode
    public ActionModeController(Context context, SelectionTracker selectionTracker, List<MediaList> list, MediaListAdapter adapter) {
        construct(context, selectionTracker, adapter);
        currentList = list;
        isList = true;
    }

    //MediaItem action mode
    public ActionModeController(Context context, SelectionTracker selectionTracker, List<MediaItem> mediaItemList,
                                String name, MediaItemAdapter adapter, MediaList list) {
        construct(context, selectionTracker, adapter);
        currentItems = mediaItemList;
        listName = name;
        isList = false;
        listChosen = list;
    }

    //List action mode construct
    private void construct(Context context, SelectionTracker selectionTracker, MediaListAdapter adapter) {
        this.context = context;
        this.selectionTracker = selectionTracker;
        this.listManager = ListManager.getInstance();
        this.mediaListAdapter = adapter;
    }

    //Media Item action mode
    private void construct(Context context, SelectionTracker selectionTracker, MediaItemAdapter adapter) {
        this.context = context;
        this.selectionTracker = selectionTracker;
        this.listManager = ListManager.getInstance();
        this.mediaItemAdapter = adapter;
    }


    //inflate popup menu
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater menuInflater = mode.getMenuInflater();
        menuInflater.inflate(R.menu.selection_action_menu, menu); //TODO probably change menu depending on type too
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        if(isList) { // change action based on list or item "list"
            handleListActions(item, mode);
        } else {
            handleItemActions(item, mode);
        }
        return true;
    }

    // handle actions when lists are selected
    private void handleListActions(MenuItem item, ActionMode mode) {
        Iterator<MediaList> listIterator = selectionTracker.getSelection().iterator();
        switch (item.getItemId()) {
            case R.id.list_selection_delete:
                int count = 0;
                while (listIterator.hasNext()) {
                    try {
                        MediaList next = listIterator.next();
                        currentList.remove(next);
                        listManager.removeList(next);
                        Database.deleteInfo("Lists", next.hashCode());
                        count++;
                    } catch (ItemNotFoundException e) {
                        Toast.makeText(context, "Fail to delete all lists selected. Please contact support", Toast.LENGTH_SHORT);
                    }
                }
                onDestroyActionMode(mode);
                mediaListAdapter.notifyDataSetChanged();
                Toast.makeText(context, count + " lists deleted", Toast.LENGTH_SHORT);
                break;
            default:
                break;
        }
    }



    //handle list actions when items are selected
    private void handleItemActions(MenuItem item, ActionMode mode) {
        Iterator<MediaItem> itemIterator = selectionTracker.getSelection().iterator();
        switch (item.getItemId()) {
            case R.id.list_selection_delete:
                int count = 0;
                while (itemIterator.hasNext()) {
                    try {
                        MediaItem next = itemIterator.next();
                        listManager.deleteMediaItemFromList(listChosen, next);
                        Database.deleteInfo("Items", next.hashCode());
                        count++;
                    } catch (ItemNotFoundException e) {
                        Toast.makeText(context, "Fail to delete all items selected. Please contact support", Toast.LENGTH_SHORT);
                    }
                }
                onDestroyActionMode(mode);
                mediaItemAdapter.notifyDataSetChanged();
                Toast.makeText(context, count + " lists deleted", Toast.LENGTH_SHORT);

        }
        //TODO handle item deletes
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mode.finish();
        selectionTracker.clearSelection();
    }

}
