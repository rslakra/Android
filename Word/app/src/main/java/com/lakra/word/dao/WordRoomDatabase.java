package com.lakra.word.dao;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.lakra.word.persistence.Word;

@Database(entities = {Word.class}, version = 1)
public abstract class WordRoomDatabase extends RoomDatabase {
    //Singleton Instance
    private static volatile WordRoomDatabase INSTANCE;

    /**
     * To delete all content and repopulate the database whenever the app is started, you create a
     * RoomDatabase.Callback and override onOpen(). Because you cannot do Room database operations
     * on the UI thread, onOpen() creates and executes an AsyncTask to add content to the database.
     */
    private final static RoomDatabase.Callback sRoomDBCallback = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            new PopulateDbAsync(INSTANCE).execute();
        }
    };

    /**
     *
     */
    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final WordDao mDao;

        PopulateDbAsync(WordRoomDatabase db) {
            mDao = db.getWordDao();
        }

        /**
         * @param params
         * @return
         */
        @Override
        protected Void doInBackground(final Void... params) {
            mDao.deleteAll();
            Word word = new Word("Hello");
            mDao.insert(word);
            word = new Word("World");
            mDao.insert(word);
            return null;
        }
    }

    /**
     * Returns the room database instance.
     *
     * @return
     */
    public static WordRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (WordRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), WordRoomDatabase.class, "Words")
                            .addCallback(sRoomDBCallback)
                            .build();
                }
            }
        }

        return INSTANCE;
    }


    /**
     * @return
     */
    public abstract WordDao getWordDao();

    /**
     *
     */
    public abstract void deleteAll();
}
