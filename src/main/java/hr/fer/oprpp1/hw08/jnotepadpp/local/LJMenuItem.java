package hr.fer.oprpp1.hw08.jnotepadpp.local;

import javax.swing.Action;
import javax.swing.JMenuItem;

public class LJMenuItem extends JMenuItem{
	private static final long serialVersionUID = 1L;
	private String key;
	ILocalizationProvider prov;
	ILocalizationListener listener;
	
	public LJMenuItem(String key, ILocalizationProvider lp, Action a) {
		this.key = key;
		this.prov = lp;
		listener = new ILocalizationListener() {
			
			@Override
			public void localizationChanged() {
				updateLabel();
			}
		};
		setAction(a);
		prov.addLocalizationListener(listener);
		updateLabel();
	}
	
	private void updateLabel() {
		String translation = prov.getString(key);
		setText(translation);
		if(this.getParent() != null) {
			this.getParent().revalidate();
			this.getParent().repaint();
		}
	}
}
