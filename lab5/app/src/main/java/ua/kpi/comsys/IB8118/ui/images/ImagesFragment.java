package ua.kpi.comsys.IB8118.ui.images;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ua.kpi.comsys.IB8118.R;

import static android.app.Activity.RESULT_OK;

public class ImagesFragment extends Fragment {
    View root;
    Adapter adapter;
    ListView images;

    static int width;
    List<List<String>> handledImages = new ArrayList<>();
    String sharedPref = "images";
    Boolean layoutActive = false;
    ConstraintLayout containerImages;
    double aspectRatio = 5.0/3.0;

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences settings = getActivity().getSharedPreferences(sharedPref, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(sharedPref, imagesListsToString(handledImages));
        editor.apply();
    }

    @Override
    public void onResume() {
        super.onResume();
        setImagesList();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_images, container, false);

        Display screensize = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        screensize.getSize(size);
        width = size.x;

        Button buttonAddImage = root.findViewById(R.id.buttonAddImage);
        images = root.findViewById(R.id.images);
        containerImages = root.findViewById(R.id.containerImages);

        buttonAddImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1);
            }
        });

        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        if (!layoutActive){
            layoutActive = true;
        }

        if (requestCode == 1 & imageReturnedIntent!=null) {
            if (resultCode == RESULT_OK) {
                try {
                    final Uri source = imageReturnedIntent.getData();
                    final InputStream is = getContext().getContentResolver().openInputStream(source);
                    Bitmap imageBitmp = BitmapFactory.decodeStream(is);

                    String imgName = "userImage-"+source.hashCode();

                    if (handledImages != null){
                        if (handledImages.size()==0){
                            List<String> imgList = new ArrayList<>();
                            handledImages.add(imgList);
                        }
                        if (handledImages.get(handledImages.size()-1).size()>=9){
                            List<String> imgList = new ArrayList<>();
                            imgList.add(imgName);
                            handledImages.add(imgList);
                        }
                        else
                            handledImages.get(handledImages.size()-1).add(imgName);
                    }

                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    if (imageBitmp.getWidth() < 450 | imageBitmp.getHeight() < 450)
                        imageBitmp.compress(Bitmap.CompressFormat.PNG, 100, bos);
                    else {
                        float ratioImage = (float)imageBitmp.getWidth()/imageBitmp.getHeight();
                        imageBitmp = Bitmap.createScaledBitmap(imageBitmp, (int)(450*ratioImage), 450, false);
                        imageBitmp.compress(Bitmap.CompressFormat.PNG, 100, bos);
                    }

                    byte[] bitmapdata = bos.toByteArray();
                    File imageFile = new File(getContext().getFilesDir(), imgName);

                    try {
                        FileOutputStream fos = new FileOutputStream(imageFile);
                        fos.write(bitmapdata);
                        fos.flush();
                        fos.close();

                    } catch (Exception ignored) {}

                    if(handledImages != null & handledImages.get(0).size()==1){
                        adapter = new Adapter(getActivity(), R.layout.images, handledImages, getActivity());
                        images.setAdapter(adapter);
                    }
                    else if (handledImages.size()>0)
                        adapter.notifyDataSetChanged();
                    setListViewHeight();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    double getSizeListViewOfCoefficient(int usedHeight){
        return 1.0-(double)usedHeight/5.0;
    }

    void setListViewHeight(){
        if (adapter != null) {
            int totalHeight, imgOnLV = handledImages.get(handledImages.size()-1).size();
            ConstraintLayout main = root.findViewById(R.id.imageFragment);

            int heightView = main.getHeight();

            float useOfScreen = (float)(width*aspectRatio)/(float)heightView;
            double sizeListViewMul;

            if (imgOnLV < 4)
                sizeListViewMul = getSizeListViewOfCoefficient(2);
            else if (imgOnLV < 7)
                sizeListViewMul = getSizeListViewOfCoefficient(3);
            else if (imgOnLV < 8)
                sizeListViewMul = getSizeListViewOfCoefficient(4);
            else
                sizeListViewMul = 0.0;
            totalHeight = heightView+(int)(heightView*useOfScreen*sizeListViewMul);

            ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) containerImages.getLayoutParams();
            lp.height = totalHeight;
            containerImages.setLayoutParams(lp);
        }
    }

    public String imagesListsToString(List<List<String>> img){
        StringBuilder result = new StringBuilder();
        for (List<String> obj1 : img)
            for (int i = 0; i < obj1.size(); i++) {
                result.append(obj1.get(i));
                result.append("@");
            }
        String resultStr = result.toString();
        if (resultStr.length()>0)
            return resultStr.substring(0, resultStr.length() - 1);
        return "";
    }

    public List<List<String>> getImagesListsFromString(String imgStr){
        List<List<String>> result = new ArrayList<>();
        List<String> firstStep = new ArrayList<>(Arrays.asList(imgStr.split("@")));

        if (imgStr.equals(""))
            return result;
        for (String obj : firstStep) {
            if (result.size() == 0) {
                List<String> imgList = new ArrayList<>();
                result.add(imgList);
            }
            if (result.get(result.size() - 1).size() >= 9) {
                List<String> imgList = new ArrayList<>();
                imgList.add(obj);
                result.add(imgList);
            } else
                result.get(result.size()-1).add(obj);
        }
        return result;
    }

    public void setImagesList(){
        if (!layoutActive) {
            SharedPreferences settings = getActivity().getSharedPreferences(sharedPref, Context.MODE_PRIVATE);
            handledImages = getImagesListsFromString(settings.getString(sharedPref, ""));
            if (handledImages != null & handledImages.size() > 0) {
                if (handledImages.get(0).size() > 0) {
                    adapter = new Adapter(getActivity(), R.layout.images, handledImages, getActivity());
                    images.setAdapter(adapter);
                    setListViewHeight();
                } else
                    handledImages = new ArrayList<>();
            } else
                handledImages = new ArrayList<>();
            layoutActive = true;
        }
    }

    class Adapter extends ArrayAdapter<List<String>> {
        private final List<List<String>> taskImg;
        Activity generalAct;

        Adapter(Context context, int textViewResourceId, List<List<String>> objects, Activity generalAct) {
            super(context, textViewResourceId, objects);
            this.taskImg = objects;
            this.generalAct = generalAct;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = inflater.inflate(R.layout.images, parent, false);
            setListViewHeight();

            LinearLayout layout = row.findViewById(R.id.imgSet);
            ViewGroup.LayoutParams lp = layout.getLayoutParams();
            lp.height = (int)(width*aspectRatio);
            lp.width = width;
            layout.setLayoutParams(lp);

            List<ImageView> listIV = new ArrayList<>();
            listIV.add(row.findViewById(R.id.img1));
            listIV.add(row.findViewById(R.id.img2));
            listIV.add(row.findViewById(R.id.img3));
            listIV.add(row.findViewById(R.id.img4));
            listIV.add(row.findViewById(R.id.img5));
            listIV.add(row.findViewById(R.id.img6));
            listIV.add(row.findViewById(R.id.img7));
            listIV.add(row.findViewById(R.id.img8));
            listIV.add(row.findViewById(R.id.img9));

            int imgsNum = taskImg.get(position).size();

            for (int i=0; i<imgsNum; i++){
                try {
                    SetIV handler = new SetIV(listIV.get(i), generalAct, getContext(), taskImg.get(position).get(i));
                    new Thread(handler).start();
                } catch (Exception ignored){}
            }
            return row;
        }

        public class SetIV implements Runnable {
            protected ImageView imageView;
            protected Activity uiActivity;
            protected Context context;
            protected String file;

            public SetIV(ImageView imageView, Activity uiActivity, Context context, String file) {
                this.imageView = imageView;
                this.uiActivity = uiActivity;
                this.context = context;
                this.file = file;
            }

            public void run() {
                try {
                    InputStream is = new FileInputStream(new File(context.getFilesDir() + "/" + file));
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    uiActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImageBitmap(bitmap);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
