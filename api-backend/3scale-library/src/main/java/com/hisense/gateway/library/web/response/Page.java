package com.hisense.gateway.library.web.response;

import com.hisense.gateway.library.model.dto.web.WrapResponseBody;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@WrapResponseBody(skip = true)
public class Page<T> implements Serializable {
    private static final long serialVersionUID = 5105223900137785597L;
    private final List<T> content = new ArrayList<>();
    private Boolean first;
    private Boolean last;
    private Integer totalPages;
    private Integer totalElements;
    private Integer size;
    private Integer number;
    private Integer numberOfElements;
}
