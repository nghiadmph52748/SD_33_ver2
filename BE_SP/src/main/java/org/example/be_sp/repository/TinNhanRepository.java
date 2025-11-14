package org.example.be_sp.repository;

import org.example.be_sp.entity.TinNhan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TinNhanRepository extends JpaRepository<TinNhan, Integer> {

    /**
     * Lấy tất cả tin nhắn giữa 2 người dùng (cả 2 chiều, hỗ trợ cả customer và staff)
     * Sắp xếp theo thời gian gửi giảm dần (mới nhất trước)
     */
    @Query("SELECT t FROM TinNhan t WHERE " +
           "(" +
           "  ((t.nguoiGui.id = :userId1 AND t.nguoiNhan.id = :userId2) OR " +
           "   (t.nguoiGui.id = :userId2 AND t.nguoiNhan.id = :userId1)) OR " +
           "  ((t.khachHangGui.id = :userId1 AND t.nguoiNhan.id = :userId2) OR " +
           "   (t.nguoiGui.id = :userId1 AND t.khachHangNhan.id = :userId2) OR " +
           "   (t.khachHangGui.id = :userId2 AND t.nguoiNhan.id = :userId1) OR " +
           "   (t.nguoiGui.id = :userId2 AND t.khachHangNhan.id = :userId1))" +
           ") AND t.deleted = false " +
           "ORDER BY t.thoiGianGui DESC")
    Page<TinNhan> findMessagesBetweenUsers(
        @Param("userId1") Integer userId1,
        @Param("userId2") Integer userId2,
        Pageable pageable
    );

    /**
     * Lấy tin nhắn chưa đọc của 1 người dùng (tin nhắn được gửi ĐẾN người này mà chưa đọc)
     */
    @Query("SELECT t FROM TinNhan t WHERE " +
           "t.nguoiNhan.id = :userId AND " +
           "t.daDoc = false AND " +
           "t.deleted = false " +
           "ORDER BY t.thoiGianGui DESC")
    List<TinNhan> findUnreadMessagesByReceiver(@Param("userId") Integer userId);

    /**
     * Đếm số tin nhắn chưa đọc giữa 2 người dùng (tin nhắn gửi TỪ senderId ĐẾN receiverId mà chưa đọc)
     */
    @Query("SELECT COUNT(t) FROM TinNhan t WHERE " +
           "t.nguoiGui.id = :senderId AND " +
           "t.nguoiNhan.id = :receiverId AND " +
           "t.daDoc = false AND " +
           "t.deleted = false")
    Long countUnreadMessagesBetweenUsers(
        @Param("senderId") Integer senderId,
        @Param("receiverId") Integer receiverId
    );

    /**
     * Đếm tổng số tin nhắn chưa đọc của 1 người dùng
     */
    @Query("SELECT COUNT(t) FROM TinNhan t WHERE " +
           "t.nguoiNhan.id = :userId AND " +
           "t.daDoc = false AND " +
           "t.deleted = false")
    Long countTotalUnreadMessages(@Param("userId") Integer userId);

    /**
     * Lấy tin nhắn cuối cùng giữa 2 người dùng
     */
    @Query("SELECT t FROM TinNhan t WHERE " +
           "((t.nguoiGui.id = :userId1 AND t.nguoiNhan.id = :userId2) OR " +
           "(t.nguoiGui.id = :userId2 AND t.nguoiNhan.id = :userId1)) AND " +
           "t.deleted = false " +
           "ORDER BY t.thoiGianGui DESC")
    List<TinNhan> findLastMessageBetweenUsers(
        @Param("userId1") Integer userId1,
        @Param("userId2") Integer userId2,
        Pageable pageable
    );

    /**
     * Đánh dấu tất cả tin nhắn chưa đọc từ senderId tới receiverId là đã đọc (hỗ trợ cả customer và staff)
     */
    @Modifying
    @Query("UPDATE TinNhan t SET t.daDoc = true, t.thoiGianDoc = CURRENT_TIMESTAMP " +
           "WHERE " +
           "(" +
           "  (t.nguoiGui.id = :senderId AND t.nguoiNhan.id = :receiverId) OR " +
           "  (t.khachHangGui.id = :senderId AND t.nguoiNhan.id = :receiverId) OR " +
           "  (t.nguoiGui.id = :senderId AND t.khachHangNhan.id = :receiverId) OR " +
           "  (t.khachHangGui.id = :senderId AND t.khachHangNhan.id = :receiverId)" +
           ") AND t.daDoc = false")
    int markMessagesAsRead(
        @Param("senderId") Integer senderId,
        @Param("receiverId") Integer receiverId
    );
}
