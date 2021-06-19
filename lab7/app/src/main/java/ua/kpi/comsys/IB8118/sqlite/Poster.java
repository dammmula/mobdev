package ua.kpi.comsys.IB8118.sqlite;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "poster")
public class Poster {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String link, fileName;

    public int getId() {
        return id;
    }

    public String getLink() {
        return link;
    }

    public String getFileName() {
        return fileName;
    }
}