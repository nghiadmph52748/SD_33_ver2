package org.example.be_sp.controller;

import org.example.be_sp.model.request.ChatLieuRequest;
import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.service.ChatLieuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat-lieu-management")
@CrossOrigin(origins = "*")
public class ChatLieuController {
    @Autowired
    private ChatLieuService chatLieuService;

    @GetMapping("/playlist")
    public ResponseObject<?> getAll() {
        return new ResponseObject<>(chatLieuService.getAllChatLieu());
    }

    @GetMapping("/paging")
    public ResponseObject<?> paging(@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "10") Integer size) {
        return new ResponseObject<>(chatLieuService.paging(page, size), "Hiển thị trang" + page);
    }

    @GetMapping("/detail/{id}")
    public ResponseObject<?> detail(@PathVariable("id") Integer id) {
        return new ResponseObject<>(chatLieuService.getById(id), "Hiển thị chi tiết sản phẩm" + id);
    }

    @PostMapping("/add")
    public ResponseObject<?> add(@RequestBody ChatLieuRequest chatLieuRequest) {
        chatLieuService.add(chatLieuRequest);
        return new ResponseObject<>(true,null, "Thêm mới thành công");
    }

    @PutMapping("/update/{id}")
    public ResponseObject<?> update(@PathVariable("id") Integer id, @RequestBody ChatLieuRequest chatLieuRequest) {
        chatLieuService.update(id, chatLieuRequest);
        return new ResponseObject<>(true,null, "Cập nhật thành công");
    }

    @PutMapping("/update/status/{id}")
    public ResponseObject<?> updateStatus(@PathVariable ("id") Integer id) {
        chatLieuService.updateStatus(id);
        return new ResponseObject<>(true,null, "Cập nhật trạng thái thành công");
    }
}
