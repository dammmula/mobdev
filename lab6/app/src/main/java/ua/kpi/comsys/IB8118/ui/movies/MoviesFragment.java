package ua.kpi.comsys.IB8118.ui.movies;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
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
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ua.kpi.comsys.IB8118.R;

public class MoviesFragment extends Fragment {
    List<Movie> movies;
    final List<Movie> foundMovies = new ArrayList<>();
    List<Movie> adapterObjects;
    Adapter customAdapter;
    ListView filmEntityLV;
    int widthScreen;
    int maximumLettersTitle;
    boolean requiredRecreate = false;
    EditText findField;
    String API_KEY = "7e9fe69e",
        REQUEST;
    View rootView;

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
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_movies, container, false);
        Button buttonSearch = rootView.findViewById(R.id.buttonSearch);
        findField = rootView.findViewById(R.id.fieldFind);
        filmEntityLV = rootView.findViewById(R.id.movies_listView);
        movies = MovHandler.getUserMovies(getContext());

        if(movies != null){
            customAdapter = new Adapter(getActivity(), R.layout.movies, movies);
            filmEntityLV.setAdapter(customAdapter);
        }

        buttonSearch.setOnClickListener(new View.OnClickListener() { // search
            @Override
            public void onClick(View v) {
                if (findField.getText().toString().length()>2)
                    search();
                else
                    Toast.makeText(getContext(), "Request must be more than 2 symbols", Toast.LENGTH_SHORT);
            }
        });

        filmEntityLV.setOnItemClickListener(new AdapterView.OnItemClickListener() { // call detail info about movie
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(getContext(), MovieInfoActivity.class).putExtra("imdbID", adapterObjects.get(position).getImdbID()));
            }
        });
        return rootView;
    }

    private class Adapter extends ArrayAdapter<Movie> {
        Adapter(Context context, int textViewResourceId, List<Movie> objects) {
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
                new DownloadPoster(posterIV).execute(poster);
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
            try {
                URL url = new URL("http://www.omdbapi.com/?apikey="+API_KEY+"&s="+REQUEST+"&page=1");
                String found = (new BufferedReader(new InputStreamReader(url.openStream()))).readLine();
                movies = MovHandler.movsFromJson(found);
                if (movies.size()==0)
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), "No movies", Toast.LENGTH_SHORT).show();
                            rootView.findViewById(R.id.movies_listView).setVisibility(View.INVISIBLE);
                        }
                    });
                else
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            rootView.findViewById(R.id.movies_listView).setVisibility(View.VISIBLE);
                        }
                    });

                MovHandler.exportToCached(getContext(), movies);
                try {
                    if (!(movies.size()==0))
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                customAdapter = new Adapter(getActivity(), R.layout.movies, movies);
                                filmEntityLV.setAdapter(customAdapter);
                            }
                        });
                } catch (Exception ignored){}
            } catch (Exception e) {
                e.printStackTrace();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "Check internet connection", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private class DownloadPoster extends AsyncTask<String, Void, Bitmap> {
        ImageView bitmap;

        public DownloadPoster(ImageView bitmap) {
            this.bitmap = bitmap;
        }

        protected Bitmap doInBackground(String... links) {
            String link = links[0];
            Bitmap bitmap = null;
            try {
                bitmap = BitmapFactory.decodeStream(new java.net.URL(link).openStream());
            } catch (Exception ignored) {}
            return bitmap;
        }
        protected void onPostExecute(Bitmap bitmap) {
            this.bitmap.setImageBitmap(bitmap);
        }
    }
}