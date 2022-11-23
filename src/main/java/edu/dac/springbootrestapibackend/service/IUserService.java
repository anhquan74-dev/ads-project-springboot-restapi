package edu.dac.springbootrestapibackend.service;

import edu.dac.springbootrestapibackend.model.user.User;

import java.util.Optional;

public interface IUserService {
    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    User save(User user);
}
