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
    private Adapter adapter;
    ListView listView;
    int width, numOfSymbols;
    boolean requiredRecreate = false;
    EditText findField;

    public void setSize() {
        super.onResume();
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        numOfSymbols = (int)(width/18);
    }

    @Override
    public void onPause() {
        super.onPause();
        setSize();
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
        setSize();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_movies, container, false);
//        numOfSymbols = (int)(width/18); //max length of movie name
        Button buttonAdd = root.findViewById(R.id.buttonAdd);
        findField = root.findViewById(R.id.fieldFind);

        listView = root.findViewById(R.id.movies_listView);

        movies = MovHandler.getListMovFromRaw(getContext());

        if(movies != null){
            adapter = new Adapter(getActivity(), R.layout.movies, movies);
            listView.setAdapter(adapter);
        }

        findField.addTextChangedListener(new TextWatcher() { // user print on findField
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {runSearch();}
        });

        buttonAdd.setOnClickListener(new View.OnClickListener() { // start adding
            @Override
            public void onClick(View v) {
                requiredRecreate = true;
                startActivity(new Intent(getContext(), MovieAddActivity.class));
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() { // call detail info about movie
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(getContext(), MovieInfoActivity.class).putExtra("imdbID", adapterObjects.get(position).getImdbID()));
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()  { // deleting
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View itemClicked, int position,
                                           long id) {
                if(findField.getText().toString().equals("")) {
                    itemClicked.setBackgroundResource(R.color.ocean_main);
                    try {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage("Do you want to delete selected movie?");
                        builder.setCancelable(true);
                        builder.setIcon(R.drawable.ic_movie_icon);
                        builder.setTitle("Deleting");

                        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                itemClicked.setBackgroundResource(R.color.white);
                            }
                        });

                        builder.setPositiveButton("Delete selected movie", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                movies.remove((int) id);
                                adapter.notifyDataSetChanged();
                                MovHandler.exportToCached(getContext(), movies);
                                requiredRecreate = true;
                                dialog.dismiss();
                            }
                        });

                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                itemClicked.setBackgroundResource(R.color.white);
                            }
                        });

                        AlertDialog dialog = builder.create();
                        dialog.show();

                    } catch (Exception e) {
                        Toast.makeText(getContext(), "ERROR", Toast.LENGTH_SHORT).show();
                    }
                }
                else Toast.makeText(getContext(), "Clear your search term first.", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        return root;
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
            if (title.length() > numOfSymbols)
                title = title.substring(0, numOfSymbols) + "...";

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
        String fieldText = findField.getText().toString().toLowerCase();

        if(!fieldText.equals("")){
            foundMovies.clear();
            for (int i = 0; i < movies.size(); i++)
                if(movies.get(i).getTitle().toLowerCase().contains(fieldText))
                    foundMovies.add(movies.get(i));
            if(foundMovies.isEmpty())
                Toast.makeText(getContext(), "No movies :(", Toast.LENGTH_SHORT).show();
            adapter = new Adapter(getActivity(), R.layout.movies, foundMovies);
        }
        else
            adapter = new Adapter(getActivity(), R.layout.movies, movies);
        listView.setAdapter(adapter);
    }
}