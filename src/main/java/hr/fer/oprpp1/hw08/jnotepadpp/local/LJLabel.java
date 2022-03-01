package hr.fer.oprpp1.hw08.jnotepadpp.local;

import javax.swing.JLabel;

public class LJLabel extends JLabel {
	private static final long serialVersionUID = 1L;
	private String key;
	ILocalizationProvider prov;
	ILocalizationListener listener;
	String text;
	
	public LJLabel(String key, ILocalizationProvider lp, String text) {
		this.key = key;
		this.prov = lp;
		this.text = text;
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
		setText(translation + text);
	}
}
