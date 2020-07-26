package com.huoyan.myframe;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UnsupportedLookAndFeelException;

import com.huoyan.analysis.TianjinPoolHandle;
import com.huoyan.analysis.YaRuiQpcrHandle;
import com.huoyan.util.FileChooseUtil;

public class MyPanel extends JPanel implements ActionListener, MouseListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JButton input1;
	JButton input2;
	JButton input3;
	File input1File;
	File input2File;
	private MyJFrame frame;		
	private static boolean running=false;
	public MyPanel(MyJFrame frame) {
		this.frame = frame;
		input1 = new JButton("选择qpcr下机文件");
		//input1.setBounds(30, 40,80, 40);
		input1.addMouseListener(this);
		input2 = new JButton("选择任务单");
		//input2.setBounds(230, 40, 80, 40);
		input2.addMouseListener(this);
		
		input3 = new JButton("运行");		
		input3.addMouseListener(this);
		this.add(input1);
		this.add(input2);
		this.add(input3);
		this.setEnabled(false);
	}



	@Override
	public void actionPerformed(ActionEvent e) {
		JOptionPane.showMessageDialog(null, "使用前请联系张帅或林婷");
		
	}



	@Override
	public void mouseClicked(MouseEvent e) {
		String out = null;
		try {
			if (e.getSource() == this.input1) {
				input1File = getFile();
			}else if(e.getSource() == this.input2) {
				input2File=getFile();
			}else if (e.getSource() == this.input3) {
				if (input1File==null) {
					JOptionPane.showMessageDialog(null, "下机文件未选择！");
					return;
				}
				frame.jta.append("程序运行中..............");
				out=run();
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		frame.jta.setText("");
		if (input1File!=null) {
			frame.jta.append("qpcr文件："+input1File.getAbsolutePath());
			frame.jta.append("\r\n");
		}
		if (input2File!=null) {
			frame.jta.append("任务单："+input2File.getAbsolutePath());
			frame.jta.append("\r\n");
		}
		if (out!=null) {
			frame.jta.append("结果："+out);
			frame.jta.append("\r\n");
		}
		
	}
	private String run() throws InterruptedException, ExecutionException {
		if (running) {
			JOptionPane.showMessageDialog(null, "程序运行中，请稍后");
			return null;
		}
		if (input1File==null) {
			JOptionPane.showMessageDialog(null, "下机文件未选择！");
			return null;
		}
		Work work=new Work(input1File, input2File);
		FutureTask ft=new FutureTask<>(work);
		Thread thread=new Thread(ft);
		thread.start();
		String object = (String) ft.get();
		return object;
	}



	private File getFile() throws Exception {
		String qpcr = FileChooseUtil.chooseFiles("请选择qpcr文件：", JFileChooser.DIRECTORIES_ONLY);
		return new File(qpcr);

	}




	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	static class Work implements Callable{
		private File qpcr;
		private File task;
		public Work(File qpcr, File task) {
			super();
			this.qpcr = qpcr;
			this.task = task;
		}

		@Override
		public String call() {
			running=true;
			YaRuiQpcrHandle handle=new YaRuiQpcrHandle(new TianjinPoolHandle(), qpcr, task);
			String out = null;
			try {
				out= handle.apply();
			} catch (Exception e) {
				e.printStackTrace();
			}finally {
				running=false;
			}
			return out;
		}

		
		
	}

}


