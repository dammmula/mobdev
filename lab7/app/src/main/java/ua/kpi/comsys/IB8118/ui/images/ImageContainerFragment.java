package ua.kpi.comsys.IB8118.ui.images;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import ua.kpi.comsys.IB8118.sqlite.Image;
import ua.kpi.comsys.IB8118.sqlite.ImageDao;

public class ImageContainerFragment extends Fragment {
    View rootView;
    Adapter customAdapter;
    ListView imagesLV;
    static int widthScreen;
    List<List<String>> handledImages = new ArrayList<>();
    ConstraintLayout containerImages;
    double aspectRatio = 5.0 / 3.0;
    ImageDao imageDao;
    //url settings in 113 line


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_images, container, false);
        imageDao = App.getInstance().getDatabase().imageDao();
        new Thread(new ClearDatabase()).start();

        Display screenSize = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        screenSize.getSize(size);
        widthScreen = size.x;

        imagesLV = rootView.findViewById(R.id.images);
        containerImages = rootView.findViewById(R.id.containerImages);

        new ParseAndSetImage("StartLoad").start();

        return rootView;
    }

    double getSizeListViewOfCoefficient(int usedHeight) {
        return 1.0 - (double) usedHeight / 5.0;
    }

    void setListViewHeight() {
        try {
            if (customAdapter != null) {
                ConstraintLayout main = rootView.findViewById(R.id.imageFragment);
                int actualHeightLV, imgOnLV = handledImages.get(handledImages.size() - 1).size(),
                        defaultHeight = main.getHeight();
                float useOfScreen = (float) (widthScreen * aspectRatio) / (float) defaultHeight;
                double sizeListViewMul;

                if (imgOnLV < 4)
                    sizeListViewMul = getSizeListViewOfCoefficient(2);
                else if (imgOnLV < 7)
                    sizeListViewMul = getSizeListViewOfCoefficient(3);
                else if (imgOnLV < 8)
                    sizeListViewMul = getSizeListViewOfCoefficient(4);
                else
                    sizeListViewMul = 0.0;
                actualHeightLV = defaultHeight + (int) (defaultHeight * useOfScreen * sizeListViewMul);

                ConstraintLayout.LayoutParams paramCL = (ConstraintLayout.LayoutParams) containerImages.getLayoutParams();
                paramCL.height = actualHeightLV;
                containerImages.setLayoutParams(paramCL);
            }
        } catch (Exception ignored) {
        }
    }

    class ParseAndSetImage extends Thread {
        ParseAndSetImage(String name) {
            super(name);
        }

        public void run() {
            List<String> links = new ArrayList<>();
            List<List<String>> listOfLinks = new ArrayList<>();

            try { // has connection
                URL url = new URL("https://pixabay.com/api/?key=" + "19193969-87191e5db266905fe8936d565" + "&q=" + "yellow+flowers" + "&image_type=photo&per_page=" + 27);

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));
                String currentLine;
                StringBuilder finalString = new StringBuilder();
                while ((currentLine = bufferedReader.readLine()) != null) {
                    finalString.append(currentLine);
                }

                String[] strings = finalString.toString().split("\"webformatURL\":\"");
                for (String entity : strings) {
                    if (entity.startsWith("htt")) {
                        links.add(entity.split("\",\"")[0]);
                    }
                }
                String currentLink;
                for (int j = 0; j < links.size(); j++) {
                    currentLink = links.get(j);
                    if (listOfLinks.isEmpty()) {
                        List<String> additional = new ArrayList<>();
                        listOfLinks.add(additional);
                    }
                    if (listOfLinks.get(listOfLinks.size() - 1).size() >= 9) {
                        List<String> additional = new ArrayList<>();
                        additional.add(currentLink);
                        listOfLinks.add(additional);
                    } else {
                        listOfLinks.get(listOfLinks.size() - 1).add(currentLink);
                    }
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        customAdapter = new Adapter(getActivity(), R.layout.images, listOfLinks, getActivity());
                        imagesLV.setAdapter(customAdapter);
                    }
                });

            } catch (Exception ignored) { // hasn't connection
                List<Image> listOfImagesEnt = imageDao.getAll();

                for (Image entity : listOfImagesEnt) {
                    if (handledImages != null) {
                        if (handledImages.size() == 0) {
                            List<String> list = new ArrayList<>();
                            handledImages.add(list);
                        }
                        if (handledImages.get(handledImages.size() - 1).size() >= 9) {
                            List<String> list = new ArrayList<>();
                            list.add(entity.getUrl());
                            handledImages.add(list);
                        } else {
                            handledImages.get(handledImages.size() - 1).add(entity.getUrl());
                        }
                    }
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                        if (handledImages != null) {
                            imagesLV = rootView.findViewById(R.id.images);
                            customAdapter = new Adapter(getActivity(), R.layout.images, handledImages, getActivity());
                            imagesLV.setAdapter(customAdapter);
                        } else {
                            Toast.makeText(getContext(), "Error getting imgs", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }

    class Adapter extends ArrayAdapter<List<String>> {
        final List<List<String>> unableImages;
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
            lp.height = (int) (widthScreen * aspectRatio);
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

            for (int j = 0; j < sizeOfImages; j++) {
                try {
                    new Thread(new DownloadPoster(listIV.get(j), getActivity(), unableImages.get(position).get(j), getContext())).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return container;
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
            System.out.println("INIT HANDLE");
        }

        public void run() {
            Image workingImg = new Image();
            String fileStr;

            if (link.startsWith("http")) {
                List<Image> imageEntities = imageDao.getByUrl(link);
                String cacheDir = context.getCacheDir() + "";
                System.out.println("STARTS WITH HTTP");

                boolean hasImage = false;
                if (imageEntities.size() != 0) {
                    String imageCachePath = cacheDir + "/" + imageEntities.get(0).getFileName();
                    hasImage = new File(imageCachePath).exists();
                }

                if (imageEntities.size() == 0 | !hasImage) {
                    System.out.println("IMG HASN'T IN DB");

                    if (!hasImage & imageEntities.size() > 0)
                        fileStr = imageEntities.get(0).getFileName();
                    else {
                        fileStr = "img_" + hashCode() + ".png";
                    }

                    URL link;
                    try {
                        link = new URL(this.link);
                        try (InputStream input = link.openStream()) {
                            try (OutputStream output = new FileOutputStream(cacheDir + "/" + fileStr)) {
                                byte[] buffer = new byte[2048];
                                int bytesRead;
                                while ((bytesRead = input.read(buffer, 0, buffer.length)) >= 0) {
                                    output.write(buffer, 0, bytesRead);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    workingImg.url = this.link;
                    workingImg.fileName = fileStr;
                    imageDao.insert(workingImg);
                }

                try {
                    String imageName = imageDao.getByUrl(link).get(0).getFileName();
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

    public class ClearDatabase implements Runnable {
        @Override
        public void run() {
            imageDao.clearTrash();
        }
    }
}