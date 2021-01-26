package com.example.android.guesstheword.screens.game

import android.os.CountDownTimer
import android.text.format.DateUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

private val CORRECT_BUZZ_PATTERN = longArrayOf(100, 100, 100, 100, 100, 100)
private val PANIC_BUZZ_PATTERN = longArrayOf(0, 200)
private val GAME_OVER_BUZZ_PATTERN = longArrayOf(0, 2000)
private val NO_BUZZ_PATTERN = longArrayOf(0)

enum class BuzzType(val pattern: LongArray) {
  CORRECT(CORRECT_BUZZ_PATTERN),
  GAME_OVER(GAME_OVER_BUZZ_PATTERN),
  COUNTDOWN_PANIC(PANIC_BUZZ_PATTERN),
  NO_BUZZ(NO_BUZZ_PATTERN)
}

class GameViewModel : ViewModel() {

  companion object {
    private const val DONE = 0L

    private const val ONE_SECOND = 1000L

    private const val COUNTDOWN_TIME = 10000L
  }

  private val timer: CountDownTimer

  private val currentTime = MutableLiveData<Long>()

  val currentTimeString = Transformations.map(currentTime) { time ->
    DateUtils.formatElapsedTime(time)
  }

  // The current word
  private val _word = MutableLiveData<String>()
  val word : LiveData<String>
    get() = _word

  // The current score
  private val _score = MutableLiveData<Int>()
  val score: LiveData<Int>
    get() = _score

  // The list of words - the front of the list is the next word to guess
  private lateinit var wordList: MutableList<String>

  private val _eventGameFinished = MutableLiveData<Boolean>()
  val eventGameFinished: LiveData<Boolean>
    get() = _eventGameFinished

  private val _eventBuzz = MutableLiveData<BuzzType>()
  val eventBuzz: LiveData<BuzzType>
    get() = _eventBuzz

  init {
    _eventGameFinished.value = false
    _eventBuzz.value = BuzzType.NO_BUZZ
    resetList()
    nextWord()
    _score.value = 0
    currentTime.value = COUNTDOWN_TIME / 1000

    timer = object : CountDownTimer(COUNTDOWN_TIME, ONE_SECOND) {
      override fun onTick(millisUntilFinished: Long) {
        currentTime.value = currentTime.value?.minus(1)

        if(currentTime.value!! < (COUNTDOWN_TIME / 3000)) {
          _eventBuzz.value = BuzzType.COUNTDOWN_PANIC
        }
      }

      override fun onFinish() {
        _eventGameFinished.value = true
        _eventBuzz.value = BuzzType.GAME_OVER
      }
    }

    timer.start()
  }

  /**
   * Resets the list of words and randomizes the order
   */
  private fun resetList() {
    wordList = mutableListOf(
        "queen",
        "hospital",
        "basketball",
        "cat",
        "change",
        "snail",
        "soup",
        "calendar",
        "sad",
        "desk",
        "guitar",
        "home",
        "railway",
        "zebra",
        "jelly",
        "car",
        "crow",
        "trade",
        "bag",
        "roll",
        "bubble"
    )
    wordList.shuffle()
  }

  /**
   * Moves to the next word in the list
   */
  private fun nextWord() {
    //Select and remove a word from the list
    if (wordList.isEmpty()) {
      resetList()
    }
    _word.value = wordList.removeAt(0)
  }

  /** Methods for buttons presses **/

  fun onSkip() {
    _score.value = (_score.value)?.minus(1)
    nextWord()
  }

  fun onCorrect() {
    _score.value = (_score.value)?.plus(1)
    nextWord()
    _eventBuzz.value = BuzzType.CORRECT
  }

  fun onGameFinishedComplete() {
    _eventGameFinished.value = false
  }

  fun onBuzzComplete() {
    _eventBuzz.value = BuzzType.NO_BUZZ
  }

  override fun onCleared() {
    super.onCleared()
    timer.cancel()
  }
}