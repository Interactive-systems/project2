package com.cs.helsinki.fi.interactivesystems;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class BookmarkListActivity extends ListActivity {
    private ArrayList<String> mDeletedEntryIds = new ArrayList<String>();
    private ArrayAdapter<Entry> mBookmarkAdapter;
    private Button mClearButton;
    private Button mExitButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bookmark_list);
        Bundle extras = getIntent().getExtras();
        
        ArrayList<Entry> data = extras.getParcelableArrayList("bookmarkEntries");
        populateListView(data);
        addActionListeners();
    }
    
    public void populateListView(ArrayList<Entry> data) {
        ListView itemListView = getListView();
        
        mBookmarkAdapter = new BookmarkAdapter(this, R.layout.bookmark_list_item, data);
        itemListView.setAdapter(mBookmarkAdapter);
    }
    
    public void addActionListeners() {
        // clear bookmarks list when clear button is clicked
        mClearButton = (Button) findViewById(R.id.clear_bookmarks);
        mClearButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mBookmarkAdapter.clear();
                mBookmarkAdapter.notifyDataSetChanged();
            }
        });
        
        // exit bookmarks list when exit button is clicked
        mExitButton = (Button) findViewById(R.id.exit);
        mExitButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(null);
            }
        });
    }
    
    public void finish(Entry entry) {
        Intent intent = getIntent();
        // send list of entry id's that were deleted from the list
        intent.putStringArrayListExtra("deletedBookmarks", mDeletedEntryIds);
        
        // if entry is set, send it's id
        // this is used to "activate" its marker
        // (=set map camera position according to its coords and show its info window)
        if(entry != null) {
            intent.putExtra("activeMarker", entry.getId());
        }
        
        setResult(RESULT_OK, intent);
        BookmarkListActivity.this.finish(); // finish this activity
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    
    
    private class BookmarkAdapter extends ArrayAdapter<Entry> {
        private Context mContext; 
        private int mLayoutResourceId;
        private ArrayList<Entry> mData;
        
        public BookmarkAdapter(Context context, int textViewResourceId, ArrayList<Entry> data) {
            super(context, textViewResourceId, data);
            
            mContext = context;
            mLayoutResourceId = textViewResourceId;
            mData = data;
        }
        
        public void remove(Entry entry) {
            mData.remove(entry); // remove from existing entries list
            BookmarkListActivity.this.mDeletedEntryIds.add(entry.getId()); // add to deleted list
            super.remove(entry); // remove from the listview
        }
        
        public void clear() {
            // add all entries to deleted list
            for(Entry entry : mData) {
                BookmarkListActivity.this.mDeletedEntryIds.add(entry.getId());
            }
            super.clear();
        }
        
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View row = convertView;
            BookmarkHolder holder = null;
            
            if(row == null) { // row hasn't been created yet
                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                row = inflater.inflate(mLayoutResourceId, parent, false);
                
                // create holder for the subviews so we don't need to find them
                // every time we need them
                holder = new BookmarkHolder();
                holder.icon = (ImageView) row.findViewById(R.id.map_icon);
                holder.title = (TextView) row.findViewById(R.id.title);
                
                row.setTag(holder);
            } else {
                holder = (BookmarkHolder) row.getTag(); // get subviews
            }
            
            final Entry entry = mData.get(position); // get the correct entry
            holder.title.setText(entry.getJob());
            
            // set OnClickListener for the title textview
            holder.title.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // open browser with the specified url and entry id
                    Entry entry = (Entry) mBookmarkAdapter.getItem(position);
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, UrlLibrary.getJobUrl(entry.getId()));
                    startActivity(browserIntent); 
                }
            });
            
            // set OnClickListener for the map icon
            holder.icon.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // finish BookmarkListActivity and return to map
                    // map camera position will be set according to the entry's position
                    // and its info window will be shown
                    BookmarkListActivity.this.finish(entry); 
                }
            });
            
            return row;
        }
    }
    
    static class BookmarkHolder {
        ImageView icon;
        TextView title;
    }
}
