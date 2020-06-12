package com.huoyan.util;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.EnumSet;
import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;

public class SmbjUtil {

	public static void connect(String url, String dir) throws Exception {
		SMBClient client = new SMBClient();

		try (Connection connection = client.connect(url)) {
			AuthenticationContext ac = new AuthenticationContext("BGI", "BGI".toCharArray(), "");
			Session session = connection.authenticate(ac);
			// Connect to Share
			try (DiskShare share = (DiskShare) session.connectShare("d")) {
				for (FileIdBothDirectoryInformation f : share.list(dir, "*.*")) {
					// System.out.println("File : " + f.getFileName());
					if (f.getFileName().equals("20200501_出库样本明细表.xlsx")) {
						String filePath = "1-灭活组" + "\\" + f.getFileName();
						com.hierynomus.smbj.share.File file = share.openFile(filePath,
								EnumSet.of(AccessMask.GENERIC_READ), null, SMB2ShareAccess.ALL,
								SMB2CreateDisposition.FILE_OPEN, null);
						System.out.println("a");
						InputStream in = file.getInputStream();
						byte[] buffer = new byte[4096];
						int len = 0;
						FileOutputStream fos = new FileOutputStream("D:\\ex1.xlsx");
						BufferedOutputStream bos = new BufferedOutputStream(fos);
						while ((len = in.read(buffer, 0, buffer.length)) != -1) {
							bos.write(buffer, 0, len);
						}
						bos.flush();
						bos.close();
						in.close();
						file.close();
					}
				}
			}
		}
		client.close();
	}

}
