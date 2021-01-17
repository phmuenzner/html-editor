package onlinehilfe.dialogs;

import java.util.Properties;

public class RenameContentWizard extends NewContentWizard {
	
	public RenameContentWizard(Properties customFieldConfigurationProperties, Properties returnProperties) {
		super(customFieldConfigurationProperties, returnProperties);
		
		wizardTitle = "Content Umbenennen oder Konfigurieren"; 
		wizardPageDescription = "Hier können sie den Titel des Content umbenennen sowie den Content konfigurieren. Der Titel muss eindeutig sein.";
		wizardPageTitleLabel = "Titel des Contents:";
	}
}
