package com.scratchpad;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;

import com.scratchpad.document.CssDocument;
import com.scratchpad.document.MarkdownDocument;
import com.scratchpad.document.WebDocument;


public class EditorActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks
{

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private boolean isUserInitiatedNavDrawerSelection;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position)
    {
        FragmentManager fragmentManager = getFragmentManager();
        if (!isUserInitiatedNavDrawerSelection)
        {
            isUserInitiatedNavDrawerSelection = true;
            Fragment fragment = fragmentManager.findFragmentById(R.id.container);
            if (fragment == null)
            {
                fragmentManager.beginTransaction()
                        .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                        .commit();
            }
        }
    }

    public void onSectionAttached(int number)
    {
        switch (number)
        {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar()
    {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        if (!mNavigationDrawerFragment.isDrawerOpen())
        {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.editor, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings)
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment
    {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private String editMarkdownText;
        private EditText editMarkdown;
        private WebView webPreview;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber)
        {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment()
        {
        }

        @Override
        public void onViewStateRestored(Bundle savedInstanceState) {
            super.onViewStateRestored(savedInstanceState);
            if (savedInstanceState != null) {
                // Restore text from editor.
                String loadedEditMarkdownText = savedInstanceState.getString("editMarkdownText");
                Log.i("EditorActivity.onSaveInstanceState",
                        "Loading from editMarkdownText in Bundle savedInstanceState: " +
                                loadedEditMarkdownText);
                editMarkdownText = loadedEditMarkdownText;
                editMarkdown.setText(editMarkdownText);
            }
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putString("editMarkdownText", editMarkdownText);
            Log.d("EditorActivity", "editMarkdown = " + editMarkdown);
            Log.i("EditorActivity",
                    "Saving as `editMarkdownText` in Bundle outState: " + editMarkdownText);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
        {
            Log.d("EditorActivity", "Entering onCreateView()...");
            View rootView = inflater.inflate(R.layout.fragment_editor, container, false);

            if (null == editMarkdown)
            {
                Log.d("EditorActivity", "Initializing editMarkdown with editMarkdownText = " +
                        editMarkdownText + " ...");
                editMarkdown = (EditText) rootView.findViewById(R.id.edit_markdown);
                editMarkdown.setTypeface(Typeface.MONOSPACE);
                if (savedInstanceState != null)
                {
                    String loadedEditMarkdownText =
                            savedInstanceState.getString("editMarkdownText");
                    Log.i("EditorActivity.onSaveInstanceState",
                            "Loading from editMarkdownText in Bundle savedInstanceState: " +
                                    loadedEditMarkdownText);
                    Log.i("EditorActivity", "loadedEditMarkdownText = " + loadedEditMarkdownText);
                    editMarkdownText = loadedEditMarkdownText;
                    editMarkdown.setText(editMarkdownText);
                }
                else
                {
                    editMarkdown.setText(editMarkdownText);
                }
            }

            if (null == webPreview)
            {
                Log.d("EditorActivity", "Initializing webPreview...");
                webPreview = (WebView) rootView.findViewById(R.id.web_preview);
                webPreview.setBackgroundColor(0x00000000);
                webPreview.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
            }

            editMarkdown.addTextChangedListener(new TextWatcher()
            {
                public void afterTextChanged(Editable s)
                {
                    editMarkdownText = editMarkdown.getText().toString();
                    MarkdownDocument markdownDocument =
                            MarkdownDocument.getInstance(editMarkdownText);
                    WebDocument webDocument = WebDocument.getInstance(markdownDocument,
                            CssDocument.getDefaultInstance());
                    webPreview.loadData(webDocument.getContent(), "text/html", null);
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                public void onTextChanged(CharSequence s, int start, int before, int count) {}
            });

            Log.d("EditorActivity", "Exiting onCreateView() with return = " + rootView + " ...");
            return rootView;
        }

        @Override
        public void onAttach(Activity activity)
        {
            super.onAttach(activity);
            ((EditorActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }
}