package org.example.be_sp.repository;

import java.util.List;

import org.example.be_sp.entity.ChatLieu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatLieuRepository extends JpaRepository<ChatLieu, Integer> {

    ChatLieu findChatLieuById(Integer id);

    @Query("SELECT c FROM ChatLieu c WHERE c.deleted = ?1")
    List<ChatLieu> findAllByDeleted(Boolean deleted);

    Page<ChatLieu> findAllByDeleted(Boolean deleted, Pageable pageable);
}
