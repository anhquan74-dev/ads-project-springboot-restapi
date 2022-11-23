package edu.dac.springbootrestapibackend.repository;

import edu.dac.springbootrestapibackend.model.user.Role;
import edu.dac.springbootrestapibackend.model.user.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IRoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}