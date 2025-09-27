package org.example.be_sp.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.be_sp.entity.ChatLieu;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatLieuResponse {
    private Integer id;
    private String maChatLieu;
    private String tenChatLieu;
    private Boolean trangThai;
    private Boolean deleted;
    private LocalDate createAt;
    private Integer createBy;
    private LocalDate updateAt;
    private Integer updateBy;

    public ChatLieuResponse(ChatLieu data) {
        this.id = data.getId();
        this.maChatLieu = data.getMaChatLieu();
        this.tenChatLieu = data.getTenChatLieu();
        this.deleted = data.getDeleted();
        this.trangThai = data.getTrangThai();
        this.createAt = data.getCreateAt();
        this.createBy = data.getCreateBy();
        this.updateAt = data.getUpdateAt();
        this.updateBy = data.getUpdateBy();
    }
}
