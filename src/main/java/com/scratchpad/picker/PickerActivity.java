package com.scratchpad.picker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Toast;

import com.scratchpad.R;
import com.scratchpad.document.CssDocument;
import com.scratchpad.document.Document;
import com.scratchpad.document.MarkdownDocument;
import com.scratchpad.document.WebDocument;
import com.scratchpad.editor.EditorActivity;
import com.scratchpad.util.Constants;
import com.scratchpad.util.MarkdownUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class PickerActivity extends Activity {

    private RecyclerView recyclerView;

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
    public static final int MAX_CONTENT_LENGTH_PORTRAIT = 135;

    /**
     * Navigation drawer character width for landscape orientation.
     */
    public static final int MAX_CONTENT_LENGTH_LANDSCAPE = 135;

    /**
     * Navigation drawer character width for landscape orientation.
     */
    public static final int MAX_NEWLINE_COUNT = 8;

    private String title;
    private WebView webPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picker);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_documents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (null == webPreview)
        {
            Log.d("EditorActivity.onCreateView", "Initializing webPreview...");
            webPreview = (WebView) findViewById(R.id.web_pickerpreview);
            webPreview.setBackgroundColor(0x00000000);
            webPreview.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        }

        reloadRecyclerView();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        reloadRecyclerView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.picker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Intent intent = new Intent(this, EditorActivity.class);
        switch (item.getItemId())
        {
            case R.id.action_new:
                startActivity(intent);
                return true;
            case R.id.action_edit:
                intent.putExtra("title", title);
                startActivity(intent);
                return true;
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void reloadRecyclerView() {
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
                int newLineCount = 0;
                while (scanner.hasNext() && !shouldStopScanning)
                {
                    String nextLine = scanner.nextLine();
                    if ((markdown.length() + nextLine.length()) > maxContentLength)
                    {
                        shouldStopScanning = true;
                        int truncateLength = maxContentLength - markdown.length();
                        markdown += nextLine.substring(0, truncateLength) + "...";
                    }
                    else
                    {
                        markdown += nextLine;
                        if (newLineCount >= MAX_NEWLINE_COUNT)
                        {
                            markdown += "...";
                            shouldStopScanning = true;
                        }
                        else
                        {
                            markdown += "\n";
                            newLineCount++;
                        }
                    }
                }
            }
            catch (FileNotFoundException e)
            {
                Log.e("PickerActivity.reloadRecyclerView",
                        String.format("File %s not found.", title),
                        e);
            }

            Document document = MarkdownDocument.getInstance(title, markdown);
            documents.add(document);
            documentMap.put(TITLE, title);
            documentMap.put(MARKDOWN, markdown);
            documentMaps.add(documentMap);
        }

        DocumentAdapter documentAdapter = new DocumentAdapter(this, recyclerView, documents);
        recyclerView.setAdapter(documentAdapter);
        recyclerView.invalidate();
    }

    public void displayPreview(String title)
    {
        String openTitle = title;
        String openFilename = openTitle + Constants.MD_EXTENSION;
        File dirMarkdown = new File(getExternalFilesDir(), getString(R.string.dir_md));
        File inputFile = new File(dirMarkdown, openFilename);
        if (!inputFile.exists())
        {
            Toast.makeText(this,
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
                MarkdownDocument markdownDocument =
                        MarkdownDocument.getInstance(openFilename, openText);
                WebDocument webDocument = WebDocument.getInstance(markdownDocument,
                        CssDocument.getDefaultInstance(this));
                webPreview.loadData(webDocument.getContent(), "text/html", null);
                this.title = title;
            }
            catch (IOException e)
            {
                Log.e("editMarkdown.showOpenDocumentDialog",
                        String.format("Unable to read file %s:", openFilename), e);
            }
        }
    }

    /**
     * Returns the maximum content length allowed for the navigation drawer
     * based on the current display orientation.
     *
     * @return maximum content length
     */
    private int calculateMaxContentLength() {
        int maxContentLength;Display getOrient = getWindowManager().getDefaultDisplay();
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

    private File getExternalFilesDir()
    {
        return Environment.getExternalStorageDirectory();
    }
}
