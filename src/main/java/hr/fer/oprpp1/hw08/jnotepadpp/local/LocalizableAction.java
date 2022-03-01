package hr.fer.oprpp1.hw08.jnotepadpp.local;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

public class LocalizableAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	private String key;
	private ILocalizationProvider prov;
	private ILocalizationListener listener;
	
	public LocalizableAction(String key, ILocalizationProvider lp) {
		this.key = key;
		this.prov = lp;
		String translation = prov.getString(key);
		putValue(NAME, translation);
		listener = new ILocalizationListener() {
			
			@Override
			public void localizationChanged() {
				String translation = prov.getString(key);
				putValue(NAME, translation);
			}
		};
		prov.addLocalizationListener(listener);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		String translation = prov.getString(key);
		putValue(NAME, translation);
	}

}
