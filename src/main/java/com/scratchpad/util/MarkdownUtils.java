package com.scratchpad.util;

import android.util.Log;

/**
 * Created by silare on 5/4/14.
 */
public class MarkdownUtils
{
    public static final String LINE_SEPARATOR = "\n";
    public static final String DEFAULT_TITLE = "Scratch";

    public static String getTitleFromMarkdown(String markdown)
    {
        // Get the first line of the given Markdown.
        int indexOfLineSeparator = markdown.indexOf(LINE_SEPARATOR);
        String firstLine = markdown;
        if (indexOfLineSeparator > 0)
        {
            firstLine = markdown.substring(0, indexOfLineSeparator);
        }

        // Remove any preceding whitespace or '#' symbols (that indicate a header).
        String title = firstLine;
        if (!title.isEmpty())
        {
            // While there are at least two characters and the first one is whitespace or '#',
            // remove the first character from the title.
            while (title.length() >= 2 && (title.charAt(0) == '#' || title.charAt(0) == ' '))
            {
                title = title.substring(1);
            }
        }

        // If the extracted title ends up being empty, attempt to use the first line, or if that is
        // empty too, use the default title.
        if (title.isEmpty())
        {
            title = !firstLine.isEmpty() ? firstLine : DEFAULT_TITLE;
        }

        Log.d("MarkdownUtils.getTitleFromMarkdown", "title = " + title);
        return firstLine;
    }
}
