package ua.kpi.comsys.IB8118.ui.movies;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;
import java.util.List;

import ua.kpi.comsys.IB8118.R;

public class MoviesFragment extends Fragment {
    private List<Movie> movies;
    private final List<Movie> foundMovies = new ArrayList<>();
    private List<Movie> adapterObjects;
    private Adapter customAdapter;
    ListView moviesList;
    int widthScreen;
    int maximumLattersTitle;
    boolean requiredRecreate = false;
    EditText findFieldET;

    public void handleScreenSize() {
        super.onResume();
        Display screen = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        screen.getSize(size);
        widthScreen = size.x;
        maximumLattersTitle = widthScreen /16;
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
        View rootView = inflater.inflate(R.layout.fragment_movies, container, false);
        Button buttonAddMovie = rootView.findViewById(R.id.buttonAdd);
        findFieldET = rootView.findViewById(R.id.fieldFind);

        moviesList = rootView.findViewById(R.id.movies_listView);
        movies = MovHandler.getListMovFromRaw(getContext());

        if(movies != null){
            customAdapter = new Adapter(getActivity(), R.layout.movies, movies);
            moviesList.setAdapter(customAdapter);
        }

        findFieldET.addTextChangedListener(new TextWatcher() { // user print on findField
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {runSearch();}
        });

        buttonAddMovie.setOnClickListener(new View.OnClickListener() { // start adding
            @Override
            public void onClick(View v) {
                requiredRecreate = true;
                startActivity(new Intent(getContext(), MovieAddActivity.class));
            }
        });

        moviesList.setOnItemClickListener(new AdapterView.OnItemClickListener() { // call detail info about movie
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(getContext(), MovieInfoActivity.class).putExtra("imdbID", adapterObjects.get(position).getImdbID()));
            }
        });

        moviesList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()  { // deleting
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View itemClicked, int position,
                                           long id) {
                if(findFieldET.getText().toString().equals("")) {
                    itemClicked.setBackgroundResource(R.color.ocean_main);
                    try {
                        AlertDialog.Builder constructor = new AlertDialog.Builder(getActivity());
                        constructor.setMessage("Do you want to delete selected movie?");
                        constructor.setCancelable(true);
                        constructor.setIcon(R.drawable.ic_movie_icon);
                        constructor.setTitle("Deleting");

                        constructor.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                itemClicked.setBackgroundResource(R.color.white);
                            }
                        });

                        constructor.setPositiveButton("Delete selected movie", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                movies.remove((int) id);
                                customAdapter.notifyDataSetChanged();
                                MovHandler.exportToCached(getContext(), movies);
                                requiredRecreate = true;
                                dialog.dismiss();
                            }
                        });

                        constructor.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                itemClicked.setBackgroundResource(R.color.white);
                            }
                        });

                        AlertDialog alertDialog = constructor.create();
                        alertDialog.show();

                    } catch (Exception e) {
                        Toast.makeText(getContext(), "ERROR", Toast.LENGTH_SHORT).show();
                    }
                }
                else Toast.makeText(getContext(), "Clear your search term first.", Toast.LENGTH_SHORT).show();
                return true;
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

            String title = (adapterObjects.get(position).getTitle());
            if (title.length() > maximumLattersTitle)
                title = title.substring(0, maximumLattersTitle) + "...";

            titleTV.setText(title);
            typeTV.setText(adapterObjects.get(position).getType());
            yearTV.setText("Year: " + adapterObjects.get(position).getYear());

            ImageView posterIV = row.findViewById(R.id.poster_listview);
            String poster = adapterObjects.get(position).getPoster();

            int imgResID = getContext().getResources().getIdentifier(poster.replaceAll(".jpg",""), "drawable", getContext().getPackageName());

            if(imgResID!=0) posterIV.setImageResource(imgResID);
            else posterIV.setImageResource(R.drawable.unknown_file);
            return row;
        }
    }

    void runSearch(){
        String fieldText = findFieldET.getText().toString().toLowerCase();

        if(!fieldText.equals("")){
            foundMovies.clear();
            for (int i = 0; i < movies.size(); i++)
                if(movies.get(i).getTitle().toLowerCase().contains(fieldText))
                    foundMovies.add(movies.get(i));
            if(foundMovies.isEmpty())
                Toast.makeText(getContext(), "No movies :(", Toast.LENGTH_SHORT).show();
            customAdapter = new Adapter(getActivity(), R.layout.movies, foundMovies);
        }
        else
            customAdapter = new Adapter(getActivity(), R.layout.movies, movies);
        moviesList.setAdapter(customAdapter);
    }
}