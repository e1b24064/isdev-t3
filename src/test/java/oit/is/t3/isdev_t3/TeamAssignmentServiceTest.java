package oit.is.t3.isdev_t3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import oit.is.t3.isdev_t3.service.TeamAssignmentService;

class TeamAssignmentServiceTest {

  @Test
  void fourParticipantsFormOneTeam() {
    var service = new TeamAssignmentService();
    var result = service.assign(List.of("a", "b", "c", "d"), Map.of());
    assertEquals(1, result.size());
    assertEquals(4, result.get(1).size());
  }

  @Test
  void eachTeamHasAtMostFourMembers() {
    var service = new TeamAssignmentService();
    var result = service.assign(List.of("a", "b", "c", "d", "e"), Map.of());
    assertTrue(result.values().stream().allMatch(team -> team.size() <= 4));
  }

  @Test
  void mutuallyPreferredPairJoinsSameTeam() {
    var service = new TeamAssignmentService();
    var prefs = Map.of("a", "b", "b", "a");
    var result = service.assign(List.of("a", "b", "c", "d", "e"), prefs);
    boolean sameTeam = result.values().stream().anyMatch(team -> team.contains("a") && team.contains("b"));
    assertTrue(sameTeam);
  }

  @Test
  void throwsWhenMoreThanFortyParticipants() {
    var service = new TeamAssignmentService();
    List<String> many = new ArrayList<>();
    for (int i = 0; i < 41; i++) {
      many.add("u" + i);
    }
    assertThrows(IllegalArgumentException.class, () -> service.assign(many, Map.of()));
  }
}
