package ua.kpi.comsys.IB8118.sqlite;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "image")
public class Image {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String url, fileName;

    public int getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getFileName() {
        return fileName;
    }
}