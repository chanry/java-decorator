package demo.decorator.extend;

import java.io.IOException;

import com.trs.infra.util.html.HtmlElement;
import com.trs.infra.util.html.HtmlElementFinder;

import demo.decorator.DecoratorAbstract;
import demo.service.DecoratorService;
import xy.entity.Const;

public class EmbedDecorator extends DecoratorAbstract {

	public EmbedDecorator(DecoratorService datachange) {
		super(datachange);
	}

	@Override
	public String uploadContentFileToDocument(String _doccontent, String domain) throws IOException {
		// TODO Auto-generated method stub
		_doccontent = super.uploadContentFileToDocument(_doccontent, domain);
		return embedDealWith(_doccontent, domain, Const.NEW_SITE_DOMAIN);
	}
	
	
	private String embedDealWith(String _doccontent, String domain, String newDomain) {
		String currTagName = "embed";
		String currTagSrcName = "src";
		String flashvarsName = "flashvars";
		HtmlElement element = null;
		HtmlElementFinder embedFinder = new HtmlElementFinder(_doccontent);
		while ((element = embedFinder.findNextElement(currTagName, true)) != null) {
			String strSrc = element.getAttributeValue(currTagSrcName);
			String strFlashvars = element.getAttributeValue(flashvarsName);
			if (strSrc != null && strSrc.startsWith(domain)) { // 处理标记中的属性
				strSrc = strSrc.substring(domain.length() - 1);
				element.setAttribute(currTagSrcName, strSrc);
			}
			if (strFlashvars != null && strFlashvars.indexOf(domain) > -1) { // 处理标记中的属性
				strFlashvars = strFlashvars.replaceAll(domain, newDomain);
				element.setAttribute(flashvarsName, strFlashvars);
			}
			embedFinder.putElement(element);
		}
		return embedFinder.getContent();
	}
}
