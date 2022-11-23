package edu.dac.springbootrestapibackend.controller;

import edu.dac.springbootrestapibackend.dto.request.LoginRequest;
import edu.dac.springbootrestapibackend.dto.request.RegisterRequest;
import edu.dac.springbootrestapibackend.dto.request.TokenRefreshRequest;
import edu.dac.springbootrestapibackend.dto.response.JwtResponse;
import edu.dac.springbootrestapibackend.dto.response.ResponseMessage;
import edu.dac.springbootrestapibackend.dto.response.TokenRefreshResponse;
import edu.dac.springbootrestapibackend.exception.TokenRefreshException;
import edu.dac.springbootrestapibackend.model.user.RefreshToken;
import edu.dac.springbootrestapibackend.model.user.Role;
import edu.dac.springbootrestapibackend.model.user.RoleName;
import edu.dac.springbootrestapibackend.model.user.User;
import edu.dac.springbootrestapibackend.security.jwt.JwtProvider;
import edu.dac.springbootrestapibackend.security.userprincipal.UserPrincipal;
import edu.dac.springbootrestapibackend.service.impl.RefreshTokenService;
import edu.dac.springbootrestapibackend.service.impl.RoleServiceImpl;
import edu.dac.springbootrestapibackend.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@RequestMapping("/api/v1/auth")
@RestController
@CrossOrigin(origins = "*")
public class AuthenticationController {
    @Autowired
    UserServiceImpl userService;

    @Autowired
    RoleServiceImpl roleService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        if (userService.existsByEmail(registerRequest.getEmail())) {
            return new ResponseEntity<>(new ResponseMessage("Error: Email is already in use!"), HttpStatus.OK);
        }

        User user = new User(registerRequest.getEmail(), passwordEncoder.encode(registerRequest.getPassword()),
                registerRequest.getFirstName(), registerRequest.getLastName(), registerRequest.getAddress(),
                registerRequest.getPhoneNumber());

        Set<String> strRoles = registerRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role advertiserRole = roleService.findByName(RoleName.ADVERTISER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(advertiserRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "ADMIN":
                        Role adminRole = roleService.findByName(RoleName.ADMIN).orElseThrow(
                                () -> new RuntimeException("Role not found"));
                        roles.add(adminRole);
                        break;
                    case "DAC":
                        Role dacRole = roleService.findByName(RoleName.DAC)
                                .orElseThrow(() -> new RuntimeException("Role not found"));
                        roles.add(dacRole);
                        break;
                    default:
                        Role advertiserRole = roleService.findByName(RoleName.ADVERTISER)
                                .orElseThrow(() -> new RuntimeException("Role not found"));
                        roles.add(advertiserRole);
                }
            });
        }

        user.setRoles(roles);
        userService.save(user);
        return new ResponseEntity<>(new ResponseMessage("Create Account Success!"), HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) throws Exception {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String token = jwtProvider.generateJwtToken(userPrincipal);

        List<String> roles = userPrincipal.getAuthorities().stream().map(item -> item.getAuthority())
                .collect(Collectors.toList());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userPrincipal.getId());

        return ResponseEntity
                .ok(new JwtResponse(token, refreshToken.getToken(), userPrincipal.getId(), userPrincipal.getUsername(),
                        userPrincipal.getFirstName(), userPrincipal.getLastName(), userPrincipal.getAddress(),
                        userPrincipal.getPhoneNumber(), roles));
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtProvider.generateTokenFromEmail(user.getEmail());
                    return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                        "Refresh token is not in database!"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();
        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    refreshTokenService.deleteByUserId(user.getId());
                    return ResponseEntity.ok(new ResponseMessage("Logout Success!"));

                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                        "Refresh token is not in database!"));
    }

}
