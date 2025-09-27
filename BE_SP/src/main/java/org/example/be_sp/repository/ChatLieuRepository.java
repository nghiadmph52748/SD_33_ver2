package org.example.be_sp.repository;

import org.example.be_sp.entity.ChatLieu;
import org.example.be_sp.model.response.ChatLieuResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatLieuRepository extends JpaRepository<ChatLieu, Integer> {
    ChatLieu findChatLieuById(Integer id);
    @Query("SELECT c FROM ChatLieu c WHERE c.deleted = ?1")
    List<ChatLieu> findAllByDeleted(Boolean deleted);
}
