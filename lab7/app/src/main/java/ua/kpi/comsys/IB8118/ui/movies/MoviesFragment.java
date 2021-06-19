package ua.kpi.comsys.IB8118.ui.movies;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ua.kpi.comsys.IB8118.R;
import ua.kpi.comsys.IB8118.sqlite.App;
import ua.kpi.comsys.IB8118.sqlite.MovieDao;
import ua.kpi.comsys.IB8118.sqlite.Poster;
import ua.kpi.comsys.IB8118.sqlite.PosterDao;
import ua.kpi.comsys.IB8118.sqlite.SQLiteDB;

public class MoviesFragment extends Fragment {
    List<ua.kpi.comsys.IB8118.ui.movies.Movie> movies;
    List<ua.kpi.comsys.IB8118.ui.movies.Movie> foundMovies = new ArrayList<>();
    List<ua.kpi.comsys.IB8118.ui.movies.Movie> adapterObjects;
    Adapter customAdapter;
    ListView movieEntityLV;
    int widthScreen;
    int maximumLettersTitle;
    boolean requiredRecreate = false;
    EditText findField;
    String API_KEY = "7e9fe69e",
        REQUEST;
    View rootView;
    MovieDao movieDao;
    PosterDao posterDao;

    public void handleScreenSize() {
        super.onResume();
        Display screen = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        screen.getSize(size);
        widthScreen = size.x;
        maximumLettersTitle = widthScreen /16;
    }

    @Override
    public void onPause() {
        super.onPause();
        handleScreenSize();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (requiredRecreate)
            requireActivity().recreate();
        requiredRecreate = false;
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleScreenSize();
        SQLiteDB database = App.getInstance().getDatabase();
        movieDao = database.filmDao();
        posterDao = database.posterDao();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_movies, container, false);
        Button buttonSearch = rootView.findViewById(R.id.buttonSearch);
        findField = rootView.findViewById(R.id.fieldFind);
        movieEntityLV = rootView.findViewById(R.id.movies_listView);
        movies = MovHandler.getUserMovies(getContext());

        if(movies != null){
            customAdapter = new Adapter(getActivity(), R.layout.movies, movies);
            movieEntityLV.setAdapter(customAdapter);
        }

        buttonSearch.setOnClickListener(new View.OnClickListener() { // search
            @SuppressLint("ShowToast")
            @Override
            public void onClick(View v) {
                if (findField.getText().toString().length()>2)
                    search();
                else
                    Toast.makeText(getContext(), "Request must be more than 2 symbols", Toast.LENGTH_SHORT);
            }
        });

        movieEntityLV.setOnItemClickListener(new AdapterView.OnItemClickListener() { // call detail info about movie
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(getContext(), MovieInfoActivity.class).putExtra("imdbID", adapterObjects.get(position).getImdbID()));
            }
        });
        return rootView;
    }

    private class Adapter extends ArrayAdapter<ua.kpi.comsys.IB8118.ui.movies.Movie> {
        Adapter(Context context, int textViewResourceId, List<ua.kpi.comsys.IB8118.ui.movies.Movie> objects) {
            super(context, textViewResourceId, objects);
            adapterObjects = objects;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            @SuppressLint("ViewHolder") View row = inflater.inflate(R.layout.movies, parent, false);
            TextView    titleTV = row.findViewById(R.id.movieTitle),
                        typeTV = row.findViewById(R.id.movieType),
                        yearTV = row.findViewById(R.id.movieYear);

//            ImageView loading = row.findViewById(R.id.loading);
            RotateAnimation rotate = new RotateAnimation(0, 999999999);
            rotate.setDuration(9999);
//            loading.startAnimation(rotate);

            String title = (adapterObjects.get(position).getTitle());
            if (title.length() > maximumLettersTitle)
                title = title.substring(0, maximumLettersTitle) + "...";

            titleTV.setText(title);
            typeTV.setText(adapterObjects.get(position).getType());
            yearTV.setText("Year: " + adapterObjects.get(position).getYear());

            ImageView posterIV = row.findViewById(R.id.poster_listview);
            String poster = adapterObjects.get(position).getPoster();

            try {
                new Thread(new DownloadPoster(posterIV, getActivity(), poster, getContext())).start();
            } catch (Exception e) {
                posterIV.setImageResource(R.drawable.unknown_file);
//                loading.setVisibility(View.INVISIBLE);
            }
            return row;
        }
    }

    void search(){
        foundMovies.clear();
        REQUEST = findField.getText().toString().toLowerCase();
        new LoadRequest("search").start();
    }

    class LoadRequest extends Thread {
        LoadRequest(String name){
            super(name);
        }

        public void run(){
            List<ua.kpi.comsys.IB8118.sqlite.Movie> entityByRequest = movieDao.getByRequest(REQUEST);

            try {
                URL url = new URL("http://www.omdbapi.com/?apikey="+API_KEY+"&s="+REQUEST+"&page=1");
                String found = (new BufferedReader(new InputStreamReader(url.openStream()))).readLine();
                movies = MovHandler.movsFromJson(found);

                if (movies.size()==0) // success request, movies not found
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), "No movies", Toast.LENGTH_SHORT).show();
                            rootView.findViewById(R.id.movies_listView).setVisibility(View.INVISIBLE);
                        }
                    });
                else {// success request, movies found
                    new pushMoviesToSqlite("saver").start();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            rootView.findViewById(R.id.movies_listView).setVisibility(View.VISIBLE);
                        }
                    });
                }

                MovHandler.exportToCached(getContext(), movies);
                try {
                    if (!(movies.size()==0))
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                customAdapter = new Adapter(getActivity(), R.layout.movies, movies);
                                movieEntityLV.setAdapter(customAdapter);
                            }
                        });
                } catch (Exception ignored){}
            } catch (Exception e) { // no connection
                e.printStackTrace();
                if (entityByRequest.size()>0){ // data has in DB
                    movies = ua.kpi.comsys.IB8118.ui.movies.Movie.movieEntitiesToListMovies(entityByRequest);
                    MovHandler.exportToCached(getContext(), movies);
                    try {
                        if (movies == null)
                            movies = new ArrayList<>();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                customAdapter = new Adapter(getActivity(), R.layout.movies, movies);
                                movieEntityLV.setAdapter(customAdapter);
                            }
                        });

                    } catch (Exception exc) {
                        exc.printStackTrace();
                    }
                } else { // data hasn't in DB
                    movies = new ArrayList<>();
                    customAdapter = new Adapter(getContext(), R.layout.movies, movies);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), "No record in database", Toast.LENGTH_SHORT).show();
                            movieEntityLV.setAdapter(customAdapter);
                        }
                    });
                }
//                getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(getContext(), "Check internet connection", Toast.LENGTH_SHORT).show();
//                    }
//                });
            }
        }
    }

    public class DownloadPoster implements Runnable {
        String link;
        ImageView posterIV;
        Context context;
        Activity appAct;

        public DownloadPoster(ImageView posterIV, Activity appAct, String link, Context context) {
            this.link = link;
            this.posterIV = posterIV;
            this.context = context;
            this.appAct = appAct;
        }

        public void run() {
            Poster workingImg = new Poster();
            String fileStr;

            if (link.startsWith("http")) { // if link is correct
                List<Poster> posters = posterDao.getByUrl(link);
                String cacheDir = context.getCacheDir().toString();

                boolean hasImage = false;
                if (posters.size() != 0) {
                    String imageCachePath = cacheDir + "/" + posters.get(0).getFileName();
                    hasImage = new File(imageCachePath).exists();
                }

                if (posters.size() == 0 | !hasImage) {
                    if (!hasImage & posters.size()>0)
                        fileStr = posters.get(0).getFileName();
                    else {
                        fileStr = "poster_"+hashCode()+".png";
                    }

                    URL link;
                    try {
                        link = new URL(this.link);
                        try (InputStream is = link.openStream()) {
                            try (OutputStream os = new FileOutputStream(cacheDir + "/" + fileStr)) {
                                byte[] buffer = new byte[2048];
                                int bytesRead;
                                while ((bytesRead = is.read(buffer, 0, buffer.length)) >= 0) {
                                    os.write(buffer, 0, bytesRead);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    workingImg.link = this.link;
                    workingImg.fileName = fileStr;
                    posterDao.insert(workingImg);
                }

                try {
                    String imageName = posterDao.getByUrl(link).get(0).getFileName();
                    File f = new File(context.getCacheDir() + "/" + imageName);
                    InputStream is = new FileInputStream(f);
                    Bitmap bitmap = BitmapFactory.decodeStream(is);

                    appAct.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            posterIV.setImageBitmap(bitmap);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (posterIV != null) {
                appAct.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        posterIV.setImageResource(R.drawable.unknown_file);
                    }
                });
            }
        }
    }

    class pushMoviesToSqlite extends Thread {
        pushMoviesToSqlite(String name){
            super(name);
        }

        public void run(){
            ua.kpi.comsys.IB8118.sqlite.Movie filmEntity = new ua.kpi.comsys.IB8118.sqlite.Movie();
            try {
                if (movieDao.getByRequest(REQUEST).size() == 0) {
                    if (movies != null)
                        for (ua.kpi.comsys.IB8118.ui.movies.Movie movie : movies) {
                            filmEntity.setTitle(movie.getTitle());
                            filmEntity.setImdbID(movie.getImdbID());
                            filmEntity.setType(movie.getType());
                            filmEntity.setPoster(movie.getPoster());
                            filmEntity.setYear(movie.getYear());
                            filmEntity.SearchRequest = REQUEST;
                            movieDao.insert(filmEntity);
                        }
                }
            } catch (Exception ignored){}
        }
    }
}