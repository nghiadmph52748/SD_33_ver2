package org.example.be_sp.model.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QuyenHanRequest {
    private String tenQuyenHan;
    private Boolean deleted;
}
