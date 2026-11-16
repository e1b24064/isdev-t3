package oit.is.t3.isdev_t3.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import oit.is.t3.isdev_t3.model.QuizQuestion;

@Mapper
public interface QuizQuestionMapper {

  @Select("SELECT id, question_text AS questionText, option_a AS optionA, "
      + "option_b AS optionB, option_c AS optionC, option_d AS optionD, "
      + "correct_option AS correctOption FROM quiz_questions ORDER BY id")
  List<QuizQuestion> findAll();
}
