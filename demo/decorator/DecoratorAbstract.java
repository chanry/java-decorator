package demo.decorator;

import java.io.IOException;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.trs.web2frame.WCMServiceCaller;
import com.trs.web2frame.dispatch.Dispatch;

import demo.service.DecoratorService;
import xy.entity.ColumnInfo;
import xy.entity.Const;
import xy.entity.PostDocument;
import zhxz.HttpSaveToFile;

public abstract class DecoratorAbstract implements DecoratorService {

	private static Logger logger = Logger.getLogger(DecoratorAbstract.class);

	public static final Set<String> typeSet = new HashSet<String>();

	static {
		typeSet.add("doc");
		typeSet.add("docx");
		typeSet.add("dox");
		typeSet.add("xls");
		typeSet.add("xlsx");
		typeSet.add("zip");
		typeSet.add("rar");
		typeSet.add("pdf");
		typeSet.add("jpg");
		typeSet.add("png");
		typeSet.add("gif");
		typeSet.add("ceb");
	}

	private DecoratorService datachange;

	public DecoratorAbstract(DecoratorService datachange) {
		this.datachange = datachange;
	}

	// 获取wcmdate中图片附件的相应地址 W020150513616095189985.png
	public static String filepath(String filename) {
		String returnStr = "";
		String num1path = "";// 第一层路径
		String num2path = "";// 第二次路径

		num1path = filename.substring(0, 8);
		num2path = filename.substring(0, 10);

		returnStr = "webpic" + "/" + num1path + "/" + num2path + "/" + filename;

		return returnStr;
	}

	public static String fileNopath(String filename) {
		String returnStr = "";
		String num1path = "";// 第一层路径
		String num2path = "";// 第二次路径

		num1path = filename.substring(2, 8);
		num2path = filename.substring(2, 10);

		returnStr = "webpic" + "/P0" + num1path + "/P0" + num2path + "/P0" + filename.substring(2, filename.length());

		return returnStr;
	}

	/**
	 * 上传图片附件到wcm中
	 * 
	 * @param localFile
	 * @return
	 */

	public String UploadLocalFileToWCM(String localFile) {
		// 上传文件到WCM里面，并选择上传到webpic目录下
		String fileName = "";
		Dispatch oDispatch;
		try {
			oDispatch = WCMServiceCaller.UploadFile(localFile, "W0");
			// 获得WCMData下面的路径
			fileName = oDispatch.getUploadShowName();
			SimpleDateFormat fmt = new SimpleDateFormat("'/webpic/W0'yyyyMM'/W0'yyyyMMdd'/'");
			java.util.Date now = new java.util.Date();
			fileName = fmt.format(now) + fileName;
		} catch (Exception e) {
			logger.error("上传文件" + localFile + "出错", e);
		}
		return fileName;

	}

	/**
	 * 下载文件到本地并上传到服务器
	 * 
	 * @param url
	 * @return
	 */
	public String downloadAndUploadAttachmentFile(String url) {
		HttpSaveToFile.saveToFile(url, Const.LOCAL_TEMP_FILE_PATH + "/", returfilename(url));
		String filePath = UploadLocalFileToWCM(Const.LOCAL_TEMP_FILE_PATH + "/" + returfilename(url));
		return filePath;
	}

	// 获取wcmdate中图片附件的相应地址 W020150513616095189985.png

	public static String fileNoimagepath(String filename) {
		String returnStr = "";
		String num1path = "";// 第一层路径
		String num2path = "";// 第二次路径

		num1path = filename.substring(0, 8);
		num2path = filename.substring(0, 10);

		returnStr = "protect" + "/" + num1path + "/" + num2path + "/" + filename;

		return returnStr;
	}

	/**
	 * 获取附件名称
	 */
	public static String returfilename(String filePath) {
		String filname = "";

		String s[] = filePath.split("/");
		filname = (s[s.length - 1]);
		return filname;
	}

	// 特定编号
	public static String liushuihao() {
		Calendar cal = Calendar.getInstance();

		int minute = cal.get(Calendar.MINUTE);// 分
		int second = cal.get(Calendar.SECOND);// 秒
		String liushui = minute + "" + second;

		return liushui;
	}
	
	public String channelChange(Connection conn, String oldchnlid, int newchnlid, String lastdate,
			ColumnInfo columnInfo, String domain, String docSql, String dlinkSql) {
		return datachange.channelChange(conn, oldchnlid, newchnlid, lastdate, columnInfo, domain, docSql, dlinkSql);
	}

	public String uploadContentFileToDocument(String _doccontent, String domain) throws IOException {
		return datachange.uploadContentFileToDocument(_doccontent, domain);
	}

	public void pictureDealWith(String D_Picture, PostDocument document, String domain) throws IOException {
		datachange.pictureDealWith(D_Picture, document, domain);
	}
	
	
}
