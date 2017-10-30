package demo.service.impl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.trs.infra.common.WCMException;
import com.trs.infra.util.CMyString;
import com.trs.web2frame.WCMServiceCaller;

import demo.service.DecoratorService;
import xy.entity.ColumnInfo;
import xy.entity.PostDocument;
import xy.service.CommonService;

public class DecoratorServiceImpl extends CommonService implements DecoratorService {
	
	private static Logger logger = Logger.getLogger(DecoratorServiceImpl.class);

	public String channelChange(Connection conn, String oldchnlid, int newchnlid, String lastdate, ColumnInfo columnInfo,
			String domain, String docSql, String dlinkSql) {
		PreparedStatement stmt = null;
		PreparedStatement dlinkStmt = null;
		ResultSet rs = null;
		String result = "";
		try {
			// Ԥ���� dlink��"/pub"��ͷ���ĵ���ѯ���
			dlinkStmt = conn.prepareStatement(dlinkSql);
			// Ԥ������Ŀ�ĵ���ѯ��䲢��ʼ����ѯ����
			stmt = conn.prepareStatement(docSql);
			initQuerySqlParams(stmt, new Object[]{oldchnlid, lastdate});
			rs = stmt.executeQuery();
			result = importDocumentToGov(rs, stmt, dlinkStmt, oldchnlid, newchnlid, columnInfo, domain);
		} catch (SQLException e) {
			String errorMsg = "\n��Ŀ[ClassID=" + oldchnlid + "]��Ǩ���ĵ�����SQL�쳣";
			result += errorMsg;
			logger.error(errorMsg, e);
		} catch (Exception e) {
			String errorMsg = "\n��Ŀ[ClassID=" + oldchnlid + "]��Ǩ���ĵ������쳣��" + e.getMessage();
			result += errorMsg;
			logger.error(errorMsg, e);
		} finally {
			close(rs, stmt, dlinkStmt);
		}
		return result;
	}

	public String doccontentDealWith(String _doccontent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	/**
	 * ����ѯ���������ƽ̨
	 * 
	 * @param rs
	 * @param oldchnlid
	 * @param newchnlid
	 * @param lastdate
	 * @return
	 * @throws IOException
	 * @throws SQLException
	 */
	public String importDocumentToGov(ResultSet rs, PreparedStatement stmt, PreparedStatement dlinkStmt, String oldchnlid, int newchnlid,
			ColumnInfo columnInfo, String domain) throws SQLException, IOException {
		int num_success = 0;
		int num_fail = 0;
		String result = "";
		while (rs.next()) {
			// ��ʼǨ���ĵ�
			PostDocument document = new PostDocument();
			int docid = rs.getInt(columnInfo.getId());
			String dochtmlcon = getStrValue(rs.getString(columnInfo.getContent()));
			String D_Picture = getStrValue(rs.getString(columnInfo.getPicture()));
			String DOCLINK = getStrValue(rs.getString(columnInfo.getDocLink()));
			String doctitle = getStrValue(rs.getString(columnInfo.getDoctitle()));
			document.setChannelId(newchnlid);
			document.setDocTitle(doctitle);
			document.setShortTitle(doctitle);
			try {
				document.setDocType(docLinkDealWith(DOCLINK, document, domain));
			} catch (WCMException e1) {
				logger.error(e1.getMessage());
				continue;
			}
			document.setCrtime(rs.getTimestamp(columnInfo.getCrTime()));
			document.setDOCRELTIME(rs.getTimestamp(columnInfo.getCrTime()));
			document.setDOCSOURCENAME(getStrValue(rs.getString(columnInfo.getDocSourceName())));
			document.setTitleColor(getStrValue(rs.getString(columnInfo.getTitleColor())));
			document.setDOCAUTHOR(getStrValue(rs.getString(columnInfo.getDocAuthor())));
			
			//������վ���������еĴ���
			dochtmlcon=dochtmlcon.replaceAll("&gt;", ">");
			dochtmlcon=dochtmlcon.replaceAll("&lt;", "<");
			dochtmlcon=dochtmlcon.replaceAll("&lt;", "\"");
			dochtmlcon=dochtmlcon.replaceAll("&quot;", "\"");//
			dochtmlcon=dochtmlcon.replaceAll("&amp;", "&");
			document.setDocContent(CMyString.Html2Text(dochtmlcon));
			document.setDOCKEYWORDS(getStrValue(rs.getString(columnInfo.getKeywords())));
			try {
				// ���ı��ֶ�ת��
				dochtmlcon = uploadContentFileToDocument(dochtmlcon, domain);
			} catch (IOException e) {
				logger.error("���������и��������쳣", e);
			}
			dochtmlcon = "<DIV class=\"TRS_UEDITOR TRS_WEB\">" + dochtmlcon + "</DIV>"; // ϵͳҪ��ͼ�Ļ������ݶ�Ҫ��һ��DIV
			document.setDocHtmlCon(dochtmlcon);
			/**
			 * ͼƬ��������
			 */
			pictureDealWith(D_Picture, document, domain);

			try {
				if (DOCLINK.startsWith("/pub")) {
					initQuerySqlParams(dlinkStmt, new Object[]{returfilename(DOCLINK)});
					rs = dlinkStmt.executeQuery();
					result += "\n" + importDocumentToGov(rs, stmt, dlinkStmt, oldchnlid, newchnlid, columnInfo, domain);
				} else {
					WCMServiceCaller.Call("gov_webdocument", "saveDocumentInWeb", document.toMap(), true);
				}
				num_success++;
				logger.info("�ĵ�[" + doctitle + "]Ǩ�Ƴɹ�");
			} catch (Exception e) {
				num_fail++;
				logger.info("�ĵ�[" + doctitle + "]Ǩ���쳣", e);
				writeErrorLog(doctitle, docid, newchnlid, oldchnlid);
			}
		}
		result += getResultMessage(oldchnlid, num_success, num_fail);
		logger.info(result);
		return result;
	}
	
	/**
	 * ��ʼ��sql������
	 * @param psstmt
	 * @param params
	 * @throws Exception
	 */
	public void initQuerySqlParams(PreparedStatement psstmt, Object... params) throws Exception {
		psstmt.clearParameters();
		for (int i = 0; i < params.length; i++) {
			try {
				if (params[i] instanceof Integer) {
					psstmt.setInt(i+1, (Integer) params[i]);
				} else if (params[i] instanceof String) {
					psstmt.setString(i+1, params[i].toString());
				} else {
					throw new Exception("��ѯ�������͸�ʽ����");
				}
			} catch (SQLException e) {
				logger.error("��ʼ��sql����["+params[i]+"]�쳣��", e);
				throw new Exception("sql������");
			}
		}
	}
	
}
