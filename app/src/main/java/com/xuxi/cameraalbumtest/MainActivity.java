package com.xuxi.cameraalbumtest;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    public static final int TAKE_PHOTO = 1;
    private ImageView picture;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        picture = findViewById(R.id.picture);

        Button takePhoto = findViewById(R.id.take_photo);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //创建File对象，用于存储拍照后的图片
                File outputImage = new File(getExternalCacheDir(),"output_image.jpg");
                try{
                    if (outputImage.exists()){
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                }catch (IOException e){
                    e.printStackTrace();
                }

                if (Build.VERSION.SDK_INT >= 24){
                    imageUri = FileProvider.getUriForFile(MainActivity.this,"com.xuxi.cameraalbumtest.fileprovider",outputImage);
                }else {
                    imageUri = Uri.fromFile(outputImage);
                }
                //启动相机程序
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                startActivityForResult(intent,TAKE_PHOTO);


            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK){
                    try{
                        //将拍摄的照片显示出来
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));

                        picture.setImageBitmap(bitmap);
                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                }

                break;
                default:
                    break;
        }

    }

    public static int calculateInSampleSize(BitmapFactory.Options options,int reqWidth,int reqHeight){
        //源图片的高度和宽度
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth){
            //计算出实际宽高和目标宽高的比率
            final int heightRatio = Math.round((float) height/(float) reqHeight);
            final int widthRatio = Math.round((float) width/(float) reqWidth);
            //选择宽和高中最小的比率作为imSampleSize的值，这样可以保证最终图片的宽和高
            //一定都会大于等于目标的宽度
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    public static Bitmap decodeSampleBitmapFromResource(String imagePath,int reqWidth,int reqHeight){
        //第一次解析将inJustDecodeBouds设置为true，来获取图片大小
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath,options);
        //调用上面定义的方法计算inSampleSize值
        options.inSampleSize = calculateInSampleSize(options,reqWidth,reqHeight);
        //使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(imagePath,options);
    }

}
