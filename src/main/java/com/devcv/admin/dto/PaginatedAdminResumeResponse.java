package com.devcv.admin.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedAdminResumeResponse {

    private List<AdminResumeList> content;
    private Long totalElements;
    private int numberOfElements;
    private int currentPage;
    private int totalPages;
    private int size;
    private int startPage;
    private int endPage;

}

