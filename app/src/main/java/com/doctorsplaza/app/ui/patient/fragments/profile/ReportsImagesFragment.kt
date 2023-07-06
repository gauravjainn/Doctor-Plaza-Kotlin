package com.doctorsplaza.app.ui.patient.fragments.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.doctorsplaza.app.R
import com.doctorsplaza.app.databinding.FragmentReportsImagesBinding
import com.doctorsplaza.app.ui.patient.fragments.profile.adapter.CarouselAdapter
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ReportsImagesFragment : Fragment(), View.OnClickListener {

    private var imageLists: List<String> = ArrayList()
    lateinit var binding: FragmentReportsImagesBinding
    private var currentView: View? = null

    @Inject
    lateinit var carouselAdapter: CarouselAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_reports_images, container, false)
            binding = FragmentReportsImagesBinding.bind(currentView!!)
            init()
            setOnClickListener()
        }
        return currentView!!
    }


    private fun init() {

        val position = arguments?.getInt("position") ?: 0

        val imageDataString = arguments?.getString("images").toString()
        val imageListType = object : TypeToken<List<String>>() {}.type
        imageLists = Gson().fromJson(imageDataString, imageListType)

        carouselAdapter.differ.submitList(imageLists)
        binding.viewPager.isUserInputEnabled = true
        binding.viewPager.adapter = carouselAdapter
        binding.viewPager.currentItem = position

        if (position == imageLists.size - 1) {
            binding.arrowRight.alpha = 0.5F
        }

        binding.viewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == 0) {
                    binding.arrowLeft.alpha = 0.5F
                } else {
                    binding.arrowLeft.alpha = 1F
                }

                if (position == imageLists.size - 1) {
                    binding.arrowRight.alpha = 0.5F
                } else {
                    binding.arrowRight.alpha = 1F
                }
            }
        })
    }

    private fun setOnClickListener() {
        with(binding) {
            arrowLeft.setOnClickListener(this@ReportsImagesFragment)
            arrowRight.setOnClickListener(this@ReportsImagesFragment)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.arrowLeft -> {
                val currentItem: Int = binding.viewPager.currentItem
                val nextPage = currentItem - 1
                binding.viewPager.currentItem = nextPage
            }

            R.id.arrowRight -> {
                val currentItem: Int = binding.viewPager.currentItem
                val nextPage = currentItem + 1
                binding.viewPager.currentItem = nextPage
            }
        }

    }
}