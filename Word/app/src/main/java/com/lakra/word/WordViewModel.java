package com.lakra.word;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.lakra.word.persistence.Word;
import com.lakra.word.persistence.WordRepository;

import java.util.List;

/**
 *
 */
public class WordViewModel extends AndroidViewModel {

    private WordRepository mWordRepository;
    private LiveData<List<Word>> mAllWords;

    /**
     * @param application
     */
    public WordViewModel(@NonNull Application application) {
        super(application);
        mWordRepository = new WordRepository(application);
        mAllWords = mWordRepository.getAllWords();
    }

    /**
     * @return
     */
    public LiveData<List<Word>> getAllWords() {
        return mAllWords;
    }

    /**
     * @param word
     */
    public void insert(Word word) {
        mWordRepository.insert(word);
    }
}
