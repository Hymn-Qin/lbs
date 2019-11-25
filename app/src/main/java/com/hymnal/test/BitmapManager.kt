package com.hymnal.test

import android.content.res.Resources
import android.graphics.*
import android.graphics.Bitmap.createBitmap
import android.graphics.drawable.shapes.RoundRectShape
import kotlin.properties.Delegates


class BitmapManager {
    private val matrix = Matrix()

    var bitmapBuilder: ((Bitmap) -> Unit)? = null

    var source by Delegates.notNull<Int>()

    /**
     * 按比例缩放图片
     * [scale]  比例
     */
    var scale: Float? = null
        set(value) {
            field = value
            value?.let { matrix.postScale(it, it) }
        }

    var roundPx: Float? = null


    var roundRectShape: RoundRectShape? = null


    /**
     * 选择变换
     * [alpha]  旋转角度，可正可负
     */
    var alpha: Float? = null
        set(value) {
            field = value
            value?.let { matrix.setRotate(it) }
        }

    /**
     * 偏移效果
     *  [skew] 偏移坐标距离
     */

    var skew: PointF? = null
        set(value) {
            field = value
            value?.let { matrix.postSkew(-it.x, -it.y) }
        }


    fun createBitmap(resources: Resources): Bitmap {
        return BitmapFactory.decodeResource(resources, source).let {

            it.transform()
        }
    }

    private fun Bitmap.transform(): Bitmap {
        val bitmap = createBitmap(this, 0, 0, width, height, matrix, false)
        recycle()
        return bitmap
    }


    /**
     * 裁剪
     *
     */
    fun Bitmap.crop(): Bitmap {
        var cropWidth = if (width >= height) height else width// 裁切后所取的正方形区域边长
        cropWidth /= 2
        val cropHeight = (cropWidth / 1.2).toInt()
        val cropBitmap = createBitmap(this, width / 3, 0, cropWidth, cropHeight, null, false)
        recycle()
        return cropBitmap
    }
}

fun Resources.bitmap(action: BitmapManager.() -> Unit) =
    BitmapManager().apply(action).let { it.createBitmap(this) }

/**
 * 自定义裁剪，根据第一个像素点(左上角)X和Y轴坐标和需要的宽高来裁剪
 * @param srcBitmap
 * @param firstPixelX
 * @param firstPixelY
 * @param needWidth
 * @param needHeight
 * @param recycleSrc
 * @return
 */
fun cropBitmapCustom(
    srcBitmap: Bitmap,
    firstPixelX: Int,
    firstPixelY: Int,
    needWidth: Int,
    needHeight: Int,
    recycleSrc: Boolean
): Bitmap {
    var needWidth = needWidth
    var needHeight = needHeight

    if (firstPixelX + needWidth > srcBitmap.width) {
        needWidth = srcBitmap.width - firstPixelX
    }

    if (firstPixelY + needHeight > srcBitmap.height) {
        needHeight = srcBitmap.height - firstPixelY
    }
    /**裁剪关键步骤 */
    val cropBitmap = Bitmap.createBitmap(srcBitmap, firstPixelX, firstPixelY, needWidth, needHeight)
    /**回收之前的Bitmap */
    srcBitmap.recycle()

    return cropBitmap
}

//圆角矩形
fun getRoundedCornerBitmap(bitmap: Bitmap, roundPx: Float): Bitmap {

    val output = Bitmap.createBitmap(
        bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(output)

    val color = -0xbdbdbe
    val paint = Paint()
    val rect = Rect(0, 0, bitmap.width, bitmap.height)
    val rectF = RectF(rect)

    paint.isAntiAlias = true
    canvas.drawARGB(0, 0, 0, 0)
    paint.color = color

    //
    canvas.drawRoundRect(rectF, roundPx, roundPx, paint)

    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(bitmap, rect, rect, paint)

    return output
}

//圆形
fun getOvalBitmap(bitmap: Bitmap): Bitmap {

    val output = Bitmap.createBitmap(
        bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(output)

    val color = -0xbdbdbe
    val paint = Paint()
    val rect = Rect(0, 0, bitmap.width, bitmap.height)

    val rectF = RectF(rect)

    paint.isAntiAlias = true
    canvas.drawARGB(0, 0, 0, 0)
    paint.color = color
    //
    canvas.drawOval(rectF, paint)

    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(bitmap, rect, rect, paint)
    return output
}
