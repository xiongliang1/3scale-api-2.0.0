package com.hisense.gateway.developer.web.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hisense.gateway.developer.web.response.Page;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * PageHttpMessageConverter
 * @Date 2019/11/20 09:14
 * @Author wangjinshan
 * @Version v1.0
 */
public class PageHttpMessageConverter extends AbstractHttpMessageConverter<Page> {
	
	ObjectMapper mapper = new ObjectMapper();
	
	public PageHttpMessageConverter() {
		super(new MediaType[] {
                new MediaType("application", "json", Charset.forName("UTF-8")),  
                new MediaType("application", "*+json", Charset.forName("UTF-8")) });  
	}

	@Override
	protected boolean supports(Class<?> clazz) {
		 return Page.class.isAssignableFrom(clazz);
	}

	@Override
	protected Page readInternal(Class<? extends Page> clazz, HttpInputMessage message)
			throws IOException, HttpMessageNotReadableException {
		String result = StreamUtils.copyToString(message.getBody(), Charset.forName("UTF-8"));
		Page page = mapper.readValue(result, Page.class);
		return page;
	}

	@Override
	protected void writeInternal(Page page, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		mapper.writeValue (outputMessage.getBody(), page);
		//outputMessage.getBody().write(mapper.writeValueAsBytes(page));
	}

}
