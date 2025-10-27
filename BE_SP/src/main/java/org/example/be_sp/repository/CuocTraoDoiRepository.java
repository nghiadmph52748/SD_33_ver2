package org.example.be_sp.repository;

import org.example.be_sp.entity.CuocTraoDoi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CuocTraoDoiRepository extends JpaRepository<CuocTraoDoi, Integer> {

    /**
     * Tìm conversation giữa 2 nhân viên (cả 2 chiều)
     */
    @Query("SELECT c FROM CuocTraoDoi c WHERE " +
           "((c.nhanVien1.id = :userId1 AND c.nhanVien2.id = :userId2) OR " +
           "(c.nhanVien1.id = :userId2 AND c.nhanVien2.id = :userId1)) AND " +
           "c.deleted = false")
    Optional<CuocTraoDoi> findConversationBetweenUsers(
        @Param("userId1") Integer userId1,
        @Param("userId2") Integer userId2
    );

    /**
     * Lấy tất cả cuộc trò chuyện của 1 người dùng
     * Sắp xếp theo thời gian tin nhắn cuối giảm dần (mới nhất trước)
     */
    @Query("SELECT c FROM CuocTraoDoi c WHERE " +
           "(c.nhanVien1.id = :userId OR c.nhanVien2.id = :userId) AND " +
           "c.deleted = false " +
           "ORDER BY c.thoiGianTinNhanCuoi DESC NULLS LAST")
    List<CuocTraoDoi> findAllConversationsByUser(@Param("userId") Integer userId);

    /**
     * Đếm tổng số tin nhắn chưa đọc của 1 người dùng (từ tất cả cuộc trò chuyện)
     */
    @Query("SELECT " +
           "COALESCE(SUM(CASE WHEN c.nhanVien1.id = :userId THEN c.soTinNhanChuaDocNv1 " +
           "                   WHEN c.nhanVien2.id = :userId THEN c.soTinNhanChuaDocNv2 " +
           "                   ELSE 0 END), 0) " +
           "FROM CuocTraoDoi c WHERE " +
           "(c.nhanVien1.id = :userId OR c.nhanVien2.id = :userId) AND " +
           "c.deleted = false")
    Long countTotalUnreadMessagesForUser(@Param("userId") Integer userId);

    /**
     * Kiểm tra cuộc trò chuyện có tồn tại giữa 2 người dùng không
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
           "FROM CuocTraoDoi c WHERE " +
           "((c.nhanVien1.id = :userId1 AND c.nhanVien2.id = :userId2) OR " +
           "(c.nhanVien1.id = :userId2 AND c.nhanVien2.id = :userId1)) AND " +
           "c.deleted = false")
    boolean existsConversationBetweenUsers(
        @Param("userId1") Integer userId1,
        @Param("userId2") Integer userId2
    );

    /**
     * Lấy các cuộc trò chuyện có tin nhắn chưa đọc của 1 người dùng
     */
    @Query("SELECT c FROM CuocTraoDoi c WHERE " +
           "((c.nhanVien1.id = :userId AND c.soTinNhanChuaDocNv1 > 0) OR " +
           "(c.nhanVien2.id = :userId AND c.soTinNhanChuaDocNv2 > 0)) AND " +
           "c.deleted = false " +
           "ORDER BY c.thoiGianTinNhanCuoi DESC")
    List<CuocTraoDoi> findConversationsWithUnreadMessages(@Param("userId") Integer userId);
}
