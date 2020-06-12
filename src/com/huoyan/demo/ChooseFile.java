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
		// ʹ��Windows�Ľ�����
        try
        {
            // ��windows
            if (System.getProperty("os.name").toUpperCase().indexOf("WINDOWS") != -1)
            {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            }
        } catch (Exception e)
        {
            System.out.println("���ý���й��쳣!");
            e.printStackTrace();
        }
        setTitle("JFileChooserTest");
        setBounds(100,100,350,150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final JLabel label = new JLabel();
        JFileChooser fileChooser = new JFileChooser();  //�Ի���
        int i = fileChooser.showOpenDialog(getContentPane());  //opendialog
        if(i==JFileChooser.APPROVE_OPTION)  //�ж��Ƿ�Ϊ�򿪵İ�ť
        {
            File selectedFile = fileChooser.getSelectedFile();  //ȡ��ѡ�е��ļ�
            label.setText(selectedFile.getPath());   //ȡ��·��
        }
	}
	public static void main(String[] args) {
		ChooseFile chooseFile=new ChooseFile();
		chooseFile.setVisible(true);
	}
}
