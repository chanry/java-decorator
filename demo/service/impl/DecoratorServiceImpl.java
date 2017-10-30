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
			// 预编译 dlink以"/pub"开头的文档查询语句
			dlinkStmt = conn.prepareStatement(dlinkSql);
			// 预编译栏目文档查询语句并初始化查询参数
			stmt = conn.prepareStatement(docSql);
			initQuerySqlParams(stmt, new Object[]{oldchnlid, lastdate});
			rs = stmt.executeQuery();
			result = importDocumentToGov(rs, stmt, dlinkStmt, oldchnlid, newchnlid, columnInfo, domain);
		} catch (SQLException e) {
			String errorMsg = "\n栏目[ClassID=" + oldchnlid + "]下迁移文档出现SQL异常";
			result += errorMsg;
			logger.error(errorMsg, e);
		} catch (Exception e) {
			String errorMsg = "\n栏目[ClassID=" + oldchnlid + "]下迁移文档出现异常：" + e.getMessage();
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
	 * 将查询结果导入新平台
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
			// 开始迁移文档
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
			
			//工商联站点数据特有的处理
			dochtmlcon=dochtmlcon.replaceAll("&gt;", ">");
			dochtmlcon=dochtmlcon.replaceAll("&lt;", "<");
			dochtmlcon=dochtmlcon.replaceAll("&lt;", "\"");
			dochtmlcon=dochtmlcon.replaceAll("&quot;", "\"");//
			dochtmlcon=dochtmlcon.replaceAll("&amp;", "&");
			document.setDocContent(CMyString.Html2Text(dochtmlcon));
			document.setDOCKEYWORDS(getStrValue(rs.getString(columnInfo.getKeywords())));
			try {
				// 纯文本字段转换
				dochtmlcon = uploadContentFileToDocument(dochtmlcon, domain);
			} catch (IOException e) {
				logger.error("下载正文中附件出现异常", e);
			}
			dochtmlcon = "<DIV class=\"TRS_UEDITOR TRS_WEB\">" + dochtmlcon + "</DIV>"; // 系统要求图文混排内容都要包一个DIV
			document.setDocHtmlCon(dochtmlcon);
			/**
			 * 图片附件处理
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
				logger.info("文档[" + doctitle + "]迁移成功");
			} catch (Exception e) {
				num_fail++;
				logger.info("文档[" + doctitle + "]迁移异常", e);
				writeErrorLog(doctitle, docid, newchnlid, oldchnlid);
			}
		}
		result += getResultMessage(oldchnlid, num_success, num_fail);
		logger.info(result);
		return result;
	}
	
	/**
	 * 初始化sql语句参数
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
					throw new Exception("查询参数类型格式错误！");
				}
			} catch (SQLException e) {
				logger.error("初始化sql参数["+params[i]+"]异常：", e);
				throw new Exception("sql语句错误！");
			}
		}
	}
	
}
