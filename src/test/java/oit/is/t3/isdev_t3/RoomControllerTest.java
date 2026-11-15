package oit.is.t3.isdev_t3;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class RoomControllerTest {

  @Autowired
  private MockMvc mockMvc;

  private MockHttpSession adminSession;
  private MockHttpSession participantSession;

  @BeforeEach
  void setUp() throws Exception {
    adminSession = new MockHttpSession();
    participantSession = new MockHttpSession();

    mockMvc.perform(post("/login")
        .session(adminSession)
        .param("username", "admin")
        .param("password", "p@ss"));

    mockMvc.perform(post("/login")
        .session(participantSession)
        .param("username", "participant")
        .param("password", "p@ss"));
  }

  @Test
  void adminCanCreateRoomAndSeeAdminControls() throws Exception {
    MvcResult createResult = mockMvc.perform(post("/rooms/create")
        .session(adminSession)
        .param("mode", "INDIVIDUAL"))
        .andExpect(status().is3xxRedirection())
        .andReturn();
    String roomUrl = createResult.getResponse().getRedirectedUrl();

    mockMvc.perform(get("/title")
        .session(adminSession))
        .andExpect(status().isOk())
        .andExpect(view().name("title"))
        .andExpect(content().string(containsString("<summary>部屋を作成</summary>")))
        .andExpect(content().string(containsString("value=\"INDIVIDUAL\"")))
        .andExpect(content().string(containsString("value=\"TEAM\"")))
        .andExpect(content().string(not(containsString("現在の部屋"))));

    mockMvc.perform(get(roomUrl)
        .session(adminSession))
        .andExpect(status().isOk())
        .andExpect(view().name("room"))
        .andExpect(content().string(containsString("個人戦")))
        .andExpect(content().string(containsString("ゲーム開始")));
  }

  @Test
  void participantCanJoinRoomAndCannotSeeStartButton() throws Exception {
    String roomUrl = mockMvc.perform(post("/rooms/create")
        .session(adminSession)
        .param("mode", "TEAM"))
        .andExpect(status().is3xxRedirection())
        .andReturn()
        .getResponse()
        .getRedirectedUrl();

    mockMvc.perform(post(roomUrl + "/join")
        .session(participantSession))
        .andExpect(status().is3xxRedirection());

    mockMvc.perform(get(roomUrl)
        .session(participantSession))
        .andExpect(status().isOk())
        .andExpect(view().name("room"))
        .andExpect(content().string(containsString("チーム戦")))
        .andExpect(content().string(containsString("participant")))
        .andExpect(content().string(not(containsString("現在の部屋"))))
        .andExpect(content().string(not(containsString("ゲーム開始"))));
  }

  @Test
  void adminCanRefreshParticipantListAfterParticipantJoins() throws Exception {
    String roomUrl = mockMvc.perform(post("/rooms/create")
        .session(adminSession)
        .param("mode", "TEAM"))
        .andExpect(status().is3xxRedirection())
        .andReturn()
        .getResponse()
        .getRedirectedUrl();

    mockMvc.perform(post(roomUrl + "/join")
        .session(participantSession))
        .andExpect(status().is3xxRedirection());

    mockMvc.perform(get(roomUrl + "/participants")
        .session(adminSession))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0]").value("participant"));

    mockMvc.perform(get(roomUrl)
        .session(adminSession))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("setInterval(refreshParticipants, 3000)")))
        .andExpect(content().string(containsString("/participants`")));
  }

  @Test
  void participantLeavingRemovesNameFromAdminParticipantList() throws Exception {
    String roomUrl = mockMvc.perform(post("/rooms/create")
        .session(adminSession)
        .param("mode", "TEAM"))
        .andExpect(status().is3xxRedirection())
        .andReturn()
        .getResponse()
        .getRedirectedUrl();

    mockMvc.perform(post(roomUrl + "/join")
        .session(participantSession))
        .andExpect(status().is3xxRedirection());

    mockMvc.perform(get(roomUrl)
        .session(participantSession))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("ルームから退出")));

    mockMvc.perform(post(roomUrl + "/leave")
        .session(participantSession))
        .andExpect(status().is3xxRedirection());

    mockMvc.perform(get(roomUrl + "/participants")
        .session(adminSession))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());
  }

  @Test
  void onlyRoomCreatingAdminCanSeeStartButton() throws Exception {
    String roomUrl = mockMvc.perform(post("/rooms/create")
        .session(adminSession)
        .param("mode", "INDIVIDUAL"))
        .andExpect(status().is3xxRedirection())
        .andReturn()
        .getResponse()
        .getRedirectedUrl();

    MockHttpSession anotherAdminSession = new MockHttpSession();
    mockMvc.perform(post("/login")
        .session(anotherAdminSession)
        .param("username", "admin")
        .param("password", "p@ss"));

    mockMvc.perform(get(roomUrl)
        .session(anotherAdminSession))
        .andExpect(status().isOk())
        .andExpect(content().string(not(containsString("ゲーム開始"))));
  }
}
