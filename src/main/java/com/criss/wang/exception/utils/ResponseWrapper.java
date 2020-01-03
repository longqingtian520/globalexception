package com.criss.wang.exception.utils;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * 
 * @author lihaosu
 *
 * @date 2018-06-21 11:54:20
 * 
 * @description Http响应包装器，便于对返回结果进行二次处理
 */
public class ResponseWrapper extends HttpServletResponseWrapper {

	private ByteArrayOutputStream buffer = null;

	private ServletOutputStream out = null;

	private PrintWriter writer = null;

	public ResponseWrapper(HttpServletResponse response, String charset) throws IOException {
		super(response);

		buffer = new ByteArrayOutputStream();
		out = new WapperedOutputStream(buffer);
		writer = new PrintWriter(new OutputStreamWriter(buffer, charset));
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return out;
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		return writer;
	}

	@Override
	public void flushBuffer() throws IOException {
		if (out != null) {
			out.flush();
		}
		if (writer != null) {
			writer.flush();
		}
	}

	@Override
	public void reset() {
		buffer.reset();
	}

	public byte[] getResponseData(String charset) throws IOException {
		flushBuffer();// 将out、writer中的数据强制输出到WapperedResponse的buffer里面，否则取不到数据
		return buffer.toByteArray();
	}

	/**
	 * 
	 * @author lihaosu
	 *
	 * @date 2018-06-21 11:56:31
	 * 
	 * @description 内部类，对ServletOutputStream进行包装，指定输出流的输出端
	 */
	private class WapperedOutputStream extends ServletOutputStream {

		private ByteArrayOutputStream bos = null;

		public WapperedOutputStream(ByteArrayOutputStream stream) throws IOException {
			bos = stream;
		}

		/*
		 * 将指定字节写入输出流bos(non-Javadoc)
		 * 
		 * @see java.io.OutputStream#write(int)
		 */
		@Override
		public void write(int b) throws IOException {
			bos.write(b);
		}

		@Override
		public boolean isReady() {
			return false;
		}

		@Override
		public void setWriteListener(WriteListener listener) {
		}
	}

}
