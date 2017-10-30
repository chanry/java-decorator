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
		// ���˴���mailTo��<a>��ǩ
		_doccontent = DatachangeUtil.aTagReplace(_doccontent);
		// ���<a>��ǩ�������Ƿ���pingͨ
		// _doccontent = DatachangeUtil.aTagUrlCheck(_doccontent);

		String a_currTagName = "a";// a��ǩ
		String a_currTagHrefName = "href";// href����
		HtmlElement a_element = null;// aԪ��
		// html�ļ���ʼ�࣬���ڲ���aԪ�ؽڵ�
		HtmlElementFinder aFinder = new HtmlElementFinder(_doccontent);

		// �ڶ���ѭ�������ҳ�����������aԪ�أ������䴦��
		while ((a_element = aFinder.findNextElement(a_currTagName, true)) != null) {
			String strHref = a_element.getAttributeValue(a_currTagHrefName);
			if (!CMyString.isEmpty(strHref)) { // �������е�����
				if (strHref.startsWith("http://")) {
					// �ж��ļ�����
					if (DatachangeUtil.isConnect(strHref) == 200) {
						String type = strHref.substring(strHref.lastIndexOf(".") + 1, strHref.length());
						if (typeSet.contains(type.toLowerCase())) {
							strHref = downloadAndUploadAttachmentFile(strHref);
						}
					} else {
						strHref = "javascript:void(0)";
					}
				} else if (strHref.startsWith("/")) { // Ϊ������������·��������Ҳ���м��
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

		// aԪ��Ҳ������ϣ����ش������ı�����������ú�������
		return _doccontent = aFinder.getContent();
	}

}
