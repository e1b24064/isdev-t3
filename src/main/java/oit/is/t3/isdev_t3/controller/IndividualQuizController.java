package oit.is.t3.isdev_t3.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import oit.is.t3.isdev_t3.service.QuizAnswerService;

@Controller
public class IndividualQuizController {

  private final QuizAnswerService quizAnswerService;

  public IndividualQuizController(QuizAnswerService quizAnswerService) {
    this.quizAnswerService = quizAnswerService;
  }

  @PostMapping("/quiz/answer")
  public String answer(@RequestParam("option") int option, HttpSession session,
      RedirectAttributes redirectAttributes) {
    String username = participantUsername(session);
    if (username == null) {
      return "redirect:/login";
    }

    QuizAnswerService.AnswerResult result = quizAnswerService.answer(username, option);
    if (!result.accepted()) {
      redirectAttributes.addFlashAttribute("answerMessage", "回答権がありません。");
    } else {
      redirectAttributes.addFlashAttribute("answerMessage",
          result.correct() ? "正解です。" : "不正解です。");
    }
    return "redirect:/quiz";
  }

  private String participantUsername(HttpSession session) {
    if (!"PARTICIPANT".equals(session.getAttribute("role"))) {
      return null;
    }
    return (String) session.getAttribute("username");
  }
}
