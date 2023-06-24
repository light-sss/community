package com.light.community;

import java.io.IOException;

/**
 * @author light
 * @Description 生成长图测试类
 * @create 2023-06-23 20:34
 */
public class WKTests {
	public static void main(String[] args) {
		String cmd="D:/wkhtmltopdf/bin/wkhtmltopdf";
		String cmd2="D:/wkhtmltopdf/bin/wkhtmltoimage --quality 75 https://www.nowcoder.com d:/wkData/wk-images/2.png";
		try {
			Runtime.getRuntime().exec(cmd2);
			//异步传输
			System.out.println("成功！");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
