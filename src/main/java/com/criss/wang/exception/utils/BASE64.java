package com.criss.wang.exception.utils;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

/**
 * 
 * @author lihaosu
 *
 * @date 2018-03-02 10:03:33
 * 
 * @description BASE64编解码工具
 */
public class BASE64 {
	/**
	 * 编码
	 * 
	 * @param bytes
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String encode(byte[] bytes) throws UnsupportedEncodingException {
		return new String(Base64.getEncoder().encode(bytes), "UTF-8");
	}

	/**
	 * 编码
	 * 
	 * @param s
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String encode(String s) throws UnsupportedEncodingException {
		return encode(s.getBytes("UTF-8"));
	}

	/**
	 * 解码
	 * 
	 * @param bytes
	 * @return
	 */
	public static byte[] decode(byte[] bytes) {
		return Base64.getDecoder().decode(bytes);
	}

	/**
	 * 解码
	 * 
	 * @param s
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static byte[] decode(String s) throws UnsupportedEncodingException {
		return decode(s.getBytes("UTF-8"));
	}

	/**
	 * 解码成字符串
	 * 
	 * @param bytes
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String decodeToString(byte[] bytes) throws UnsupportedEncodingException {
		return new String(decode(bytes), "UTF-8");
	}

	/**
	 * 解码成字符串
	 * 
	 * @param s
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String decodeToString(String s) throws UnsupportedEncodingException {
		return decodeToString(s.getBytes("UTF-8"));
	}
}
