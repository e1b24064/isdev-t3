package oit.is.t3.isdev_t3.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import oit.is.t3.isdev_t3.model.QuizQuestion;

@SpringBootTest
class QuizQuestionMapperTest {

  @Autowired
  private QuizQuestionMapper quizQuestionMapper;

  @Test
  void 登録済み問題の問題文選択肢正解情報を取得できる() {
    List<QuizQuestion> questions = quizQuestionMapper.findAll();

    assertThat(questions).hasSizeGreaterThanOrEqualTo(3);
    QuizQuestion capitalQuestion = questions.stream()
        .filter(question -> question.getQuestionText().equals("日本の首都はどこ？"))
        .findFirst()
        .orElseThrow();
    assertThat(capitalQuestion.getId()).isPositive();
    assertThat(capitalQuestion.getOptionA()).isEqualTo("大阪");
    assertThat(capitalQuestion.getOptionB()).isEqualTo("東京");
    assertThat(capitalQuestion.getOptionC()).isEqualTo("京都");
    assertThat(capitalQuestion.getOptionD()).isEqualTo("札幌");
    assertThat(capitalQuestion.getCorrectOption()).isEqualTo(2);

    QuizQuestion planetQuestion = questions.stream()
        .filter(question -> question.getQuestionText().equals("最も太陽に近い惑星はどれ？"))
        .findFirst()
        .orElseThrow();
    assertThat(planetQuestion.getId()).isPositive();
    assertThat(planetQuestion.getCorrectOption()).isEqualTo(4);

    assertThat(questions).allSatisfy(question -> {
      assertThat(question.getId()).isPositive();
      assertThat(question.getQuestionText()).isNotBlank();
      assertThat(question.getOptionA()).isNotBlank();
      assertThat(question.getOptionB()).isNotBlank();
      assertThat(question.getOptionC()).isNotBlank();
      assertThat(question.getOptionD()).isNotBlank();
      assertThat(question.getCorrectOption()).isBetween(1, 4);
    });
  }
}
