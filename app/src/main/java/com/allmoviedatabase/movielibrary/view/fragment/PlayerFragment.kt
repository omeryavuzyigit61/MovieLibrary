package com.allmoviedatabase.movielibrary.view.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.allmoviedatabase.movielibrary.R
import com.allmoviedatabase.movielibrary.databinding.FragmentPlayerBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@AndroidEntryPoint
class PlayerFragment : Fragment() {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!
    private val args: PlayerFragmentArgs by navArgs()

    private var player: ExoPlayer? = null
    private lateinit var audioManager: AudioManager

    private lateinit var gestureDetector: GestureDetector
    private lateinit var scaleGestureDetector: ScaleGestureDetector // Zoom için

    // Kontroller
    private var btnPlayPause: ImageButton? = null
    private var btnRewind: ImageButton? = null
    private var btnForward: ImageButton? = null
    private var btnMute: ImageButton? = null
    private var volumeSeekBar: SeekBar? = null

    private val hideVolumeHandler = Handler(Looper.getMainLooper())
    private val hideVolumeRunnable = Runnable { binding.volumeIndicator.visibility = View.GONE }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        hideSystemUi()

        if (player == null) {
            var videoUrl = args.videoUrl
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
            if (!videoUrl.isNullOrEmpty()) initializePlayer(videoUrl)
            else {
                Toast.makeText(context, "Link Hatası", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        } else {
            binding.playerView.player = player
            updatePlayPauseIcon()
        }

        // KONTROLLERİ BUL VE BAĞLA
        btnPlayPause = binding.playerView.findViewById(R.id.btnPlayPause)
        btnRewind = binding.playerView.findViewById(R.id.btnRewind)
        btnForward = binding.playerView.findViewById(R.id.btnForward)
        btnMute = binding.playerView.findViewById(R.id.btnMute)
        volumeSeekBar = binding.playerView.findViewById(R.id.volumeSeekBar)

        setupCustomControls()
        setupVolumeControls() // Yeni ses kontrolü
        setupGestures()       // Dokunma ve Zoom

        binding.btnClose.setOnClickListener { findNavController().navigateUp() }
    }

    private fun initializePlayer(url: String) {
        player = ExoPlayer.Builder(requireContext()).build()
        binding.playerView.player = player

        val mediaItem = MediaItem.fromUri(url)
        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.playWhenReady = true

        player?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) { updatePlayPauseIcon() }
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    binding.playerView.showController()
                    updatePlayPauseIcon()
                }
            }
        })
    }

    private fun setupCustomControls() {
        btnPlayPause?.setOnClickListener {
            player?.let { exo ->
                if (exo.isPlaying) exo.pause()
                else {
                    if (exo.playbackState == Player.STATE_ENDED) exo.seekTo(0)
                    exo.play()
                }
                updatePlayPauseIcon()
            }
        }
        btnRewind?.setOnClickListener { player?.let { it.seekTo(it.currentPosition - 10000) } }
        btnForward?.setOnClickListener { player?.let { it.seekTo(it.currentPosition + 10000) } }
    }

    // --- YENİ: GÖRSEL SES KONTROLLERİ ---
    private fun setupVolumeControls() {
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        volumeSeekBar?.max = maxVolume
        volumeSeekBar?.progress = currentVolume
        updateMuteIcon(currentVolume)

        // SeekBar Değişimi
        volumeSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
                    updateMuteIcon(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Mute Butonu
        btnMute?.setOnClickListener {
            val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            if (current > 0) {
                // Sesi kapat (Mute)
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
                volumeSeekBar?.progress = 0
                updateMuteIcon(0)
            } else {
                // Sesi aç (Örn: %50)
                val newVol = (maxVolume * 0.5).toInt()
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                volumeSeekBar?.progress = newVol
                updateMuteIcon(newVol)
            }
        }
    }

    private fun updateMuteIcon(volume: Int) {
        if (volume == 0) btnMute?.setImageResource(android.R.drawable.ic_lock_silent_mode)
        else btnMute?.setImageResource(android.R.drawable.ic_lock_silent_mode_off)
    }

    private fun updatePlayPauseIcon() {
        player?.let { exo ->
            if (exo.isPlaying) btnPlayPause?.setImageResource(R.drawable.ic_stop)
            else btnPlayPause?.setImageResource(R.drawable.ic_continue)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupGestures() {
        // 1. PINCH-TO-ZOOM DETECTOR (İki Parmak)
        scaleGestureDetector = ScaleGestureDetector(requireContext(), object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                // Eğer parmaklar açılıyorsa (Scale factor > 1) -> ZOOM
                // Eğer kapanıyorsa (Scale factor < 1) -> FIT
                if (detector.scaleFactor > 1.0f) {
                    binding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    // İstersen kullanıcıya bilgi ver: Toast.makeText(context, "Doldur", Toast.LENGTH_SHORT).show()
                } else if (detector.scaleFactor < 1.0f) {
                    binding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    // Toast.makeText(context, "Sığdır", Toast.LENGTH_SHORT).show()
                }
                return true
            }
        })

        // 2. TAP & SCROLL DETECTOR
        gestureDetector = GestureDetector(requireContext(), object : GestureDetector.SimpleOnGestureListener() {

            // "Uzun basma" hissini yok etmek için onDown true dönmeli
            override fun onDown(e: MotionEvent): Boolean = true

            override fun onDoubleTap(e: MotionEvent): Boolean {
                player?.let { exo ->
                    if (e.x > binding.playerView.width / 2) exo.seekTo(exo.currentPosition + 10000)
                    else exo.seekTo(exo.currentPosition - 10000)
                }
                return true
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                if (binding.playerView.isControllerFullyVisible) binding.playerView.hideController()
                else binding.playerView.showController()
                return true
            }

            override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                // Zoom yapılıyorsa scroll'u iptal et
                if (scaleGestureDetector.isInProgress) return false

                if (abs(distanceY) > abs(distanceX)) {
                    adjustVolume(distanceY > 0)
                    return true
                }
                return false
            }
        })

        binding.playerView.setOnTouchListener { _, event ->
            // Her iki dedektöre de olayı gönder
            scaleGestureDetector.onTouchEvent(event)
            gestureDetector.onTouchEvent(event)

            if (event.action == MotionEvent.ACTION_UP) {
                binding.volumeIndicator.visibility = View.GONE
            }
            true
        }
    }

    private fun adjustVolume(increase: Boolean) {
        hideVolumeHandler.removeCallbacks(hideVolumeRunnable)
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

        val newVolume = if (increase) (currentVolume + 1).coerceAtMost(maxVolume)
        else (currentVolume - 1).coerceAtLeast(0)

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)

        // Hem büyük göstergeyi hem de alttaki slider'ı güncelle
        volumeSeekBar?.progress = newVolume
        updateMuteIcon(newVolume)

        binding.volumeIndicator.visibility = View.VISIBLE
        val percentage = (newVolume.toFloat() / maxVolume.toFloat() * 100).toInt()
        binding.volumeText.text = "$percentage%"

        if (newVolume == 0) binding.volumeIcon.setImageResource(android.R.drawable.ic_lock_silent_mode)
        else binding.volumeIcon.setImageResource(android.R.drawable.ic_lock_silent_mode_off)

        hideVolumeHandler.postDelayed(hideVolumeRunnable, 1500)
    }

    private fun hideSystemUi() {
        val window = requireActivity().window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.root).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun showSystemUi() {
        val window = requireActivity().window
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, binding.root).show(WindowInsetsCompat.Type.systemBars())
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
        showSystemUi()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideVolumeHandler.removeCallbacks(hideVolumeRunnable)
        _binding = null
    }
}