package oit.is.t3.isdev_t3.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

@Controller
public class RoomController {

  private final Map<Long, Room> rooms = new ConcurrentHashMap<>();
  private final AtomicLong roomIdSequence = new AtomicLong(1L);

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
    return "room";
  }

  @PostMapping("/rooms/{roomId}/join")
  public String joinRoom(@PathVariable long roomId, HttpSession session) {
    String role = (String) session.getAttribute("role");
    String username = (String) session.getAttribute("username");
    if (!"PARTICIPANT".equals(role) || username == null) {
      return "redirect:/login";
    }

    Room room = rooms.get(roomId);
    if (room == null) {
      return "redirect:/title";
    }
    room.addParticipant(session.getId(), username);
    return "redirect:/rooms/" + roomId;
  }

  @PostMapping("/rooms/join")
  public String joinRoomById(@RequestParam long roomId, HttpSession session) {
    return joinRoom(roomId, session);
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

  public static class Room {

    private final long id;
    private final String mode;
    private final String ownerSessionId;
    private final String ownerUsername;
    private final Map<String, String> participants = new ConcurrentHashMap<>();

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
