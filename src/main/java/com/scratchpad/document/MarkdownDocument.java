package com.scratchpad.document;

public class MarkdownDocument implements Document
{
    private static MarkdownDocument nullInstance;
    String title;
    String markdown;

    public static MarkdownDocument getInstance(String title, String markdown)
    {
        MarkdownDocument markdownDocument = new MarkdownDocument(title, markdown);
        return markdownDocument;
    }

    public static MarkdownDocument getDefaultInstance()
    {
        if (null == nullInstance)
        {
            nullInstance = new MarkdownDocument("", "");
        }
        return nullInstance;
    }

    @Override
    public String getTitle()
    {
        return title;
    }

    @Override
    public String getContent()
    {
        return markdown;
    }

    MarkdownDocument(String title, String markdown)
    {
        this.title = title;
        this.markdown = markdown;
    }
}
