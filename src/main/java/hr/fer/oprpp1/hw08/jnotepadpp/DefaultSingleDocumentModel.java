package hr.fer.oprpp1.hw08.jnotepadpp;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.nio.file.Path;
import java.util.ArrayList;

import javax.swing.JTextArea;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;

/**
 * This class represents a single document.
 * @author Goran
 *
 */
public class DefaultSingleDocumentModel implements SingleDocumentModel{
	JTextArea text;
	Path path;
	boolean isModified = false;
	ArrayList<SingleDocumentListener> observers;
	public DefaultSingleDocumentModel(Path path, String text) {
		this.path = path;
		this.text = new JTextArea(text);
		observers = new ArrayList<>();
		
		this.text.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				setModified(true);
			}
			
			@Override
			public void keyReleased(KeyEvent e) {}
			
			@Override
			public void keyPressed(KeyEvent e) {}
		});
	}
	
	public DefaultSingleDocumentModel(Path path, String text, StatusBar statusBar) {
		this.path = path;
		this.text = new JTextArea(text);
		observers = new ArrayList<>();
		
		this.text.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				setModified(true);
			}
			
			@Override
			public void keyReleased(KeyEvent e) {}
			
			@Override
			public void keyPressed(KeyEvent e) {}
		});
		
		this.text.addCaretListener(new CaretListener() {
			
			@Override
			public void caretUpdate(CaretEvent e) {
				statusBar.setLength(DefaultSingleDocumentModel.this.text.getText().length());
				int dot = e.getDot();
				
				try {
					int line = getLineOfOffset(DefaultSingleDocumentModel.this.text, dot);
					int positionInLine = dot - getLineStartOffset(DefaultSingleDocumentModel.this.text, line);
					statusBar.setLine(line);
					statusBar.setColumn(positionInLine);
					statusBar.setSelection(Math.abs(DefaultSingleDocumentModel.this.text.getCaret().getDot() -
							 DefaultSingleDocumentModel.this.text.getCaret().getMark()));
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				}
				statusBar.repaint();
			}
		});
	}
	
	@Override
	public JTextArea getTextComponent() {
		return text;
	}

	@Override
	public Path getFilePath() {
		return path;
	}

	@Override
	public void setFilePath(Path path) {
		this.path = path;
		observers.forEach(observer -> observer.documentFilePathUpdated(this));
	}

	@Override
	public boolean isModified() {
		return isModified;
	}

	@Override
	public void setModified(boolean modified) {
		isModified = modified;
		observers.forEach(observer -> observer.documentModifyStatusUpdated(this));
	}

	@Override
	public void addSingleDocumentListener(SingleDocumentListener l) {
		observers.add(l);
	}

	@Override
	public void removeSingleDocumentListener(SingleDocumentListener l) {
		observers.remove(l);
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
