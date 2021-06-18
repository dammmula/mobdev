package ua.kpi.comsys.IB8118.ui.images;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ua.kpi.comsys.IB8118.R;

public class ImageContainerFragment extends Fragment {
    View rootView;
    Adapter customAdapter;
    ListView imagesLV;
    static int widthScreen;
    List<List<String>> handledImages = new ArrayList<>();
    String sharedPref = "images";
    boolean fragmentInFocus = false;
    ConstraintLayout containerImages;
    double aspectRatio = 5.0/3.0;
    //url settings in 117 line


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_images, container, false);

        Display screensize = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        screensize.getSize(size);
        widthScreen = size.x;

        imagesLV = rootView.findViewById(R.id.images);
        containerImages = rootView.findViewById(R.id.containerImages);

        new ParseAndSetImage("StartLoad").start();

        return rootView;
    }

    double getSizeListViewOfCoefficient(int usedHeight){
        return 1.0-(double)usedHeight/5.0;
    }

    void setListViewHeight(){
        try {
            if (customAdapter != null) {
                ConstraintLayout main = rootView.findViewById(R.id.imageFragment);
                int actualHeightLV, imgOnLV = handledImages.get(handledImages.size()-1).size(),
                    defaultHeight = main.getHeight();
                float useOfScreen = (float)(widthScreen*aspectRatio)/(float)defaultHeight;
                double sizeListViewMul;

                if (imgOnLV < 4)
                    sizeListViewMul = getSizeListViewOfCoefficient(2);
                else if (imgOnLV < 7)
                    sizeListViewMul = getSizeListViewOfCoefficient(3);
                else if (imgOnLV < 8)
                    sizeListViewMul = getSizeListViewOfCoefficient(4);
                else
                    sizeListViewMul = 0.0;
                actualHeightLV = defaultHeight+(int)(defaultHeight*useOfScreen*sizeListViewMul);

                ConstraintLayout.LayoutParams paramCL = (ConstraintLayout.LayoutParams) containerImages.getLayoutParams();
                paramCL.height = actualHeightLV;
                containerImages.setLayoutParams(paramCL);
            }
        } catch (Exception ignored){}
    }

    class ParseAndSetImage extends Thread {
        ParseAndSetImage(String name){
            super(name);
        }

        public void run(){
            List<String> links = new ArrayList<>();
            List<List<String>> listOfLinks = new ArrayList<>();

            try {
                URL url = new URL("https://pixabay.com/api/?key="+"19193969-87191e5db266905fe8936d565"+"&q="+"yellow+flowers"+"&image_type=photo&per_page="+27);

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));
                String currentLine;
                StringBuilder finalString = new StringBuilder();
                while ((currentLine = bufferedReader.readLine()) != null) {
                    finalString.append(currentLine);
                }

                String[] strings = finalString.toString().split("\"webformatURL\":\"");
                for (String entity : strings){
                    if (entity.startsWith("htt")){
                        links.add(entity.split("\",\"")[0]);
                    }
                }
                String currentLink;
                for (int j = 0; j < links.size(); j++) {
                    currentLink = links.get(j);
                    if (listOfLinks.isEmpty()){
                        List<String> additional = new ArrayList<>();
                        listOfLinks.add(additional);
                    }
                    if (listOfLinks.get(listOfLinks.size()-1).size()>=9){
                        List<String> additional = new ArrayList<>();
                        additional.add(currentLink);
                        listOfLinks.add(additional);
                    }
                    else {
                        listOfLinks.get(listOfLinks.size()-1).add(currentLink);
                    }
                }
            } catch (Exception ignored) {}

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    customAdapter = new Adapter(getActivity(), R.layout.images, listOfLinks, getActivity());
                    imagesLV.setAdapter(customAdapter);
                }
            });
        }
    }

    class Adapter extends ArrayAdapter<List<String>> {
        private final List<List<String>> unableImages;
        Activity generalAct;

        Adapter(Context context, int textViewResourceId, List<List<String>> objects, Activity generalAct) {
            super(context, textViewResourceId, objects);
            this.unableImages = objects;
            this.generalAct = generalAct;
            setListViewHeight();
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View container = inflater.inflate(R.layout.images, parent, false);

            LinearLayout containerGroup = container.findViewById(R.id.imgSet);
            ViewGroup.LayoutParams lp = containerGroup.getLayoutParams();
            lp.height = (int)(widthScreen *aspectRatio);
            containerGroup.setLayoutParams(lp);

            List<ImageView> listIV = new ArrayList<>();
            listIV.add(container.findViewById(R.id.img1));
            listIV.add(container.findViewById(R.id.img2));
            listIV.add(container.findViewById(R.id.img3));
            listIV.add(container.findViewById(R.id.img4));
            listIV.add(container.findViewById(R.id.img5));
            listIV.add(container.findViewById(R.id.img6));
            listIV.add(container.findViewById(R.id.img7));
            listIV.add(container.findViewById(R.id.img8));
            listIV.add(container.findViewById(R.id.img9));

            int sizeOfImages = unableImages.get(position).size();

            for (int j=0; j<sizeOfImages; j++){
                try {
                    new DownloadPoster(listIV.get(j)).execute(unableImages.get(position).get(j));
                } catch (Exception ignored){}
            }
            return container;
        }
    }

    private static class DownloadPoster extends AsyncTask<String, Void, Bitmap> {
        @SuppressLint("StaticFieldLeak")
        ImageView bitmap;

        public DownloadPoster(ImageView bitmap) {
            this.bitmap = bitmap;
        }

        protected Bitmap doInBackground(String... links) {
            String link = links[0];
            Bitmap bitmap;
            try {
                bitmap = BitmapFactory.decodeStream(new java.net.URL(link).openStream());
            } catch (Exception ignored) {
                bitmap = null;
            }
            return bitmap;
        }
        protected void onPostExecute(Bitmap bitmap) {
            this.bitmap.setImageBitmap(bitmap);
        }
    }
}
