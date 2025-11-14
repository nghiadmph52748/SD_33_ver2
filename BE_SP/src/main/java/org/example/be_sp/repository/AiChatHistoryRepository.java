package org.example.be_sp.repository;

import org.example.be_sp.entity.AiChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiChatHistoryRepository extends JpaRepository<AiChatHistory, Integer> {
    
    /**
     * Get all AI chat history for a customer, ordered by timestamp
     */
    @Query("SELECT a FROM AiChatHistory a WHERE a.khachHang.id = :customerId ORDER BY a.timestamp ASC")
    List<AiChatHistory> findByCustomerIdOrderByTimestamp(@Param("customerId") Integer customerId);
    
    /**
     * Get AI chat history for a customer by session ID
     */
    @Query("SELECT a FROM AiChatHistory a WHERE a.khachHang.id = :customerId AND a.sessionId = :sessionId ORDER BY a.timestamp ASC")
    List<AiChatHistory> findByCustomerIdAndSessionId(@Param("customerId") Integer customerId, @Param("sessionId") String sessionId);
    
    /**
     * Get the most recent session ID for a customer (the session with the latest message)
     * Using JPQL with subquery to find the session with the maximum timestamp
     */
    @Query("SELECT a.sessionId FROM AiChatHistory a WHERE a.khachHang.id = :customerId AND a.timestamp = (SELECT MAX(a2.timestamp) FROM AiChatHistory a2 WHERE a2.khachHang.id = :customerId)")
    List<String> findMostRecentSessionIds(@Param("customerId") Integer customerId);
    
    /**
     * Check if customer has any AI chat history
     */
    @Query("SELECT COUNT(a) FROM AiChatHistory a WHERE a.khachHang.id = :customerId")
    Long countByCustomerId(@Param("customerId") Integer customerId);
}

