package ca.trinityonhale.donationkiosk.fragment

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import ca.trinityonhale.donationkiosk.MainActivity
import ca.trinityonhale.donationkiosk.databinding.FragmentCoverBinding
import ca.trinityonhale.donationkiosk.settings.SettingsFragment
import com.squareup.picasso.Picasso

class CoverFragment : Fragment() {

    companion object {
        const val TAG: String = "CoverFragment"
    }

    private lateinit var viewModel: CoverViewModel
    private lateinit var binding: FragmentCoverBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentCoverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(CoverViewModel::class.java)

        binding.button.setOnClickListener {
            (requireActivity() as MainActivity).navigateTo(DonateFragment.TAG, DonateFragment(),
                addToBackStack = true
            )
        }

        var list = mutableListOf(
            "https://picsum.photos/seed/1/800/600",
            "https://picsum.photos/seed/2/800/600",
            "https://picsum.photos/seed/3/800/600",
            "https://picsum.photos/seed/4/800/600",
        )

        binding.adaptorViewFlipper.adapter = ViewFlipperAdapter(list)
        binding.adaptorViewFlipper.flipInterval = 5000
        binding.adaptorViewFlipper.isAutoStart = true
        binding.adaptorViewFlipper.startFlipping()

        // binding.imgLogoView
        // tap 8 times to show the settings
        binding.imgLogoView.setOnClickListener {
            viewModel.tapCount++
            if (viewModel.tapCount >= 8) {
                (requireActivity() as MainActivity).navigateTo(SettingsFragment.TAG, SettingsFragment(),
                    addToBackStack = true
                )
                viewModel.tapCount = 0
            }
        }
    }

    class ViewFlipperAdapter (
        private val list: List<String>,
    ): BaseAdapter() {
        override fun getCount(): Int {
            return list.size
        }

        override fun getItem(position: Int): Any {
            return list[position]
        }

        override fun getItemId(position: Int): Long {
            return 0;
        }

        override fun getView(position: Int, view: View?, parent: ViewGroup?): View {
            var imageView = ImageView(parent?.context)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            Picasso.get().load(list[position]).into(imageView)
            return imageView
        }
    }
}