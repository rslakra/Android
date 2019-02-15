package com.lakra.word.persistence;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.lakra.word.ActionType;
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
    private static final class ContentAsyncTask extends AsyncTask<Word, Void, Void> {

        private ActionType mActionType;
        private WordDao mAsyncTaskDao;

        /**
         * @param wordDao
         * @param actionType
         */
        ContentAsyncTask(WordDao wordDao, ActionType actionType) {
            mActionType = actionType;
            mAsyncTaskDao = wordDao;
        }

        /**
         * @param params
         * @return
         */
        @Override
        protected Void doInBackground(final Word... params) {
            switch (mActionType) {
                case ADD:
                    mAsyncTaskDao.insert(params[0]);
                    break;
                case UPDATE:
                    mAsyncTaskDao.update(params[0]);
                    break;
                case DELETE:
                    mAsyncTaskDao.delete(params[0]);
                    break;
                default:
                    break;
            }
            return null;
        }
    }

    /**
     * @param word
     */
    public void handleAction(ActionType actionType, Word word) {
        new ContentAsyncTask(mWordDao, actionType).execute(word);
    }
}
