package org.example.be_sp.controller;

import java.util.Set;

import org.example.be_sp.listener.WebSocketEventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API để check online status
 */
@RestController
@RequestMapping("/api/presence")
public class PresenceController {

    private final WebSocketEventListener eventListener;

    public PresenceController(WebSocketEventListener eventListener) {
        this.eventListener = eventListener;
    }

    /**
     * Lấy danh sách tất cả users đang online
     * GET /api/presence/online-users
     */
    @GetMapping("/online-users")
    public ResponseEntity<Set<Integer>> getOnlineUsers() {
        return ResponseEntity.ok(eventListener.getOnlineUsers());
    }

    /**
     * Check xem user cụ thể có online không
     * GET /api/presence/check/{userId}
     */
    @GetMapping("/check/{userId}")
    public ResponseEntity<Boolean> checkUserOnline(@PathVariable Integer userId) {
        return ResponseEntity.ok(eventListener.isUserOnline(userId));
    }
}
