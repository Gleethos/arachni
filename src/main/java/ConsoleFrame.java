import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;


public class ConsoleFrame extends JFrame{

	JTextArea outputField;
	JTextField inputField;
	boolean entered = false;
	boolean receptive = true;
	String input = "";
	int linecount;

	public ConsoleFrame(String title, int maxLineCount) {
			linecount = maxLineCount;
			input = "";
			entered = false;
			this.setTitle(title);
			this.setSize(800, 400);
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			this.setLocationRelativeTo(null);

		outputField = new JTextArea();
		outputField.setText("");
		// outputField.setBorder(BorderFactory.createLineBorder(Color.black));
		outputField.setEditable(false);

		Font txt = new Font("Verdana", Font.LAYOUT_LEFT_TO_RIGHT, 15);
		outputField.setBackground(Color.BLACK);
		outputField.setForeground(Color.CYAN);
		outputField.setFont(txt);

		JScrollPane scroll = new JScrollPane(outputField);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroll.setBackground(Color.DARK_GRAY);
		scroll.setForeground(Color.BLUE);
		scroll.getVerticalScrollBar().setBackground(Color.DARK_GRAY);
		scroll.getHorizontalScrollBar().setBackground(Color.DARK_GRAY);

		//scroll.getVerticalScrollBar().addAdjustmentListener(
		//		new AdjustmentListener() {
		//			public void adjustmentValueChanged(AdjustmentEvent e) {
		//				e.getAdjustable().setValue(
		//						e.getAdjustable().getMaximum());
		//			}
		//		});
		//scroll.getHorizontalScrollBar().addAdjustmentListener(
		//		new AdjustmentListener() {
		//			public void adjustmentValueChanged(AdjustmentEvent e) {
		//				e.getAdjustable().setValue(
		//						e.getAdjustable().getMaximum());
		//			}
		//		});
		this.add(scroll, BorderLayout.CENTER);

		// this.addInto(outputField, BorderLayout.CENTER);

		inputField = new JTextField();
		inputField.setToolTipText("Insert input here!");
		inputField.setPreferredSize(new Dimension(0, 40));
		inputField.setBackground(Color.BLACK);
		inputField.setForeground(Color.CYAN);
		inputField.setFont(txt);
		this.add(inputField, BorderLayout.PAGE_END);

		// Eingabefeld Enter-Listener
		inputField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (receptive) {
					input = inputField.getText();
					entered = true;
					inputField.setText("");
				}
			}
		});
		this.getContentPane().add(scroll, BorderLayout.CENTER);
		this.setVisible(true);
	}

	public synchronized void println(String text) {
		outputField.setText(outputField.getText() + text + "\n");
		if (outputField.getLineCount() > linecount) {
			try {
				outputField.replaceRange("", 0, outputField.getLineEndOffset(0));
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized void print(String text) {
		outputField.setText(outputField.getText() + text);
		if (outputField.getLineCount() > linecount) {
			try {
				outputField.replaceRange("", 0, outputField.getLineEndOffset(0));
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized String read() {
		entered = false;
		while (!entered) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return input;
	}
	
	
	
	
}
