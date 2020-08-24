package onlinehilfe.dialogs;

import java.util.Properties;

public class RenameContentWizard extends NewContentWizard {

	static {
		wizardTitle = "Content Umbenennen"; 
		wizardPageDescription = "Hier können sie den Titel des Content umbenennen. Dieser Titel muss eindeutig sein.";
		wizardPageTitleLabel = "Titel des Contents:";
	}
	
	public RenameContentWizard(Properties returnProperties) {
		super(returnProperties);
	}
}
