package ua.kpi.comsys.IB8118.ui.movies;

import android.content.Context;
import android.content.res.Resources;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import ua.kpi.comsys.IB8118.R;

public class MovHandler {
    public static String cachedMoviesFileName = "moviesCache";
    public static boolean hasCached = false;
    public static File cachedFile;
    private static int resMovList = R.raw.movies_list;

    public static void checkHasCached(Context context){
        cachedFile = new File(context.getFilesDir() + "/" + cachedMoviesFileName);

        if(cachedFile.exists()){
            hasCached = true;
        }
    }

    public static List<Movie> getUserMovies(Context context){
        try {
            Gson gson = new Gson();
            cachedFile = new File(context.getFilesDir() + "/" + cachedMoviesFileName);

            if(cachedFile.exists()){
                hasCached = true;
            } else {
                try (FileWriter writer = new FileWriter(cachedFile)) {
                    writer.write(strFromRaw(context));
                    writer.flush();
                    hasCached = true;

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            MoviesJsonSearch mjs = gson.fromJson(strFromRawFile(context), MoviesJsonSearch.class);

            return mjs.getSearch();
        }
        catch (Exception e){
            return null;
        }
    }

    public static boolean exportToCached(Context context, List<Movie> dataList) {
        Gson gson = new Gson();
        MoviesJsonSearch mjs = new MoviesJsonSearch();
        mjs.setSearch(dataList);
        String jsonString = gson.toJson(mjs);

        FileOutputStream fos = null;

        try {
            fos = context.openFileOutput(cachedMoviesFileName, Context.MODE_PRIVATE);
            fos.write(jsonString.getBytes());
            hasCached = true;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public static Movie movFromRes(Context context, int res) {
        try{
            Gson gson = new Gson();
            return gson.fromJson(strFromRawFileInfo(context, res), Movie.class);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    public static List<Movie> getListMovFromRaw(Context context) {
        try{
            Gson gson = new Gson();
            MoviesJsonSearch moviesJsonSearch = gson.fromJson(strFromRawFile(context), MoviesJsonSearch.class);
            return moviesJsonSearch.getSearch();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    private static String strFromRaw(Context context) {
        Resources res = context.getResources();
        InputStream is = res.openRawResource(resMovList);
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

    public static String strFromRawFile(Context context) {
        InputStream is = null;
        checkHasCached(context);
        if(!hasCached) {
            Resources r = context.getResources();
            is = r.openRawResource(resMovList);
            System.out.println(">>> !hasCached");
        }
        else {
            try {
                is = new FileInputStream(cachedFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

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

        System.out.println("STR: "+str);
        return  str;
    }

    public static String strFromRawFileInfo(Context context, int res) {
        Resources r = context.getResources();
        InputStream is = r.openRawResource(res);
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
        return  str;
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
        void setSearch(List<Movie> search) {
            this.Search = search;
        }
    }
}
