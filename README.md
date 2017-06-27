提供一个获取Bitmap的工具类：

```java
package com.example.administrator.bitmapfactory;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;

/**
 * Created by AFinalStone on 2017/6/27.
 * 邮箱：602392033@qq.com
 * 使用完毕Bitmap之后，我们可以通过Bitmap.recycle()方法来释放位图所占的空间，当然前提是位图没有被使用。
 */

public class BitmapUtil {

    /**
     * 从path中获取图片信息,在通过BitmapFactory.decodeFile(String path)方法将突破转成Bitmap时，
     * 遇到大一些的图片，我们经常会遇到OOM(Out Of Memory)的问题。所以用到了我们上面提到的BitmapFactory.Options这个类。
     *
     * @param path   文件路径
     * @param width  想要显示的图片的宽度
     * @param height 想要显示的图片的高度
     * @return
     */
    private Bitmap decodeBitmap(String path, int width, int height) {
        BitmapFactory.Options op = new BitmapFactory.Options();
        // inJustDecodeBounds如果设置为true,仅仅返回图片实际的宽和高,宽和高是赋值给opts.outWidth,opts.outHeight;
        op.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(path, op); //获取尺寸信息
        //获取比例大小
        int wRatio = (int) Math.ceil(op.outWidth / width);
        int hRatio = (int) Math.ceil(op.outHeight / height);
        //如果超出指定大小，则缩小相应的比例
        if (wRatio > 1 && hRatio > 1) {
            if (wRatio > hRatio) {
                op.inSampleSize = wRatio;
            } else {
                op.inSampleSize = hRatio;
            }
        }
        op.inJustDecodeBounds = false;
        bmp = BitmapFactory.decodeFile(path, op);
        return bmp;
    }

    /**
     * 以最省内存的方式读取本地资源的图片
     * @param context
     * @param resId
     * @return
     */
    public static Bitmap readBitMap(Context context, int resId){
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        //获取资源图片
        InputStream is = context.getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is,null,opt);
    }

    private Bitmap decodeBitmap(String path) {
        BitmapFactory.Options opts = new BitmapFactory.Options();

        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, opts);

        opts.inSampleSize = computeSampleSize(opts, -1, 128*128);

        opts.inJustDecodeBounds = false;

         return BitmapFactory.decodeFile(path, opts);
    }

    /**
     * @param options
     * @param minSideLength
     * @param maxNumOfPixels
     * @return
     * 设置恰当的inSampleSize是解决该问题的关键之一。BitmapFactory.Options提供了另一个成员inJustDecodeBounds。
     * 设置inJustDecodeBounds为true后，decodeFile并不分配空间，但可计算出原始图片的长度和宽度，即opts.width和opts.height。
     * 有了这两个参数，再通过一定的算法，即可得到一个恰当的inSampleSize。
     * 查看Android源码，Android提供了下面这种动态计算的方法。
     */
    public int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {

        int initialSize = computeInitialSampleSize(options, minSideLength,   maxNumOfPixels);

        int roundedSize;

        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }


    private  int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {

        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == -1) ? 1 :
        (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));

        int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math.floor(w / minSideLength), Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }

        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }
}

```


**尽量不要使用setImageBitmap或setImageResource或BitmapFactory.decodeResource来设置一张大图，
因为这些函数在完成decode后，最终都是通过java层的createBitmap来完成的，需要消耗更多内存，容易出现OOM异常。**

**因此，改用先通过BitmapFactory.decodeStream方法，创建出一个bitmap，再将其设为ImageView的source，
decodeStream最大的秘密在于其直接调用JNI>>nativeDecodeAsset()来完成decode，无需再使用java层的createBitmap，从而节省了java层的空间。
如果在读取时加上图片的Config参数，可以更有效减少加载的内存，从而更有效阻止抛out of Memory异常。**

**另外，decodeStream直接拿的图片来读取字节码了， 不会根据机器的各种分辨率来自动适应， 使用了decodeStream之后，
需要在hdpi和mdpi，ldpi中配置相应的图片资源， 否则在不同分辨率机器上都是同样大小（像素点数量），显示出来的大小就不对了。**

另外，以下方式也大有帮助：

```java
    InputStream is = this.getResources().openRawResource(R.drawable.pic1);
    BitmapFactory.Options options=new BitmapFactory.Options();
    options.inJustDecodeBounds = false;
    options.inSampleSize = 10;   //width，hight设为原来的十分一
    Bitmap btp =BitmapFactory.decodeStream(is,null,options);
```


```java
if(!bmp.isRecycle() ){
         bmp.recycle()   //回收图片所占的内存
         system.gc()  //提醒系统及时回收
}
```

