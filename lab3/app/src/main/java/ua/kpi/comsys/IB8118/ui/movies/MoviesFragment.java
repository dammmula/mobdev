package ua.kpi.comsys.IB8118.ui.movies;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;

import ua.kpi.comsys.IB8118.R;

public class MoviesFragment extends Fragment {
    private List<Movie> movies;
    private Adapter adapter;
    ListView listView;
    int width, numOfSymbols;

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
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSize();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_movies, container, false);


        listView = root.findViewById(R.id.movies_listView);

        movies = MovHandler.getListMovFromRaw(getContext());
        numOfSymbols = (int)(width/18);

        if(movies != null){
//            for (Movie mov : movies)
//                System.out.println(mov);

            adapter = new Adapter(getActivity(), R.layout.movies, movies);
            listView.setAdapter(adapter);
        }
        return root;
    }

    private class Adapter extends ArrayAdapter<Movie> {
        Adapter(Context context, int textViewResourceId, List<Movie> objects) {
            super(context, textViewResourceId, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            @SuppressLint("ViewHolder") View row = inflater.inflate(R.layout.movies, parent, false);
            TextView    titleTV = row.findViewById(R.id.movieTitle),
                        typeTV = row.findViewById(R.id.movieType),
                        yearTV = row.findViewById(R.id.movieYear);

            String title = (movies.get(position).getTitle());
            if (title.length() > numOfSymbols)
                title = title.substring(0, numOfSymbols) + "...";

            titleTV.setText(title);
            typeTV.setText(movies.get(position).getType());
            yearTV.setText("Year: " + movies.get(position).getYear());

            ImageView posterIV = row.findViewById(R.id.poster_listview);
            String poster = movies.get(position).getPoster();

            int imgResID = getContext().getResources().getIdentifier(poster.replaceAll(".jpg",""), "drawable", getContext().getPackageName());

            if(imgResID!=0) posterIV.setImageResource(imgResID);
            else posterIV.setImageResource(R.drawable.unknown_file);
            return row;
        }
    }
}