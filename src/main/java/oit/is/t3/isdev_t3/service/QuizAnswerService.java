package oit.is.t3.isdev_t3.service;

import java.util.HashMap;
import java.util.Map;
import oit.is.t3.isdev_t3.model.QuizQuestion;
import org.springframework.stereotype.Service;

@Service
public class QuizAnswerService {

  private QuizQuestion currentQuestion;
  private String answerer;
  private boolean answered;
  private final Map<String, Integer> scores = new HashMap<>();

  public synchronized void start(QuizQuestion question) {
    currentQuestion = question;
    answerer = null;
    answered = false;
    scores.clear();
  }

  public synchronized boolean isStarted() {
    return currentQuestion != null;
  }

  public synchronized QuizQuestion getCurrentQuestion() {
    return currentQuestion;
  }

  public synchronized boolean claim(String username) {
    if (!isStarted() || answerer != null || answered) {
      return false;
    }
    answerer = username;
    scores.putIfAbsent(username, 0);
    return true;
  }

  public synchronized boolean isAnswerer(String username) {
    return username != null && username.equals(answerer) && !answered;
  }

  public synchronized boolean canAttempt(String username) {
    return isStarted() && !answered && (answerer == null || isAnswerer(username));
  }

  public synchronized boolean hasAnswerer() {
    return answerer != null;
  }

  public synchronized int getScore(String username) {
    return scores.getOrDefault(username, 0);
  }

  public synchronized AnswerResult answer(String username, int selectedOption) {
    if (!isStarted() || answered || (answerer != null && !username.equals(answerer))) {
      return AnswerResult.notAllowed(getScore(username));
    }

    answerer = username;
    scores.putIfAbsent(username, 0);

    boolean correct = currentQuestion.getCorrectOption() == selectedOption;
    int score = getScore(username) + (correct ? 10 : -10);
    scores.put(username, score);
    answered = true;
    return new AnswerResult(true, correct, score);
  }

  public record AnswerResult(boolean accepted, boolean correct, int score) {

    static AnswerResult notAllowed(int score) {
      return new AnswerResult(false, false, score);
    }
  }
}
