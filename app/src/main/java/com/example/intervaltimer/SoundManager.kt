package com.example.intervaltimer

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

object SoundManager {
    private var soundPool: SoundPool? = null
    private var saveSoundId: Int = 0
    private var isInitialized = false
    private var lastSecondSoundId: Int = 0
    fun init(context: Context) {
        if (isInitialized) return

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(audioAttributes)
            .build()

        // Загружаем звук сохранения
        saveSoundId = try {
            soundPool?.load(context, R.raw.save_sound, 1) ?: 0
        } catch (e: Exception) {
            0
        }
        lastSecondSoundId = try {
            soundPool?.load(context, R.raw.last_beep, 1) ?: 0
        } catch (e: Exception) {
            0
        }

        isInitialized = true
    }
    fun playLastSecondSound() {
        if (lastSecondSoundId != 0) {
            soundPool?.play(lastSecondSoundId, 1.0f, 1.0f, 0, 0, 1.0f)
        }
    }
    fun playSaveSound() {
        if (saveSoundId != 0) {
            soundPool?.play(saveSoundId, 1.0f, 1.0f, 0, 0, 1.0f)
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        isInitialized = false
    }
}