package org.example.be_sp.model.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QuyenHanResponse {
    private Integer id;
    private String maQuyenHan;
    private String tenQuyenHan;
    private Boolean deleted;
}
