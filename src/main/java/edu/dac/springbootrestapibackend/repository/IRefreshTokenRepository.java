package edu.dac.springbootrestapibackend.repository;

import java.util.Optional;

import edu.dac.springbootrestapibackend.model.user.RefreshToken;
import edu.dac.springbootrestapibackend.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

@Repository
public interface IRefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    int deleteByUser(User user);
}
