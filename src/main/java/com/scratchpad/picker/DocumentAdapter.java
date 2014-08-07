package com.scratchpad.picker;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.scratchpad.R;
import com.scratchpad.document.Document;

import java.util.List;

/**
 * Created by silare on 7/6/14.
 */
public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.ViewHolder>
{
    private PickerActivity pickerActivity;
    private RecyclerView recyclerView;
    private List<Document> documents;

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView titleText;
        public TextView contentText;

        public ViewHolder(View view)
        {
            super(view);
            this.titleText = (TextView) view.findViewById(R.id.text_title);
            this.contentText = (TextView) view.findViewById(R.id.text_content);
        }
    }

    public DocumentAdapter(PickerActivity pickerActivity, RecyclerView recyclerView,
                           List<Document> documents)
    {
        this.pickerActivity = pickerActivity;
        this.recyclerView = recyclerView;
        this.documents = documents;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_document_card, null);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = recyclerView.getChildPosition(view);
                String title = documents.get(position).getTitle();
                pickerActivity.displayPreview(title);
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i)
    {
        viewHolder.titleText.setText(documents.get(i).getTitle());
        viewHolder.contentText.setText(documents.get(i).getContent());
    }

    @Override
    public int getItemCount()
    {
        return documents.size();
    }

    public List<Document> getDocuments()
    {
        return documents;
    }

    public Document getDocument(int position)
    {
        return documents.get(position);
    }
}
