package oit.is.t3.isdev_t3;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void adminCanLoginAndSeeRoomCreationControls() throws Exception {
    MvcResult loginResult = mockMvc.perform(post("/login")
        .param("username", "admin")
        .param("password", "p@ss"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/title"))
        .andReturn();

    mockMvc.perform(get("/title")
        .session((MockHttpSession) loginResult.getRequest().getSession(false)))
        .andExpect(status().isOk())
        .andExpect(view().name("title"))
        .andExpect(content().string(containsString("部屋を作成")))
        .andExpect(content().string(containsString("個人戦")))
        .andExpect(content().string(containsString("チーム戦")));
  }

  @Test
  void participantCanLoginAndSeeRoomJoinControlsWithoutAdminControls() throws Exception {
    MvcResult loginResult = mockMvc.perform(post("/login")
        .param("username", "participant")
        .param("password", "p@ss"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/title"))
        .andReturn();

    mockMvc.perform(get("/title")
        .session((MockHttpSession) loginResult.getRequest().getSession(false)))
        .andExpect(status().isOk())
        .andExpect(view().name("title"))
        .andExpect(content().string(containsString("部屋に参加")))
        .andExpect(content().string(containsString("部屋ID")))
        .andExpect(content().string(not(containsString("部屋を作成"))));
  }

  @Test
  void invalidCredentialsStayOnLoginPageWithError() throws Exception {
    mockMvc.perform(post("/login")
        .param("username", "admin")
        .param("password", "wrong"))
        .andExpect(status().isOk())
        .andExpect(view().name("login"))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("ユーザー名またはパスワードが正しくありません。")))
        .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("タイトル画面"))));
  }
}
