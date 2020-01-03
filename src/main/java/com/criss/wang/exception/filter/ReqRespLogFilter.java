package com.criss.wang.exception.filter;

import com.criss.wang.exception.base.BaseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.annotation.WebFilter;

/**
 * 
 * @author lihaosu
 *
 * @date 2018-06-21 11:48:42
 * 
 * @description 日志过滤器
 */
@Component
@WebFilter(filterName = "reqrespLog", urlPatterns = "/*")
public class ReqRespLogFilter extends com.criss.wang.exception.utils.ReqRespLogFilter {

	private static final Logger logger = LoggerFactory.getLogger(ReqRespLogFilter.class);

	@Autowired
	private ObjectMapper objectMapper;

	@Value("${spring.http.encoding.charset:UTF-8}")
	private String charset;

	@Override
	protected String getCharset() {
		return charset;
	}

	@Override
	protected String logSerialize(BaseEntity logEntity) {
		try {
			return objectMapper.writeValueAsString(logEntity);
		} catch (Exception e) {
			logger.error("write json error", e);
			return e.getMessage();
		}
	}

	@Override
	protected String getSessionKey() {
		return null;
	}

}
