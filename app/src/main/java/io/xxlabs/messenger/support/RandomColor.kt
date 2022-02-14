package io.xxlabs.messenger.support

import android.graphics.Color
import timber.log.Timber
import java.math.BigInteger


class RandomColor {
    companion object {
        val colorsList = listOf(
            "#037281", //brand_dark
            "#0DB9CB", //brand_default
            "#00D3ED", //brand_dark_mode
            "#C0E2E3", //brand_light
            "#F6F4F4", //brand_background
            "#242424", //neutral_active
            "#373737", //neutral_dark
            "#3D3D3D", //neutral_body
            "#A4A4A4", //neutral_weak
            "#ADB5BD", //neutral_disabled
            "#EDEDED", //neutral_line
            "#F7F7FC", //neutral_off_white
            "#D12D4B", //accent_danger
            "#FDCF41", //accent_warning
            "#2CC069", //accent_success
            "#FE7751"  //accent_safe
        )

        val lightColors = listOf(
            "#ADB5BD", //neutral_disabled
            "#EDEDED", //neutral_line
            "#F7F7FC", //neutral_off_white
        )

        fun getRandomColor(userId: ByteArray): Pair<Int, Boolean> {
            var big = BigInteger(userId)
            if (big < BigInteger.ZERO) {
                big = big.multiply(-BigInteger.ONE)
            }

            val length = BigInteger(userId).bitLength()
            val lsb = BigInteger(userId).lowestSetBit
            Timber.v("[RANDOM COLOR] Entire number: $big")
            Timber.v("[RANDOM COLOR] Big: $length")
            Timber.v("[RANDOM COLOR] lsb: $lsb")
            val mod = big.multiply(length.toBigInteger()).mod(colorsList.size.toBigInteger())
            Timber.v("[RANDOM COLOR] Color mod result: $mod")
            val result = colorsList[mod.toInt()]
            Timber.v("[RANDOM COLOR] Picked color: $result")
            val color = Color.parseColor(result)
            val isLight = isLight(result)
            Timber.v("[RANDOM COLOR] Is light color: $isLight")
            return Pair(color, isLight)
        }

        private fun isLight(color: String): Boolean {
           return lightColors.contains(color)
        }
    }
}