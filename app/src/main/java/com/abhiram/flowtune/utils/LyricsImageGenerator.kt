package com.abhiram.flowtune.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.withClip
import androidx.core.graphics.withTranslation
import coil.ImageLoader
import coil.request.ImageRequest
import com.abhiram.flowtune.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

object ComposeToImage {

    fun saveBitmapAsFile(context: Context, bitmap: Bitmap, fileName: String): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveToMediaStore(context, bitmap, fileName)
        } else {
            saveToCache(context, bitmap, fileName)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveToMediaStore(context: Context, bitmap: Bitmap, fileName: String): Uri {
        val contentValues = buildContentValues(fileName)
        val uri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ) ?: throw IllegalStateException("Failed to create new MediaStore record")

        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        }
        return uri
    }

    private fun buildContentValues(fileName: String): ContentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, "$fileName.png")
        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/OpenTune")
    }

    private fun saveToCache(context: Context, bitmap: Bitmap, fileName: String): Uri {
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val imageFile = File(cachePath, "$fileName.png")

        FileOutputStream(imageFile).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        }

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.FileProvider",
            imageFile
        )
    }



    private data class ImageConfig(
        val cardSize: Int,
        val padding: Float = 32f,
        val cornerRadius: Float = 20f,
        val imageCornerRadius: Float = 12f,
        val coverArtRatio: Float = 0.15f,
        val logoRatio: Float = 0.05f
    )

    private data class TextConfig(
        val titleSizeRatio: Float = 0.045f,
        val artistSizeRatio: Float = 0.035f,
        val lyricsSizeRatio: Float = 0.08f,
        val appNameSizeRatio: Float = 0.042f,
        val maxLyricsWidthRatio: Float = 0.85f,
        val lineSpacing: Float = 8f,
        val lineSpacingMultiplier: Float = 1.3f,
        val letterSpacing: Float = 0.01f
    )

    private data class ColorScheme(
        val backgroundColor: Int,
        val mainTextColor: Int,
        val secondaryTextColor: Int
    ) {
        companion object {
            fun createDefault() = ColorScheme(
                backgroundColor = 0xFF121212.toInt(),
                mainTextColor = 0xFFFFFFFF.toInt(),
                secondaryTextColor = 0xB3FFFFFF.toInt()
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    suspend fun createLyricsImage(
        context: Context,
        coverArtUrl: String?,
        songTitle: String,
        artistName: String,
        lyrics: String,
        width: Int,
        height: Int,
        backgroundColor: Int? = null,
        textColor: Int? = null,
        secondaryTextColor: Int? = null
    ): Bitmap = withContext(Dispatchers.Default) {
        val config = ImageConfig(cardSize = calculateOptimalSize(width, height))
        val colorScheme = buildColorScheme(backgroundColor, textColor, secondaryTextColor)

        val bitmap = initializeBitmap(config.cardSize)
        val canvas = Canvas(bitmap)

        renderBackground(canvas, config, colorScheme.backgroundColor)

        val coverArt = loadCoverArtwork(context, coverArtUrl)
        renderCoverArt(canvas, coverArt, config)

        renderSongMetadata(canvas, songTitle, artistName, config, colorScheme)
        renderLyricsContent(canvas, lyrics, config, colorScheme)

        renderAppLogo(context, canvas, config.cardSize, config.padding, colorScheme.secondaryTextColor, colorScheme.backgroundColor)

        bitmap
    }

    private fun calculateOptimalSize(width: Int, height: Int): Int = min(width, height) - 32

    private fun buildColorScheme(
        backgroundColor: Int?,
        textColor: Int?,
        secondaryTextColor: Int?
    ): ColorScheme {
        val defaultScheme = ColorScheme.createDefault()
        return ColorScheme(
            backgroundColor = backgroundColor ?: defaultScheme.backgroundColor,
            mainTextColor = textColor ?: defaultScheme.mainTextColor,
            secondaryTextColor = secondaryTextColor ?: defaultScheme.secondaryTextColor
        )
    }

    private fun initializeBitmap(size: Int): Bitmap = createBitmap(size, size)

    private fun renderBackground(canvas: Canvas, config: ImageConfig, backgroundColor: Int) {
        val paint = Paint().apply {
            color = backgroundColor
            isAntiAlias = true
        }
        val rect = RectF(0f, 0f, config.cardSize.toFloat(), config.cardSize.toFloat())
        canvas.drawRoundRect(rect, config.cornerRadius, config.cornerRadius, paint)
    }

    private suspend fun loadCoverArtwork(context: Context, coverArtUrl: String?): Bitmap? {
        if (coverArtUrl.isNullOrEmpty()) return null
        return try {
            val imageLoader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(coverArtUrl)
                .size(256)
                .allowHardware(false)
                .build()
            val result = imageLoader.execute(request)
            result.drawable?.toBitmap(256, 256, Bitmap.Config.ARGB_8888)
        } catch (e: Exception) {
            null
        }
    }

    private fun renderCoverArt(canvas: Canvas, coverArt: Bitmap?, config: ImageConfig) {
        coverArt?.let { artwork ->
            val size = config.cardSize * config.coverArtRatio
            val rect = RectF(config.padding, config.padding, config.padding + size, config.padding + size)
            val clipPath = Path().apply {
                addRoundRect(rect, config.imageCornerRadius, config.imageCornerRadius, Path.Direction.CW)
            }
            canvas.withClip(clipPath) {
                drawBitmap(artwork, null, rect, null)
            }
        }
    }

    private fun renderSongMetadata(
        canvas: Canvas,
        songTitle: String,
        artistName: String,
        config: ImageConfig,
        colorScheme: ColorScheme
    ) {
        val textConfig = TextConfig()
        val coverArtSize = config.cardSize * config.coverArtRatio

        val titlePaint = TextPaint().apply {
            color = colorScheme.mainTextColor
            textSize = config.cardSize * textConfig.titleSizeRatio
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        val artistPaint = TextPaint().apply {
            color = colorScheme.secondaryTextColor
            textSize = config.cardSize * textConfig.artistSizeRatio
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }

        val maxWidth = config.cardSize - (config.padding * 2 + coverArtSize + 16f)
        val startX = config.padding + coverArtSize + 16f

        val titleLayout = StaticLayout.Builder.obtain(songTitle, 0, songTitle.length, titlePaint, maxWidth.toInt())
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setMaxLines(1)
            .build()

        val artistLayout = StaticLayout.Builder.obtain(artistName, 0, artistName.length, artistPaint, maxWidth.toInt())
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setMaxLines(1)
            .build()

        val centerY = config.padding + coverArtSize / 2f
        val textBlockHeight = titleLayout.height + artistLayout.height + 8f
        val startY = centerY - textBlockHeight / 2f

        canvas.withTranslation(startX, startY) {
            titleLayout.draw(this)
            translate(0f, titleLayout.height.toFloat() + 8f)
            artistLayout.draw(this)
        }
    }

    private fun renderLyricsContent(
        canvas: Canvas,
        lyrics: String,
        config: ImageConfig,
        colorScheme: ColorScheme
    ) {
        val textConfig = TextConfig()
        val alignment = determineMixedTextAlignment(lyrics)

        val paint = TextPaint().apply {
            color = colorScheme.mainTextColor
            textSize = config.cardSize * textConfig.lyricsSizeRatio
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        // Ajuste dinámico del tamaño de texto para que encaje en el área
        var textSize = paint.textSize
        val maxWidth = (config.cardSize * textConfig.maxLyricsWidthRatio).toInt()
        val maxHeight = config.cardSize * 0.5f
        val minTextSize = 24f

        var layout: StaticLayout

        do {
            paint.textSize = textSize
            layout = StaticLayout.Builder.obtain(lyrics, 0, lyrics.length, paint, maxWidth)
                .setAlignment(alignment)
                .setIncludePad(false)
                .setLineSpacing(textConfig.lineSpacing, textConfig.lineSpacingMultiplier)
                .build()
            if (layout.height <= maxHeight) break
            textSize -= 3f
        } while (textSize > minTextSize)

        if (textSize < minTextSize) {
            paint.textSize = minTextSize
            layout = StaticLayout.Builder.obtain(lyrics, 0, lyrics.length, paint, maxWidth)
                .setAlignment(alignment)
                .setIncludePad(false)
                .setLineSpacing(textConfig.lineSpacing, textConfig.lineSpacingMultiplier)
                .build()
        }

        val headerHeight = config.padding + (config.cardSize * config.coverArtRatio) + 32f
        val footerHeight = 80f
        val availableHeight = config.cardSize - headerHeight - footerHeight

        val posX = (config.cardSize - layout.width) / 2f
        val posY = headerHeight + (availableHeight - layout.height) / 2f

        canvas.withTranslation(posX, posY) {
            layout.draw(this)
        }
    }

    private fun renderAppLogo(
        context: Context,
        canvas: Canvas,
        cardSize: Int,
        padding: Float,
        logoColor: Int,
        backgroundColor: Int
    ) {
        val logoSize = cardSize * 0.05f

        val logoBitmap = getBitmapFromVectorDrawable(
            context, R.drawable.opentune, logoSize.toInt(), logoSize.toInt()
        ) ?: return

        // Pintar el logo
        val paint = Paint().apply {
            colorFilter = PorterDuffColorFilter(logoColor, PorterDuff.Mode.SRC_IN)
            isAntiAlias = true
        }

        val logoX = padding
        val logoY = cardSize - padding - logoSize

        canvas.drawBitmap(logoBitmap, logoX, logoY, paint)

        // Obtener el nombre de la app
        val appName = context.getString(R.string.app_name)

        // Configurar la pintura del texto
        val textPaint = TextPaint().apply {
            color = logoColor
            textSize = cardSize * 0.042f
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            isAntiAlias = true
            letterSpacing = 0.01f
        }

        // Medir altura para ajustar alineación vertical
        val textBounds = Rect()
        textPaint.getTextBounds(appName, 0, appName.length, textBounds)
        val textHeight = textBounds.height()

        val textX = logoX + logoSize + 12f
        val textY = logoY + logoSize / 2f + textHeight / 2f - 4f

        // Dibujar el nombre de la app
        canvas.drawText(appName, textX, textY, textPaint)
    }


    private fun getBitmapFromVectorDrawable(
        context: Context,
        drawableId: Int,
        width: Int,
        height: Int
    ): Bitmap? {
        val drawable = context.getDrawable(drawableId) ?: return null
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun determineMixedTextAlignment(text: String): Layout.Alignment {
        val hasRTL = text.any { Character.getDirectionality(it).toInt() == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC.toInt() }
        return if (hasRTL) Layout.Alignment.ALIGN_CENTER else Layout.Alignment.ALIGN_NORMAL
    }
}
