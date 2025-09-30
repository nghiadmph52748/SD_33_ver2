package org.example.be_sp.service;

import java.util.List;

import org.example.be_sp.entity.ChatLieu;
import org.example.be_sp.exception.ApiException;
import org.example.be_sp.model.request.ChatLieuRequest;
import org.example.be_sp.model.response.ChatLieuResponse;
import org.example.be_sp.model.response.PagingResponse;
import org.example.be_sp.repository.ChatLieuRepository;
import org.example.be_sp.util.GenericCrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class ChatLieuService extends GenericCrudService<ChatLieu, Integer, ChatLieuResponse, ChatLieuRequest> {

    @Autowired
    private ChatLieuRepository chatLieuRepository;

    public ChatLieuService(Class<ChatLieu> entity, Class<ChatLieuResponse> chatLieuResponseClass, Class<ChatLieuRequest> chatLieuRequestClass, JpaRepository<ChatLieu, Integer> chatLieuRepository) {
        super(entity, chatLieuResponseClass, chatLieuRequestClass, chatLieuRepository);
    }

    public PagingResponse<ChatLieuResponse> pagingwithdeletedfalse(int page, int size) {
        return new PagingResponse<>(chatLieuRepository.findAllByDeleted(false, PageRequest.of(page, size)).map(ChatLieuResponse::new), page);
    }

    public List<ChatLieuResponse> getAllChatLieu() {
        return chatLieuRepository.findAllByDeleted(false).stream().map(ChatLieuResponse::new).toList();
    }

    public void updateStatus(Integer id) {
        ChatLieu chatLieu = chatLieuRepository.findById(id).orElseThrow(() -> new ApiException("ChatLieu not found", "404"));
        chatLieu.setDeleted(true);
        chatLieuRepository.save(chatLieu);
    }
}
