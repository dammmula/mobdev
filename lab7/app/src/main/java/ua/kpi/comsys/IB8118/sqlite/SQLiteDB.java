package ua.kpi.comsys.IB8118.sqlite;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Movie.class, Poster.class, Image.class}, version = 2, exportSchema = false)
public abstract class SQLiteDB extends RoomDatabase {
    public abstract MovieDao filmDao();
    public abstract PosterDao posterDao();
    public abstract ImageDao imageDao();
}
