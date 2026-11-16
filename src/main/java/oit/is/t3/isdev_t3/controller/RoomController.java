package oit.is.t3.isdev_t3.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import oit.is.t3.isdev_t3.mapper.TeamMapper;
import oit.is.t3.isdev_t3.service.TeamAssignmentService;

@Controller
public class RoomController {

  private final Map<Long, Room> rooms = new ConcurrentHashMap<>();
  private final AtomicLong roomIdSequence = new AtomicLong(1L);

  private final TeamMapper teamMapper;
  private final TeamAssignmentService teamAssignmentService;

  public RoomController(TeamMapper teamMapper, TeamAssignmentService teamAssignmentService) {
    this.teamMapper = teamMapper;
    this.teamAssignmentService = teamAssignmentService;
  }

  @GetMapping("/title")
  public String titlePage(HttpSession session, Model model) {
    String role = (String) session.getAttribute("role");
    String username = (String) session.getAttribute("username");
    if (role == null || username == null) {
      return "redirect:/login";
    }

    model.addAttribute("role", role);
    model.addAttribute("username", username);
    return "title";
  }

  @PostMapping("/rooms/create")
  public String createRoom(HttpSession session, @RequestParam String mode) {
    String role = (String) session.getAttribute("role");
    String username = (String) session.getAttribute("username");
    if (!"ADMIN".equals(role) || username == null) {
      return "redirect:/login";
    }

    String normalizedMode = "TEAM".equalsIgnoreCase(mode) ? "TEAM" : "INDIVIDUAL";
    long roomId = roomIdSequence.getAndIncrement();
    Room room = new Room(roomId, normalizedMode, session.getId(), username);
    rooms.put(roomId, room);
    return "redirect:/rooms/" + room.getId();
  }

  @GetMapping("/rooms/{roomId}")
  public String roomPage(@PathVariable long roomId, HttpSession session, Model model) {
    String role = (String) session.getAttribute("role");
    String username = (String) session.getAttribute("username");
    if (role == null || username == null) {
      return "redirect:/login";
    }

    Room room = rooms.get(roomId);
    if (room == null) {
      return "redirect:/title";
    }

    model.addAttribute("room", room);
    model.addAttribute("role", role);
    model.addAttribute("username", username);
    model.addAttribute("isAdmin", "ADMIN".equals(role) && room.isOwnedBy(session.getId()));

    Map<Integer, List<String>> teams = new TreeMap<>();
    for (TeamMapper.TeamMemberRow row : teamMapper.selectMembersByRoom(roomId)) {
      teams.computeIfAbsent(row.teamNumber(), k -> new ArrayList<>()).add(row.username());
    }
    model.addAttribute("teams", teams);
    return "room";
  }

  @PostMapping("/rooms/{roomId}/join")
  public String joinRoom(@PathVariable long roomId, HttpSession session,
      RedirectAttributes redirectAttributes) {
    String role = (String) session.getAttribute("role");
    String username = (String) session.getAttribute("username");
    if (!"PARTICIPANT".equals(role) || username == null) {
      return "redirect:/login";
    }

    Room room = rooms.get(roomId);
    if (room == null) {
      return "redirect:/title";
    }
    if (room.getParticipants().size() >= TeamAssignmentService.MAX_PARTICIPANTS) {
      redirectAttributes.addFlashAttribute("joinError", "定員(40人)に達しているため参加できません。");
      return "redirect:/rooms/" + roomId;
    }
    room.addParticipant(session.getId(), username);
    return "redirect:/rooms/" + roomId;
  }

  @PostMapping("/rooms/join")
  public String joinRoomById(@RequestParam long roomId, HttpSession session,
      RedirectAttributes redirectAttributes) {
    return joinRoom(roomId, session, redirectAttributes);
  }

  @PostMapping("/rooms/{roomId}/leave")
  public String leaveRoom(@PathVariable long roomId, HttpSession session) {
    String role = (String) session.getAttribute("role");
    if (!"PARTICIPANT".equals(role)) {
      return "redirect:/login";
    }

    Room room = rooms.get(roomId);
    if (room != null) {
      room.removeParticipant(session.getId());
    }
    return "redirect:/title";
  }

  @GetMapping("/rooms/{roomId}/participants")
  public ResponseEntity<List<String>> participants(@PathVariable long roomId, HttpSession session) {
    String role = (String) session.getAttribute("role");
    String username = (String) session.getAttribute("username");
    if (role == null || username == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    Room room = rooms.get(roomId);
    if (room == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(room.getParticipants());
  }

  @PostMapping("/rooms/{roomId}/preference")
  public String setPreference(@PathVariable long roomId, @RequestParam String preferredUsername,
      HttpSession session) {
    String role = (String) session.getAttribute("role");
    String username = (String) session.getAttribute("username");
    if (!"PARTICIPANT".equals(role) || username == null) {
      return "redirect:/login";
    }
    teamMapper.deletePreference(roomId, username);
    teamMapper.insertPreference(roomId, username, preferredUsername);
    return "redirect:/rooms/" + roomId;
  }

  @PostMapping("/rooms/{roomId}/assign-teams")
  public String assignTeams(@PathVariable long roomId, HttpSession session) {
    String role = (String) session.getAttribute("role");
    Room room = rooms.get(roomId);
    if (room == null || !"ADMIN".equals(role) || !room.isOwnedBy(session.getId())) {
      return "redirect:/login";
    }

    List<String> participants = room.getParticipants();
    Map<String, String> preferences = new HashMap<>();
    for (TeamMapper.PreferenceRow row : teamMapper.selectPreferencesByRoom(roomId)) {
      preferences.put(row.username(), row.preferredUsername());
    }

    Map<Integer, List<String>> teams = teamAssignmentService.assign(participants, preferences);

    teamMapper.deleteMembersByRoom(roomId);
    teamMapper.deleteTeamsByRoom(roomId);
    for (Map.Entry<Integer, List<String>> entry : teams.entrySet()) {
      teamMapper.insertTeam(roomId, entry.getKey());
      for (String member : entry.getValue()) {
        teamMapper.insertMember(roomId, entry.getKey(), member);
      }
    }
    return "redirect:/rooms/" + roomId;
  }

  public static class Room {

    private final long id;
    private final String mode;
    private final String ownerSessionId;
    private final String ownerUsername;
    private final Map<String, String> participants = java.util.Collections
        .synchronizedMap(new java.util.LinkedHashMap<>());

    Room(long id, String mode, String ownerSessionId, String ownerUsername) {
      this.id = id;
      this.mode = mode;
      this.ownerSessionId = ownerSessionId;
      this.ownerUsername = ownerUsername;
    }

    public long getId() {
      return id;
    }

    public String getMode() {
      return mode;
    }

    public String getOwner() {
      return ownerUsername;
    }

    public List<String> getParticipants() {
      return new ArrayList<>(participants.values());
    }

    boolean isOwnedBy(String sessionId) {
      return ownerSessionId.equals(sessionId);
    }

    void addParticipant(String sessionId, String username) {
      participants.putIfAbsent(sessionId, username);
    }

    void removeParticipant(String sessionId) {
      participants.remove(sessionId);
    }
  }
}
