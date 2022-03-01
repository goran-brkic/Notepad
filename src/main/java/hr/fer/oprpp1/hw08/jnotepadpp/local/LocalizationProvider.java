package hr.fer.oprpp1.hw08.jnotepadpp.local;

import java.util.Locale;
import java.util.ResourceBundle;

public class LocalizationProvider extends AbstractLocalizationProvider{
	private static final LocalizationProvider instance = new LocalizationProvider();
	private String language;
	private ResourceBundle bundle;
	
	public LocalizationProvider() {
		language = "en";
		Locale locale = Locale.forLanguageTag(language);
		bundle = ResourceBundle.getBundle("hr.fer.oprpp1.hw08.jnotepadpp.local.prijevodi", locale);
	};
	
	public static LocalizationProvider getInstance() {
		return instance;
	}
	
	public void setLanguage(String language) {
		this.language = language;
		Locale locale = Locale.forLanguageTag(language);
		bundle = ResourceBundle.getBundle("hr.fer.oprpp1.hw08.jnotepadpp.local.prijevodi", locale);
		localizationChanged();
	}
	
	public String getString(String key) {
		return bundle.getString(key);
	}
	
	public String getLanguage() {
		return language;
	}

	@Override
	public void localizationChanged() {
		fire();
	}

}
