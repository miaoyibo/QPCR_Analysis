package com.huoyan.demo;

import java.awt.BorderLayout;
import java.awt.FileDialog;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager;

public class ChooseFile extends JFrame{

	public ChooseFile()  {
		super();
		// 使用Windows的界面风格
        try
        {
            // 是windows
            if (System.getProperty("os.name").toUpperCase().indexOf("WINDOWS") != -1)
            {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            }
        } catch (Exception e)
        {
            System.out.println("设置界面感官异常!");
            e.printStackTrace();
        }
        setTitle("JFileChooserTest");
        setBounds(100,100,350,150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final JLabel label = new JLabel();
        JFileChooser fileChooser = new JFileChooser();  //对话框
        int i = fileChooser.showOpenDialog(getContentPane());  //opendialog
        if(i==JFileChooser.APPROVE_OPTION)  //判断是否为打开的按钮
        {
            File selectedFile = fileChooser.getSelectedFile();  //取得选中的文件
            label.setText(selectedFile.getPath());   //取得路径
        }
	}
	public static void main(String[] args) {
		ChooseFile chooseFile=new ChooseFile();
		chooseFile.setVisible(true);
	}
}
