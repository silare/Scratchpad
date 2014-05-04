package com.scratchpad.document;

public class MarkdownDocument implements Document
{
    private static MarkdownDocument nullInstance;
    String markdown;

    public static MarkdownDocument getInstance(String markdown)
    {
        MarkdownDocument markdownDocument = new MarkdownDocument(markdown);
        return markdownDocument;
    }

    public static MarkdownDocument getDefaultInstance()
    {
        if (null == nullInstance)
        {
            nullInstance = new MarkdownDocument("");
        }
        return nullInstance;
    }

    @Override
    public String getContent()
    {
        return markdown;
    }

    MarkdownDocument(String markdown)
    {
        this.markdown = markdown;
    }
}
