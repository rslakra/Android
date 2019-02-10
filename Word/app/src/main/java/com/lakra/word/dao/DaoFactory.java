//package com.lakra.word.dao;
//
//import android.arch.persistence.room.Room;
//import android.content.Context;
//
//public final class DaoFactory {
//
//    private static DaoFactory mInstance;
//
//    private WordRoomDatabase mWordDatabase;
//
//    private DaoFactory(Context context) {
//        mWordDatabase = Room.databaseBuilder(context.getApplicationContext(), WordRoomDatabase.class, "Words").allowMainThreadQueries().build();
//    }
//
//    /**
//     * @param context
//     * @return
//     */
//    public static DaoFactory getInstance(final Context context) {
//        if (mInstance == null) {
//            synchronized (DaoFactory.class) {
//                if (mInstance == null) {
//                    mInstance = new DaoFactory(context);
//                }
//            }
//        }
//
//        return mInstance;
//    }
//
//    /**
//     * @return
//     */
//    public WordRoomDatabase getDatabase() {
//        return mWordDatabase;
//    }
//}
