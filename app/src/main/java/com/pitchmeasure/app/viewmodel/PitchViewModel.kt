package com.pitchmeasure.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pitchmeasure.app.audio.PitchDetector
import com.pitchmeasure.app.model.PitchInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PitchViewModel : ViewModel() {

    var pitchInfo by mutableStateOf<PitchInfo?>(null)
        private set

    var isDetecting by mutableStateOf(false)
        private set

    private var _curvePoints = mutableListOf<Float>()
    val curvePoints: List<Float> get() = _curvePoints.toList()

    private var detector: PitchDetector? = null

    fun startDetection() {
        if (isDetecting) return
        isDetecting = true
        _curvePoints.clear()

        detector = PitchDetector { info ->
            viewModelScope.launch(Dispatchers.Main) {
                pitchInfo = info
                _curvePoints.add(info.frequency)
                if (_curvePoints.size > 200) {
                    _curvePoints.removeAt(0)
                }
            }
        }
        detector?.start()
    }

    fun stopDetection() {
        if (!isDetecting) return
        isDetecting = false
        detector?.stop()
        detector = null
    }

    override fun onCleared() {
        super.onCleared()
        stopDetection()
    }
}
