package oit.is.t3.isdev_t3.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import oit.is.t3.isdev_t3.model.QuizQuestion;
import oit.is.t3.isdev_t3.service.QuizAnswerService;

@SpringBootTest
@AutoConfigureMockMvc
class IndividualQuizControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private QuizAnswerService quizAnswerService;

  private MockHttpSession adminSession;
  private MockHttpSession firstParticipantSession;
  private MockHttpSession secondParticipantSession;
  private QuizQuestion question;

  @BeforeEach
  void setUp() throws Exception {
    adminSession = login("admin");
    firstParticipantSession = login("participant");
    secondParticipantSession = login("participant");
    mockMvc.perform(post("/quiz/start").session(adminSession))
        .andExpect(status().is3xxRedirection());
    question = quizAnswerService.getCurrentQuestion();
  }

  @Test
  void 最初に解答ボタンを押した参加者だけが回答でき正解で10点増える() throws Exception {
    mockMvc.perform(post("/quiz/answer").session(firstParticipantSession)
        .param("option", String.valueOf(question.getCorrectOption())))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/quiz"));

    mockMvc.perform(get("/quiz").session(firstParticipantSession))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("得点: <span>10</span>")))
        .andExpect(content().string(containsString("正解です。")));

    mockMvc.perform(post("/quiz/answer").session(secondParticipantSession)
        .param("option", String.valueOf(question.getCorrectOption())))
        .andExpect(status().is3xxRedirection());

    mockMvc.perform(get("/quiz").session(secondParticipantSession))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("回答権がありません。")));
  }

  @Test
  void 回答権を得た参加者が不正解なら10点減る() throws Exception {
    int incorrectOption = question.getCorrectOption() == 1 ? 2 : 1;

    mockMvc.perform(post("/quiz/answer").session(firstParticipantSession)
        .param("option", String.valueOf(incorrectOption)))
        .andExpect(status().is3xxRedirection());

    mockMvc.perform(get("/quiz").session(firstParticipantSession))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("得点: <span>-10</span>")))
        .andExpect(content().string(containsString("不正解です。")));
  }

  @Test
  void 管理者は回答できない() throws Exception {
    mockMvc.perform(post("/quiz/answer").session(adminSession).param("option", "1"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login"));
  }

  private MockHttpSession login(String username) throws Exception {
    MockHttpSession session = new MockHttpSession();
    mockMvc.perform(post("/login").session(session)
        .param("username", username).param("password", "p@ss"))
        .andExpect(status().is3xxRedirection());
    return session;
  }
}
