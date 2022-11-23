package edu.dac.springbootrestapibackend.service.impl;

import edu.dac.springbootrestapibackend.model.user.Role;
import edu.dac.springbootrestapibackend.model.user.RoleName;
import edu.dac.springbootrestapibackend.repository.IRoleRepository;
import edu.dac.springbootrestapibackend.service.IRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RoleServiceImpl implements IRoleService {
    @Autowired
    IRoleRepository roleRepository;

    @Override
    public Optional<Role> findByName(RoleName name) {
        return roleRepository.findByName(name);
    }
}
