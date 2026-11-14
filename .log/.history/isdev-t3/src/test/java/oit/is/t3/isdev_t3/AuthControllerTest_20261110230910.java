package oit.is.t3.isdev_t3;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void adminCanLoginAndSeeAdminTitle() throws Exception {
		mockMvc.perform(post("/login")
				.param("username", "admin")
				.param("password", "p@ss"))
			.andExpect(status().isOk())
			.andExpect(view().name("login"))
			.andExpect(content().string(org.hamcrest.Matchers.containsString("管理者タイトル画面")));
	}

	@Test
	void participantCanLoginAndSeeParticipantTitle() throws Exception {
		mockMvc.perform(post("/login")
				.param("username", "participant")
				.param("password", "p@ss"))
			.andExpect(status().isOk())
			.andExpect(view().name("login"))
			.andExpect(content().string(org.hamcrest.Matchers.containsString("参加者タイトル画面")));
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