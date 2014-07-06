package com.scratchpad.editor;


import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.scratchpad.R;
import com.scratchpad.document.Document;
import com.scratchpad.document.MarkdownDocument;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer
 * .html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends Fragment
{
    /**
     * Header for the list view adapter.
     */
    public static final String TITLE = "title";

    /**
     * Subheader for the list view adapter.
     */
    public static final String MARKDOWN = "markdown";

    /**
     * Navigation drawer character width for portrait orientation.
     */
    public static final int MAX_CONTENT_LENGTH_PORTRAIT = 22;

    /**
     * Navigation drawer character width for landscape orientation.
     */
    public static final int MAX_CONTENT_LENGTH_LANDSCAPE = 32;

    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private NavigationDrawerCallbacks callbacks;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle drawerToggle;

    private DrawerLayout drawerLayout;
    private ListView drawerListView;
    private View fragmentContainerView;
    private EditorActivity.EditorFragment editorFragment;

    private int currentSelectedPosition = 0;
    private boolean isFromSavedInstanceState;
    private boolean isUserLearnedDrawer;

    private List<Document> documents;

    public void setEditorFragment(EditorActivity.EditorFragment editorFragment)
    {
        this.editorFragment = editorFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        isUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null)
        {
            currentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            isFromSavedInstanceState = true;
        }

        // Select either the default item (0) or the last selected item.
        selectItem(currentSelectedPosition);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        drawerListView = (ListView) inflater.inflate(
                R.layout.fragment_list, container, false);
        drawerListView.setBackgroundColor(Color.rgb(50, 50, 50));
        drawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });
        reloadListView();
        return drawerListView;
    }

    public void reloadListView() {
        File dirMarkdown = new File(Environment.getExternalStorageDirectory(), getString(R.string.dir_md));
        dirMarkdown.mkdirs();
        List<File> files = Arrays.asList(dirMarkdown.listFiles());
        List<Document> documents = new ArrayList<Document>();
        List<Map<String, String>> documentMaps = new ArrayList<Map<String, String>>();

        int maxContentLength = calculateMaxContentLength();

        for (File file : files)
        {
            Map<String, String> documentMap = new HashMap<String, String>();
            String title = file.getName().substring(0, file.getName().length() - 3);
            String markdown = "";
            try
            {
                Scanner scanner = new Scanner(file);
                boolean shouldStopScanning = false;
                while (scanner.hasNext() && !shouldStopScanning)
                {
                    String nextWord = scanner.next();
                    if ((markdown.length() + nextWord.length()) > maxContentLength)
                    {
                        shouldStopScanning = true;
                        int truncateLength = maxContentLength - markdown.length();
                        markdown += nextWord.substring(0, truncateLength) + "...";
                    }
                    else
                    {
                        markdown += nextWord + " ";
                    }
                }
            }
            catch (FileNotFoundException e)
            {
                Log.e("DocumentListFragment.onCreate",
                        String.format("File %s not found.", title),
                        e);
            }

            Document document = MarkdownDocument.getInstance(title, markdown);
            documents.add(document);
            documentMap.put(TITLE, title);
            documentMap.put(MARKDOWN, markdown);
            documentMaps.add(documentMap);
        }

        // TODO: replace with a real list adapter.
        SimpleAdapter simpleAdapter = new SimpleAdapter(
                getActivity(),
                documentMaps,
                android.R.layout.simple_list_item_2,
                new String[] { TITLE, MARKDOWN },
                new int[] { android.R.id.text1, android.R.id.text2 });
        drawerListView.setAdapter(simpleAdapter);
        drawerListView.setItemChecked(currentSelectedPosition, true);
        drawerListView.invalidateViews();
        this.documents = documents;
    }

    /**
     * Returns the maximum content length allowed for the navigation drawer
     * based on the current display orientation.
     *
     * @return maximum content length
     */
    private int calculateMaxContentLength() {
        int maxContentLength;Display getOrient = getActivity().getWindowManager().getDefaultDisplay();
        Point outPoint = new Point();
        getOrient.getSize(outPoint);
        if (outPoint.x > outPoint.y)
        {
            maxContentLength = MAX_CONTENT_LENGTH_LANDSCAPE;
        }
        else
        {
            maxContentLength = MAX_CONTENT_LENGTH_PORTRAIT;
        }
        return maxContentLength;
    }

    public boolean isDrawerOpen()
    {
        return drawerLayout != null && drawerLayout.isDrawerOpen(fragmentContainerView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout)
    {
        fragmentContainerView = getActivity().findViewById(fragmentId);
        this.drawerLayout = drawerLayout;
        this.drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        drawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                NavigationDrawerFragment.this.drawerLayout,                    /* DrawerLayout object */
                R.drawable.ic_drawer,             /* nav drawer image to replace 'Up' caret */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        )
        {
            @Override
            public void onDrawerClosed(View drawerView)
            {
                super.onDrawerClosed(drawerView);
                reloadListView();
                if (!isAdded())
                {
                    return;
                }

                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView)
            {
                super.onDrawerOpened(drawerView);
                reloadListView();
                if (!isAdded())
                {
                    return;
                }

                if (!isUserLearnedDrawer)
                {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    isUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }

                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!isUserLearnedDrawer && !isFromSavedInstanceState)
        {
            this.drawerLayout.openDrawer(fragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        this.drawerLayout.post(new Runnable() {
            @Override
            public void run() {
                drawerToggle.syncState();
            }
        });

        this.drawerLayout.setDrawerListener(drawerToggle);
    }

    private void selectItem(int position)
    {
        currentSelectedPosition = position;
        if (drawerListView != null)
        {
            drawerListView.setItemChecked(position, true);
        }
        if (drawerLayout != null)
        {
            drawerLayout.closeDrawer(fragmentContainerView);
        }
        if (callbacks != null)
        {
            callbacks.onNavigationDrawerItemSelected(position);
        }
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            callbacks = (NavigationDrawerCallbacks) activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        callbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, currentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        // If the drawer is open, show the global app actions in the action bar. See also
        // showGlobalContextActionBar, which controls the top-left area of the action bar.
        if (drawerLayout != null && isDrawerOpen())
        {
            inflater.inflate(R.menu.global, menu);
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (drawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public List<Document> getDocuments()
    {
        return documents;
    }

    /**
     * Per the navigation drawer design guidelines, updates the action bar to show the global app
     * 'context', rather than just what's in the current screen.
     */
    private void showGlobalContextActionBar()
    {
        /*
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        //actionBar.setTitle(R.string.app_name);
        */
    }

    private ActionBar getActionBar()
    {
        return getActivity().getActionBar();
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks
    {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(int position);
    }

    private void newDocument()
    {
        editorFragment.newDocument();
    }

    private void openDocument()
    {
        editorFragment.showOpenDocumentDialog();
    }

    private void saveDocument()
    {
        editorFragment.saveDocument();
    }
}
