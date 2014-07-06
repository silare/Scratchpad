package com.scratchpad.editor;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.Toast;

import com.scratchpad.R;
import com.scratchpad.document.CssDocument;
import com.scratchpad.document.MarkdownDocument;
import com.scratchpad.document.WebDocument;
import com.scratchpad.util.MarkdownUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;


public class EditorActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks
{
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment navigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence actionBarTitle;

    /**
     * Used to determine if the call to onNavigationDrawerItemSElect is user-initiated; ensures
     * preservation of the data kept in the editor.
     */
    private boolean isUserInitiatedNavDrawerSelection;

    private EditorFragment editorFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        navigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        navigationDrawerFragment.setEditorFragment(editorFragment);
        actionBarTitle = getTitle();

        // Set up the drawer.
        navigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        navigationDrawerFragment.reloadListView();
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
                editorFragment = EditorFragment.newInstance(position + 1);
                fragmentManager.beginTransaction()
                        .replace(R.id.container, editorFragment)
                        .commit();
            }
        }
        if (navigationDrawerFragment != null &&
                navigationDrawerFragment.getDocuments() != null) {
            editorFragment.openDocument(navigationDrawerFragment
                    .getDocuments().get(position).getTitle());
        }
    }

    public void onSectionAttached(int number)
    {
        switch (number)
        {
            case 1:
                //actionBarTitle = getString(R.string.title_section1);
                break;
            case 2:
                //actionBarTitle = getString(R.string.title_section2);
                break;
            case 3:
                //actionBarTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar()
    {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(actionBarTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        if (!navigationDrawerFragment.isDrawerOpen())
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
        switch (item.getItemId())
        {
            case R.id.action_new:
                editorFragment.newDocument();
                return true;
            case R.id.action_open:
                editorFragment.showOpenDocumentDialog();
                return true;
            case R.id.action_save:
                editorFragment.saveDocument();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class EditorFragment extends Fragment
    {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private static final String ACTION_BAR_TITLE_BUNDLE = "ACTION_BAR_TITLE";
        private static final String FILENAME_BUNDLE = "FILENAME_TITLE";
        private static final String EDIT_MARKDOWN_TEXT_BUNDLE = "EDIT_MARKDOWN_TEXT";
        public static final String MD_EXTENSION = ".md";
        private String title;
        private String filename;
        private String editMarkdownText;
        private EditText editMarkdown;
        private WebView webPreview;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static EditorFragment newInstance(int sectionNumber)
        {
            EditorFragment fragment = new EditorFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public EditorFragment()
        {
        }

        @Override
        public void onViewStateRestored(Bundle savedInstanceState) {
            super.onViewStateRestored(savedInstanceState);
            if (null == savedInstanceState)
            {
                newDocument();
            }
            else
            {
                // Restore text from editor.
                String loadedTitle = savedInstanceState.getString(ACTION_BAR_TITLE_BUNDLE);
                String loadedFilename = savedInstanceState.getString(FILENAME_BUNDLE);
                String loadedEditMarkdownText =
                        savedInstanceState.getString(EDIT_MARKDOWN_TEXT_BUNDLE);
                Log.i("EditorActivity.onViewStateRestored",
                        String.format("Loading from Bundle savedInstanceState: " +
                                        "(title = %s, filename = %s, editMarkdownText = %s)",
                                loadedTitle, loadedFilename, loadedEditMarkdownText
                        )
                );
                setTitle(loadedTitle);
                filename = loadedFilename;
                editMarkdownText = loadedEditMarkdownText;
                editMarkdown.setText(editMarkdownText);
            }
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putString(ACTION_BAR_TITLE_BUNDLE, title);
            outState.putString(FILENAME_BUNDLE, filename);
            outState.putString(EDIT_MARKDOWN_TEXT_BUNDLE, editMarkdownText);
            Log.i("EditorActivity.onSaveInstanceState",
                    String.format("Saving in Bundle outState: " +
                                    "(title = %s, filename = %s, editMarkdownText = %s)",
                            title, filename, editMarkdownText
                    )
            );
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
        {
            View rootView = inflater.inflate(R.layout.fragment_editor, container, false);

            if (null == editMarkdown)
            {
                Log.d("EditorActivity.onCreateView",
                        "Initializing editMarkdown with editMarkdownText = " + editMarkdownText +
                                " ..."
                );
                editMarkdown = (EditText) rootView.findViewById(R.id.edit_content);
                editMarkdown.setTypeface(Typeface.MONOSPACE);
                if (savedInstanceState != null)
                {
                    String loadedEditMarkdownText =
                            savedInstanceState.getString("editMarkdownText");
                    Log.i("EditorActivity.onCreateView",
                            "Loading from editMarkdownText in Bundle savedInstanceState: " +
                                    loadedEditMarkdownText);
                    Log.i("EditorActivity.onCreateView", "loadedEditMarkdownText = " +
                            loadedEditMarkdownText);
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
                Log.d("EditorActivity.onCreateView", "Initializing webPreview...");
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
                            MarkdownDocument.getInstance(filename, editMarkdownText);
                    WebDocument webDocument = WebDocument.getInstance(markdownDocument,
                            CssDocument.getDefaultInstance(getActivity()));
                    webPreview.loadData(webDocument.getContent(), "text/html", null);

                    saveDocument();
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                public void onTextChanged(CharSequence s, int start, int before, int count) {}
            });

            return rootView;
        }

        public void newDocument()
        {
            // TODO Clean up this block of code!
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

            alert.setTitle(R.string.create_title);

            // Set an EditText view to get user input
            final EditText input = new EditText(getActivity());
            input.setSingleLine(true);
            input.setHint(R.string.create_hint);
            alert.setView(input);

            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    if ((filename != null) && (!filename.isEmpty()))
                    {
                        saveDocument();
                    }
                    title = input.getText().toString();
                    filename = title + MD_EXTENSION;
                    Log.i("EditorActivity.onViewStateRestored",
                            String.format("Creating new document (title = %s): %s",
                                    title, filename)
                    );
                    setTitle(title);
                    editMarkdownText = "";
                    editMarkdown.setText(editMarkdownText);
                    // Do something with value!
                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int whichButton)
                {
                    // Cancelled.
                    dialog.cancel();
                }
            });

            alert.show();
        }

        public void showOpenDocumentDialog()
        {
            // TODO Clean up this block of code!
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

            alert.setTitle(R.string.open_title);

            // Set an EditText view to get user input
            final EditText input = new EditText(getActivity());
            input.setSingleLine(true);
            input.setHint(R.string.open_hint);
            alert.setView(input);

            alert.setPositiveButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    openDocument(input.getText().toString());
                }
            });

            alert.setNegativeButton(getString(R.string.button_cancel), new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int whichButton)
                {
                    // Cancelled.
                    dialog.cancel();
                }
            });

            alert.show();
        }

        public void openDocument(String title) {
            if ((filename != null) && (!filename.isEmpty()))
            {
                saveDocument();
            }

            String openTitle = title;
            String openFilename = openTitle + MD_EXTENSION;
            File dirMarkdown = new File(getExternalFilesDir(), getString(R.string.dir_md));
            File inputFile = new File(dirMarkdown, openFilename);
            if (!inputFile.exists())
            {
                Toast.makeText(getActivity(),
                        String.format("Could not find document %s.", openTitle),
                        Toast.LENGTH_SHORT).show();
                Log.e("editMarkdown.showOpenDocumentDialog",
                        String.format("Could not find file %s.", openFilename));
            }
            else
            {
                try
                {
                    Log.i("editMarkdown.showOpenDocumentDialog",
                            String.format("Opening file %s.", openFilename));
                    Scanner scanner = new Scanner(inputFile);
                    String openText = "";
                    while (scanner.hasNextLine())
                    {
                        openText += scanner.nextLine() + MarkdownUtils.LINE_SEPARATOR;
                    }
                    scanner.close();
                     setTitle(openTitle);
                    filename = openFilename;
                    editMarkdownText = openText;
                    editMarkdown.setText(openText);
                    MarkdownDocument markdownDocument =
                            MarkdownDocument.getInstance(filename, editMarkdownText);
                    WebDocument webDocument = WebDocument.getInstance(markdownDocument,
                            CssDocument.getDefaultInstance(getActivity()));
                    webPreview.loadData(webDocument.getContent(), "text/html", null);
                }
                catch (IOException e)
                {
                    Log.e("editMarkdown.showOpenDocumentDialog",
                            String.format("Unable to read file %s:", openFilename), e);
                }
            }
        }

        public void saveDocument()
        {
            String dirName = getString(R.string.dir_md) != null
                    ? getString(R.string.dir_md)
                    : "Scratchpad/markdown";
            File dirMarkdown = new File(getExternalFilesDir(), getString(R.string.dir_md));
            dirMarkdown.mkdirs();
            File outputFile = new File(dirMarkdown, filename);
            if (!outputFile.exists())
            {
                try
                {
                    outputFile.createNewFile();
                }
                catch (IOException e)
                {
                    Log.e("editMarkdown.saveDocument",
                            String.format("Unable to create new file %s:",
                                    filename), e
                    );
                }
            }
            try
            {
                PrintWriter out = new PrintWriter(outputFile);
                out.print(editMarkdownText);
                out.close();
            }
            catch (Exception e)
            {
                Log.e("editMarkdown.saveDocument",
                        String.format("Unable to write to file %s:",
                                filename), e
                );
            }
        }

        @Override
        public void onAttach(Activity activity)
        {
            super.onAttach(activity);
            ((EditorActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }

        private void setTitle(String title)
        {
            this.title = title;
            getActivity().setTitle(title);
            getActivity().getActionBar().setTitle(title);
        }

        private File getExternalFilesDir()
        {
            return Environment.getExternalStorageDirectory();
        }
    }
}