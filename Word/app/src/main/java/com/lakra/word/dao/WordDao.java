package com.lakra.word.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.lakra.word.persistence.Word;

import java.util.List;

@Dao
public interface WordDao {

    /**
     * @return
     */
    @Query("select * from Words order by word ASC")
    public LiveData<List<Word>> getWords();

    /**
     * @param words
     */
    @Insert
    public void insert(Word... words);

    /**
     * @param words
     */
    @Update
    public void update(Word... words);

    /**
     * @param words
     */
    @Delete
    public void delete(Word... words);

    @Query("DELETE FROM Words")
    public abstract void deleteAll();

}
