package com.sparta.camp.java.FinalProject.domain.history.repository;

import com.sparta.camp.java.FinalProject.domain.history.entity.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {

}
