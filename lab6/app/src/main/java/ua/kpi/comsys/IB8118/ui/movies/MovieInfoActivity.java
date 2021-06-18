package ua.kpi.comsys.IB8118.ui.movies;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import ua.kpi.comsys.IB8118.R;

public class MovieInfoActivity extends AppCompatActivity {
    Movie movie;
    String API_KEY = "7e9fe69e";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_full_info);
        ActionBar backArrow = getSupportActionBar();
        backArrow.setDisplayHomeAsUpEnabled(true);

        try {
            new LoadRequest("NAME").start();
        } catch (Exception ignored){}

//            ((TextView)findViewById(R.id.fullTitle)).setText(title);
//            ((TextView)findViewById(R.id.fullProduction)).setText(movie.getProduction());
//            ((TextView)findViewById(R.id.fullType)).setText(movie.getType());
//            ((TextView)findViewById(R.id.fullRated)).setText("Rated: "+movie.getRated());
//            ((TextView)findViewById(R.id.fullImdbRating)).setText("IMDB: "+movie.getImdbRating());
//            ((TextView)findViewById(R.id.fullImdbVotes)).setText("("+movie.getImdbVotes()+")");
//            ((TextView)findViewById(R.id.fullReleased)).setText("Released: "+movie.getReleased());
//            ((TextView)findViewById(R.id.fullRuntime)).setText("Runtime: "+movie.getRuntime());
//            ((TextView)findViewById(R.id.fullGenre)).setText("Genre: "+movie.getGenre());
//            ((TextView)findViewById(R.id.fullDirector)).setText("Director: "+movie.getDirector());
//            ((TextView)findViewById(R.id.fullWriter)).setText("Writer: "+movie.getWriter());
//            ((TextView)findViewById(R.id.fullActors)).setText("Actors: "+movie.getActors());
//            ((TextView)findViewById(R.id.fullLanguage)).setText("Language: "+movie.getLanguage());
//            ((TextView)findViewById(R.id.fullPlot)).setText(movie.getPlot());

    }

    class LoadRequest extends Thread {
        LoadRequest(String name){
            super(name);
        }

        public void run(){
            BufferedReader bufferedReader;
            URL link;
            try {
                Bundle extras = getIntent().getExtras();
                String IDENTIFIER = extras.get("imdbID").toString();

                link = new URL("http://www.omdbapi.com/?apikey="+API_KEY+"&i="+IDENTIFIER);
                bufferedReader = new BufferedReader(new InputStreamReader(link.openStream()));
                String found = (bufferedReader).readLine();
                movie = MovHandler.movFromJson(found);
                System.out.println(IDENTIFIER+" / "+movie);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("IMDB:"+IDENTIFIER+"; mov: "+movie);
                        String title = movie.getTitle();
                        if (title.length()>60)
                            title = title.substring(0, 60) + "...";

                        ((TextView)findViewById(R.id.fullTitle)).setText(title);
                        ((TextView)findViewById(R.id.fullProduction)).setText(movie.getProduction());
                        ((TextView)findViewById(R.id.fullType)).setText(movie.getType());
                        ((TextView)findViewById(R.id.fullRated)).setText("Rated: "+movie.getRated());
                        ((TextView)findViewById(R.id.fullImdbRating)).setText("IMDB: "+movie.getImdbRating());
                        ((TextView)findViewById(R.id.fullImdbVotes)).setText("("+movie.getImdbVotes()+")");
                        ((TextView)findViewById(R.id.fullReleased)).setText("Released: "+movie.getReleased());
                        ((TextView)findViewById(R.id.fullRuntime)).setText("Runtime: "+movie.getRuntime());
                        ((TextView)findViewById(R.id.fullGenre)).setText("Genre: "+movie.getGenre());
                        ((TextView)findViewById(R.id.fullDirector)).setText("Director: "+movie.getDirector());
                        ((TextView)findViewById(R.id.fullWriter)).setText("Writer: "+movie.getWriter());
                        ((TextView)findViewById(R.id.fullActors)).setText("Actors: "+movie.getActors());
                        ((TextView)findViewById(R.id.fullLanguage)).setText("Language: "+movie.getLanguage());
                        ((TextView)findViewById(R.id.fullPlot)).setText(movie.getPlot());

                        ImageView posterIV = (ImageView) findViewById(R.id.fullPoster);
                        String poster = movie.getPoster();

                        try {
                            new DownloadPoster(posterIV).execute(poster);
                        } catch (Exception e){posterIV.setImageResource(R.drawable.unknown_file);};
                    }
                });
            } catch (Exception e){
                e.printStackTrace();
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
            Bitmap bitmap;
            try {
                bitmap = BitmapFactory.decodeStream(new java.net.URL(link).openStream());
            } catch (Exception e) {
                bitmap = null;
            }
            return bitmap;
        }
        protected void onPostExecute(Bitmap bitmap) {
            this.bitmap.setImageBitmap(bitmap);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
