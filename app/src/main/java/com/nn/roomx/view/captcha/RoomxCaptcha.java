package com.nn.roomx.view.captcha;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.nn.roomx.R;
import com.nn.roomx.RoomxUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by user on 2017-03-10.
 */

public class RoomxCaptcha extends LinearLayout {

    private CaptchImageAdapter captchImageAdapter;
    private RoomxCaptchaListner captchaListener;

    public RoomxCaptcha(Context context) {
        super(context);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.captcha, this, true);

        captchImageAdapter = new CaptchImageAdapter(getContext());

        initGrid();


    }

    public void setCaptchaDoneListener(RoomxCaptchaListner listener){
        this.captchaListener = listener;
    }

    private void initGrid() {
        LinearLayout container = (LinearLayout) getChildAt(0);

        Log.i(RoomxUtils.TAG, "container " + container.getId());
        LinearLayout gridContainer = (LinearLayout) container.getChildAt(1);

        Log.i(RoomxUtils.TAG, "gridContainer " + gridContainer.getId());
        GridView gridview = (GridView) gridContainer.getChildAt(0);


        gridview.setAdapter(captchImageAdapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Log.i(RoomxUtils.TAG, position + " Image clicked " + v.getClass());

                CaptchImage ci = (CaptchImage) captchImageAdapter.getItem(position);
                ci.selectImage();
                if (captchImageAdapter.allRequiredSelected()) {
                    Log.i(RoomxUtils.TAG, position + " allRequiredSelected ");
                    captchaListener.onDone();
                }
            }
        });
    }

    private class CaptchImageAdapter extends BaseAdapter {

        private Context mContext;
        private List<CaptchImage> images;
        private Set<CaptchImage> captchImages;

        public CaptchImageAdapter(Context c) {
            mContext = c;
            prepareImages();
        }

        private void prepareImages() {
            Log.i(RoomxUtils.TAG, "prepareImages ============= " );
            captchImages = new HashSet<CaptchImage>() {
                {
                    add(new CaptchImage(mContext, R.drawable.captcha_car1, true, "1"));
                    add(new CaptchImage(mContext, R.drawable.captcha_car2, true, "2"));
                    add(new CaptchImage(mContext, R.drawable.captcha_car3, true, "3"));
                    add(new CaptchImage(mContext, R.drawable.captcha_nn, false, "4"));
                    add(new CaptchImage(mContext, R.drawable.captcha_nn, false, "5"));
                    add(new CaptchImage(mContext, R.drawable.captcha_nn, false, "6"));
                    add(new CaptchImage(mContext, R.drawable.captcha_nn, false, "7"));
                    add(new CaptchImage(mContext, R.drawable.captcha_nn, false, "8"));
                    add(new CaptchImage(mContext, R.drawable.captcha_nn, false, "9"));
                    add(new CaptchImage(mContext, R.drawable.captcha_nn, false, "10"));
                }
            };

            images = new ArrayList<CaptchImage>();
            images.addAll(captchImages);
            Collections.shuffle(images);
        }


        @Override
        public int getCount() {
            return images.size();
        }

        @Override
        public Object getItem(int position) {
            return images.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CaptchImage imageView;


            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                imageView = images.get(position);
            } else {
                imageView = (CaptchImage) convertView;
            }
            return imageView;
        }


        public boolean allRequiredSelected() {
            boolean result = true;
            for (CaptchImage ci : images) {
                if(ci.isRequired()){
                    if(ci.isSelectedImage()){
                        result = result && true;
                    }else{
                        result = result && false;
                    }
                }else{
                    if(ci.isSelectedImage()){
                        return false;
                    }
                }
            }
            return result;
        }


    }

    private class CaptchImage extends ImageView {

        private boolean selectedImage = false;
        private boolean required = false;
        public String name ="";

        public CaptchImage(Context context, int imageResource, boolean required, String s) {
            super(context);
            setImageResource(imageResource);
            setLayoutParams(new GridView.LayoutParams(250, 250));
            if (selectedImage) {
                setBackground(getResources().getDrawable(R.drawable.captcha_image_border_selected));
            } else {
                setBackground(getResources().getDrawable(R.drawable.captcha_image_border));
            }
            this.required = required;
            this.name = s;

        }

        public void selectImage() {
            this.selectedImage = !this.selectedImage;

            if (selectedImage) {
                setBackground(getResources().getDrawable(R.drawable.captcha_image_border_selected));
            } else {
                setBackground(getResources().getDrawable(R.drawable.captcha_image_border));
            }
        }

        public boolean isSelectedImage() {
            return this.selectedImage;
        }

        public boolean isRequired() {
            return this.required;
        }
    }

    public interface RoomxCaptchaListner {
        public void onDone();
    }
}
