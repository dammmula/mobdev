package ua.kpi.comsys.IB8118.sqlite;

import android.app.Application;

import androidx.room.Room;

public class App extends Application {
    public static App instance;

    private SQLiteDB database;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        database = Room.databaseBuilder(this, SQLiteDB.class, "database").build();
    }

    public static App getInstance() {
        return instance;
    }

    public SQLiteDB getDatabase() {
        return database;
    }
}
