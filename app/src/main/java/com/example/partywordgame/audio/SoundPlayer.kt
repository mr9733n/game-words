package com.example.partywordgame.audio

import android.content.Context
import android.media.AudioManager
import android.media.RingtoneManager
import android.media.ToneGenerator

class SoundPlayer(
    private val context: Context
) {
    fun playNotificationSound() {
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context, uri)

            if (ringtone != null) {
                ringtone.play()
                return
            }
        } catch (_: Exception) {
            // fallback below
        }

        try {
            val tone = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
            tone.startTone(ToneGenerator.TONE_PROP_BEEP, 1000)
        } catch (_: Exception) {
            // ignore
        }
    }
}