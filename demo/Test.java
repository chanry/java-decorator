package demo;

import java.io.IOException;

import demo.decorator.DecoratorAbstract;
import demo.decorator.extend.ATagDecorator;
import demo.decorator.extend.EmbedDecorator;
import demo.decorator.extend.ImgTagDecorator;
import demo.service.DecoratorService;
import demo.service.impl.DecoratorServiceImpl;

public class Test {
	
	public static void main(String[] args) throws IOException {
		
		DecoratorService decorator = new DecoratorServiceImpl();
		DecoratorAbstract embeddabstract = new EmbedDecorator(decorator);
		DecoratorAbstract adabstract = new ATagDecorator(embeddabstract);
		DecoratorAbstract imgdabstract = new ImgTagDecorator(adabstract);
		// ���ʣ�Ϊʲô����ʽ����ÿһ��װ���ߵ�uploadContentFileToDocument������
		String content = imgdabstract.uploadContentFileToDocument("<a href='http://www.baidu.com'></a><img src='aaa'><embed>", "http://www.baidu.com");
		System.out.println(content);
		
	}
	
}
