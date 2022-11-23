package edu.dac.springbootrestapibackend.service;

import edu.dac.springbootrestapibackend.model.user.Role;
import edu.dac.springbootrestapibackend.model.user.RoleName;

import java.util.Optional;

public interface IRoleService {
    Optional<Role> findByName(RoleName name);

}
