package com.grocery.controller;

import com.grocery.model.AppUser;
import com.grocery.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public AuthController(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @GetMapping("/")
    public String home(HttpSession session) {
        return session.getAttribute("userId") == null ? "redirect:/login" : "redirect:/dashboard";
    }

    @GetMapping("/register")
    public String registerPage() { return "register"; }

    @PostMapping("/register")
    public String register(@RequestParam String name,
                           @RequestParam String email,
                           @RequestParam String password,
                           RedirectAttributes redirect) {
        String cleanName = name == null ? "" : name.trim();
        String cleanEmail = email == null ? "" : email.trim().toLowerCase();
        if (cleanName.isBlank() || cleanEmail.isBlank() || password == null || password.length() < 6) {
            redirect.addFlashAttribute("error", "Enter your name and email, and use a password of at least 6 characters.");
            return "redirect:/register";
        }
        if (users.existsByEmailIgnoreCase(cleanEmail)) {
            redirect.addFlashAttribute("error", "An account with this email already exists.");
            return "redirect:/register";
        }
        AppUser user = new AppUser();
        user.setName(cleanName);
        user.setEmail(cleanEmail);
        user.setPasswordHash(encoder.encode(password));
        users.save(user);
        redirect.addFlashAttribute("success", "Account created. Please sign in.");
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage() { return "login"; }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password,
                        HttpSession session, RedirectAttributes redirect) {
        AppUser user = users.findByEmailIgnoreCase(email.trim())
                .filter(found -> encoder.matches(password, found.getPasswordHash()))
                .orElse(null);
        if (user == null) {
            redirect.addFlashAttribute("error", "Invalid email or password.");
            return "redirect:/login";
        }
        session.setAttribute("userId", user.getId());
        session.setAttribute("userName", user.getName());
        return "redirect:/dashboard";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
