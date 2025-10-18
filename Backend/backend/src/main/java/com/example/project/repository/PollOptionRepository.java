package com.example.project.repository;

import com.example.project.entity.PollOption;
import com.example.project.entity.Poll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PollOptionRepository extends JpaRepository<PollOption, Long> {
    List<PollOption> findByPoll(Poll poll);
}
