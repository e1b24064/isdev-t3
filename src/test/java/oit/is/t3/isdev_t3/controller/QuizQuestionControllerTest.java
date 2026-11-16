package oit.is.t3.isdev_t3.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;
import oit.is.t3.isdev_t3.mapper.QuizQuestionMapper;
import oit.is.t3.isdev_t3.model.QuizQuestion;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class QuizQuestionControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private QuizQuestionMapper quizQuestionMapper;

  private MockHttpSession adminSession;
  private MockHttpSession participantSession;

  @BeforeEach
  void setUp() throws Exception {
    adminSession = new MockHttpSession();
    participantSession = new MockHttpSession();
    mockMvc.perform(post("/login").session(adminSession)
        .param("username", "admin").param("password", "p@ss"));
    mockMvc.perform(post("/login").session(participantSession)
        .param("username", "participant").param("password", "p@ss"));
  }

  @Test
  void 管理者が開始すると問題文と4つの選択肢を表示する() throws Exception {
    mockMvc.perform(post("/quiz/start").session(adminSession))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/quiz"));

    MvcResult result = mockMvc.perform(get("/quiz").session(participantSession))
        .andExpect(status().isOk())
        .andExpect(view().name("quiz"))
        .andExpect(content().string(not(containsString("correctOption"))))
        .andReturn();

    String response = result.getResponse().getContentAsString();
    List<QuizQuestion> questions = quizQuestionMapper.findAll();
    List<QuizQuestion> displayedQuestions = questions.stream()
        .filter(question -> response.contains(question.getQuestionText()))
        .toList();
    org.assertj.core.api.Assertions.assertThat(displayedQuestions).hasSize(1);
    QuizQuestion displayedQuestion = displayedQuestions.get(0);
    org.assertj.core.api.Assertions.assertThat(response).contains(displayedQuestion.getOptionA(),
        displayedQuestion.getOptionB(), displayedQuestion.getOptionC(), displayedQuestion.getOptionD());
  }

  @Test
  void 管理者が開始するまで参加者には問題を表示しない() throws Exception {
    MvcResult result = mockMvc.perform(get("/quiz").session(participantSession))
        .andExpect(status().isOk())
        .andExpect(view().name("quiz"))
        .andReturn();

    String response = result.getResponse().getContentAsString();
    quizQuestionMapper.findAll().forEach(
        question -> org.assertj.core.api.Assertions.assertThat(response).doesNotContain(question.getQuestionText()));
  }

  @Test
  void 管理者が開始すると参加者にも開始状態を通知する() throws Exception {
    mockMvc.perform(get("/quiz/status").session(participantSession))
        .andExpect(status().isOk())
        .andExpect(content().string("false"));

    mockMvc.perform(post("/quiz/start").session(adminSession))
        .andExpect(status().is3xxRedirection());

    mockMvc.perform(get("/quiz/status").session(participantSession))
        .andExpect(status().isOk())
        .andExpect(content().string("true"));
  }

  @Test
  void 管理者と参加者には同じ問題を表示する() throws Exception {
    mockMvc.perform(post("/quiz/start").session(adminSession))
        .andExpect(status().is3xxRedirection());

    MvcResult adminResult = mockMvc.perform(get("/quiz").session(adminSession))
        .andExpect(status().isOk())
        .andReturn();
    MvcResult participantResult = mockMvc.perform(get("/quiz").session(participantSession))
        .andExpect(status().isOk())
        .andReturn();

    org.assertj.core.api.Assertions.assertThat(adminResult.getResponse().getContentAsString())
        .isEqualTo(participantResult.getResponse().getContentAsString());
  }
}
