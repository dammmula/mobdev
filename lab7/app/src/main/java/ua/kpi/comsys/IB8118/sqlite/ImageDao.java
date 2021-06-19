package ua.kpi.comsys.IB8118.sqlite;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ImageDao {
    @Query("SELECT * FROM image WHERE id <= 27")
    List<Image> getAll();

    @Query("SELECT * FROM image WHERE url = :url")
    List<Image> getByUrl(String url);

    @Query("DELETE FROM image WHERE id > 27")
    void clearTrash();

    @Insert
    void insert(Image image);

    @Delete
    void delete(Image image);
}