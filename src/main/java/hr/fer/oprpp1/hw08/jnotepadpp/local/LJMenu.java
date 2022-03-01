package hr.fer.oprpp1.hw08.jnotepadpp.local;

import javax.swing.JMenu;

public class LJMenu extends JMenu{
	private static final long serialVersionUID = 1L;
	private String key;
	ILocalizationProvider prov;
	ILocalizationListener listener;
	
	public LJMenu(String key, ILocalizationProvider lp) {
		this.key = key;
		this.prov = lp;
		listener = new ILocalizationListener() {
			
			@Override
			public void localizationChanged() {
				updateLabel();
			}
		};
		prov.addLocalizationListener(listener);
		updateLabel();
	}
	
	private void updateLabel() {
		String translation = prov.getString(key);
		setText(translation);
	}
}
