package com.doctorsplaza.app.ui.videoCall

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.ActivityVideoBinding
import com.doctorsplaza.app.ui.patient.fragments.appointments.AppointmentViewModel
import com.doctorsplaza.app.ui.patient.loginSignUp.PatientLoginSignup
import com.doctorsplaza.app.utils.*
import com.google.gson.JsonObject
import com.twilio.audioswitch.AudioDevice
import com.twilio.audioswitch.AudioDevice.*
import com.twilio.audioswitch.AudioSwitch
import com.twilio.video.*
import com.twilio.video.ktx.Video.connect
import com.twilio.video.ktx.createLocalAudioTrack
import com.twilio.video.ktx.createLocalVideoTrack
import dagger.hilt.android.AndroidEntryPoint
import tvi.webrtc.VideoSink
import javax.inject.Inject
import kotlin.properties.Delegates


@SuppressLint("SetTextI18n")
@AndroidEntryPoint
class VideoActivity : AppCompatActivity() {

    private var appointmentId: String = ""
    private lateinit var binding: ActivityVideoBinding

    @Inject
    lateinit var session: SessionManager

    private val appointmentViewModel: AppointmentViewModel by viewModels()

    private val CAMERA_MIC_PERMISSION_REQUEST_CODE = 1
    private val TAG = "VideoActivity"
    private val CAMERA_PERMISSION_INDEX = 0
    private val MIC_PERMISSION_INDEX = 1


    private lateinit var accessToken: String

    private var room: Room? = null
    private var localParticipant: LocalParticipant? = null

    companion object {
        const val PREF_AUDIO_CODEC = "audio_codec"
        const val PREF_AUDIO_CODEC_DEFAULT = OpusCodec.NAME
        const val PREF_VIDEO_CODEC = "video_codec"
        const val PREF_VIDEO_CODEC_DEFAULT = Vp8Codec.NAME
        const val PREF_SENDER_MAX_AUDIO_BITRATE = "sender_max_audio_bitrate"
        const val PREF_SENDER_MAX_AUDIO_BITRATE_DEFAULT = "0"
        const val PREF_SENDER_MAX_VIDEO_BITRATE = "sender_max_video_bitrate"
        const val PREF_SENDER_MAX_VIDEO_BITRATE_DEFAULT = "0"
        const val PREF_VP8_SIMULCAST = "vp8_simulcast"
        const val PREF_VP8_SIMULCAST_DEFAULT = false
        const val PREF_ENABLE_AUTOMATIC_SUBSCRIPTION = "enable_automatic_subscription"
        const val PREF_ENABLE_AUTOMATIC_SUBCRIPTION_DEFAULT = true
        val VIDEO_CODEC_NAMES = arrayOf(Vp8Codec.NAME, H264Codec.NAME, Vp9Codec.NAME)
        val AUDIO_CODEC_NAMES =
            arrayOf(IsacCodec.NAME, OpusCodec.NAME, PcmaCodec.NAME, PcmuCodec.NAME, G722Codec.NAME)
    }

    /*
     * AudioCodec and VideoCodec represent the preferred codec for encoding and decoding audio and
     * video.
     */
    private val audioCodec: AudioCodec
        get() {
            val audioCodecName = sharedPreferences.getString(
                PREF_AUDIO_CODEC,
                PREF_AUDIO_CODEC_DEFAULT
            )

            return when (audioCodecName) {
                IsacCodec.NAME -> IsacCodec()
                OpusCodec.NAME -> OpusCodec()
                PcmaCodec.NAME -> PcmaCodec()
                PcmuCodec.NAME -> PcmuCodec()
                G722Codec.NAME -> G722Codec()
                else -> OpusCodec()
            }
        }
    private val videoCodec: VideoCodec
        get() {
            val videoCodecName = sharedPreferences.getString(
                PREF_VIDEO_CODEC,
                PREF_VIDEO_CODEC_DEFAULT
            )

            return when (videoCodecName) {
                Vp8Codec.NAME -> {
                    val simulcast = sharedPreferences.getBoolean(
                        PREF_VP8_SIMULCAST,
                        PREF_VP8_SIMULCAST_DEFAULT
                    )
                    Vp8Codec(simulcast)
                }
                H264Codec.NAME -> H264Codec()
                Vp9Codec.NAME -> Vp9Codec()
                else -> Vp8Codec()
            }
        }

    private val enableAutomaticSubscription: Boolean
        get() {
            return sharedPreferences.getBoolean(
                PREF_ENABLE_AUTOMATIC_SUBSCRIPTION,
                PREF_ENABLE_AUTOMATIC_SUBCRIPTION_DEFAULT
            )
        }

    /*
     * Encoding parameters represent the sender side bandwidth constraints.
     */
    private val encodingParameters: EncodingParameters
        get() {
            val defaultMaxAudioBitrate = PREF_SENDER_MAX_AUDIO_BITRATE_DEFAULT
            val defaultMaxVideoBitrate = PREF_SENDER_MAX_VIDEO_BITRATE_DEFAULT
            val maxAudioBitrate = Integer.parseInt(
                sharedPreferences.getString(
                    PREF_SENDER_MAX_AUDIO_BITRATE,
                    defaultMaxAudioBitrate
                ) ?: defaultMaxAudioBitrate
            )
            val maxVideoBitrate = Integer.parseInt(
                sharedPreferences.getString(
                    PREF_SENDER_MAX_VIDEO_BITRATE,
                    defaultMaxVideoBitrate
                ) ?: defaultMaxVideoBitrate
            )

            return EncodingParameters(maxAudioBitrate, maxVideoBitrate)
        }

    /*
     * Room events listener
     */
    private val roomListener = object : Room.Listener {
        override fun onConnected(room: Room) {
            localParticipant = room.localParticipant
            binding.videoStatusTextView.text = "Connected to ${room.name}"
            title = room.name

            // Only one participant is supported
            room.remoteParticipants.firstOrNull()?.let { addRemoteParticipant(it) }
        }

        override fun onReconnected(room: Room) {
            binding.videoStatusTextView.text = "Connected to ${room.name}"
            binding.reconnectingProgressBar.visibility = View.GONE
        }

        override fun onReconnecting(room: Room, twilioException: TwilioException) {
            binding.videoStatusTextView.text = "Reconnecting to ${room.name}"
            binding.reconnectingProgressBar.visibility = View.VISIBLE
        }

        override fun onConnectFailure(room: Room, e: TwilioException) {
            Log.e("==Connection Failure==", e.message.toString())
            binding.videoStatusTextView.text = "Failed to connect"
            audioSwitch.deactivate()
            initializeUI()
        }

        override fun onDisconnected(room: Room, e: TwilioException?) {
            localParticipant = null
            binding.videoStatusTextView.text = "Disconnected from ${room.name}"
            binding.reconnectingProgressBar.visibility = View.GONE
            this@VideoActivity.room = null
            // Only reinitialize the UI if disconnect was not called from onDestroy()
            if (!disconnectedFromOnDestroy) {
                audioSwitch.deactivate()
                initializeUI()
                moveLocalVideoToPrimaryView()
            }
//            finish()
        }

        override fun onParticipantConnected(room: Room, participant: RemoteParticipant) {
            addRemoteParticipant(participant)
        }

        override fun onParticipantDisconnected(room: Room, participant: RemoteParticipant) {
            removeRemoteParticipant(participant)
            finish()
        }

        override fun onRecordingStarted(room: Room) {
            /*
             * Indicates when media shared to a Room is being recorded. Note that
             * recording is only available in our Group Rooms developer preview.
             */
            Log.d(TAG, "onRecordingStarted")
        }

        override fun onRecordingStopped(room: Room) {
            /*
             * Indicates when media shared to a Room is no longer being recorded. Note that
             * recording is only available in our Group Rooms developer preview.
             */
            Log.d(TAG, "onRecordingStopped")
        }
    }

    /*
     * RemoteParticipant events listener
     */
    private val participantListener = object : RemoteParticipant.Listener {
        override fun onAudioTrackPublished(
            remoteParticipant: RemoteParticipant,
            remoteAudioTrackPublication: RemoteAudioTrackPublication
        ) {
            Log.i(
                TAG, "onAudioTrackPublished: " +
                        "[RemoteParticipant: identity=${remoteParticipant.identity}], " +
                        "[RemoteAudioTrackPublication: sid=${remoteAudioTrackPublication.trackSid}, " +
                        "enabled=${remoteAudioTrackPublication.isTrackEnabled}, " +
                        "subscribed=${remoteAudioTrackPublication.isTrackSubscribed}, " +
                        "name=${remoteAudioTrackPublication.trackName}]"
            )
            binding.videoStatusTextView.text = "onAudioTrackAdded"
        }

        override fun onAudioTrackUnpublished(
            remoteParticipant: RemoteParticipant,
            remoteAudioTrackPublication: RemoteAudioTrackPublication
        ) {
            Log.i(
                TAG, "onAudioTrackUnpublished: " +
                        "[RemoteParticipant: identity=${remoteParticipant.identity}], " +
                        "[RemoteAudioTrackPublication: sid=${remoteAudioTrackPublication.trackSid}, " +
                        "enabled=${remoteAudioTrackPublication.isTrackEnabled}, " +
                        "subscribed=${remoteAudioTrackPublication.isTrackSubscribed}, " +
                        "name=${remoteAudioTrackPublication.trackName}]"
            )
            binding.videoStatusTextView.text = "onAudioTrackRemoved"
        }

        override fun onDataTrackPublished(
            remoteParticipant: RemoteParticipant,
            remoteDataTrackPublication: RemoteDataTrackPublication
        ) {
            Log.i(
                TAG, "onDataTrackPublished: " +
                        "[RemoteParticipant: identity=${remoteParticipant.identity}], " +
                        "[RemoteDataTrackPublication: sid=${remoteDataTrackPublication.trackSid}, " +
                        "enabled=${remoteDataTrackPublication.isTrackEnabled}, " +
                        "subscribed=${remoteDataTrackPublication.isTrackSubscribed}, " +
                        "name=${remoteDataTrackPublication.trackName}]"
            )
            binding.videoStatusTextView.text = "onDataTrackPublished"
        }

        override fun onDataTrackUnpublished(
            remoteParticipant: RemoteParticipant,
            remoteDataTrackPublication: RemoteDataTrackPublication
        ) {
            Log.i(
                TAG, "onDataTrackUnpublished: " +
                        "[RemoteParticipant: identity=${remoteParticipant.identity}], " +
                        "[RemoteDataTrackPublication: sid=${remoteDataTrackPublication.trackSid}, " +
                        "enabled=${remoteDataTrackPublication.isTrackEnabled}, " +
                        "subscribed=${remoteDataTrackPublication.isTrackSubscribed}, " +
                        "name=${remoteDataTrackPublication.trackName}]"
            )
            binding.videoStatusTextView.text = "onDataTrackUnpublished"
        }

        override fun onVideoTrackPublished(
            remoteParticipant: RemoteParticipant,
            remoteVideoTrackPublication: RemoteVideoTrackPublication
        ) {
            Log.i(
                TAG, "onVideoTrackPublished: " +
                        "[RemoteParticipant: identity=${remoteParticipant.identity}], " +
                        "[RemoteVideoTrackPublication: sid=${remoteVideoTrackPublication.trackSid}, " +
                        "enabled=${remoteVideoTrackPublication.isTrackEnabled}, " +
                        "subscribed=${remoteVideoTrackPublication.isTrackSubscribed}, " +
                        "name=${remoteVideoTrackPublication.trackName}]"
            )
            binding.videoStatusTextView.text = "onVideoTrackPublished"
        }

        override fun onVideoTrackUnpublished(
            remoteParticipant: RemoteParticipant,
            remoteVideoTrackPublication: RemoteVideoTrackPublication
        ) {
            Log.i(
                TAG, "onVideoTrackUnpublished: " +
                        "[RemoteParticipant: identity=${remoteParticipant.identity}], " +
                        "[RemoteVideoTrackPublication: sid=${remoteVideoTrackPublication.trackSid}, " +
                        "enabled=${remoteVideoTrackPublication.isTrackEnabled}, " +
                        "subscribed=${remoteVideoTrackPublication.isTrackSubscribed}, " +
                        "name=${remoteVideoTrackPublication.trackName}]"
            )
            binding.videoStatusTextView.text = "onVideoTrackUnpublished"
        }

        override fun onAudioTrackSubscribed(
            remoteParticipant: RemoteParticipant,
            remoteAudioTrackPublication: RemoteAudioTrackPublication,
            remoteAudioTrack: RemoteAudioTrack
        ) {
            Log.i(
                TAG, "onAudioTrackSubscribed: " +
                        "[RemoteParticipant: identity=${remoteParticipant.identity}], " +
                        "[RemoteAudioTrack: enabled=${remoteAudioTrack.isEnabled}, " +
                        "playbackEnabled=${remoteAudioTrack.isPlaybackEnabled}, " +
                        "name=${remoteAudioTrack.name}]"
            )
            binding.videoStatusTextView.text = "onAudioTrackSubscribed"
        }

        override fun onAudioTrackUnsubscribed(
            remoteParticipant: RemoteParticipant,
            remoteAudioTrackPublication: RemoteAudioTrackPublication,
            remoteAudioTrack: RemoteAudioTrack
        ) {
            Log.i(
                TAG, "onAudioTrackUnsubscribed: " +
                        "[RemoteParticipant: identity=${remoteParticipant.identity}], " +
                        "[RemoteAudioTrack: enabled=${remoteAudioTrack.isEnabled}, " +
                        "playbackEnabled=${remoteAudioTrack.isPlaybackEnabled}, " +
                        "name=${remoteAudioTrack.name}]"
            )
            binding.videoStatusTextView.text = "onAudioTrackUnsubscribed"
        }

        override fun onAudioTrackSubscriptionFailed(
            remoteParticipant: RemoteParticipant,
            remoteAudioTrackPublication: RemoteAudioTrackPublication,
            twilioException: TwilioException
        ) {
            Log.i(
                TAG, "onAudioTrackSubscriptionFailed: " +
                        "[RemoteParticipant: identity=${remoteParticipant.identity}], " +
                        "[RemoteAudioTrackPublication: sid=${remoteAudioTrackPublication.trackSid}, " +
                        "name=${remoteAudioTrackPublication.trackName}]" +
                        "[TwilioException: code=${twilioException.code}, " +
                        "message=${twilioException.message}]"
            )
            binding.videoStatusTextView.text = "onAudioTrackSubscriptionFailed"
        }

        override fun onDataTrackSubscribed(
            remoteParticipant: RemoteParticipant,
            remoteDataTrackPublication: RemoteDataTrackPublication,
            remoteDataTrack: RemoteDataTrack
        ) {
            Log.i(
                TAG, "onDataTrackSubscribed: " +
                        "[RemoteParticipant: identity=${remoteParticipant.identity}], " +
                        "[RemoteDataTrack: enabled=${remoteDataTrack.isEnabled}, " +
                        "name=${remoteDataTrack.name}]"
            )
            binding.videoStatusTextView.text = "onDataTrackSubscribed"
        }

        override fun onDataTrackUnsubscribed(
            remoteParticipant: RemoteParticipant,
            remoteDataTrackPublication: RemoteDataTrackPublication,
            remoteDataTrack: RemoteDataTrack
        ) {
            Log.i(
                TAG, "onDataTrackUnsubscribed: " +
                        "[RemoteParticipant: identity=${remoteParticipant.identity}], " +
                        "[RemoteDataTrack: enabled=${remoteDataTrack.isEnabled}, " +
                        "name=${remoteDataTrack.name}]"
            )
            binding.videoStatusTextView.text = "onDataTrackUnsubscribed"
        }

        override fun onDataTrackSubscriptionFailed(
            remoteParticipant: RemoteParticipant,
            remoteDataTrackPublication: RemoteDataTrackPublication,
            twilioException: TwilioException
        ) {
            Log.i(
                TAG, "onDataTrackSubscriptionFailed: " +
                        "[RemoteParticipant: identity=${remoteParticipant.identity}], " +
                        "[RemoteDataTrackPublication: sid=${remoteDataTrackPublication.trackSid}, " +
                        "name=${remoteDataTrackPublication.trackName}]" +
                        "[TwilioException: code=${twilioException.code}, " +
                        "message=${twilioException.message}]"
            )
            binding.videoStatusTextView.text = "onDataTrackSubscriptionFailed"
        }

        override fun onVideoTrackSubscribed(
            remoteParticipant: RemoteParticipant,
            remoteVideoTrackPublication: RemoteVideoTrackPublication,
            remoteVideoTrack: RemoteVideoTrack
        ) {
            Log.i(
                TAG, "onVideoTrackSubscribed: " +
                        "[RemoteParticipant: identity=${remoteParticipant.identity}], " +
                        "[RemoteVideoTrack: enabled=${remoteVideoTrack.isEnabled}, " +
                        "name=${remoteVideoTrack.name}]"
            )
            binding.videoStatusTextView.text = "onVideoTrackSubscribed"
            addRemoteParticipantVideo(remoteVideoTrack)
        }

        override fun onVideoTrackUnsubscribed(
            remoteParticipant: RemoteParticipant,
            remoteVideoTrackPublication: RemoteVideoTrackPublication,
            remoteVideoTrack: RemoteVideoTrack
        ) {
            Log.i(
                TAG, "onVideoTrackUnsubscribed: " +
                        "[RemoteParticipant: identity=${remoteParticipant.identity}], " +
                        "[RemoteVideoTrack: enabled=${remoteVideoTrack.isEnabled}, " +
                        "name=${remoteVideoTrack.name}]"
            )
            binding.videoStatusTextView.text = "onVideoTrackUnsubscribed"
            removeParticipantVideo(remoteVideoTrack)
        }

        override fun onVideoTrackSubscriptionFailed(
            remoteParticipant: RemoteParticipant,
            remoteVideoTrackPublication: RemoteVideoTrackPublication,
            twilioException: TwilioException
        ) {
            Log.i(
                TAG, "onVideoTrackSubscriptionFailed: " +
                        "[RemoteParticipant: identity=${remoteParticipant.identity}], " +
                        "[RemoteVideoTrackPublication: sid=${remoteVideoTrackPublication.trackSid}, " +
                        "name=${remoteVideoTrackPublication.trackName}]" +
                        "[TwilioException: code=${twilioException.code}, " +
                        "message=${twilioException.message}]"
            )
            binding.videoStatusTextView.text = "onVideoTrackSubscriptionFailed"
            showToast("Failed to subscribe to ${remoteParticipant.identity}")
        }

        override fun onAudioTrackEnabled(
            remoteParticipant: RemoteParticipant,
            remoteAudioTrackPublication: RemoteAudioTrackPublication
        ) {
        }

        override fun onVideoTrackEnabled(
            remoteParticipant: RemoteParticipant,
            remoteVideoTrackPublication: RemoteVideoTrackPublication
        ) {
        }

        override fun onVideoTrackDisabled(
            remoteParticipant: RemoteParticipant,
            remoteVideoTrackPublication: RemoteVideoTrackPublication
        ) {
        }

        override fun onAudioTrackDisabled(
            remoteParticipant: RemoteParticipant,
            remoteAudioTrackPublication: RemoteAudioTrackPublication
        ) {
        }
    }

    private var localAudioTrack: LocalAudioTrack? = null
    private var localVideoTrack: LocalVideoTrack? = null
    private var alertDialog: AlertDialog? = null
    private val cameraCapturerCompat by lazy {
        CameraCapturerCompat(this, CameraCapturerCompat.Source.FRONT_CAMERA)
    }
    private val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this@VideoActivity)
    }

    /*
     * Audio management
     */
    private val audioSwitch by lazy {
        AudioSwitch(
            applicationContext, preferredDeviceList = listOf(
                BluetoothHeadset::class.java,
                WiredHeadset::class.java, Speakerphone::class.java, Earpiece::class.java
            )
        )
    }
    private var savedVolumeControlStream by Delegates.notNull<Int>()

    private var participantIdentity: String? = null
    private lateinit var localVideoView: VideoSink
    private var disconnectedFromOnDestroy = false

    override fun onCreate(savedInstanceState: Bundle?) {
        turnScreenOnAndKeyguardOff()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        super.onCreate(savedInstanceState)
        binding = ActivityVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setObserver()
        when {
            intent.hasExtra("appointmentid") -> {
                appointmentId = intent?.getStringExtra("appointmentid").toString()
                val jsonObject = JsonObject().apply {
                    addProperty("id", intent?.getStringExtra("appointmentid").toString())
                    addProperty("type", session.loginType)

                }
                appointmentViewModel.generateVideoToken(jsonObject)
                binding.oppositeName.text = intent?.getStringExtra("name").toString()
            }
            intent.hasExtra("videoToken") -> {
                appointmentId = intent?.getStringExtra("appointId").toString()
                accessToken = intent?.getStringExtra("videoToken").toString()
                binding.oppositeName.text = intent?.getStringExtra("name").toString()
            }
            else -> {
                showToast("Invalid token")
                finish()
            }
        }

        localVideoView = binding.thumbnailVideoView
        binding.thumbnailVideoView.mirror = true


        savedVolumeControlStream = volumeControlStream
        volumeControlStream = AudioManager.STREAM_VOICE_CALL


        if (!checkPermissionForCameraAndMicrophone()) {
            requestPermissionForCameraMicrophoneAndBluetooth()
        } else {
            audioSwitch.start { _, audioDevice -> updateAudioDeviceIcon(audioDevice) }
            createAudioAndVideoTracks()
        }
        initializeUI()


        when {
            intent.hasExtra("appointmentid") -> {
                return
            }
            intent.hasExtra("videoToken") -> {
                initializeUI()
                connectToRoom()
            }
            else -> {
                showToast("Invalid token")
                finish()
            }
        }

    }

    private fun setObserver() {
        callEnded.observe(this) {
            if (session.callStatus == "rejected") {
                room?.disconnect()
                finish()
            }
            session.callStatus = ""

        }
        appointmentViewModel.generateVieoToken.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data?.code != null && response.data.code == 200) {
                        response.data.message.let { showToast(it) }
                        accessToken = if (session.loginType == "doctor") {
                            response.data.data.doctor_token
                        } else {

                            response.data.data.patients_token
                        }

                        connectToRoom()

                    } else {
                        response.data?.message?.let { showToast(it) }
                    }
                }
                is Resource.Loading -> {
                }
                is Resource.Error -> {
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_MIC_PERMISSION_REQUEST_CODE) {

            val cameraAndMicPermissionGranted = ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED



            audioSwitch.start { _, audioDevice -> updateAudioDeviceIcon(audioDevice) }

            if (cameraAndMicPermissionGranted) {
                createAudioAndVideoTracks()
                connectToRoom()
            } else {
                Toast.makeText(
                    this,
                    R.string.permissions_needed,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        localVideoTrack = if (localVideoTrack == null && checkPermissionForCameraAndMicrophone()) {
            createLocalVideoTrack(
                this,
                true,
                cameraCapturerCompat
            )
        } else {
            localVideoTrack
        }
        localVideoTrack?.addSink(localVideoView)
        localVideoTrack?.let { localParticipant?.publishTrack(it) }
        localParticipant?.setEncodingParameters(encodingParameters)

        room?.let {
            binding.reconnectingProgressBar.visibility = if (it.state != Room.State.RECONNECTING)
                View.GONE else
                View.VISIBLE
            binding.videoStatusTextView.text = "Connected to ${it.name}"
        }
    }

    override fun onPause() {
        localVideoTrack?.let { localParticipant?.unpublishTrack(it) }
        localVideoTrack?.release()
        localVideoTrack = null
        super.onPause()
    }

    override fun onDestroy() {
        audioSwitch.stop()
        volumeControlStream = savedVolumeControlStream
        room?.disconnect()
        disconnectedFromOnDestroy = true
        localAudioTrack?.release()
        localVideoTrack?.release()

        super.onDestroy()
    }


    private fun checkPermissions(permissions: Array<String>): Boolean {
        var shouldCheck = true
        for (permission in permissions) {
            shouldCheck = shouldCheck and (PackageManager.PERMISSION_GRANTED ==
                    ContextCompat.checkSelfPermission(this, permission))
        }
        return shouldCheck
    }

    private fun requestPermissions(permissions: Array<String>) {
        var displayRational = false
        for (permission in permissions) {
            displayRational =
                displayRational or ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    permission
                )
        }
        if (displayRational) {
            Toast.makeText(this, R.string.permissions_needed, Toast.LENGTH_LONG).show()
        } else {
            ActivityCompat.requestPermissions(this, permissions, CAMERA_MIC_PERMISSION_REQUEST_CODE)
        }
    }

    private fun checkPermissionForCameraAndMicrophone(): Boolean {
        return checkPermissions(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )
        )
    }

    private fun requestPermissionForCameraMicrophoneAndBluetooth() {
        val permissionsList: Array<String> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )
        }
        requestPermissions(permissionsList)
    }

    private fun createAudioAndVideoTracks() {
        // Share your microphone
        localAudioTrack = createLocalAudioTrack(this, true)
        // Share your camera
        localVideoTrack = createLocalVideoTrack(this, true, cameraCapturerCompat)
    }


    private fun connectToRoom() {
        if (checkPermissionForCameraAndMicrophone()) {
            audioSwitch.activate()
            room = connect(this, accessToken, roomListener) {
                roomName("")
                audioTracks(listOf(localAudioTrack))
                videoTracks(listOf(localVideoTrack))
                preferAudioCodecs(listOf(audioCodec))
                preferVideoCodecs(listOf(videoCodec))
                encodingParameters(encodingParameters)
                enableAutomaticSubscription(enableAutomaticSubscription)
            }
            setDisconnectAction()
        } else {
            requestPermissionForCameraMicrophoneAndBluetooth()
        }
    }

    private fun initializeUI() {
        binding.endCall.setOnClickListener(connectActionClickListener())
        binding.switchCameraActionFab.isVisible = true
        binding.switchCameraActionFab.setOnClickListener(switchCameraClickListener())
        binding.muteActionFab.isVisible = true
        binding.muteActionFab.setOnClickListener(muteClickListener())
        binding.thumbnailVideoView.setOnClickListener { swipeVideoViews() }
        updateAudioDeviceIcon(audioSwitch.selectedAudioDevice)
    }

    private fun swipeVideoViews() {

    }


    private fun updateAudioDeviceIcon(selectedAudioDevice: AudioDevice?) {
        val audioDeviceMenuIcon = when (selectedAudioDevice) {
            is BluetoothHeadset -> R.drawable.ic_bluetooth_white_24dp
            is WiredHeadset -> R.drawable.ic_headset_mic_white_24dp
            is Speakerphone -> R.drawable.ic_volume_up_white_24dp
            else -> R.drawable.ic_phonelink_ring_white_24dp
        }
//        audioDeviceMenuItem.setImageDrawable(
//            ContextCompat.getDrawable(
//                this@VideoActivity, audioDeviceMenuIcon
//            )
//        )
    }


    private fun setDisconnectAction() {
        binding.endCall.isVisible = true
        binding.endCall.setOnClickListener(disconnectClickListener())
    }


    private fun addRemoteParticipant(remoteParticipant: RemoteParticipant) {
        binding.reconnectingProgressBar.visibility = View.VISIBLE
        participantIdentity = remoteParticipant.identity
        binding.videoStatusTextView.text = "Participant $participantIdentity joined"
        remoteParticipant.remoteVideoTracks.firstOrNull()?.let { remoteVideoTrackPublication ->
            if (remoteVideoTrackPublication.isTrackSubscribed) {
                remoteVideoTrackPublication.remoteVideoTrack?.let { addRemoteParticipantVideo(it) }
            }
        }

        remoteParticipant.setListener(participantListener)
    }


    private fun addRemoteParticipantVideo(videoTrack: VideoTrack) {
        moveLocalVideoToThumbnailView()
        binding.primaryVideoView.mirror = false
        videoTrack.addSink(binding.primaryVideoView)
        binding.reconnectingProgressBar.visibility = View.GONE
        binding.chronometer.isVisible = true
        binding.chronometer.base = SystemClock.elapsedRealtime()
        binding.chronometer.start()
    }

    private fun moveLocalVideoToThumbnailView() {
        if (binding.thumbnailVideoView.visibility == View.GONE) {
            binding.thumbnailVideoView.visibility = View.VISIBLE
            with(localVideoTrack) {
                this?.removeSink(binding.primaryVideoView)
                this?.addSink(binding.thumbnailVideoView)
            }
            localVideoView = binding.thumbnailVideoView
            binding.thumbnailVideoView.mirror = cameraCapturerCompat.cameraSource ==
                    CameraCapturerCompat.Source.FRONT_CAMERA
        }
    }

    private fun removeRemoteParticipant(remoteParticipant: RemoteParticipant) {
        binding.videoStatusTextView.text = "Participant $remoteParticipant.identity left."
        if (remoteParticipant.identity != participantIdentity) {
            return
        }

        remoteParticipant.remoteVideoTracks.firstOrNull()?.let { remoteVideoTrackPublication ->
            if (remoteVideoTrackPublication.isTrackSubscribed) {
                remoteVideoTrackPublication.remoteVideoTrack?.let { removeParticipantVideo(it) }
            }
        }
        moveLocalVideoToPrimaryView()
//        finish()
    }

    private fun removeParticipantVideo(videoTrack: VideoTrack) {
        videoTrack.removeSink(binding.primaryVideoView)
    }

    private fun moveLocalVideoToPrimaryView() {
        if (binding.thumbnailVideoView.visibility == View.VISIBLE) {
            binding.thumbnailVideoView.visibility = View.GONE
            with(localVideoTrack) {
                this?.removeSink(binding.thumbnailVideoView)
                this?.addSink(binding.primaryVideoView)
            }
            localVideoView = binding.primaryVideoView
            binding.primaryVideoView.mirror = cameraCapturerCompat.cameraSource ==
                    CameraCapturerCompat.Source.FRONT_CAMERA
        }
    }

    private fun disconnectClickListener(): View.OnClickListener {
        return View.OnClickListener {
            notifyCallDisconnected()
            room?.disconnect()
            finish()


        }
    }

    private fun notifyCallDisconnected() {
        val jsonObject = JsonObject().apply {
            addProperty("type", session.loginType)
            addProperty("name", "")
            addProperty("status", "declined")
            addProperty("appointmentId", appointmentId)
        }
        appointmentViewModel.callNotify(jsonObject)

        appointmentViewModel.callNotify.observe(this) {}
    }

    private fun connectActionClickListener(): View.OnClickListener {
        return View.OnClickListener {
            finish()
        }
    }


    private fun switchCameraClickListener(): View.OnClickListener {
        return View.OnClickListener {
            val cameraSource = cameraCapturerCompat.cameraSource
            cameraCapturerCompat.switchCamera()
            if (binding.thumbnailVideoView.visibility == View.VISIBLE) {
                binding.thumbnailVideoView.mirror =
                    cameraSource == CameraCapturerCompat.Source.BACK_CAMERA
            } else {
                binding.primaryVideoView.mirror =
                    cameraSource == CameraCapturerCompat.Source.BACK_CAMERA
            }
        }
    }


    private fun muteClickListener(): View.OnClickListener {
        return View.OnClickListener {
            localAudioTrack?.let {
                val enable = !it.isEnabled
                it.enable(enable)
                val icon = if (enable)
                    R.drawable.ic_mic_white_24dp
                else
                    R.drawable.ic_mic_off_black_24dp
                binding.muteActionFab.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@VideoActivity, icon
                    )
                )
            }
        }
    }
}
