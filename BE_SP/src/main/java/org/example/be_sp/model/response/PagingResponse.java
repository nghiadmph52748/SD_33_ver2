package org.example.be_sp.model.response;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PagingResponse<T> {

    public List<?> data;
    public Integer totalPages;
    public Integer currentPage;
    public Integer pageSize;
    public Long totalElements;

    public PagingResponse(Page<?> data, Integer currentPage) {
        this.totalPages = data.getTotalPages();
        this.data = data.getContent();
        this.currentPage = currentPage;
        this.pageSize = data.getSize();
        this.totalElements = data.getTotalElements();
    }
}
