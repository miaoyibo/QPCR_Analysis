package com.huoyan.myframe;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class MyJFrame extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JMenuItem info;
	JTextArea jta;

	Container con = getContentPane();
	private MyPanel myPanel;

	public MyJFrame() {
		myPanel = new MyPanel(this);
		JMenuBar jmb = new JMenuBar();
		JMenu jm1 = new JMenu("步骤");
		jmb.add(jm1);
		this.info = new JMenuItem("帮助");
		this.info.setActionCommand("go");
		this.info.addActionListener(myPanel);
		jm1.add(this.info);
		this.jta = new JTextArea(30, 20);
		this.jta.setBackground(Color.WHITE);
		JScrollPane jsp = new JScrollPane(this.jta);
		setLayout(new BorderLayout());
		this.add(myPanel, "North");
		myPanel.setEnabled(true);
		this.add(jsp, "Center");
		setJMenuBar(jmb);
		setTitle("小程序");
		setDefaultCloseOperation(3);
		setBounds(100, 200, 900, 600);
		setVisible(true);
	}

	public static void main(String[] args) {
		new MyJFrame();
	}

	public MyPanel getMyPanel() {
		return myPanel;
	}

	public void setMyPanel(MyPanel myPanel) {
		this.myPanel = myPanel;
	}

	public JTextArea getJta() {
		return jta;
	}

	public void setJta(JTextArea jta) {
		this.jta = jta;
	}

}
