package hr.fer.oprpp1.hw08.jnotepadpp;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;

import hr.fer.oprpp1.hw08.jnotepadpp.local.FormLocalizationProvider;
import hr.fer.oprpp1.hw08.jnotepadpp.local.LJLabel;
import hr.fer.oprpp1.hw08.jnotepadpp.local.LocalizationProvider;

/**
 * This class creates a JPanel containing the length of the opened document,
 * line and column where the caret is and the length of the text selection
 * if there is one.
 * @author Goran
 *
 */
public class StatusBar extends JPanel{
	MultipleDocumentModel mdm;
	int length = 0;
	int line = 1;
	int column = 1;
	int selection = 0;
	FormLocalizationProvider flp;
	
	public StatusBar(MultipleDocumentModel mdm, FormLocalizationProvider flp) {
		super(new BorderLayout());
		this.mdm = mdm;
		this.flp = flp;
		
		LJLabel lengthLabel = new LJLabel("length", flp, ":" + length);
		lengthLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 50));
		add(lengthLabel, BorderLayout.WEST);
		JLabel lineColSelLabel = new JLabel("Ln:" + line + " Col:" + column +  " Sel:" + selection);
		add(lineColSelLabel);
		
		JLabel dateAndTime;
		
		final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	    int interval = 1000; // 1000 ms
	    Calendar now = Calendar.getInstance();
	    dateAndTime = new JLabel(dateFormat.format(now.getTime()));
	    dateAndTime.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
	    add(dateAndTime, BorderLayout.EAST);
	    new Timer(interval, new ActionListener() {
	        @Override
	        public void actionPerformed(ActionEvent e) {
	            Calendar now = Calendar.getInstance();
	            dateAndTime.setText(dateFormat.format(now.getTime()));
	        }
	    }).start();
	    mdm.addMultipleDocumentListener(new MultipleDocumentListener() {
			
			@Override
			public void documentRemoved(SingleDocumentModel model) {}
			
			@Override
			public void documentAdded(SingleDocumentModel model) {}
			
			@Override
			public void currentDocumentChanged(SingleDocumentModel previousModel, SingleDocumentModel currentModel) {
				setLength(mdm.getCurrentDocument().getTextComponent().getText().length());
				lengthLabel.setText("length:" + length);
				int dot = mdm.getCurrentDocument().getTextComponent().getCaret().getDot();
				SingleDocumentModel currentDoc = mdm.getCurrentDocument();
				
				try {
					
					int line = getLineOfOffset(currentDoc.getTextComponent(), dot);
					int positionInLine = dot - getLineStartOffset(currentDoc.getTextComponent(), line);
					setLine(line);
					setColumn(positionInLine);
					setSelection(Math.abs(currentDoc.getTextComponent().getCaret().getDot() -
							currentDoc.getTextComponent().getCaret().getMark()));
					lineColSelLabel.setText("Ln:" + (line+1) + " Col:" + (++column) +  " Sel:" + selection);
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				}
				
			    currentDoc.getTextComponent().addCaretListener(new CaretListener() {
							
					@Override
					public void caretUpdate(CaretEvent e) {
						setLength(currentDoc.getTextComponent().getText().length());
						lengthLabel.setText("length:" + length);
						int dot = e.getDot();
						
						try {
							int line = getLineOfOffset(currentDoc.getTextComponent(), dot);
							int positionInLine = dot - getLineStartOffset(currentDoc.getTextComponent(), line);
							setLine(line);
							setColumn(positionInLine);
							setSelection(Math.abs(currentDoc.getTextComponent().getCaret().getDot() -
									currentDoc.getTextComponent().getCaret().getMark()));
							lineColSelLabel.setText("Ln:" + (line+1) + " Col:" + (++column) +  " Sel:" + selection);
						} catch (BadLocationException e1) {
							e1.printStackTrace();
						}
					}
				});
			}
		});
	    
	    SingleDocumentModel currentDoc = mdm.getCurrentDocument();
	    currentDoc.getTextComponent().addCaretListener(new CaretListener() {
			
			@Override
			public void caretUpdate(CaretEvent e) {
				setLength(currentDoc.getTextComponent().getText().length());
				lengthLabel.setText("length:" + length);
				int dot = e.getDot();
				
				try {
					int line = getLineOfOffset(currentDoc.getTextComponent(), dot);
					int positionInLine = dot - getLineStartOffset(currentDoc.getTextComponent(), line);
					setLine(line);
					setColumn(positionInLine);
					setSelection(Math.abs(currentDoc.getTextComponent().getCaret().getDot() -
							currentDoc.getTextComponent().getCaret().getMark()));
					lineColSelLabel.setText("Ln:" + (line+1) + " Col:" + (++column) +  " Sel:" + selection);
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				}
			}
		});
	    
	}

	public void setLength(int length) {
		this.length = length;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public void setColumn(int column) {
		this.column = column;
	}

	public void setSelection(int selection) {
		this.selection = selection;
	}
	
	static int getLineOfOffset(JTextComponent comp, int offset) throws BadLocationException {
	    Document doc = comp.getDocument();
	    if (offset < 0) {
	        throw new BadLocationException("Can't translate offset to line", -1);
	    } else if (offset > doc.getLength()) {
	        throw new BadLocationException("Can't translate offset to line", doc.getLength() + 1);
	    } else {
	        Element map = doc.getDefaultRootElement();
	        return map.getElementIndex(offset);
	    }
	}

	static int getLineStartOffset(JTextComponent comp, int line) throws BadLocationException {
	    Element map = comp.getDocument().getDefaultRootElement();
	    if (line < 0) {
	        throw new BadLocationException("Negative line", -1);
	    } else if (line >= map.getElementCount()) {
	        throw new BadLocationException("No such line", comp.getDocument().getLength() + 1);
	    } else {
	        Element lineElem = map.getElement(line);
	        return lineElem.getStartOffset();
	    }
	}
	
}
