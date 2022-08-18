package com.doctorsplaza.app.ui.patient.fragments.videos

import android.os.Bundle
import android.transition.TransitionInflater
import android.view.*
import android.widget.AbsListView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentVideosBinding
import com.doctorsplaza.app.ui.patient.fragments.videos.adapter.VideosAdapter
import com.doctorsplaza.app.ui.patient.fragments.home.HomeViewModel
import com.doctorsplaza.app.ui.patient.fragments.home.model.VideoData
import com.doctorsplaza.app.utils.*
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.loadOrCueVideo
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Exception
import javax.inject.Inject


@AndroidEntryPoint
class VideosFragment : Fragment(R.layout.fragment_videos), View.OnClickListener {

    private var videosList: MutableList<VideoData> = ArrayList()

    private lateinit var youTubePlayerNew: YouTubePlayer
    private lateinit var binding: FragmentVideosBinding

    private var currentView: View? = null

    private val homeViewModel: HomeViewModel by viewModels()

    private lateinit var appLoader: DoctorPlazaLoader

    @Inject
    lateinit var videosAdapter: VideosAdapter

    @Inject
    lateinit var session:SessionManager

    private var videoId: String = ""

    private var pageNo = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val animation =
            TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
        sharedElementEnterTransition = animation
        sharedElementReturnTransition = animation
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_videos, container, false)
            binding = FragmentVideosBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
            setAdapterClickListener()
        }
        return currentView!!
    }


    private fun init() {
        homeViewModel.videos("10", page = pageNo.toString())
        binding.loaderView.isVisible = true
        appLoader = DoctorPlazaLoader(requireContext())

        binding.youtubePlayer.enableAutomaticInitialization = false
        try {
            binding.youtubePlayer.initialize(youtubePlayerListener)

        } catch (e: Exception) {

        }

        videoId = arguments?.getString("videoLink").toString()
        val videoTitle = arguments?.getString("videoTitle").toString()
        val videoSlug = arguments?.getString("videoSlug").toString()
        setVideoView(videoId, videoTitle, videoSlug)

    }

    private fun setVideoView(videoId: String, videoTitle: String, videoSlug: String) {

        binding.videoTitle.text = videoTitle
        binding.videoSlug.text = videoSlug


//        lifecycle.addObserver(binding.youtubePlayer)
//        binding.youtubePlayer.enterFullScreen()
//        binding.youtubePlayer.exitFullScreen()
//        binding.youtubePlayer.isFullScreen()
//        binding.youtubePlayer.toggleFullScreen()

    }

    private val youtubePlayerListener = object : AbstractYouTubePlayerListener() {
        override fun onReady(youTubePlayer: YouTubePlayer) {
            youTubePlayerNew = youTubePlayer
            youTubePlayerNew.loadVideo(videoId, 0F)
            binding.loaderView.isVisible = false
            appLoader.dismiss()
        }
    }

    private fun setObserver() {
        homeViewModel.videos.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    binding.moreLoader.isVisible = false
//                    appLoader.dismiss()
                    if (response.data?.status == 401) {
                        session.isLogin = false
                        logOutUnAuthorized(requireActivity(),response.data.message)
                    } else {
                    if (response.data?.status == 200) {
                        if (pageNo == 1) {
                            videosList.addAll(response.data.data)
                            setVideos(videosList)
                        } else {
                            videosList.addAll(response.data.data)
                            videosAdapter.differ.submitList(videosList)
                            videosAdapter.notifyDataSetChanged()
                        }
                    }
                }}
                is Resource.Loading -> {
                    if(pageNo==1) appLoader.show() else binding.moreLoader.isVisible = true
                }
                is Resource.Error -> {
                    appLoader.dismiss()
                    binding.moreLoader.isVisible = false
                }
            }
        }
    }

    private fun setVideos(data: List<VideoData>) {
        videosAdapter.differ.submitList(data)
        binding.doctorVideosRv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = videosAdapter
        }.addOnScrollListener(scrollListener)
    }


    var isLoading = false
    var isScrolling = false


    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            if (dy > 0) {

                val recycleLayoutManager =
                    binding.doctorVideosRv.layoutManager as LinearLayoutManager
                if (!isLoading) {
                    if (recycleLayoutManager != null && recycleLayoutManager.findLastCompletelyVisibleItemPosition() == videosAdapter.itemCount - 1) {
                        pageNo++
                            homeViewModel.videos("10", pageNo.toString())
                        isLoading = true
                    }
                }
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }
    }

    private fun setOnClickListener() {
        with(binding) {
            backArrow.setOnClickListener(this@VideosFragment)
        }
    }

    private fun setAdapterClickListener() {
        videosAdapter.setOnVideoClickListener {
            videoId = it.video_id
            youTubePlayerNew.loadVideo(videoId, 0F)
            youTubePlayerNew.play()
            setVideoView(videoId, it.title, it.slug)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.backArrow -> {
                findNavController().popBackStack()
            }
            R.id.specialistsViewAll -> {
                val bundle = Bundle().apply {
                    putString("searchKey", "")
                }
                findNavController().navigate(R.id.searchFragment, bundle)
            }

        }
    }

    override fun onResume() {
        super.onResume()
        setObserver()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.youtubePlayer.release()
    }
}