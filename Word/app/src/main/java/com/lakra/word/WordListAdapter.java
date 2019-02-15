package com.lakra.word;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.lakra.word.R;
import com.lakra.word.persistence.Word;

import java.util.List;

/**
 *
 */
public final class WordListAdapter extends RecyclerView.Adapter<WordListAdapter.WordViewHolder> {

    /**
     * RecyclerViewHolder
     */
    final class WordViewHolder extends RecyclerView.ViewHolder {
        private final TextView mWordTextView;

        /**
         * @param itemView
         */
        public WordViewHolder(@NonNull View itemView) {
            super(itemView);
            mWordTextView = itemView.findViewById(R.id.textView);
        }
    }


    private LayoutInflater mLayoutInflater;
    private List<Word> mWords;

    /**
     * @param context
     */
    WordListAdapter(Context context) {
        mLayoutInflater = LayoutInflater.from(context);
    }

    /**
     * @param words
     */
    public void setWords(List<Word> words) {
        mWords = words;
        notifyDataSetChanged();
    }

    /**
     * @param parent
     * @param viewType
     * @return
     */
    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View recycleView = mLayoutInflater.inflate(R.layout.recyclerview_item, parent, false);
        return new WordViewHolder(recycleView);
    }

    /**
     * @param wordViewHolder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull WordViewHolder wordViewHolder, int position) {
        if (mWords == null) {
            wordViewHolder.mWordTextView.setText("No Words!");
        } else {
            Word word = mWords.get(position);
            wordViewHolder.mWordTextView.setText(word.getWord());
        }
    }


    /**
     * getItemCount() is called many times, and when it is first called, mWords has not been
     * updated (means initially, it's null, and we can't return null).
     *
     * @return
     */
    @Override
    public int getItemCount() {
        return (mWords == null ? 0 : mWords.size());
    }

}
