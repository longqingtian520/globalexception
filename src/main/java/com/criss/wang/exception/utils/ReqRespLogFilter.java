package com.criss.wang.exception.utils;

import com.criss.wang.exception.base.BaseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author lihaosu
 * @date 2018-06-21 11:48:42
 * @description 日志过滤器
 */
public abstract class ReqRespLogFilter implements Filter {

	private static final Logger logger = LoggerFactory.getLogger(ReqRespLogFilter.class);

	/**
	 * 获取项目字符集
	 *
	 * @return
	 */
	protected abstract String getCharset();

	/**
	 * 生成序列化后的日志
	 *
	 * @return
	 */
	protected abstract String logSerialize(BaseEntity logEntity);

	/**
	 * 获取session中需要记录的对象的Key
	 *
	 * @return
	 */
	protected abstract String getSessionKey();

	public void destroy() {

	}

	/**
	 * @author lihaosu
	 * @date 2018-06-21 13:27:25
	 * @description 请求日志
	 */
	class ReqLog extends BaseEntity {
		/**
		 *
		 */
		private static final long serialVersionUID = 973006107417627373L;
		private Map<String, List<String>> headers;
		private String url;
		private String httpMethod;
		private String body;
		private String queryString;
		private Object sessionObject;

		public ReqLog() {
			super();
		}

		public Map<String, List<String>> getHeaders() {
			return headers;
		}

		public void setHeaders(Map<String, List<String>> headers) {
			this.headers = headers;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getHttpMethod() {
			return httpMethod;
		}

		public void setHttpMethod(String httpMethod) {
			this.httpMethod = httpMethod;
		}

		public String getBody() {
			return body;
		}

		public void setBody(String body) {
			this.body = body;
		}

		public Object getSessionObject() {
			return sessionObject;
		}

		public void setSessionObject(Object sessionObject) {
			this.sessionObject = sessionObject;
		}

		public String getQueryString() {
			return queryString;
		}

		public void setQueryString(String queryString) {
			this.queryString = queryString;
		}
	}

	/**
	 * @author lihaosu
	 * @date 2018-06-21 13:28:40
	 * @description 响应日志
	 */
	class RespLog extends BaseEntity {

		/**
		 *
		 */
		private static final long serialVersionUID = 6603865648864990902L;

		private Map<String, String> headers;

		/**
		 * 状态码
		 */
		private String statusCode;

		/**
		 * 消耗时间
		 */
		private int timeElapsed;

		/**
		 * 响应内容
		 */
		private String body;

		public RespLog() {
			super();
		}

		public Map<String, String> getHeaders() {
			return headers;
		}

		public void setHeaders(Map<String, String> headers) {
			this.headers = headers;
		}

		public String getStatusCode() {
			return statusCode;
		}

		public void setStatusCode(String statusCode) {
			this.statusCode = statusCode;
		}

		public int getTimeElapsed() {
			return timeElapsed;
		}

		public void setTimeElapsed(int timeElapsed) {
			this.timeElapsed = timeElapsed;
		}

		public String getBody() {
			return body;
		}

		public void setBody(String body) {
			this.body = body;
		}
	}

	/**
	 * 过滤逻辑
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		// 开始处理时间
		long startTime = System.currentTimeMillis();

		RequestWrapper reqWrapper = new RequestWrapper((HttpServletRequest) request);

		ResponseWrapper respWrapper = new ResponseWrapper((HttpServletResponse) response, getCharset());

		// 记录请求日志
		ReqLog reqLog = new ReqLog();

		// 请求URL
		reqLog.setUrl(reqWrapper.getRequestURL().toString());
		// ?
		reqLog.setQueryString(reqWrapper.getQueryString());
		// HTTP方法
		reqLog.setHttpMethod(reqWrapper.getMethod());

		try {
			// 获取原始请求字节
			byte[] content = reqWrapper.getContent();

			if (content == null)
				reqLog.setBody("[EMPTY]");
			else {
				// 如果请求内容是PB字节，转BASE64进行记录
				String contentType = request.getContentType();
				if (contentType != null && contentType.startsWith("application/x-protobuf")) {
					String logBody = BASE64.encode(content);
					reqLog.setBody(logBody.length() > 1024 ? logBody.substring(0, 1024) : logBody);
				} else {
					// 按编码记录请求参数
					String c = reqWrapper.getCharacterEncoding();
					reqLog.setBody(new String(content, c != null ? c : StandardCharsets.UTF_8.name()));
				}
			}
		} catch (Exception e) {
			logger.error("get req content error", e);
		}

		// 请求Header
		Enumeration<String> e = reqWrapper.getHeaderNames();
		if (e != null) {

			Map<String, List<String>> headers = new HashMap<String, List<String>>();

			while (e.hasMoreElements()) {

				String headerName = e.nextElement();

				Enumeration<String> headerValues = reqWrapper.getHeaders(headerName);

				if (headerValues != null) {

					List<String> vals = new ArrayList<String>();
					while (headerValues.hasMoreElements()) {

						String headerValue = headerValues.nextElement();
						vals.add(headerValue);
					}

					headers.put(headerName, vals);
				}
			}

			reqLog.setHeaders(headers);
		}

		// 添加session信息
		HttpSession session = reqWrapper.getSession(false);
		String sk = getSessionKey();
		if (session != null && sk != null && session.getAttribute(sk) != null) {
			Object o = session.getAttribute(sk);
			reqLog.setSessionObject(o);
		}

		// 记录日志
		logger.info("reqInfo: {}", logSerialize(reqLog));

		chain.doFilter(reqWrapper, respWrapper);

		// 处理完成时间
		long endTime = System.currentTimeMillis();

		// 获取response返回的内容并重新写入response
		byte[] result = respWrapper.getResponseData(response.getCharacterEncoding());

		response.getOutputStream().write(result);

		RespLog respLog = new RespLog();
		// HTTP状态码
		respLog.setStatusCode(String.valueOf(respWrapper.getStatus()));

		// HTTP响应头
		Collection<String> names = respWrapper.getHeaderNames();
		if (!CollectionUtils.isEmpty(names)) {

			Map<String, String> headers = new HashMap<>();
			names.forEach(n -> {
				String v = respWrapper.getHeader(n);
				if (v != null)
					headers.put(n, v);
			});

			respLog.setHeaders(headers);
		}

		// 如果返回的是PB字节，转BASE64编码后再记录
		String contentType = respWrapper.getContentType();
		if (contentType != null && contentType.startsWith("application/x-protobuf")) {
			String logBody = BASE64.encode(result);
			respLog.setBody(logBody.length() > 1024 ? logBody.substring(0, 1024) : logBody);
		} else if (contentType != null) {
			MimeType mimeType = MimeType.valueOf(contentType);
			if (!mimeType.isCompatibleWith(MimeTypeUtils.parseMimeType("image/*")) && !mimeType.isCompatibleWith(MimeTypeUtils.parseMimeType("application/octet-stream")))
				// 响应内容
				respLog.setBody(new String(result, getCharset()));
		} else {
			// 响应内容
			respLog.setBody(new String(result, getCharset()));
		}
		// 处理时间
		respLog.setTimeElapsed((int) (endTime - startTime));

		// 记录日志
		logger.info("respInfo: {}", logSerialize(respLog));
	}

	public void init(FilterConfig fConfig) throws ServletException {

	}
}
