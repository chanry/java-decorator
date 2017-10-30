package demo.decorator.extend;

import java.io.IOException;

import com.trs.infra.util.html.HtmlElement;
import com.trs.infra.util.html.HtmlElementFinder;

import demo.decorator.DecoratorAbstract;
import demo.service.DecoratorService;
import xy.util.DatachangeUtil;

public class ImgTagDecorator extends DecoratorAbstract {

	public ImgTagDecorator(DecoratorService datachange) {
		super(datachange);
	}

	@Override
	public String uploadContentFileToDocument(String _doccontent, String domain) throws IOException {
		_doccontent = super.uploadContentFileToDocument(_doccontent, domain);
		return imgTagDealWith(_doccontent, domain);
	}

	/**
	 * 遍历<img>标签： 1.检测断链并处理;2.下载并上传图片文件
	 * 
	 * @param _doccontent
	 * @param domain
	 * @return
	 */
	public String imgTagDealWith(String _doccontent, String domain) {
		String currTagName = "img";
		String currTagSrcName = "src";
		HtmlElement element = null;
		HtmlElementFinder imgFinder = new HtmlElementFinder(_doccontent);
		while ((element = imgFinder.findNextElement(currTagName, true)) != null) {
			String strSrc = element.getAttributeValue(currTagSrcName);

			if (strSrc != null && !strSrc.equals("")) { // 处理标记中的属性

				if (strSrc.startsWith("/")) { // 只处理这种前缀的附件

					String absoluteUrl = domain + strSrc;
					if (DatachangeUtil.isConnect(absoluteUrl) == 200) {
						element.setAttribute(currTagSrcName, absoluteUrl);
					} else {
						element = null;
					}
					
				} else if (strSrc.startsWith("http://")) {
					if (DatachangeUtil.isConnect(strSrc) == 200) {

						element.setAttribute(currTagSrcName, strSrc);
					} else {
						element = null;
					}
				} else if (strSrc.startsWith("file:")) {
					element = null;
				}
			}
			imgFinder.putElement(element);
		}
		return imgFinder.getContent();
	}

}
