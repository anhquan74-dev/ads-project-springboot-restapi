package edu.dac.springbootrestapibackend.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/test")
public class TestController {
    @GetMapping("/all")
    public String allAccess() {
        return "Public Content.";
    }

    @GetMapping("/advertiser")
    // @PreAuthorize("hasRole('ADVERTISER') or hasRole('DAC') or hasRole('ADMIN')")
    public String userAccess() {
        return "ADVERTISER Content.";
    }

    @GetMapping("/dac")
    // @PreAuthorize("hasRole('DAC') or hasRole('ADMIN')")
    public String moderatorAccess() {
        return "DAC Board.";
    }

    @GetMapping("/admin")
    // @PreAuthorize("hasRole('ADMIN')")
    public String adminAccess() {
        return "Admin Board.";
    }
}
