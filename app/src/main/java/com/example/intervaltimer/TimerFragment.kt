package com.example.intervaltimer

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.intervaltimer.databinding.FragmentTimerBinding

class TimerFragment : Fragment() {
    private var _binding: FragmentTimerBinding? = null
    private val binding get() = _binding!!

    private lateinit var workout: Workout
    private lateinit var steps: List<WorkoutManager.TimerStep>
    private var currentStepIndex = 0
    private var timer: CountDownTimer? = null
    private var isPaused = false
    private var timeRemaining: Long = 0
    private var soundPool: SoundPool? = null
    private var soundId: Int = 0
    private var lastPlayedSecond: Long = -1 // Для отслеживания последней секунды, для которой проигрывался звук

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Получаем тренировку из аргументов
        workout = arguments?.getParcelable("workout") ?: return

        // Преобразуем тренировку в последовательность шагов
        steps = WorkoutManager.flattenWorkout(workout)

        // Инициализируем SoundPool для звуковых уведомлений
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()

        // Загружаем звуковой файл (добавьте свой файл в папку res/raw)
        soundId = soundPool?.load(requireContext(), R.raw.beep, 1) ?: 0

        // Настраиваем кнопки
        setupButtons()

        // Запускаем таймер, если есть шаги
        if (steps.isNotEmpty()) {
            startTimer()
        }
    }

    private fun setupButtons() {
        binding.startButton.setOnClickListener {
            if (isPaused) {
                resumeTimer()
            } else {
                startTimer()
            }
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

    @SuppressLint("SetTextI18n")
    private fun startTimer() {
        if (currentStepIndex >= steps.size) {
            // Тренировка завершена
            binding.timerText.text = "Готово!"
            return
        }

        val currentStep = steps[currentStepIndex]
        timeRemaining = currentStep.duration

        // Сбрасываем отслеживание последней секунды
        lastPlayedSecond = -1

        // Обновляем UI
        binding.currentStepNameTextView.text = currentStep.name
        updateNextStepsInfo()

        // Настраиваем видимость кнопок
        binding.startButton.visibility = View.GONE
        binding.pauseButton.visibility = View.VISIBLE
        binding.stopButton.visibility = View.VISIBLE

        // Запускаем таймер
        timer = object : CountDownTimer(timeRemaining, 100) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemaining = millisUntilFinished
                updateTimerText(millisUntilFinished)

                // Определяем текущую секунду
                val currentSecond = millisUntilFinished / 1000

                // Звуковые уведомления за 10, 3, 2, 1 секунду (проигрываем только один раз для каждой секунды)
                if (currentSecond in listOf(10L, 3L, 2L, 1L) && currentSecond != lastPlayedSecond) {
                    playBeepSound()
                    lastPlayedSecond = currentSecond
                }
            }

            override fun onFinish() {
                // Переходим к следующему шагу
                currentStepIndex++
                if (currentStepIndex < steps.size) {
                    startTimer() // Автоматически запускаем следующий шаг
                } else {
                    // Тренировка завершена
                    binding.timerText.text = "Готово!"
                    binding.startButton.visibility = View.VISIBLE
                    binding.pauseButton.visibility = View.GONE
                    binding.stopButton.visibility = View.GONE
                }
            }
        }.start()

        isPaused = false
    }

    private fun pauseTimer() {
        timer?.cancel()
        isPaused = true

        // Обновляем видимость кнопок
        binding.startButton.visibility = View.VISIBLE
        binding.pauseButton.visibility = View.GONE
    }

    private fun resumeTimer() {
        // Запускаем таймер с оставшимся временем
        timer = object : CountDownTimer(timeRemaining, 100) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemaining = millisUntilFinished
                updateTimerText(millisUntilFinished)

                // Определяем текущую секунду
                val currentSecond = millisUntilFinished / 1000

                // Звуковые уведомления за 10, 3, 2, 1 секунду (проигрываем только один раз для каждой секунды)
                if (currentSecond in listOf(10L, 3L, 2L, 1L) && currentSecond != lastPlayedSecond) {
                    playBeepSound()
                    lastPlayedSecond = currentSecond
                }
            }

            override fun onFinish() {
                // Переходим к следующему шагу
                currentStepIndex++
                if (currentStepIndex < steps.size) {
                    startTimer()
                } else {
                    binding.timerText.text = "Готово!"
                    binding.startButton.visibility = View.VISIBLE
                    binding.pauseButton.visibility = View.GONE
                    binding.stopButton.visibility = View.GONE
                }
            }
        }.start()

        isPaused = false
        binding.startButton.visibility = View.GONE
        binding.pauseButton.visibility = View.VISIBLE
    }

    private fun stopTimer() {
        timer?.cancel()
        currentStepIndex = 0
        isPaused = false

        // Сбрасываем UI
        binding.timerText.text = "00:00.0"
        binding.currentStepNameTextView.text = "Готовность"
        binding.nextStepTextView.text = "Следующее: "

        // Обновляем видимость кнопок
        binding.startButton.visibility = View.VISIBLE
        binding.pauseButton.visibility = View.GONE
        binding.stopButton.visibility = View.GONE
    }

    private fun updateTimerText(millisUntilFinished: Long) {
        val totalSeconds = millisUntilFinished / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val milliseconds = (millisUntilFinished % 1000) / 100

        binding.timerText.text = String.format("%02d:%02d.%01d", minutes, seconds, milliseconds)
    }

    private fun updateNextStepsInfo() {
        if (currentStepIndex + 1 < steps.size) {
            val nextStep = steps[currentStepIndex + 1]
            binding.nextStepTextView.text = "Следующее: ${nextStep.name} (${formatDuration(nextStep.duration)})"
        } else {
            binding.nextStepTextView.text = "Следующее: Конец тренировки"
        }
    }

    private fun formatDuration(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun playBeepSound() {
        soundPool?.play(soundId, 1.0f, 1.0f, 0, 0, 1.0f)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
        soundPool?.release()
        soundPool = null
        _binding = null
    }
}