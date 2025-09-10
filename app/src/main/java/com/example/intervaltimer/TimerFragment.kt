package com.example.intervaltimer

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.intervaltimer.databinding.FragmentTimerBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
    private var lastPlayedSecond: Long = -1
    private var totalWorkoutTime: Long = 0
    private var precomputedSums: MutableList<Long>? = null

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

        // Предварительно вычисляем общее время тренировки
        precomputedSums = MutableList(steps.size) { 0L }
        for (i in steps.size - 1 downTo 0) {
            precomputedSums!![i] = steps[i].duration + (if (i < steps.size - 1) precomputedSums!![i + 1] else 0)
        }
        totalWorkoutTime = if (steps.isNotEmpty()) precomputedSums!![0] else 0L

        // Инициализируем SoundPool для звуковых уведомлений
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()

        // Загружаем звуковой файл
        soundId = try {
            soundPool?.load(requireContext(), R.raw.beep, 1) ?: 0
        } catch (e: Exception) {
            0
        }

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
            binding.timerText.setTextColor(Color.WHITE) //САМ
            binding.currentStepNameTextView.setTextColor(Color.BLACK) //САМ
            binding.nextStepTextView1.setTextColor(Color.BLACK) //САМ
            binding.nextStepTextView2.setTextColor(Color.BLACK) //САМ
            return
        }

        val currentStep = steps[currentStepIndex]
        timeRemaining = currentStep.duration

        // Сбрасываем отслеживание последней секунды
        lastPlayedSecond = -1

        // Обновляем UI с цветом упражнения
        binding.currentStepNameTextView.text = currentStep.name
        binding.currentStepNameTextView.setTextColor(currentStep.color)
        binding.timerText.setTextColor(currentStep.color)

        updateNextStepsInfo()
        updateTotalRemainingTime()

        // Настраиваем видимость кнопок
        binding.startButton.visibility = View.GONE
        binding.pauseButton.visibility = View.VISIBLE
        binding.stopButton.visibility = View.GONE // Кнопка стоп скрыта до паузы

        // Запускаем таймер
        timer = object : CountDownTimer(timeRemaining, 100) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemaining = millisUntilFinished
                updateTimerText(millisUntilFinished)
                updateTotalRemainingTime()

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
                    binding.timerText.setTextColor(Color.WHITE) //САМ
                    binding.currentStepNameTextView.setTextColor(Color.BLACK) //САМ
                    binding.nextStepTextView1.setTextColor(Color.BLACK) //САМ
                    binding.nextStepTextView2.setTextColor(Color.BLACK) //САМ
                    binding.startButton.visibility = View.VISIBLE
                    binding.pauseButton.visibility = View.GONE
                    binding.stopButton.visibility = View.GONE
                    binding.totalRemainingTimeTextView.text = "Всего: 00:00"
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
        binding.stopButton.visibility = View.VISIBLE // Показываем кнопку стоп при паузе
    }

    private fun resumeTimer() {
        // Запускаем таймер с оставшимся временем
        timer = object : CountDownTimer(timeRemaining, 100) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemaining = millisUntilFinished
                updateTimerText(millisUntilFinished)
                updateTotalRemainingTime()

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
                    binding.currentStepNameTextView.setTextColor(Color.BLACK) //САМ
                    binding.timerText.text = "Готово!"
                    binding.timerText.setTextColor(Color.WHITE) //САМ
                    binding.nextStepTextView1.setTextColor(Color.BLACK) //САМ
                    binding.nextStepTextView2.setTextColor(Color.BLACK) //САМ
                    binding.startButton.visibility = View.VISIBLE
                    binding.pauseButton.visibility = View.GONE
                    binding.stopButton.visibility = View.GONE
                    binding.totalRemainingTimeTextView.text = "Общее время: 00:00"
                }
            }
        }.start()

        isPaused = false
        binding.startButton.visibility = View.GONE
        binding.pauseButton.visibility = View.VISIBLE
        binding.stopButton.visibility = View.GONE // Снова скрываем кнопку стоп
    }

    private fun stopTimer() {
        timer?.cancel()
        currentStepIndex = 0
        isPaused = false

        // Сбрасываем UI
        binding.timerText.text = "00:00" //.0" САМ
        binding.currentStepNameTextView.text = "Готовность"
        //binding.currentStepNameTextView.setTextColor(Color.WHITE) //САМ
        binding.nextStepTextView1.text = "Следующее: "
        binding.nextStepTextView2.text = "После: "

        // Сбрасываем цвета к значениям по умолчанию
        val defaultColor = ContextCompat.getColor(requireContext(), android.R.color.white)
        binding.currentStepNameTextView.setTextColor(defaultColor)
        binding.timerText.setTextColor(defaultColor)
        binding.nextStepTextView1.setTextColor(defaultColor)
        binding.nextStepTextView2.setTextColor(defaultColor)

        // Сбрасываем общее время
        if (steps.isNotEmpty()) {
            binding.totalRemainingTimeTextView.text = "Всего: ${formatDuration(totalWorkoutTime)}"
        } else {
            binding.totalRemainingTimeTextView.text = "Всего: 00:00"
        }

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

        // Фиксируем ширину текста, используя моноширинный шрифт
        //binding.timerText.text = String.format("%02d:%02d.%01d", minutes, seconds, milliseconds) // с милисекундами, десятыми
        binding.timerText.text = String.format("%02d:%02d", minutes, seconds) // САМ
    }

    private fun updateTotalRemainingTime() {
        if (currentStepIndex >= steps.size) {
            binding.totalRemainingTimeTextView.text = "Всего: 00:00"
            return
        }

        val remainingTime = timeRemaining + if (currentStepIndex + 1 < steps.size) {
            precomputedSums!![currentStepIndex + 1]
        } else {
            0
        }

        binding.totalRemainingTimeTextView.text = "Всего: ${formatDuration(remainingTime)}"
    }

    private fun updateNextStepsInfo() {
        val defaultColor = ContextCompat.getColor(requireContext(), android.R.color.black)

        // Первое следующее упражнение
        if (currentStepIndex + 1 < steps.size) {
            val nextStep1 = steps[currentStepIndex + 1]
            binding.nextStepTextView1.text = "Следующее: ${nextStep1.name} (${formatDuration(nextStep1.duration)})"
            binding.nextStepTextView1.setTextColor(nextStep1.color)
        } else {
            binding.nextStepTextView1.text = "Следующее: Конец тренировки"
            binding.nextStepTextView1.setTextColor(Color.WHITE)
        }

        // Второе следующее упражнение
        if (currentStepIndex + 2 < steps.size) {
            val nextStep2 = steps[currentStepIndex + 2]
            binding.nextStepTextView2.text = "После: ${nextStep2.name} (${formatDuration(nextStep2.duration)})"
            binding.nextStepTextView2.setTextColor(nextStep2.color)
        } else {
            binding.nextStepTextView2.text = "После: Конец тренировки"
            binding.nextStepTextView2.setTextColor(Color.WHITE)
        }
    }

    private fun formatDuration(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun playBeepSound() {
        try {
            soundPool?.play(soundId, 1.0f, 1.0f, 0, 0, 1.0f)
        } catch (e: Exception) {
            // Игнорируем ошибки воспроизведения звука
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
        soundPool?.release()
        soundPool = null
        _binding = null
    }
}