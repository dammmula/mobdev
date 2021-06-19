package ua.kpi.comsys.IB8118.sqlite;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MovieDao {

    @Query("SELECT * FROM movie")
    List<Movie> getAll();

    @Query("SELECT * FROM movie WHERE SearchRequest = :searchRequest")
    List<Movie> getByRequest(String searchRequest);

    @Insert
    void insert(Movie film);

    @Delete
    void delete(Movie film);
}

