package ua.kpi.comsys.IB8118.sqlite;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "movie")
public class Movie {
    public String Title, Year, Type, imdbID, Poster, Rated, Runtime, Genre, imdbRating, imdbVotes, Released, Production,
            Language, Country, Awards, Director, Writer, Actors, Plot;
    public String SearchRequest;

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        this.Title = title;
    }

    public String getYear() {
        return Year;
    }

    public void setYear(String year) {
        this.Year = year;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        this.Type = type;
    }

    public String getImdbID() {
        return imdbID;
    }

    public void setImdbID(String imdbID) {
        this.imdbID = imdbID;
    }

    public String getPoster() {
        return Poster;
    }

    public void setPoster(String poster) {
        this.Poster = poster;
    }

    public String getRated() {
        return Rated;
    }
}
