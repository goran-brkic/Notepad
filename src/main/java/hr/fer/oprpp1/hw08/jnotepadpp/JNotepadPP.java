package hr.fer.oprpp1.hw08.jnotepadpp;

import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;

import hr.fer.oprpp1.hw08.jnotepadpp.local.FormLocalizationProvider;
import hr.fer.oprpp1.hw08.jnotepadpp.local.LJMenu;
import hr.fer.oprpp1.hw08.jnotepadpp.local.LJMenuItem;
import hr.fer.oprpp1.hw08.jnotepadpp.local.LJLabel;
import hr.fer.oprpp1.hw08.jnotepadpp.local.LocalizableAction;
import hr.fer.oprpp1.hw08.jnotepadpp.local.LocalizationProvider;

/**
 * This class creates a Swing application of a JNotepadPP
 * @author Goran
 *
 */
public class JNotepadPP extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private MultipleDocumentModel mdm;
	private StatusBar statusBar;
	FormLocalizationProvider flp;
	
	public JNotepadPP() {
		
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setLocation(0, 0);
		setSize(600, 600);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent w) {
				boolean proceed = checkSaved();
				if(proceed)
					dispose();
			}
		});
		flp = new FormLocalizationProvider(LocalizationProvider.getInstance(), this);
		initGUI(this);
	}
	
	private void initGUI(JNotepadPP frame) {
		mdm = new DefaultMultipleDocumentModel();
		statusBar = new StatusBar(mdm, flp);
		getContentPane().add(mdm.getVisualComponent(), BorderLayout.CENTER);
		getContentPane().add(statusBar, BorderLayout.PAGE_END);
		setTitle("(unnamed)" + " - JNotepad++");
		
		createActions();
		createMenus();
		createToolbars();		
	}
	
	/**
	 * Checks if there are any modified documents open.
	 * @return true if not, false otherwise
	 */
	private boolean checkSaved() {
		Iterator<SingleDocumentModel> it = mdm.iterator();
		while(it.hasNext()) {
			SingleDocumentModel next = it.next();
			if(next.isModified()) {
				Object[] options = {"Save",
	                    "Discard",
	                    "Abort"};
				String fileName = next.getFilePath() == null ? "(unnamed)" : next.getFilePath().getFileName().toString();
				int n = JOptionPane.showOptionDialog(this,
				    "What would you like to do with the changes for " + fileName + "?",
				    "There are unsaved files open",
				    JOptionPane.YES_NO_CANCEL_OPTION,
				    JOptionPane.QUESTION_MESSAGE,
				    null,
				    options,
				    options[0]);
				if(n == 2) {
					return false;
				} else if (n == 1) {
					continue;
				} else {
					((DefaultMultipleDocumentModel) mdm).setCurrentDocument(next);
					JButton buttonToSimulateClicking = new JButton(saveDocumentAction);
					buttonToSimulateClicking.doClick();
				}
			}
		}
		return true;
	}
	
	private Action newDocumentAction = new AbstractAction() {
		
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			mdm.createNewDocument();
		}
	};
	
	private Action openDocumentAction = new AbstractAction() {
		
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser fc = new JFileChooser();
			fc.setDialogTitle("Open file");
			if(fc.showOpenDialog(JNotepadPP.this)!=JFileChooser.APPROVE_OPTION) {
				return;
			}
			File fileName = fc.getSelectedFile();
			Path filePath = fileName.toPath();
			
			mdm.loadDocument(filePath);

		}
	};
	
	private Action saveDocumentAction = new AbstractAction() {
		
		private static final long serialVersionUID = 1L;
		
		@Override
		public void actionPerformed(ActionEvent e) {
			Path current = mdm.getCurrentDocument().getFilePath();
			
			if(current == null) {
				// this file was never saved
				JButton buttonToSimulateClicking = new JButton(saveAsDocumentAction);
				buttonToSimulateClicking.doClick();
				return;
			}
			
			mdm.saveDocument(mdm.getCurrentDocument(), current);

			JOptionPane.showMessageDialog(
					JNotepadPP.this, 
					"File sucessfully saved.", 
					"Info", 
					JOptionPane.INFORMATION_MESSAGE);
		}
	};
	
	private Action saveAsDocumentAction = new AbstractAction() {
		
		private static final long serialVersionUID = 1L;
		
		@Override
		public void actionPerformed(ActionEvent e) {
			Path openedFilePath;
			// unsaved file
			JFileChooser jfc = new JFileChooser();
			jfc.setDialogTitle("Save document as");
			if(jfc.showSaveDialog(JNotepadPP.this)!=JFileChooser.APPROVE_OPTION) {
				JOptionPane.showMessageDialog(
						JNotepadPP.this, 
						"Not saved.",
						"Warning", 
						JOptionPane.WARNING_MESSAGE);
				return;
			}
			
			openedFilePath = jfc.getSelectedFile().toPath();
			
			if(mdm.findForPath(openedFilePath) != null) {
				JOptionPane.showMessageDialog(
						JNotepadPP.this, 
						"Tried to overwrite an open file.\nClose that file first then try again.",
						"Warning", 
						JOptionPane.WARNING_MESSAGE);
				return;
			}
			
			mdm.saveDocument(mdm.getCurrentDocument(), openedFilePath);

			JOptionPane.showMessageDialog(
					JNotepadPP.this, 
					"File sucessfully saved.", 
					"Info", 
					JOptionPane.INFORMATION_MESSAGE);
		}
	};
	
	private Action cutAction = new AbstractAction() {
		
		private static final long serialVersionUID = 1L;
		
		@Override
		public void actionPerformed(ActionEvent e) {
			SingleDocumentModel doc = mdm.getCurrentDocument();
			JTextArea editor = doc.getTextComponent();
			editor.cut();
		}
	};
	
	private Action copyAction = new AbstractAction() {
		
		private static final long serialVersionUID = 1L;
		
		@Override
		public void actionPerformed(ActionEvent e) {
			SingleDocumentModel doc = mdm.getCurrentDocument();
			JTextArea editor = doc.getTextComponent();
			editor.copy();
		}
	};
	
	private Action pasteAction = new AbstractAction() {
		
		private static final long serialVersionUID = 1L;
		
		@Override
		public void actionPerformed(ActionEvent e) {
			SingleDocumentModel doc = mdm.getCurrentDocument();
			JTextArea editor = doc.getTextComponent();
			editor.paste();
		}
	};
	
	private Action statsAction = new AbstractAction() {
		
		private static final long serialVersionUID = 1L;
		
		@Override
		public void actionPerformed(ActionEvent e) {
			long numberOfCharacters = 0;
			long numberOfNonBlankCharacters = 0;
			String[] lines = mdm.getCurrentDocument().getTextComponent().getText().split("\\r?\\n");
			for(String line : lines) {
				for(int i=0; i<line.length(); i++) {
					if(Character.isWhitespace(line.charAt(i)))
						numberOfCharacters++;
					else {
						numberOfNonBlankCharacters++;
						numberOfCharacters++;
					}
				}
			}
			JOptionPane.showMessageDialog(
					JNotepadPP.this, 
					"Your document has " + numberOfCharacters +
					" characters, " + numberOfNonBlankCharacters + " non-blank characters and " + lines.length + " lines.", 
					"Statistics", 
					JOptionPane.INFORMATION_MESSAGE);
		}
	};
	
	private Action exitAction = new AbstractAction() {
		
		private static final long serialVersionUID = 1L;
		
		@Override
		public void actionPerformed(ActionEvent e) {
			checkSaved();
			dispose();
		}
	};
	
	private Action upCaseAction = new AbstractAction() {
		
		private static final long serialVersionUID = 1L;
		
		@Override
		public void actionPerformed(ActionEvent e) {
			SingleDocumentModel doc = mdm.getCurrentDocument();
			JTextArea editor = doc.getTextComponent();
			int len = Math.abs(editor.getCaret().getDot()-editor.getCaret().getMark());
			int offset = 0;
			if(len!=0) {
				offset = Math.min(editor.getCaret().getDot(),editor.getCaret().getMark());
			} else {
				len = editor.getText().length();
			}
			try {
				String text = editor.getText(offset, len);
				text = changeCase(text);
				editor.cut();
				editor.insert(text, offset);
			} catch(BadLocationException ex) {
				ex.printStackTrace();
			}
			mdm.getCurrentDocument().setModified(true);
		}

		private String changeCase(String text) {
			char[] znakovi = text.toCharArray();
			for(int i = 0; i < znakovi.length; i++) {
				char c = znakovi[i];
				znakovi[i] = Character.toUpperCase(c);
			}
			return new String(znakovi);
		}
	};
	
	private Action downCaseAction = new AbstractAction() {
		
		private static final long serialVersionUID = 1L;
		
		@Override
		public void actionPerformed(ActionEvent e) {
			SingleDocumentModel doc = mdm.getCurrentDocument();
			JTextArea editor = doc.getTextComponent();
			int len = Math.abs(editor.getCaret().getDot()-editor.getCaret().getMark());
			int offset = 0;
			if(len!=0) {
				offset = Math.min(editor.getCaret().getDot(),editor.getCaret().getMark());
			} else {
				len = editor.getText().length();
			}
			try {
				String text = editor.getText(offset, len);
				text = changeCase(text);
				editor.cut();
				editor.insert(text, offset);
			} catch(BadLocationException ex) {
				ex.printStackTrace();
			}
			mdm.getCurrentDocument().setModified(true);
		}

		private String changeCase(String text) {
			char[] znakovi = text.toCharArray();
			for(int i = 0; i < znakovi.length; i++) {
				char c = znakovi[i];
				znakovi[i] = Character.toLowerCase(c);
			}
			return new String(znakovi);
		}
	};
	
	private Action invertCaseAction = new AbstractAction() {
		
		private static final long serialVersionUID = 1L;
		
		@Override
		public void actionPerformed(ActionEvent e) {
			SingleDocumentModel doc = mdm.getCurrentDocument();
			JTextArea editor = doc.getTextComponent();
			int len = Math.abs(editor.getCaret().getDot()-editor.getCaret().getMark());
			int offset = 0;
			if(len!=0) {
				offset = Math.min(editor.getCaret().getDot(),editor.getCaret().getMark());
			} else {
				len = editor.getText().length();
			}
			try {
				String text = editor.getText(offset, len);
				text = changeCase(text);
				editor.cut();
				editor.insert(text, offset);
			} catch(BadLocationException ex) {
				ex.printStackTrace();
			}
			mdm.getCurrentDocument().setModified(true);
		}

		private String changeCase(String text) {
			char[] znakovi = text.toCharArray();
			for(int i = 0; i < znakovi.length; i++) {
				char c = znakovi[i];
				if(Character.isLowerCase(c)) {
					znakovi[i] = Character.toUpperCase(c);
				} else if(Character.isUpperCase(c)) {
					znakovi[i] = Character.toLowerCase(c);
				}
			}
			return new String(znakovi);
		}
	};

	private Action ascSortAction = new AbstractAction() {
		
		private static final long serialVersionUID = 1L;
		
		@Override
		public void actionPerformed(ActionEvent e) {
			SingleDocumentModel doc = mdm.getCurrentDocument();
			JTextArea editor = doc.getTextComponent();
			editor.cut();
			try {
				String text = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
				String[] lines = text.split("\n");
				Arrays.sort(lines);
				text = "";
				for(String line : lines) {
					if(line.equals(lines[lines.length-1])) {
						text += line;
						break;
					}
					text += line + "\n";
				}
				StringSelection stringSelection = new StringSelection(text);
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
				editor.paste();

			} catch (HeadlessException | UnsupportedFlavorException | IOException e2) {
				e2.printStackTrace();
			} 
			
			mdm.getCurrentDocument().setModified(true);
		}

	};

	private Action descSortAction = new AbstractAction() {
		
		private static final long serialVersionUID = 1L;
		
		@Override
		public void actionPerformed(ActionEvent e) {
			SingleDocumentModel doc = mdm.getCurrentDocument();
			JTextArea editor = doc.getTextComponent();
			editor.cut();
			try {
				String text = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
				String[] lines = text.split("\n");
				Arrays.sort(lines, Collections.reverseOrder());
				text = "";
				for(String line : lines) {
					if(line.equals(lines[lines.length-1])) {
						text += line;
						break;
					}
					text += line + "\n";
				}
				StringSelection stringSelection = new StringSelection(text);
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
				editor.paste();

			} catch (HeadlessException | UnsupportedFlavorException | IOException e2) {
				e2.printStackTrace();
			} 
			
			mdm.getCurrentDocument().setModified(true);
		}

	};
	
	private Action uniqueAction = new AbstractAction() {
		
		private static final long serialVersionUID = 1L;
		
		@Override
		public void actionPerformed(ActionEvent e) {
			SingleDocumentModel doc = mdm.getCurrentDocument();
			JTextArea editor = doc.getTextComponent();
			editor.cut();
			try {
				String text = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
				String[] lines = text.split("\n");
				text = "";

				lines = Arrays.stream(lines).distinct().toArray(String[]::new);

				for(String line : lines) {
					if(line != null && line.equals(lines[lines.length-1])) {
						text += line;
						break;
					}
					if(line != null)
						text += line + "\n";
				}
				StringSelection stringSelection = new StringSelection(text);
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
				editor.paste();

			} catch (HeadlessException | UnsupportedFlavorException | IOException e2) {
				e2.printStackTrace();
			} 
			
			mdm.getCurrentDocument().setModified(true);
		}

	};
	
	private Action changeLangActionHR = new AbstractAction() {
		
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			LocalizationProvider.getInstance().setLanguage("hr");
		}
	};
	
	private Action changeLangActionEN = new AbstractAction() {
		
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			LocalizationProvider.getInstance().setLanguage("en");
		}
	};
	
	private Action changeLangActionDE = new AbstractAction() {
		
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			LocalizationProvider.getInstance().setLanguage("de");
		}
	};
	
	private void createActions() {
		
		newDocumentAction.putValue(
				Action.ACCELERATOR_KEY, 
				KeyStroke.getKeyStroke("control N")); 
		newDocumentAction.putValue(
				Action.MNEMONIC_KEY, 
				KeyEvent.VK_N); 
		newDocumentAction.putValue(
				Action.SHORT_DESCRIPTION, 
				"Creates a new document."); 
		
		openDocumentAction.putValue(
				Action.ACCELERATOR_KEY, 
				KeyStroke.getKeyStroke("control O")); 
		openDocumentAction.putValue(
				Action.MNEMONIC_KEY, 
				KeyEvent.VK_O); 
		openDocumentAction.putValue(
				Action.SHORT_DESCRIPTION, 
				"Used to open existing file from disk."); 
		
		saveDocumentAction.putValue(
				Action.ACCELERATOR_KEY, 
				KeyStroke.getKeyStroke("control S")); 
		saveDocumentAction.putValue(
				Action.MNEMONIC_KEY, 
				KeyEvent.VK_S); 
		saveDocumentAction.putValue(
				Action.SHORT_DESCRIPTION, 
				"Used to save current file to disk."); 
		
		saveAsDocumentAction.putValue(
				Action.ACCELERATOR_KEY, 
				KeyStroke.getKeyStroke("F2")); 
		saveAsDocumentAction.putValue(
				Action.MNEMONIC_KEY, 
				KeyEvent.VK_D); 
		saveAsDocumentAction.putValue(
				Action.SHORT_DESCRIPTION, 
				"Used to save the text as a file."); 
		
		cutAction.putValue(
				Action.ACCELERATOR_KEY, 
				KeyStroke.getKeyStroke("control x")); 
		cutAction.putValue(
				Action.MNEMONIC_KEY, 
				KeyEvent.VK_X); 
		cutAction.putValue(
				Action.SHORT_DESCRIPTION, 
				"Used to copy and delete the selected text."); 
		
		copyAction.putValue(
				Action.ACCELERATOR_KEY, 
				KeyStroke.getKeyStroke("control c")); 
		copyAction.putValue(
				Action.MNEMONIC_KEY, 
				KeyEvent.VK_C); 
		copyAction.putValue(
				Action.SHORT_DESCRIPTION, 
				"Copies selected text.");
		
		pasteAction.putValue(
				Action.ACCELERATOR_KEY, 
				KeyStroke.getKeyStroke("control v")); 
		pasteAction.putValue(
				Action.MNEMONIC_KEY, 
				KeyEvent.VK_V); 
		pasteAction.putValue(
				Action.SHORT_DESCRIPTION, 
				"Pastes copied text.");
		
		statsAction.putValue(
				Action.SHORT_DESCRIPTION, 
				"Gives information about the current document.");
		
		exitAction.putValue(
				Action.ACCELERATOR_KEY, 
				KeyStroke.getKeyStroke("control X"));
		exitAction.putValue(
				Action.MNEMONIC_KEY, 
				KeyEvent.VK_X); 
		exitAction.putValue(
				Action.SHORT_DESCRIPTION, 
				"Exit application."); 
	}
	
	private void createMenus() {
		JMenuBar menuBar = new JMenuBar();
		
		JMenu langMenu = new LJMenu("languages", flp);
		langMenu.add(new LJMenuItem("croatian", flp, changeLangActionHR));
		langMenu.add(new LJMenuItem("english", flp, changeLangActionEN));
		langMenu.add(new LJMenuItem("german", flp, changeLangActionDE));
		menuBar.add(langMenu);
		
		JMenu fileMenu = new LJMenu("file", flp);
		menuBar.add(fileMenu);

		fileMenu.add(new LJMenuItem("new", flp, newDocumentAction));
		fileMenu.add(new LJMenuItem("open", flp, openDocumentAction));
		fileMenu.add(new LJMenuItem("save", flp, saveDocumentAction));
		fileMenu.add(new LJMenuItem("saveAs", flp, saveAsDocumentAction));
		fileMenu.addSeparator();
		fileMenu.add(new LJMenuItem("exit", flp, exitAction));
		
		JMenu editMenu = new LJMenu("edit", flp);
		menuBar.add(editMenu);
		
		editMenu.add(new LJMenuItem("cut", flp, cutAction));
		editMenu.add(new LJMenuItem("copy", flp, copyAction));
		editMenu.add(new LJMenuItem("paste", flp, pasteAction));
		editMenu.add(new LJMenuItem("statistics", flp, statsAction));
		
		JMenu toolsMenu = new LJMenu("tools", flp);
		JMenu changeCaseMenu = new JMenu(new LJLabel("changeCase", flp, "").getText());
		changeCaseMenu.add(new LJMenuItem("toUppercase", flp, upCaseAction));
		changeCaseMenu.add(new LJMenuItem("toLowercase", flp, downCaseAction));
		changeCaseMenu.add(new LJMenuItem("invertCase", flp, invertCaseAction));
		toolsMenu.add(changeCaseMenu);
		
		JMenu sortMenu = new LJMenu("sort", flp);
		sortMenu.add(new LJMenuItem("ascending", flp, ascSortAction));
		sortMenu.add(new LJMenuItem("descending", flp, descSortAction));
		toolsMenu.add(sortMenu);
		toolsMenu.add(new LJMenuItem("unique", flp, uniqueAction));
		
		menuBar.add(toolsMenu);
		
		this.setJMenuBar(menuBar);
		
		
	}
	
	private void createToolbars() {
		JToolBar toolBar = new JToolBar(new LJLabel("tools", flp, "").getText());
		toolBar.setFloatable(true);
		
		toolBar.add(new JButton(
				new LocalizableAction("new", flp) {
					@Override
					public void actionPerformed(ActionEvent e) {
						mdm.createNewDocument();
					}
				}
		));
		toolBar.add(new JButton(
				new LocalizableAction("open", flp) {
					@Override
					public void actionPerformed(ActionEvent e) {
						JFileChooser fc = new JFileChooser();
						fc.setDialogTitle("Open file");
						if(fc.showOpenDialog(JNotepadPP.this)!=JFileChooser.APPROVE_OPTION) {
							return;
						}
						File fileName = fc.getSelectedFile();
						Path filePath = fileName.toPath();
						
						mdm.loadDocument(filePath);
					}
				}
		));
		toolBar.add(new JButton(
				new LocalizableAction("save", flp) {
					@Override
					public void actionPerformed(ActionEvent e) {
						Path current = mdm.getCurrentDocument().getFilePath();
						
						if(current == null) {
							// this file was never saved
							JButton buttonToSimulateClicking = new JButton(saveAsDocumentAction);
							buttonToSimulateClicking.doClick();
							return;
						}
						
						mdm.saveDocument(mdm.getCurrentDocument(), current);

						JOptionPane.showMessageDialog(
								JNotepadPP.this, 
								"File sucessfully saved.", 
								"Info", 
								JOptionPane.INFORMATION_MESSAGE);
					}
				}
		));
		toolBar.add(new JButton(
				new LocalizableAction("saveAs", flp) {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						Path openedFilePath;
						// unsaved file
						JFileChooser jfc = new JFileChooser();
						jfc.setDialogTitle("Save document as");
						if(jfc.showSaveDialog(JNotepadPP.this)!=JFileChooser.APPROVE_OPTION) {
							JOptionPane.showMessageDialog(
									JNotepadPP.this, 
									"Not saved.",
									"Warning", 
									JOptionPane.WARNING_MESSAGE);
							return;
						}
						
						openedFilePath = jfc.getSelectedFile().toPath();
						
						if(mdm.findForPath(openedFilePath) != null) {
							JOptionPane.showMessageDialog(
									JNotepadPP.this, 
									"Tried to overwrite an open file.\nClose that file first then try again.",
									"Warning", 
									JOptionPane.WARNING_MESSAGE);
							return;
						}
						
						mdm.saveDocument(mdm.getCurrentDocument(), openedFilePath);

						JOptionPane.showMessageDialog(
								JNotepadPP.this, 
								"File sucessfully saved.", 
								"Info", 
								JOptionPane.INFORMATION_MESSAGE);
					}
				}
		));
		toolBar.addSeparator();
		toolBar.add(new JButton(
				new LocalizableAction("cut", flp) {
					@Override
					public void actionPerformed(ActionEvent e) {
						SingleDocumentModel doc = mdm.getCurrentDocument();
						JTextArea editor = doc.getTextComponent();
						editor.cut();
					}
				}
		));
		toolBar.add(new JButton(
				new LocalizableAction("copy", flp) {
					@Override
					public void actionPerformed(ActionEvent e) {
						SingleDocumentModel doc = mdm.getCurrentDocument();
						JTextArea editor = doc.getTextComponent();
						editor.copy();
					}
				}
		));
		toolBar.add(new JButton(
				new LocalizableAction("paste", flp) {
					@Override
					public void actionPerformed(ActionEvent e) {
						SingleDocumentModel doc = mdm.getCurrentDocument();
						JTextArea editor = doc.getTextComponent();
						editor.paste();
					}
				}
		));
		toolBar.addSeparator();
		toolBar.add(new JButton(
				new LocalizableAction("statistics", flp) {
					@Override
					public void actionPerformed(ActionEvent e) {
						long numberOfCharacters = 0;
						long numberOfNonBlankCharacters = 0;
						String[] lines = mdm.getCurrentDocument().getTextComponent().getText().split("\\r?\\n");
						for(String line : lines) {
							for(int i=0; i<line.length(); i++) {
								if(Character.isWhitespace(line.charAt(i)))
									numberOfCharacters++;
								else {
									numberOfNonBlankCharacters++;
									numberOfCharacters++;
								}
							}
						}
						JOptionPane.showMessageDialog(
								JNotepadPP.this, 
								"Your document has " + numberOfCharacters +
								" characters, " + numberOfNonBlankCharacters + " non-blank characters and " + lines.length + " lines.", 
								"Statistics", 
								JOptionPane.INFORMATION_MESSAGE);
					}
				}
		));
		
		this.getContentPane().add(toolBar, BorderLayout.PAGE_START);
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new JNotepadPP().setVisible(true);
			}
		});
	}
}
