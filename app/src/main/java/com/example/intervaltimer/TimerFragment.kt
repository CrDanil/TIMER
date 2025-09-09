package com.example.intervaltimer

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.intervaltimer.databinding.FragmentTimerBinding
import com.example.intervaltimer.Workout

class TimerFragment : Fragment() {
    private var _binding: FragmentTimerBinding? = null
    private val binding get() = _binding!!

    private var currentWorkout: Workout? = null
    private lateinit var timerSteps: List<WorkoutManager.TimerStep>
    private var currentStepIndex = 0
    private var countDownTimer: CountDownTimer? = null
    private var isTimerRunning = false
    private var timeLeftInMillis: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Получаем переданную тренировку с явным указанием типа
        currentWorkout = arguments?.getParcelable("workout", Workout::class.java)

        if (currentWorkout == null) {
            binding.currentStepNameTextView.text = "Тренировка не выбрана"
            binding.timerText.text = "00:00"
            binding.nextStepTextView.text = "Выберите тренировку из списка"
            // Делаем кнопки неактивными
            binding.startButton.isEnabled = false
            binding.pauseButton.isEnabled = false
            binding.stopButton.isEnabled = false
        } else {
            timerSteps = WorkoutManager.flattenWorkout(currentWorkout!!)

            if (timerSteps.isEmpty()) {
                binding.currentStepNameTextView.text = "Тренировка пуста"
                binding.timerText.text = "00:00"
                return
            }

            setupTimer()
        }

        binding.startButton.setOnClickListener {
            startTimer()
        }

        binding.pauseButton.setOnClickListener {
            pauseTimer()
        }

        binding.stopButton.setOnClickListener {
            stopTimer()
        }

        binding.toListButton.setOnClickListener {
            findNavController().navigate(R.id.action_timer_to_list)
        }
    }

    private fun setupTimer() {
        if (currentWorkout == null) return

        currentStepIndex = 0
        updateStepDisplay()
        updateButtonVisibility(false)
    }

    private fun updateStepDisplay() {
        if (currentStepIndex < timerSteps.size) {
            val currentStep = timerSteps[currentStepIndex]
            binding.currentStepNameTextView.text = currentStep.name
            timeLeftInMillis = currentStep.duration
            updateCountDownText()

            // Показываем следующий шаг, если есть
            if (currentStepIndex + 1 < timerSteps.size) {
                binding.nextStepTextView.text = "Следующее: ${timerSteps[currentStepIndex + 1].name}"
            } else {
                binding.nextStepTextView.text = "Следующее: Конец тренировки"
            }
        } else {
            binding.currentStepNameTextView.text = "Тренировка завершена!"
            binding.timerText.text = "00:00"
            binding.nextStepTextView.text = ""
        }
    }

    private fun updateButtonVisibility(isRunning: Boolean) {
        if (isRunning) {
            binding.startButton.visibility = View.GONE
            binding.pauseButton.visibility = View.VISIBLE
            binding.stopButton.visibility = View.VISIBLE
        } else {
            binding.startButton.visibility = View.VISIBLE
            binding.pauseButton.visibility = View.GONE
            binding.stopButton.visibility = View.GONE
        }
    }
    private fun startTimer() {
        if (isTimerRunning || currentWorkout == null) return

        if (currentStepIndex >= timerSteps.size) {
            setupTimer()
        }

        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountDownText()
            }

            override fun onFinish() {
                timeLeftInMillis = 0
                updateCountDownText()
                currentStepIndex++
                if (currentStepIndex < timerSteps.size) {
                    updateStepDisplay()
                    startTimer()
                } else {
                    binding.currentStepNameTextView.text = "Тренировка завершена!"
                    isTimerRunning = false
                    updateButtonVisibility(false)
                }
            }
        }.start()

        isTimerRunning = true
        updateButtonVisibility(true)
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
        updateButtonVisibility(false)
    }

    private fun stopTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
        setupTimer()
        updateButtonVisibility(false)
    }

    private fun updateCountDownText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        binding.timerText.text = String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
        _binding = null
    }
}