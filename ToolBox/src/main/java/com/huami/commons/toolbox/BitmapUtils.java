package com.huami.android.commons.toolbox;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;

public class BitmapUtils {

    public static final int COMBINE_INDICATOR_VERTICAL = 0x1;
    public static final int COMBINE_INDICATOR_HORIZONAL = 0x2;
    public static final int COMBINE_INDICATOR_ALIGN = 0x4;
    public static final int MAX_SHARE_IMAGE_SIZE = 250 * 1024;

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        return calculateInSampleSize(options.outWidth, options.outHeight, reqWidth, reqHeight);
    }

    public static int calculateInSampleSize(int rawWidth, int rawHeight, int reqWidth, int reqHeight) {
        if (rawWidth <= reqWidth && rawHeight <= reqHeight) {
            return 1;
        }

        // Calculate ratios of height and width to requested height and
        // width
        final int widthRatio = Math.round((float) rawWidth / (float) reqWidth);
        final int heightRatio = Math.round((float) rawHeight / (float) reqHeight);

        // Choose the smallest ratio as inSampleSize value, this will
        // guarantee
        // a final image with both dimensions larger than or equal to the
        // requested height and width.
        int inSampleSize = heightRatio < widthRatio ? widthRatio : heightRatio;
        return inSampleSize;
    }

    public static Bitmap createCombinedBitmap(Bitmap pic1, Bitmap pic2, int combineIndicator) {
        if ((combineIndicator & COMBINE_INDICATOR_HORIZONAL) != 0
                && (combineIndicator & COMBINE_INDICATOR_VERTICAL) != 0) {
            throw new IllegalArgumentException("combine indicator should not be both horizonal and vertical");
        }

        if (pic1 == null && pic2 == null) {
            return null;
        }

        if (pic1 == null) {
            return pic2;
        }

        if (pic2 == null) {
            return pic1;
        }

        Bitmap bitmap = null;
        if ((combineIndicator & COMBINE_INDICATOR_HORIZONAL) != 0) {
            bitmap = createCombinedBitmapHorizonal(pic1, pic2, (combineIndicator & COMBINE_INDICATOR_ALIGN) != 0);
        }
        if ((combineIndicator & COMBINE_INDICATOR_VERTICAL) != 0) {
            bitmap = createCombinedBitmapVertical(pic1, pic2, (combineIndicator & COMBINE_INDICATOR_ALIGN) != 0);
        }
        if (bitmap == null) {
            return null;
        }
        return bitmap;
        // Bitmap newBitmap = createResizeBitmap(bitmap, reqWidth, reqHeight);
        // if (newBitmap != bitmap) {
        // bitmap.recycle();
        // }
        // return newBitmap;
    }

    public static Bitmap decodeBitmapFromByteArray(byte[] data, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // BitmapFactory.decodeFile(path, options);
        BitmapFactory.decodeByteArray(data, 0, data.length, options);

        // Calculate inSampleSize
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        int inSampleSize = 1;

        if (height > reqHeight) {
            inSampleSize = Math.round((float) height / (float) reqHeight);
        }

        int expectedWidth = width / inSampleSize;

        if (expectedWidth > reqWidth) {
            // if(Math.round((float)width / (float)reqWidth) > inSampleSize)
            // If bigger SampSize..
            inSampleSize = Math.round((float) width / (float) reqWidth);
        }

        options.inSampleSize = inSampleSize;

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeByteArray(data, 0, data.length, options);
    }

    /**
     * Draw {@code pic2} to {@code pic1} at the ({@code left}, {@code top}) point of {@code pic1}.
     *
     * @param pic1
     * @param pic2
     * @param left where pic2 should be drawn at x axis
     * @param top where pic2 should be drawn at y axis
     * @return
     */
    public static Bitmap joinBitmap(Bitmap pic1, Bitmap pic2, int left, int top) {
        if (pic1 == null && pic2 == null) {
            return null;
        }

        if (pic1 == null) {
            return pic2;
        }

        if (pic2 == null) {
            return pic1;
        }

        if (!pic1.isMutable()) {
            Bitmap oldPic1 = pic1;
            pic1 = pic1.copy(Bitmap.Config.ARGB_8888, true);
            if (pic1 != null) {
                oldPic1.recycle();
            }
        }
        Paint paint = new Paint();
        Canvas canvas = new Canvas(pic1);
        canvas.drawBitmap(pic2, left, top, paint);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        return pic1;
    }

    public static Bitmap joinBitmap(Bitmap pic1, Bitmap pic2, int left, int top, int reqWidth, int reqHeight) {
        if (pic1 == null && pic2 == null) {
            return null;
        }

        if (pic1 == null) {
            return pic2;
        }

        if (pic2 == null) {
            return pic1;
        }

        if (!pic1.isMutable()) {
            Bitmap oldPic1 = pic1;
            pic1 = pic1.copy(Bitmap.Config.ARGB_8888, true);
            if (pic1 != null) {
                oldPic1.recycle();
            }
        }
        Paint paint = new Paint();
        Canvas canvas = new Canvas(pic1);
        canvas.drawBitmap(pic2, left, top, paint);
        Bitmap newBitmap = createResizeBitmap(pic1, reqWidth, reqHeight);
        if (pic1 != newBitmap) {
            pic1.recycle();
        }
        return newBitmap;
    }

    /**
     * Save bitmap to local disk
     * @param imagePath
     * @param bmp
     * @param quality
     * @return success or not
     */
    public static boolean saveBitmapToFile(String imagePath, Bitmap bmp, int quality) {
        boolean result = false;

        // Delete bitmap file existed
        File f = new File(imagePath);
        if (f.exists()) {
            f.delete();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        int options = quality;
        // Compress the image when the image is bigger than 250k
        while ((baos.toByteArray().length > MAX_SHARE_IMAGE_SIZE) && (options > 0)) {

            baos.reset();
            options -= 10;
            bmp.compress(Bitmap.CompressFormat.JPEG, options, baos);
        }

        // Compress bitmap to file
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
            fos.write(baos.toByteArray());
            fos.flush();
        } catch (Exception e) {
            // ignore
            return false;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {}
            }
        }

        return true;
    }

    /**
     * Save bitmap to local disk
     * @param imagePath
     * @param bmp
     * @return success or not
     */
    public static boolean saveBitmapToPngFile(String imagePath, Bitmap bmp) {
        if (TextUtils.isEmpty(imagePath) || bmp == null) {
            return false;
        }
        boolean result = false;

        // Delete bitmap file existed
        File f = new File(imagePath);
        if (f.exists()) {
            f.delete();
        }

        // Compress bitmap to file
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
            result = bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            // ignore
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {}
            }
        }

        return result;
    }

    private static Bitmap createCombinedBitmapHorizonal(Bitmap pic1, Bitmap pic2, boolean shouldAlign) {
        int width1 = pic1.getWidth();
        int height1 = pic1.getHeight();

        int width2 = pic2.getWidth();
        int height2 = pic2.getHeight();

        if (height1 != height2 && shouldAlign) {
            if (height1 > height2) {
                width2 = (int) (width2 * (float) height1 / height2);
                height2 = height1;
            } else {
                width1 = (int) (width1 * (float) height2 / height1);
                height1 = height2;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width1 + width2, Math.max(height1, height2), Bitmap.Config.ARGB_8888);
        Paint paint = new Paint();
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(pic1, new Rect(0, 0, width1, height1), new Rect(0, 0, width1, height1), paint);
        canvas.drawBitmap(pic2, new Rect(0, 0, width2, height2), new Rect(width1, 0, width1 + width2, height2), paint);

        return bitmap;
    }

    private static Bitmap createCombinedBitmapVertical(Bitmap pic1, Bitmap pic2, boolean shouldAlign) {
        int width1 = pic1.getWidth();
        int height1 = pic1.getHeight();

        int width2 = pic2.getWidth();
        int height2 = pic2.getHeight();

        if (width1 != width2 && shouldAlign) {
            if (width1 > width2) {
                height2 = (int) (height2 * (float) width1 / width2);
                width2 = width1;
            } else {
                height1 = (int) (height1 * (float) width2 / width1);
                width1 = width2;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(Math.max(width1, width2), height1 + height2, Bitmap.Config.ARGB_8888);
        Paint paint = new Paint();
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(pic1, new Rect(0, 0, width1, height1), new Rect(0, 0, width1, height1), paint);
        canvas.drawBitmap(pic2, new Rect(0, 0, width2, height2), new Rect(0, height1, width2, height1 + height2), paint);

        return bitmap;
    }

    /**
     * 新建一个修改了尺寸之后的bitmap
     * @param bitmap
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private static Bitmap createResizeBitmap(Bitmap bitmap, int reqWidth, int reqHeight) {

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = (float) reqWidth / width;
        float scaleHeight = (float) reqHeight / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
        return resizedBitmap;

        // int rawWidth = bitmap.getWidth();
        // int rawHeight = bitmap.getHeight();
        // if (rawWidth == reqWidth && rawHeight == reqHeight) {
        // return bitmap;
        // }
        // int inSampleSize = calculateInSampleSize(rawWidth, rawHeight,
        // reqWidth, reqHeight);

        // return Bitmap.createScaledBitmap(bitmap, rawWidth / inSampleSize,
        // rawHeight / inSampleSize, true);
    }

    private BitmapUtils() {}
}
