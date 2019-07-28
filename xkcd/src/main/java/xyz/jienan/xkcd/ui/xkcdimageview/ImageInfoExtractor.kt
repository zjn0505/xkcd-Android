package xyz.jienan.xkcd.ui.xkcdimageview

import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import kotlin.experimental.and

/**
 * Created by Piasy{github.com/Piasy} on 2018/8/12.
 */

object ImageInfoExtractor {

    const val TYPE_STILL_IMAGE = 0
    const val TYPE_GIF = 1
    const val TYPE_ANIMATED_WEBP = 2
    const val TYPE_STILL_WEBP = 3
    const val TYPE_BITMAP = 4

    private const val ANIMATED_WEBP_MASK: Byte = 0x02

    /**
     * For GIF, we only need 3 bytes, 'GIF',
     * For WebP, we need 12 bytes, 'RIFF' + size + 'WEBP',
     * to determine still/animated WebP, we need 5 extra bytes, 4 bytes chunk header to check
     * for extended WebP format, 1 byte to check for animated bit.
     *
     * reference: https://developers.google.com/speed/webp/docs/riff_container
     */
    fun getImageType(file: File): Int {
        var type = TYPE_STILL_IMAGE
        try {
            val inputStream = FileInputStream(file)

            val header = ByteArray(20)
            val read = inputStream.read(header)
            if (read >= 3 && isGifHeader(header)) {
                type = TYPE_GIF
            } else if (read >= 12 && isWebpHeader(header)) {
                if (read >= 17 && isExtendedWebp(header)
                        && (header[16] and ANIMATED_WEBP_MASK).compareTo(0) != 0) {
                    type = TYPE_ANIMATED_WEBP
                } else {
                    type = TYPE_STILL_WEBP
                }
            }

            inputStream.close()
        } catch (e: IOException) {
            Timber.e(e)
        }

        return type
    }

    fun typeName(type: Int) = when (type) {
        TYPE_GIF -> "GIF"
        TYPE_STILL_WEBP -> "STILL_WEBP"
        TYPE_ANIMATED_WEBP -> "ANIMATED_WEBP"
        TYPE_STILL_IMAGE -> "STILL_IMAGE"
        else -> "STILL_IMAGE"
    }


    private fun isGifHeader(header: ByteArray) =
            header[0] == 'G'.toByte() && header[1] == 'I'.toByte() && header[2] == 'F'.toByte()

    private fun isWebpHeader(header: ByteArray) =
            header[0] == 'R'.toByte()
                    && header[1] == 'I'.toByte()
                    && header[2] == 'F'.toByte()
                    && header[3] == 'F'.toByte()
                    && header[8] == 'W'.toByte()
                    && header[9] == 'E'.toByte()
                    && header[10] == 'B'.toByte()
                    && header[11] == 'P'.toByte()


    private fun isExtendedWebp(header: ByteArray) =
        header[12] == 'V'.toByte()
                && header[13] == 'P'.toByte()
                && header[14] == '8'.toByte()
                && header[15] == 'X'.toByte()

}