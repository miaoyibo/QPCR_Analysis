package com.huoyan.util;

import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class FileChooseUtil {

	
	public static String chooseFiles(String title,int mode) throws Exception, UnsupportedLookAndFeelException {
		// ��windows
        if (System.getProperty("os.name").toUpperCase().indexOf("WINDOWS") != -1)
        {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        }
		JFileChooser fileChooser = new JFileChooser("D:\\");
		fileChooser.setFileSelectionMode(mode);
		fileChooser.setDialogTitle(title);
		fileChooser.setApproveButtonText("选择");
		int returnVal = fileChooser.showOpenDialog(fileChooser);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String filePath = fileChooser.getSelectedFile().getAbsolutePath();
			return filePath;
		}
		return null;
	}
}
