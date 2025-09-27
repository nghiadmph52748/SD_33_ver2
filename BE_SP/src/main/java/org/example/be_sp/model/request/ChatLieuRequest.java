package org.example.be_sp.model.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ChatLieuRequest {
    private String tenChatLieu;
    private Boolean trangThai;
    private Boolean deleted;
    private LocalDate createAt;
    private Integer createBy;
    private LocalDate updateAt;
    private Integer updateBy;

}
