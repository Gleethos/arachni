package core;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;


public class IOFrame extends JFrame implements Commander{

	private final JTextArea _output_field;
	private final boolean _receptive = true;
	private final int _lineCount;
	private final List<String> _commands = new ArrayList<>();
	private JTextField _input_field;
	private boolean _entered;
	private String _input;

	public IOFrame(String title, int maxLineCount, boolean hasInput, String... args)
	{
		_commands.addAll(List.of(args));
		_lineCount = maxLineCount;
		_input = "";
		_entered = false;
		this.setTitle(title);
		this.setSize(800, 400);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);

		_output_field = new JTextArea();
		_output_field.setText("");
		_output_field.setEditable(false);

		Font txt = new Font("Verdana", Font.LAYOUT_LEFT_TO_RIGHT, 15);
		_output_field.setBackground(Color.BLACK);
		_output_field.setForeground(Color.CYAN);
		_output_field.setFont(txt);

		JScrollPane scroll = new JScrollPane(_output_field);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroll.setBackground(Color.DARK_GRAY);
		scroll.setForeground(Color.BLUE);
		scroll.getVerticalScrollBar().setBackground(Color.DARK_GRAY);
		scroll.getHorizontalScrollBar().setBackground(Color.DARK_GRAY);

		this.add(scroll, BorderLayout.CENTER);
		if(hasInput) {
			_input_field = new JTextField();
			_input_field.setToolTipText("Insert input here!");
			_input_field.setPreferredSize(new Dimension(0, 40));
			_input_field.setBackground(Color.BLACK);
			_input_field.setForeground(Color.CYAN);
			_input_field.setFont(txt);
			this.add(_input_field, BorderLayout.PAGE_END);

			_input_field.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (_receptive) {
						_input = _input_field.getText();
						_entered = true;
						_input_field.setText("");
					}
				}
			});
		}
		this.getContentPane().add(scroll, BorderLayout.CENTER);
		this.setVisible(true);
	}

	@Override
	public synchronized void println(String text) {
		_output_field.setText(_output_field.getText() + text + "\n");
		if (_output_field.getLineCount() > _lineCount) {
			try {
				_output_field.replaceRange("", 0, _output_field.getLineEndOffset(0));
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public synchronized void print(String text) {
		_output_field.setText(_output_field.getText() + text);
		if (_output_field.getLineCount() > _lineCount) {
			try {
				_output_field.replaceRange("", 0, _output_field.getLineEndOffset(0));
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public synchronized String read() {
		if ( !_commands.isEmpty() ) return _commands.remove(0);
		_entered = false;
		while (!_entered) {
			try {
				Thread.sleep(25);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return _input;
	}
	
	
	
	
}
