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

public class ImageCollectionsFragment extends Fragment {
    View root;
    Adapter adapter;
    ListView images;

    static int screenSizeY;
    List<List<String>> handledImages = new ArrayList<>();
    String sharedPref = "images";
    boolean fragmentInFocus = false;
    ConstraintLayout containerImages;
    double aspectRatio = 5.0/3.0;

    /**
     * Called when the Fragment is no longer started.  This is generally
     * tied to {Activity.onStop} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onStop() {
        super.onStop();
        //fragment closed
    }

    @Override
    public void onPause() {
        super.onPause();
        // if fragment be inactive, run once
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(sharedPref, Context.MODE_PRIVATE);
        SharedPreferences.Editor handlerSettings = sharedPreferences.edit();
        handlerSettings.putString(sharedPref, makeStringFromImgLists(handledImages));
        handlerSettings.apply();
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
        screenSizeY = size.x;

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

        if (!fragmentInFocus){
            fragmentInFocus = true;
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

            float useOfScreen = (float)(screenSizeY *aspectRatio)/(float)heightView;
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

    public String makeStringFromImgLists(List<List<String>> listOfImage){
        StringBuilder stringBuilder = new StringBuilder();
        for (List<String> image : listOfImage)
            for (int j = 0; j < image.size(); j++) {
                stringBuilder.append(image.get(j));
                stringBuilder.append("@");
            }
        String finalString = stringBuilder.toString();
        if (finalString.length()>0)
            return finalString.substring(0, finalString.length() - 1);
        return "";
    }

    public List<List<String>> makeImgListsFromString(String stringOfImages){
        List<List<String>> listsOfImages = new ArrayList<>();
        List<String> generalSplitList = new ArrayList<>(Arrays.asList(stringOfImages.split("@")));
        if (stringOfImages.equals(""))
            return listsOfImages;
        for (String secondaryImageList : generalSplitList) {
            if (listsOfImages.size() <1) {
                List<String> imgList = new ArrayList<>();
                listsOfImages.add(imgList);
            }
            if (listsOfImages.get(listsOfImages.size() - 1).size() >= 9) {
                List<String> imgList = new ArrayList<>();
                imgList.add(secondaryImageList);
                listsOfImages.add(imgList);
            } else
                listsOfImages.get(listsOfImages.size()-1).add(secondaryImageList);
        }
        return listsOfImages;
    }

    public void setImagesList(){
        if (!fragmentInFocus) {
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences(sharedPref, Context.MODE_PRIVATE);
            handledImages = makeImgListsFromString(sharedPreferences.getString(sharedPref, ""));
            if (handledImages != null & handledImages.size() > 0) {
                if (handledImages.get(0).size() > 0) {
                    adapter = new Adapter(getActivity(), R.layout.images, handledImages, getActivity());
                    images.setAdapter(adapter);
                    setListViewHeight();
                } else
                    handledImages = new ArrayList<>();
            } else
                handledImages = new ArrayList<>();
            fragmentInFocus = true;
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
            LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View mainView = layoutInflater.inflate(R.layout.images, parent, false);
            setListViewHeight();

            LinearLayout LV = mainView.findViewById(R.id.imgSet);
            ViewGroup.LayoutParams lp = LV.getLayoutParams();
            lp.height = (int)(screenSizeY *aspectRatio);
            lp.width = screenSizeY;
            LV.setLayoutParams(lp);

            List<ImageView> listIV = new ArrayList<>();
            listIV.add(mainView.findViewById(R.id.img1));
            listIV.add(mainView.findViewById(R.id.img2));
            listIV.add(mainView.findViewById(R.id.img3));
            listIV.add(mainView.findViewById(R.id.img4));
            listIV.add(mainView.findViewById(R.id.img5));
            listIV.add(mainView.findViewById(R.id.img6));
            listIV.add(mainView.findViewById(R.id.img7));
            listIV.add(mainView.findViewById(R.id.img8));
            listIV.add(mainView.findViewById(R.id.img9));

            int imgsNum = taskImg.get(position).size();

            for (int i=0; i<imgsNum; i++){
                try {
                    SetIV setIV = new SetIV(listIV.get(i), generalAct, getContext(), taskImg.get(position).get(i)); // download + set image
                    new Thread(setIV).start();
                } catch (Exception ignored){}
            }
            return mainView;
        }

        public class SetIV implements Runnable {
            protected ImageView IV;
            protected Activity mainActivity;
            protected Context context;
            protected String imgFile;

            public SetIV(ImageView IV, Activity mainActivity, Context context, String imgFile) {
                this.IV = IV;
                this.mainActivity = mainActivity;
                this.context = context;
                this.imgFile = imgFile;
            }

            public void run() {
                try {
                    String filePath = context.getFilesDir() + "/" + imgFile;
                    InputStream is = new FileInputStream(new File(filePath));
                    Bitmap bitmapImage = BitmapFactory.decodeStream(is);
                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            IV.setImageBitmap(bitmapImage);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
