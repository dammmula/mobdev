package ua.kpi.comsys.IB8118.sqlite;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PosterDao {
    @Query("SELECT * FROM poster")
    List<Poster> getAll();

    @Query("SELECT * FROM poster WHERE link = :url")
    List<Poster> getByUrl(String url);

    @Insert
    void insert(Poster poster);

    @Delete
    void delete(Poster poster);
}