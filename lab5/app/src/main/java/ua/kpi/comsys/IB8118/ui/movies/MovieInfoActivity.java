package ua.kpi.comsys.IB8118.ui.movies;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import ua.kpi.comsys.IB8118.R;

public class MovieInfoActivity extends AppCompatActivity {
    Movie movie;
    String imdbID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_full_info);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Bundle arguments = getIntent().getExtras();
        imdbID = arguments.get("imdbID").toString();

        int res = this.getResources().getIdentifier(imdbID, "raw", this.getPackageName());

        if(res!=0) {

            movie = MovHandler.movFromRes(this, res);
            if (movie!=null)
                Toast.makeText(this, movie.getTitle(), Toast.LENGTH_SHORT).show();

            System.out.println("IMDB:"+imdbID+"; mov: "+movie);
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

            ImageView poster = (ImageView) findViewById(R.id.fullPoster);

            int posterRes;
            try {
                String imageName = movie.getPoster().replaceAll(".jpg","").toLowerCase();
                posterRes = getResources().getIdentifier(imageName, "drawable", getPackageName());
            } catch (Exception e){posterRes = 0;};

            if(posterRes!=0) poster.setImageResource(posterRes);
            else poster.setImageResource(R.drawable.unknown_file);
        }
        else {Toast.makeText(this, "No data available", Toast.LENGTH_LONG).show(); finish();}
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
