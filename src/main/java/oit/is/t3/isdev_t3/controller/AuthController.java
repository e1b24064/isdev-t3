package oit.is.t3.isdev_t3.controller;

import oit.is.t3.isdev_t3.mapper.UserMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

  private final UserMapper userMapper;
  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  public AuthController(UserMapper userMapper) {
    this.userMapper = userMapper;
  }

  @GetMapping("/login")
  public String loginPage(HttpSession session, Model model) {
    String role = (String) session.getAttribute("role");
    if (role != null) {
      model.addAttribute("title", role.equals("ADMIN") ? "管理者タイトル画面" : "参加者タイトル画面");
    }
    return "login";
  }

  @PostMapping("/login")
  public String login(@RequestParam String username, @RequestParam String password,
      HttpSession session, Model model) {
    UserMapper.User user = userMapper.findByUsername(username);
    String encodedPassword = user == null ? null : user.password().replaceFirst("^\\{bcrypt\\}", "");
    if (user == null || !passwordEncoder.matches(password, encodedPassword)) {
      model.addAttribute("error", "ユーザー名またはパスワードが正しくありません。");
      return "login";
    }

    session.setAttribute("username", user.username());
    session.setAttribute("role", user.role());
    return "redirect:/title";
  }
}
