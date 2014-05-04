package com.scratchpad.document;

public class CssDocument implements Document
{
    private static final String DEFAULT_CSS =
            "body\n" +
            "{\n" +
            "    color: #FFFFFF;\n" +
            "}\n";
    private static CssDocument nullInstance;
    String css;

    public static CssDocument getInstance(String css)
    {
        CssDocument cssDocument = new CssDocument(css);
        return cssDocument;
    }

    public static CssDocument getDefaultInstance()
    {
        if (null == nullInstance)
        {
            nullInstance = new CssDocument(DEFAULT_CSS);
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
