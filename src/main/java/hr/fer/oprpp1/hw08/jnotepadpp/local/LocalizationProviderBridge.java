package hr.fer.oprpp1.hw08.jnotepadpp.local;

public class LocalizationProviderBridge extends AbstractLocalizationProvider{
	private boolean connected;
	private String currentLanguage;
	private ILocalizationProvider parent;
	private ILocalizationListener listener;
	
	public LocalizationProviderBridge(ILocalizationProvider parent) {
		this.parent = parent;
		currentLanguage = parent.getLanguage();
//		listener = new ILocalizationListener() {
//			
//			@Override
//			public void localizationChanged() {
//				fire();
//			}
//		};
		this.listener = this::fire;
	}
	
	public void disconnect() {
		// parent.add ?
		LocalizationProvider.getInstance().removeLocalizationListener(listener);
		connected = false;
	}
	
	public void connect() {
		if(connected) {
			System.out.println("Already connected.");
			return;
		} else {
			// parent.add ?
			LocalizationProvider.getInstance().addLocalizationListener(listener);
			String currentLang = parent.getLanguage();
			if(!currentLanguage.equals(currentLang)) {
				currentLanguage = parent.getLanguage();
				fire();
			}
			connected = true;
		}
	}
	
	@Override
	public String getString(String key) {
		return parent.getString(key);
	}
	
	@Override
	public void localizationChanged() {
		fire();
	}

	@Override
	public String getLanguage() {
		return currentLanguage;
	}
	
	
}
