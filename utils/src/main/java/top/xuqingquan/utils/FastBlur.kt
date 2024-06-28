package top.xuqingquan.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.view.View
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Created by 许清泉 on 2019/4/16 01:07
 */
fun doBlur(sentBitmap: Bitmap, mRradius: Int, canReuseInBitmap: Boolean): Bitmap {
    var radius = mRradius
    val bitmap: Bitmap = if (canReuseInBitmap) {
        sentBitmap
    } else {
        sentBitmap.copy(sentBitmap.config?:Bitmap.Config.ARGB_8888, true)
    }
    if (radius < 1) {
        radius = 1
    }
    val w = bitmap.width
    val h = bitmap.height
    val pix = IntArray(w * h)
    bitmap.getPixels(pix, 0, w, 0, 0, w, h)
    val wm = w - 1
    val hm = h - 1
    val wh = w * h
    val div = radius + radius + 1
    val r = IntArray(wh)
    val g = IntArray(wh)
    val b = IntArray(wh)
    var rsum: Int
    var gsum: Int
    var bsum: Int
    var x: Int
    var y: Int
    var i: Int
    var p: Int
    var yp: Int
    var yi: Int
    var yw: Int
    val vmin = IntArray(max(w, h))
    var divsum = div + 1 shr 1
    divsum *= divsum
    val dv = IntArray(256 * divsum)
    i = 0
    while (i < 256 * divsum) {
        dv[i] = i / divsum
        i++
    }
    yi = 0
    yw = yi
    val stack = Array(div) { IntArray(3) }
    var stackpointer: Int
    var stackstart: Int
    var sir: IntArray
    var rbs: Int
    val r1 = radius + 1
    var routsum: Int
    var goutsum: Int
    var boutsum: Int
    var rinsum: Int
    var ginsum: Int
    var binsum: Int
    y = 0
    while (y < h) {
        bsum = 0
        gsum = bsum
        rsum = gsum
        boutsum = rsum
        goutsum = boutsum
        routsum = goutsum
        binsum = routsum
        ginsum = binsum
        rinsum = ginsum
        i = -radius
        while (i <= radius) {
            p = pix[yi + min(wm, max(i, 0))]
            sir = stack[i + radius]
            sir[0] = p and 0xff0000 shr 16
            sir[1] = p and 0x00ff00 shr 8
            sir[2] = p and 0x0000ff
            rbs = r1 - abs(i)
            rsum += sir[0] * rbs
            gsum += sir[1] * rbs
            bsum += sir[2] * rbs
            if (i > 0) {
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
            } else {
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
            }
            i++
        }
        stackpointer = radius

        x = 0
        while (x < w) {

            r[yi] = dv[rsum]
            g[yi] = dv[gsum]
            b[yi] = dv[bsum]

            rsum -= routsum
            gsum -= goutsum
            bsum -= boutsum

            stackstart = stackpointer - radius + div
            sir = stack[stackstart % div]

            routsum -= sir[0]
            goutsum -= sir[1]
            boutsum -= sir[2]

            if (y == 0) {
                vmin[x] = min(x + radius + 1, wm)
            }
            p = pix[yw + vmin[x]]

            sir[0] = p and 0xff0000 shr 16
            sir[1] = p and 0x00ff00 shr 8
            sir[2] = p and 0x0000ff

            rinsum += sir[0]
            ginsum += sir[1]
            binsum += sir[2]

            rsum += rinsum
            gsum += ginsum
            bsum += binsum

            stackpointer = (stackpointer + 1) % div
            sir = stack[stackpointer % div]

            routsum += sir[0]
            goutsum += sir[1]
            boutsum += sir[2]

            rinsum -= sir[0]
            ginsum -= sir[1]
            binsum -= sir[2]

            yi++
            x++
        }
        yw += w
        y++
    }
    x = 0
    while (x < w) {
        bsum = 0
        gsum = bsum
        rsum = gsum
        boutsum = rsum
        goutsum = boutsum
        routsum = goutsum
        binsum = routsum
        ginsum = binsum
        rinsum = ginsum
        yp = -radius * w
        i = -radius
        while (i <= radius) {
            yi = max(0, yp) + x

            sir = stack[i + radius]

            sir[0] = r[yi]
            sir[1] = g[yi]
            sir[2] = b[yi]

            rbs = r1 - abs(i)

            rsum += r[yi] * rbs
            gsum += g[yi] * rbs
            bsum += b[yi] * rbs

            if (i > 0) {
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
            } else {
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
            }

            if (i < hm) {
                yp += w
            }
            i++
        }
        yi = x
        stackpointer = radius
        y = 0
        while (y < h) {
            // Preserve alpha channel: ( 0xff000000 & pix[yi] )
            pix[yi] = -0x1000000 and pix[yi] or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]

            rsum -= routsum
            gsum -= goutsum
            bsum -= boutsum

            stackstart = stackpointer - radius + div
            sir = stack[stackstart % div]

            routsum -= sir[0]
            goutsum -= sir[1]
            boutsum -= sir[2]

            if (x == 0) {
                vmin[y] = min(y + r1, hm) * w
            }
            p = x + vmin[y]

            sir[0] = r[p]
            sir[1] = g[p]
            sir[2] = b[p]

            rinsum += sir[0]
            ginsum += sir[1]
            binsum += sir[2]

            rsum += rinsum
            gsum += ginsum
            bsum += binsum

            stackpointer = (stackpointer + 1) % div
            sir = stack[stackpointer]

            routsum += sir[0]
            goutsum += sir[1]
            boutsum += sir[2]

            rinsum -= sir[0]
            ginsum -= sir[1]
            binsum -= sir[2]

            yi += w
            y++
        }
        x++
    }

    bitmap.setPixels(pix, 0, w, 0, 0, w, h)

    return bitmap
}

/**
 * 给 [View] 设置高斯模糊背景图片
 *
 * @param context
 * @param bkg
 * @param view
 */
fun blur(context: Context, bkg: Bitmap, view: View) {
    val startMs = System.currentTimeMillis()
    val radius = 15f
    val scaleFactor = 8f
    //放大到整个view的大小
    val bitmap = getReSizeBitmap(bkg, view.measuredWidth.toFloat(), view.measuredHeight.toFloat())
    var overlay: Bitmap? = Bitmap.createBitmap(
        (view.measuredWidth / scaleFactor).toInt(),
        (view.measuredHeight / scaleFactor).toInt(),
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(overlay!!)
    canvas.translate(-view.left / scaleFactor, -view.top / scaleFactor)
    canvas.scale(1 / scaleFactor, 1 / scaleFactor)
    val paint = Paint()
    paint.flags = Paint.FILTER_BITMAP_FLAG
    canvas.drawBitmap(bitmap, 0f, 0f, paint)
    overlay = doBlur(overlay, radius.toInt(), true)
    view.background = BitmapDrawable(context.resources, overlay)
    Timber.w("cost " + (System.currentTimeMillis() - startMs) + "ms")
}

/**
 * 将 [Bitmap] 高斯模糊并返回
 *
 * @param bkg
 * @param width
 * @param height
 * @return
 */
fun blurBitmap(bkg: Bitmap, width: Int, height: Int): Bitmap? {
    val startMs = System.currentTimeMillis()
    val radius = 15f//越大模糊效果越大
    val scaleFactor = 8f
    //放大到整个view的大小
    val bitmap = getReSizeBitmap(bkg, width.toFloat(), height.toFloat())
    var overlay: Bitmap? = Bitmap.createBitmap(
        (width / scaleFactor).toInt(),
        (height / scaleFactor).toInt(),
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(overlay!!)
    canvas.scale(1 / scaleFactor, 1 / scaleFactor)
    val paint = Paint()
    paint.flags = Paint.FILTER_BITMAP_FLAG
    canvas.drawBitmap(bitmap, 0f, 0f, paint)
    overlay = doBlur(overlay, radius.toInt(), true)
    Timber.w("cost " + (System.currentTimeMillis() - startMs) + "ms")
    return overlay
}

/**
 * 改变Bitmap的长宽
 *
 * @param bitmap
 * @return
 */
fun getReSizeBitmap(bitmap: Bitmap, targetWidth: Float, targetheight: Float): Bitmap {
    var returnBm: Bitmap? = null
    val width = bitmap.width
    val height = bitmap.height
    val matrix = Matrix()
    matrix.postScale(targetWidth / width, targetheight / height) //长和宽放大缩小的比例
    try {
        returnBm = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
    } catch (e: Throwable) {
        e.printStackTrace()
    }

    if (returnBm == null) {
        returnBm = bitmap
    }
    if (bitmap != returnBm) {
        bitmap.recycle()
    }
    return returnBm
}