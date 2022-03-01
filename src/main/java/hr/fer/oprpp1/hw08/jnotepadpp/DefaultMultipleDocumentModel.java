package hr.fer.oprpp1.hw08.jnotepadpp;

import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * This class represents a model of notepadpp
 * containing one or more single documents open.
 * @author Goran
 *
 */
public class DefaultMultipleDocumentModel extends JTabbedPane implements MultipleDocumentModel{
	private static final long serialVersionUID = 1L;
	
	ArrayList<SingleDocumentModel> singleDocuments;
	SingleDocumentModel currentDoc;
	ArrayList<MultipleDocumentListener> observers;
	ImageIcon modified = loadImage("/icons/modified.png");
	ImageIcon notModified = loadImage("/icons/notModified.png");
	
	public DefaultMultipleDocumentModel() {
		singleDocuments = new ArrayList<>();
		observers = new ArrayList<>();
		currentDoc = new DefaultSingleDocumentModel(null, "");
		currentDoc.addSingleDocumentListener(new SingleDocumentListener() {

			@Override
			public void documentModifyStatusUpdated(SingleDocumentModel model) {
				int indexOfSelected = DefaultMultipleDocumentModel.this.getSelectedIndex();
				if(model.isModified()) {
					DefaultMultipleDocumentModel.this.setTabComponentAt(indexOfSelected, new ButtonTabComponent(
							DefaultMultipleDocumentModel.this, singleDocuments, modified));
				} else {
					DefaultMultipleDocumentModel.this.setTabComponentAt(indexOfSelected, new ButtonTabComponent(
							DefaultMultipleDocumentModel.this, singleDocuments, notModified));
				}
			}

			@Override
			public void documentFilePathUpdated(SingleDocumentModel model) {
				Window window = SwingUtilities.getWindowAncestor(DefaultMultipleDocumentModel.this);
				JFrame frame = (JFrame) window;
				frame.setTitle(model.getFilePath() + " - JNotepad++");
				int indexOfSelected = DefaultMultipleDocumentModel.this.getSelectedIndex();
				DefaultMultipleDocumentModel.this.setTitleAt(indexOfSelected, model.getFilePath().getFileName().toString());
				DefaultMultipleDocumentModel.this.setTabComponentAt(indexOfSelected, new ButtonTabComponent(
						DefaultMultipleDocumentModel.this, singleDocuments, notModified));
			}
			
		});
		
		singleDocuments.add(currentDoc);
		observers.forEach(observer -> observer.documentAdded(currentDoc));
		addTab("(unnamed)", new JScrollPane(currentDoc.getTextComponent()));
		setToolTipTextAt(singleDocuments.size()-1, "(unnamed)");
		setTabComponentAt(0, new ButtonTabComponent(this, singleDocuments, notModified));
		setSelectedIndex(0);
		
		addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Window window = SwingUtilities.getWindowAncestor(DefaultMultipleDocumentModel.this);
				JFrame frame = (JFrame) window;
				DefaultMultipleDocumentModel dmdm = (DefaultMultipleDocumentModel) e.getSource();
				String path = "(unnamed)";
				int previousDocIndex = getIndexOfDocument(currentDoc);
				currentDoc = singleDocuments.get(dmdm.getSelectedIndex());
				if(currentDoc.getFilePath() != null)
					path = dmdm.getCurrentDocument().getFilePath().toString();
				frame.setTitle(path + " - JNotepad++");
				frame.repaint();
				SingleDocumentModel previousDoc = getDocument(previousDocIndex);
				observers.forEach(l -> l.currentDocumentChanged(previousDoc, currentDoc));
			}
		});

	}
	
	@Override
	public Iterator<SingleDocumentModel> iterator() {
		Iterator<SingleDocumentModel> it = new Iterator<SingleDocumentModel>() {
			private int currentIndex = 0;
			
			@Override
			public boolean hasNext() {
				return currentIndex < singleDocuments.size();
			}

			@Override
			public SingleDocumentModel next() {
				return singleDocuments.get(currentIndex++);
			}
		};
		return it;
	}

	@Override
	public JComponent getVisualComponent() {
		return this;
	}

	/**
	 * Creates a new document and adds it to this model.
	 */
	@Override
	public SingleDocumentModel createNewDocument() {
		currentDoc = new DefaultSingleDocumentModel(null, "");
		currentDoc.addSingleDocumentListener(new SingleDocumentListener() {

			@Override
			public void documentModifyStatusUpdated(SingleDocumentModel model) {
				int indexOfSelected = DefaultMultipleDocumentModel.this.getSelectedIndex();
				if(model.isModified()) {
					DefaultMultipleDocumentModel.this.setTabComponentAt(indexOfSelected, new ButtonTabComponent(
							DefaultMultipleDocumentModel.this, singleDocuments, modified));
				} else {
					DefaultMultipleDocumentModel.this.setTabComponentAt(indexOfSelected, new ButtonTabComponent(
							DefaultMultipleDocumentModel.this, singleDocuments, notModified));
				}
			}

			@Override
			public void documentFilePathUpdated(SingleDocumentModel model) {
				Window window = SwingUtilities.getWindowAncestor(DefaultMultipleDocumentModel.this);
				JFrame frame = (JFrame) window;
				frame.setTitle(model.getFilePath() + " - JNotepad++");
				int indexOfSelected = DefaultMultipleDocumentModel.this.getSelectedIndex();
				DefaultMultipleDocumentModel.this.setTitleAt(indexOfSelected, model.getFilePath().getFileName().toString());
				DefaultMultipleDocumentModel.this.setTabComponentAt(indexOfSelected, new ButtonTabComponent(
						DefaultMultipleDocumentModel.this, singleDocuments, notModified));
			}
			
		});
		singleDocuments.add(currentDoc);
		observers.forEach(observer -> observer.documentAdded(currentDoc));
		// open a new tab
		addTab("(unnamed)", new JScrollPane(currentDoc.getTextComponent()));
		setToolTipTextAt(singleDocuments.size()-1, "(unnamed)");
		setTabComponentAt(singleDocuments.size()-1, new ButtonTabComponent(this, singleDocuments, notModified));
		setSelectedIndex(singleDocuments.size()-1);
		return currentDoc;
	}

	@Override
	public SingleDocumentModel getCurrentDocument() {
		return currentDoc;
	}

	/**
	 * Loads an existing document from the disk and
	 * adds it to this model.
	 */
	@Override
	public SingleDocumentModel loadDocument(Path path) {
		if(path == null) throw new NullPointerException("Path must not be null.");
		
		if(!Files.isReadable(path)) {
			JOptionPane.showMessageDialog(
					DefaultMultipleDocumentModel.this, 
					"File "+path.toAbsolutePath()+" doesn't exist!", 
					"Error", 
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
		
		byte[] okteti;
		try {
			okteti = Files.readAllBytes(path);
		} catch(Exception ex) {
			JOptionPane.showMessageDialog(
					this, 
					"Error reading file " + path.toAbsolutePath() + ".", 
					"Error", 
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
		String text = new String(okteti, StandardCharsets.UTF_8);
		
		SingleDocumentModel foundDoc = findForPath(path);
		if(foundDoc == null) {
			DefaultSingleDocumentModel newLoaded = new DefaultSingleDocumentModel(path, text);
			currentDoc = newLoaded;
			currentDoc.addSingleDocumentListener(new SingleDocumentListener() {

				@Override
				public void documentModifyStatusUpdated(SingleDocumentModel model) {
					int indexOfSelected = DefaultMultipleDocumentModel.this.getSelectedIndex();
					if(model.isModified()) {
						DefaultMultipleDocumentModel.this.setTabComponentAt(indexOfSelected, new ButtonTabComponent(
								DefaultMultipleDocumentModel.this, singleDocuments, modified));
					} else {
						DefaultMultipleDocumentModel.this.setTabComponentAt(indexOfSelected, new ButtonTabComponent(
								DefaultMultipleDocumentModel.this, singleDocuments, notModified));
					}
				}

				@Override
				public void documentFilePathUpdated(SingleDocumentModel model) {
					Window window = SwingUtilities.getWindowAncestor(DefaultMultipleDocumentModel.this);
					JFrame frame = (JFrame) window;
					frame.setTitle(model.getFilePath() + " - JNotepad++");
					int indexOfSelected = DefaultMultipleDocumentModel.this.getSelectedIndex();
					DefaultMultipleDocumentModel.this.setTitleAt(indexOfSelected, model.getFilePath().getFileName().toString());
					DefaultMultipleDocumentModel.this.setTabComponentAt(indexOfSelected, new ButtonTabComponent(
							DefaultMultipleDocumentModel.this, singleDocuments, notModified));
				}
				
			});
			singleDocuments.add(currentDoc);
			observers.forEach(observer -> observer.documentAdded(currentDoc));
			
			addTab(path.getFileName().toString(), new JScrollPane(currentDoc.getTextComponent()));
			setToolTipTextAt(singleDocuments.size()-1, path.toString());
			setTabComponentAt(singleDocuments.size()-1, new ButtonTabComponent(this, singleDocuments, notModified));
			setSelectedIndex(singleDocuments.size()-1);
			return currentDoc;
		} else {
			setSelectedIndex(getIndexOfDocument(foundDoc));
			currentDoc = foundDoc;
			return foundDoc;
		}
		
	}

	/**
	 * Saves current document changes or saves the document to
	 * the desired location if it hasn't been saved yet.
	 */
	@Override
	public void saveDocument(SingleDocumentModel document, Path newPath) {
		byte[] podatci = currentDoc.getTextComponent().getText().getBytes(StandardCharsets.UTF_8);
		
		try {
			if(newPath == null)
				// save
				Files.write(document.getFilePath(), podatci);
			else {
				// save as
				Files.write(newPath, podatci);
				currentDoc.setFilePath(newPath);
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(
					DefaultMultipleDocumentModel.this, 
					"Error saving file "+newPath.toFile().getAbsolutePath()+".\n", 
					"Error", 
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		currentDoc.setModified(false);
	}

	/**
	 * Removes the desired document from this model.
	 */
	@Override
	public void closeDocument(SingleDocumentModel document) {
		singleDocuments.remove(document);
		observers.forEach(observer -> observer.documentRemoved(document));
	}

	@Override
	public void addMultipleDocumentListener(MultipleDocumentListener l) {
		observers.add(l);
	}

	@Override
	public void removeMultipleDocumentListener(MultipleDocumentListener l) {
		observers.remove(l);
	}

	@Override
	public int getNumberOfDocuments() {
		return singleDocuments.size();
	}

	@Override
	public SingleDocumentModel getDocument(int index) {
		return singleDocuments.get(index);
	}

	/**
	 * Trys to find a document among the opens ones in this model
	 * using the provided path.
	 * @return Document with the desired path, null if not found.
	 */
	@Override
	public SingleDocumentModel findForPath(Path path) {
		if(path == null) throw new NullPointerException("Path must not be null.");
		Iterator<SingleDocumentModel> it = iterator();
		while(it.hasNext()) {
			SingleDocumentModel next = it.next();
			if(next.getFilePath() != null && next.getFilePath().equals(path)) return next;
		}
		return null;
	}

	/**
	 * @return index of provided document in the list of open documents
	 * of this model, -1 otherwise
	 */
	@Override
	public int getIndexOfDocument(SingleDocumentModel doc) {
		if(doc == null) return -1;
		int index = -1;
		int i = 0;
		while(iterator().hasNext()) {
			SingleDocumentModel next = iterator().next();
			if(next.getFilePath() == doc.getFilePath()) {
				index = i;
				break;
			}
			
			i++;
		}
		return index;
	}
	
	public ArrayList<SingleDocumentModel> getSingleDocuments(){
		return singleDocuments;
	}

	public void setCurrentDocument(SingleDocumentModel doc) {
		currentDoc = doc;
	}
	
	private ImageIcon loadImage(String path) {
		InputStream is = this.getClass().getResourceAsStream(path);
		if(is==null)
			throw new NullPointerException();
		byte[] bytes;
		try {
			bytes = is.readAllBytes();
			is.close();
			return new ImageIcon(bytes);
		} catch (IOException e) {
			System.out.println("Error reading image bytes.");
			return null;
		}
	}
}
