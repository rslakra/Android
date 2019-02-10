package com.lakra.word.persistence;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "Words")
public class Word {
//    @PrimaryKey(autoGenerate = true)
//    private int id;

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "word")
    private String mWord;

    /**
     * @param word
     */
    public Word(String word) {
        this.mWord = word;
    }

//    /**
//     * @return
//     */
//    public int getId() {
//        return id;
//    }
//
//    /**
//     * @param id
//     */
//    public void setId(int id) {
//        this.id = id;
//    }

    /**
     * @return
     */
    public String getWord() {
        return mWord;
    }

}