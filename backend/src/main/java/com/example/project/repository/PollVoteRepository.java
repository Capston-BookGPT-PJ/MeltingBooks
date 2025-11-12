package com.example.project.repository;

import com.example.project.entity.PollVote;
import com.example.project.entity.PollOption;
import com.example.project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PollVoteRepository extends JpaRepository<PollVote, Long> {
    long countByOption(PollOption option);
    Optional<PollVote> findByUserAndOption(User user, PollOption option);
}
