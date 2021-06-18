package ua.kpi.comsys.IB8118.ui.movies;

import android.content.Context;
import android.content.res.Resources;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import ua.kpi.comsys.IB8118.R;

public class MovHandler {
    public static List<Movie> getListMovFromRaw(Context context) {
        try{
            Gson gson = new Gson();
            MoviesJsonSearch moviesJsonSearch = gson.fromJson(strFromRaw(context), MoviesJsonSearch.class);
            return moviesJsonSearch.getSearch();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    private static String strFromRaw(Context context) {
        Resources res = context.getResources();
        InputStream is = res.openRawResource(R.raw.movies_list);
        String str = null;
        try {
            str = isToStr(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }

    static String isToStr(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int i = is.read();
        while( i != -1){
            baos.write(i);
            i = is.read();
        }
        return  baos.toString();
    }

    private static class MoviesJsonSearch {
        private List<Movie> Search;
        List<Movie> getSearch() {
            return Search;
        }
    }
}
