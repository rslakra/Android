package com.lakra.word.persistence;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.lakra.word.dao.WordDao;
import com.lakra.word.dao.WordRoomDatabase;

import java.util.List;

public class WordRepository {

    private WordDao mWordDao;
    private LiveData<List<Word>> mAllWords;

    /**
     * @param application
     */
    public WordRepository(Application application) {
        WordRoomDatabase database = WordRoomDatabase.getDatabase(application);
        mWordDao = database.getWordDao();
        mAllWords = mWordDao.getWords();
    }

    /**
     * @return
     */
    public LiveData<List<Word>> getAllWords() {
        return mAllWords;
    }


    /**
     *
     */
    private static final class InsertAsyncTask extends AsyncTask<Word, Void, Void> {

        private WordDao mAsyncTaskDao;

        InsertAsyncTask(WordDao dao) {
            mAsyncTaskDao = dao;
        }

        /**
         * @param params
         * @return
         */
        @Override
        protected Void doInBackground(final Word... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    /**
     * @param word
     */
    public void insert(Word word) {
        new InsertAsyncTask(mWordDao).execute(word);
    }

}
