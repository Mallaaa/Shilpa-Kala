package com.shilpakala.app.data

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min

object BrandComposer {

    private const val OUT_W = 1200
    private const val OUT_H = 1500
    private const val PHOTO_SIZE = 980
    private const val PHOTO_MARGIN = 110f
    private const val PHOTO_TOP = 100f
    private const val SEAL_SIZE = 220f
    private const val DESIGN_WIDTH_DP = 360f

    suspend fun compose(sourceBitmap: Bitmap, opts: BrandOptions): Bitmap = withContext(Dispatchers.Default) {
        val out = Bitmap.createBitmap(OUT_W, OUT_H, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val cornerPx = 28f * OUT_W / DESIGN_WIDTH_DP

        drawBackground(canvas, opts.background)
        drawVignette(canvas)

        val photo = centerCropSquare(sourceBitmap, PHOTO_SIZE)
        val photoLeft = PHOTO_MARGIN
        val photoTop = PHOTO_TOP

        drawPhotoShadow(canvas, photoLeft, photoTop, PHOTO_SIZE.toFloat(), cornerPx)
        drawRoundedPhoto(canvas, photo, photoLeft, photoTop, PHOTO_SIZE.toFloat(), cornerPx)

        drawHeritageSeal(canvas)

        drawInfoBar(canvas, opts)

        drawFooter(canvas)

        if (photo !== sourceBitmap) {
            photo.recycle()
        }

        out
    }

    private fun drawBackground(canvas: Canvas, bg: BrandBackground) {
        val (top, bottom) = when (bg) {
            BrandBackground.CREAM -> Color.parseColor("#F8E9C4") to Color.parseColor("#E9C97A")
            BrandBackground.WOOD -> Color.parseColor("#6B3E1F") to Color.parseColor("#3A2010")
            BrandBackground.TERRACOTTA -> Color.parseColor("#C56B3A") to Color.parseColor("#7A2E12")
        }
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f,
                0f,
                0f,
                OUT_H.toFloat(),
                top,
                bottom,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, OUT_W.toFloat(), OUT_H.toFloat(), paint)
    }

    private fun drawVignette(canvas: Canvas) {
        val cx = OUT_W / 2f
        val cy = OUT_H / 2f
        val radius = max(OUT_W, OUT_H) * 0.85f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx,
                cy,
                radius,
                intArrayOf(Color.TRANSPARENT, Color.argb(100, 30, 18, 12)),
                floatArrayOf(0.35f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, OUT_W.toFloat(), OUT_H.toFloat(), paint)
    }

    private fun drawPhotoShadow(
        canvas: Canvas,
        left: Float,
        top: Float,
        size: Float,
        radius: Float
    ) {
        val shadow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(90, 0, 0, 0)
            maskFilter = android.graphics.BlurMaskFilter(28f, android.graphics.BlurMaskFilter.Blur.NORMAL)
        }
        val rect = RectF(left + 6f, top + 14f, left + size + 6f, top + size + 14f)
        canvas.drawRoundRect(rect, radius, radius, shadow)
    }

    private fun drawRoundedPhoto(
        canvas: Canvas,
        bitmap: Bitmap,
        left: Float,
        top: Float,
        size: Float,
        radius: Float
    ) {
        val rect = RectF(left, top, left + size, top + size)
        val path = Path().apply { addRoundRect(rect, radius, radius, Path.Direction.CW) }
        canvas.save()
        canvas.clipPath(path)
        canvas.drawBitmap(bitmap, null, RectF(left, top, left + size, top + size), null)
        canvas.restore()

        val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            color = Color.argb(120, 255, 255, 255)
        }
        canvas.drawRoundRect(rect, radius, radius, border)
    }

    private fun drawHeritageSeal(canvas: Canvas) {
        val left = OUT_W - PHOTO_MARGIN - SEAL_SIZE
        val top = PHOTO_TOP
        val cx = left + SEAL_SIZE / 2f
        val cy = top + SEAL_SIZE / 2f
        val r = SEAL_SIZE / 2f - 6f

        val goldFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx - r * 0.2f,
                cy - r * 0.2f,
                r * 1.1f,
                Color.parseColor("#F5D77E"),
                Color.parseColor("#B8862A"),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(cx, cy, r, goldFill)

        val ringOuter = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 5f
            color = Color.parseColor("#7A5218")
        }
        val ringInner = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = Color.parseColor("#4A300E")
        }
        canvas.drawCircle(cx, cy, r - 3f, ringOuter)
        canvas.drawCircle(cx, cy, r - 14f, ringInner)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#3E2A1A")
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textSize = 26f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("HANDMADE IN", cx, cy - 8f, textPaint)
        textPaint.textSize = 28f
        canvas.drawText("KARNATAKA", cx, cy + 28f, textPaint)
    }

    private fun drawInfoBar(canvas: Canvas, opts: BrandOptions) {
        val barTop = PHOTO_TOP + PHOTO_SIZE + 72f
        val barLeft = 56f
        val barRight = OUT_W - 56f
        val barBottom = barTop + 200f
        val rect = RectF(barLeft, barTop, barRight, barBottom)

        val pill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(230, 255, 255, 255)
        }
        canvas.drawRoundRect(rect, 48f, 48f, pill)

        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = Color.argb(40, 62, 42, 26)
        }
        canvas.drawRoundRect(rect, 48f, 48f, stroke)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#3E2A1A")
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textSize = 48f
        }
        val title = opts.productName.trim().ifBlank { "Heritage piece" }
        val titleX = barLeft + 40f
        val titleY = barTop + 72f
        canvas.drawText(title, titleX, titleY, titlePaint)

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(200, 80, 60, 45)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textSize = 26f
        }
        val wood = opts.woodType.trim().ifBlank { "Craft" }
        val artisan = opts.artisanName.trim().ifBlank { "Artisan" }
        canvas.drawText("$wood · By $artisan", titleX, barTop + 118f, subPaint)

        val priceText = "₹ ${opts.price.trim().ifBlank { "—" }}"
        val pricePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFF7E8")
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textSize = 34f
        }
        val padX = 28f
        val padY = 18f
        val tw = pricePaint.measureText(priceText)
        val priceRect = RectF(
            barRight - tw - padX * 2 - 36f,
            barTop + 36f,
            barRight - 36f,
            barTop + 36f + 56f + padY * 2
        )
        val priceBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#7A2E12")
        }
        canvas.drawRoundRect(priceRect, 32f, 32f, priceBg)
        canvas.drawText(
            priceText,
            priceRect.centerX() - tw / 2f,
            priceRect.centerY() + pricePaint.textSize / 3f,
            pricePaint
        )
    }

    private fun drawFooter(canvas: Canvas) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(160, 62, 42, 26)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textSize = 22f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "SHILPA-KALA · HERITAGE PHOTO STUDIO",
            OUT_W / 2f,
            OUT_H - 40f,
            p
        )
    }

    private fun centerCropSquare(src: Bitmap, size: Int): Bitmap {
        val w = src.width
        val h = src.height
        val d = min(w, h)
        val x = (w - d) / 2
        val y = (h - d) / 2
        val cropped = Bitmap.createBitmap(src, x, y, d, d)
        return if (d == size) {
            cropped
        } else {
            val scaled = Bitmap.createScaledBitmap(cropped, size, size, true)
            if (scaled != cropped) cropped.recycle()
            scaled
        }
    }
}
