package oit.is.t3.isdev_t3.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class TeamAssignmentService {

  public static final int MAX_PARTICIPANTS = 40;
  private static final int MAX_TEAM_SIZE = 4;
  private static final int MAX_TEAMS = 10;

  public Map<Integer, List<String>> assign(List<String> participants, Map<String, String> preferences) {
    if (participants.size() > MAX_PARTICIPANTS) {
      throw new IllegalArgumentException("参加者が上限(" + MAX_PARTICIPANTS + "人)を超えています");
    }

    int teamCount = Math.min(MAX_TEAMS,
        Math.max(1, (int) Math.ceil(participants.size() / (double) MAX_TEAM_SIZE)));
    Map<Integer, List<String>> teams = new LinkedHashMap<>();
    for (int i = 1; i <= teamCount; i++) {
      teams.put(i, new ArrayList<>());
    }

    List<String> placed = new ArrayList<>();

    // ① 相互希望のペアを先着順で先に配置
    for (String a : participants) {
      if (placed.contains(a))
        continue;
      String b = preferences.get(a);
      boolean mutual = b != null && a.equals(preferences.get(b)) && participants.contains(b) && !placed.contains(b);
      if (mutual) {
        Integer team = findOpenTeam(teams, 2);
        if (team != null) {
          teams.get(team).add(a);
          teams.get(team).add(b);
          placed.add(a);
          placed.add(b);
        }
      }
    }

    // ② 残りはランダムに配置
    List<String> remaining = new ArrayList<>();
    for (String p : participants) {
      if (!placed.contains(p))
        remaining.add(p);
    }
    Collections.shuffle(remaining);
    for (String p : remaining) {
      Integer team = findOpenTeam(teams, 1);
      if (team != null)
        teams.get(team).add(p);
    }

    return teams;
  }

  private Integer findOpenTeam(Map<Integer, List<String>> teams, int needed) {
    for (Map.Entry<Integer, List<String>> e : teams.entrySet()) {
      if (e.getValue().size() + needed <= MAX_TEAM_SIZE)
        return e.getKey();
    }
    return null;
  }
}
