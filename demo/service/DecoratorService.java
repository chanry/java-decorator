package demo.service;

import java.io.IOException;
import java.sql.Connection;

import xy.entity.ColumnInfo;
import xy.entity.PostDocument;

/**
 * 定义迁移服务被装饰类
 * @author chenli
 *
 */
public interface DecoratorService {
	/**
	 * 按栏目迁移
	 * @return
	 */
	public String channelChange(Connection conn, String oldchnlid, int newchnlid, String lastdate, ColumnInfo columnInfo,
			String domain, String docSql, String dlinkSql);
	
	/**
	 * 处理正文内容
	 * @param _doccontent
	 * @return
	 */
	public String uploadContentFileToDocument(String _doccontent, String domain) throws IOException;
	
	/**
	 * 图片附件处理
	 * @param D_Picture
	 * @param document
	 * @param domain
	 * @throws IOException
	 */
	public void pictureDealWith(String D_Picture, PostDocument document, String domain) throws IOException;
}
