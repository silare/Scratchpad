package com.scratchpad.document;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

public class CssDocument implements Document
{
    private static final String DEFAULT_CSS_ASSET = "default_css.css";
    private static CssDocument nullInstance;
    String css;

    public static CssDocument getInstance(String css)
    {
        CssDocument cssDocument = new CssDocument(css);

        // TODO Add validation here.

        return cssDocument;
    }

    public static CssDocument getDefaultInstance(Context context)
    {
        if (null == nullInstance)
        {
            AssetManager assetManager = context.getAssets();
            String defaultCss = "";
            try
            {
                InputStream stream = assetManager.open(DEFAULT_CSS_ASSET);
                int size = stream.available();
                byte[] buffer = new byte[size];
                stream.read(buffer);
                stream.close();
                defaultCss = new String(buffer);
            }
            catch (IOException e)
            {
                Log.e("CssDocument.getDefaultInstance", "Could not open asset " +
                        DEFAULT_CSS_ASSET + ".", e);
            }
            nullInstance = new CssDocument(defaultCss);
        }
        return nullInstance;
    }

    @Override
    public String getContent()
    {
        return css;
    }

    CssDocument(String css)
    {
        this.css = css;
    }
}
