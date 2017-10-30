package demo.decorator.extend;

import java.io.IOException;

import com.trs.infra.util.CMyString;
import com.trs.infra.util.html.HtmlElement;
import com.trs.infra.util.html.HtmlElementFinder;

import demo.decorator.DecoratorAbstract;
import demo.service.DecoratorService;
import xy.util.DatachangeUtil;

public class ATagDecorator extends DecoratorAbstract {

	public ATagDecorator(DecoratorService datachange) {
		super(datachange);
	}

	@Override
	public String uploadContentFileToDocument(String _doccontent, String domain) throws IOException {
		// TODO Auto-generated method stub
		_doccontent = super.uploadContentFileToDocument(_doccontent, domain);
		return aTagDealWith(_doccontent, domain);
	}

	private String aTagDealWith(String _doccontent, String domain) {
		// 过滤带有mailTo的<a>标签
		_doccontent = DatachangeUtil.aTagReplace(_doccontent);
		// 检测<a>标签的连接是否能ping通
		// _doccontent = DatachangeUtil.aTagUrlCheck(_doccontent);

		String a_currTagName = "a";// a标签
		String a_currTagHrefName = "href";// href属性
		HtmlElement a_element = null;// a元素
		// html文件初始类，用于查找a元素节点
		HtmlElementFinder aFinder = new HtmlElementFinder(_doccontent);

		// 第二个循环，查找出正文中所有a元素，并对其处理
		while ((a_element = aFinder.findNextElement(a_currTagName, true)) != null) {
			String strHref = a_element.getAttributeValue(a_currTagHrefName);
			if (!CMyString.isEmpty(strHref)) { // 处理标记中的属性
				if (strHref.startsWith("http://")) {
					// 判断文件类型
					if (DatachangeUtil.isConnect(strHref) == 200) {
						String type = strHref.substring(strHref.lastIndexOf(".") + 1, strHref.length());
						if (typeSet.contains(type.toLowerCase())) {
							strHref = downloadAndUploadAttachmentFile(strHref);
						}
					} else {
						strHref = "javascript:void(0)";
					}
				} else if (strHref.startsWith("/")) { // 为避免断链，相对路径的链接也进行检测
					String absoluteUrl = domain + strHref;
					if (DatachangeUtil.isConnect(absoluteUrl) == 200) {
						String type = strHref.substring(strHref.lastIndexOf(".") + 1, strHref.length());
						if (typeSet.contains(type.toLowerCase())) {
							strHref = downloadAndUploadAttachmentFile(absoluteUrl);
						}
					} else {
						strHref = "javascript:void(0)";
					}
				}

				a_element.setAttribute(a_currTagHrefName, strHref);
			}
			aFinder.putElement(a_element);
		}

		// a元素也处理完毕，返回处理后的文本结果，供调用函数更新
		return _doccontent = aFinder.getContent();
	}

}
