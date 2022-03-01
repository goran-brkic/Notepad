package hr.fer.oprpp1.hw08.jnotepadpp.local;

import java.util.ArrayList;

public abstract class AbstractLocalizationProvider implements ILocalizationProvider, ILocalizationListener{
	ArrayList<ILocalizationListener> listeners;
	
	public AbstractLocalizationProvider() {
		listeners = new ArrayList<>();
	};
	
	public void addLocalizationListener(ILocalizationListener l) {
		listeners.add(l);
	}
	
	public void removeLocalizationListener(ILocalizationListener l) {
		listeners.remove(l);
	}
	
	public void fire() {
		listeners.forEach(listener -> listener.localizationChanged());
	}
}
