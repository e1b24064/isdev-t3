package oit.is.t3.isdev_t3.controller;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import oit.is.t3.isdev_t3.mapper.QuizQuestionMapper;
import oit.is.t3.isdev_t3.model.QuizQuestion;
import oit.is.t3.isdev_t3.service.QuizAnswerService;

@Controller
public class QuizQuestionController {

  private final QuizQuestionMapper quizQuestionMapper;
  private final QuizAnswerService quizAnswerService;

  public QuizQuestionController(QuizQuestionMapper quizQuestionMapper,
      QuizAnswerService quizAnswerService) {
    this.quizQuestionMapper = quizQuestionMapper;
    this.quizAnswerService = quizAnswerService;
  }

  @GetMapping("/quiz")
  public String quizPage(HttpSession session, Model model) {
    String role = (String) session.getAttribute("role");
    if (role == null) {
      return "redirect:/login";
    }

    model.addAttribute("quizStarted", quizAnswerService.isStarted());
    if (quizAnswerService.isStarted()) {
      model.addAttribute("question", quizAnswerService.getCurrentQuestion());
    }
    model.addAttribute("isAdmin", "ADMIN".equals(role));
    String username = (String) session.getAttribute("username");
    model.addAttribute("canAnswer", quizAnswerService.canAttempt(username));
    model.addAttribute("answerClaimed", quizAnswerService.hasAnswerer());
    model.addAttribute("score", quizAnswerService.getScore(username));
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
      quizAnswerService.start(questions.get(questionIndex));
    }
    return "redirect:/quiz";
  }

  @GetMapping("/quiz/status")
  public ResponseEntity<Boolean> quizStatus(HttpSession session) {
    if (session.getAttribute("role") == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    return ResponseEntity.ok(quizAnswerService.isStarted());
  }
}
