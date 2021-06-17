package ua.kpi.comsys.IB8118.ui.movies;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import ua.kpi.comsys.IB8118.R;

public class MovieAddActivity extends AppCompatActivity {
    EditText movieNameET;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_add);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        movieNameET = findViewById(R.id.fieldAddName);

        movieNameET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                movieNameET.setHintTextColor(getColor(R.color.hint_color));
                movieNameET.setHint(R.string.default_hint_title);
            }
        });
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

    public void buttonAddMovie(View view) {
        String movieName = movieNameET.getText().toString();
        String movieType = ((EditText)findViewById(R.id.fieldAddType)).getText().toString();
        String movieYear = ((EditText)findViewById(R.id.fieldAddYear)).getText().toString();

        if (movieNameET.getText().toString().length()>0){
            MovHandler movHandler = new MovHandler();
            List<Movie> movies = movHandler.getUserMovies(view.getContext());

            movies.add(new Movie(movieName, movieType, movieYear));
            movHandler.exportToCached(view.getContext(), movies);

            finish();
            Toast.makeText(view.getContext(), "Success", Toast.LENGTH_SHORT).show();

        } else{ // incorrect name
            movieNameET.setHintTextColor(getColor(R.color.error));
            movieNameET.setHint(R.string.warning_hint_title);
        }
    }
}
