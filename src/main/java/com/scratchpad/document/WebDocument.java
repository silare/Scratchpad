package com.scratchpad.document;

import org.markdownj.MarkdownProcessor;

public class WebDocument implements Document
{
    private static final String HTML_FORMAT =
            "<html>\n" +
                    "<head>\n" +
                    "<style>\n" +
                    "%s\n" +
                    "</style>\n" +
                    "<body>\n" +
                    "%s\n" +
                    "</body>\n" +
                    "</html>";

    private static WebDocument nullInstance;
    MarkdownDocument markdownDocument;
    CssDocument cssDocument;

    public static WebDocument getInstance(MarkdownDocument markdownDocument,
                                          CssDocument cssDocument)
    {
        WebDocument webDocument = new WebDocument(markdownDocument, cssDocument);
        return webDocument;
    }

    public static WebDocument getDefaultInstance()
    {
        if (null == nullInstance)
        {
            nullInstance = new WebDocument(MarkdownDocument.getDefaultInstance(),
                    CssDocument.getDefaultInstance());
        }
        return nullInstance;
    }

    @Override
    public String getContent()
    {
        String css = cssDocument.getContent();
        String markdown = markdownDocument.getContent();
        String body = new MarkdownProcessor().markdown(markdown);
        return String.format(HTML_FORMAT, css, body);
    }

    WebDocument(MarkdownDocument markdownDocument, CssDocument cssDocument)
    {
        this.markdownDocument = markdownDocument;
        this.cssDocument = cssDocument;
    }
}