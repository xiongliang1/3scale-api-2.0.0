package com.hisense.gateway.developer.web.response;

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
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8004673227573598562L;
	private final List<T> content = new ArrayList<T>();
	private Boolean first;
	private Boolean last;
	private Integer totalPages;
	private Integer totalElements;
	private Integer size;
	private Integer number;
	private Integer numberOfElements;

}
