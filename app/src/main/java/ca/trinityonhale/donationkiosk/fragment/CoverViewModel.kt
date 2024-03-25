package ca.trinityonhale.donationkiosk.fragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ca.trinityonhale.donationkiosk.module.trinityonhale.TrinityOnHaleService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CoverViewModel @Inject constructor(
    private var trinityOnHaleService: TrinityOnHaleService
) : ViewModel() {

    var tapCount = 0
    var slides = MutableLiveData(listOf<String>())

    suspend fun loadCoverImages() {
        val slides = trinityOnHaleService.getSlides().map { slide ->
            slide.asString
        }

        this.slides.postValue(slides)
    }

}