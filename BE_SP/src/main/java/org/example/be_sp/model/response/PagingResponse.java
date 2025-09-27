package org.example.be_sp.model.response;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
public class PagingResponse<T> {
    public List<?> data;
    public Integer totalPages;
    public Integer currentPage;
    public Integer pageSize;

    public PagingResponse(Page<?> data, Integer currentPage) {
        this.totalPages = data.getTotalPages();
        this.data = data.getContent();
        this.currentPage = currentPage;
        this.pageSize = data.getSize();
    }
}