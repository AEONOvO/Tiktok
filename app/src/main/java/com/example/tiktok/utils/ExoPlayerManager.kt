package com.example.tiktok.utils

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource


class ExoPlayerManager(private val context: Context) {

    private var player: ExoPlayer? = null

    /**
     * 初始化播放器
     */
    fun initPlayer(): ExoPlayer {
        if (player == null) {
            player = ExoPlayer.Builder(context)
                .build()
                .apply {
                    repeatMode = Player.REPEAT_MODE_ONE  // 单曲循环
                    playWhenReady = false  // 初始不自动播放
                }
        }
        return player!!
    }

    /**
     * 设置视频资源
     */
    @OptIn(UnstableApi::class)
    fun setVideoSource(videoResId: String) {
        player?.let {
            // 如果是本地资源 ID（R.raw.xxx）
            val uri = RawResourceDataSource.buildRawResourceUri(videoResId.toInt())
            val mediaItem = MediaItem.fromUri(uri)

            val dataSourceFactory = DefaultDataSource.Factory(context)
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mediaItem)

            it.setMediaSource(mediaSource)
            it.prepare()
        }
    }

    /**
     * 播放
     */
    fun play() {
        player?.play()
    }

    /**
     * 暂停
     */
    fun pause() {
        player?.pause()
    }

    /**
     * 停止
     */
    fun stop() {
        player?.stop()
    }

    /**
     * 释放资源
     */
    fun release() {
        player?.release()
        player = null
    }

    /**
     * 是否正在播放
     */
    fun isPlaying(): Boolean {
        return player?.isPlaying ?: false
    }

    /**
     * 获取播放器实例
     */
    fun getPlayer(): ExoPlayer? {
        return player
    }
}