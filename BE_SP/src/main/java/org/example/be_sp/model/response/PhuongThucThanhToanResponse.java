package org.example.be_sp.model.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PhuongThucThanhToanResponse {
    private Integer id;

    private String maPhuongThucThanhToan;

    private String tenPhuongThucThanhToan;

    private Boolean trangThai;

    private Boolean deleted;
}
