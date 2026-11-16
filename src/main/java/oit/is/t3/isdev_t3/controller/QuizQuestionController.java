package oit.is.t3.isdev_t3.controller;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import oit.is.t3.isdev_t3.mapper.QuizQuestionMapper;
import oit.is.t3.isdev_t3.model.QuizQuestion;

@Controller
public class QuizQuestionController {

  private final QuizQuestionMapper quizQuestionMapper;
  private final AtomicBoolean quizStarted = new AtomicBoolean();
  private volatile QuizQuestion currentQuestion;

  public QuizQuestionController(QuizQuestionMapper quizQuestionMapper) {
    this.quizQuestionMapper = quizQuestionMapper;
  }

  @GetMapping("/quiz")
  public String quizPage(HttpSession session, Model model) {
    String role = (String) session.getAttribute("role");
    if (role == null) {
      return "redirect:/login";
    }

    model.addAttribute("quizStarted", quizStarted.get());
    if (quizStarted.get() && currentQuestion != null) {
      model.addAttribute("question", currentQuestion);
    }
    model.addAttribute("isAdmin", "ADMIN".equals(role));
    return "quiz";
  }

  @PostMapping("/quiz/start")
  public String startQuiz(HttpSession session) {
    if (!"ADMIN".equals(session.getAttribute("role"))) {
      return "redirect:/login";
    }
    List<QuizQuestion> questions = quizQuestionMapper.findAll();
    if (!questions.isEmpty()) {
      int questionIndex = ThreadLocalRandom.current().nextInt(questions.size());
      currentQuestion = questions.get(questionIndex);
    }
    quizStarted.set(true);
    return "redirect:/quiz";
  }

  @GetMapping("/quiz/status")
  public ResponseEntity<Boolean> quizStatus(HttpSession session) {
    if (session.getAttribute("role") == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    return ResponseEntity.ok(quizStarted.get());
  }
}
