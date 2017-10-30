package demo.service;

import java.io.IOException;
import java.sql.Connection;

import xy.entity.ColumnInfo;
import xy.entity.PostDocument;

/**
 * ����Ǩ�Ʒ���װ����
 * @author chenli
 *
 */
public interface DecoratorService {
	/**
	 * ����ĿǨ��
	 * @return
	 */
	public String channelChange(Connection conn, String oldchnlid, int newchnlid, String lastdate, ColumnInfo columnInfo,
			String domain, String docSql, String dlinkSql);
	
	/**
	 * ������������
	 * @param _doccontent
	 * @return
	 */
	public String uploadContentFileToDocument(String _doccontent, String domain) throws IOException;
	
	/**
	 * ͼƬ��������
	 * @param D_Picture
	 * @param document
	 * @param domain
	 * @throws IOException
	 */
	public void pictureDealWith(String D_Picture, PostDocument document, String domain) throws IOException;
}
