package com.huoyan.demo;

import java.io.File;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.UIManager;

public class JFDemo {

	public static void main(String[] args) throws Exception {
		/*// ÊÇwindows
        if (System.getProperty("os.name").toUpperCase().indexOf("WINDOWS") != -1)
        {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        }
		JFileChooser fileChooser = new JFileChooser("D:\\");
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = fileChooser.showOpenDialog(fileChooser);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String filePath = fileChooser.getSelectedFile().getAbsolutePath();
			System.out.println(filePath);
		}*/

		File file =new File("src/model.xlsx");
		URL resource = JFDemo.class.getClassLoader().getResource("model.xlsx");
		System.out.println(JFDemo.class.getClassLoader().getResource("model.xlsx").getPath());
	}
}
