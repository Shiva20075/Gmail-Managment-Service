
package com.example.demo.Repo;

import com.example.demo.Model.GmsUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface userInterface
        extends JpaRepository<GmsUser, Long> {

    Optional<GmsUser> findByEmail(String email);
}
