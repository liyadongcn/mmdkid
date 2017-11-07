package com.mmdkid.mmdkid.models;

import java.util.ArrayList;

/*
 * Created by liyadong 2017/11/05
 */
public class CustomImage {

    private String mUrl;
    private String mDescription;

    public CustomImage(String url, String description) {
        this.mUrl = url;
        this.mDescription = description;
    }
    /**
     *  通过image类型的content获得带描述的图片列表对象
     */
    public static ArrayList<CustomImage> getCustomImagesFromContent(Content content)
    {
        ArrayList<CustomImage> customImages = new ArrayList<CustomImage>();
        if (content.mImageList!=null && content.mModelType==content.TYPE_IMAGE){
            CustomImage customImage;
            for( int i =0;i<content.mImageList.size();i++){
                if (content.mImageDescriptionList!=null && content.mImageDescriptionList.get(i)!=null){
                    customImage = new CustomImage(content.mImageList.get(i),content.mImageDescriptionList.get(i));
                }else {
                    customImage = new CustomImage(content.mImageList.get(i),"");
                }
                customImages.add(customImage);
            }
            return customImages;
        }
        return null;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getDescription() {
        return mDescription;
    }
}