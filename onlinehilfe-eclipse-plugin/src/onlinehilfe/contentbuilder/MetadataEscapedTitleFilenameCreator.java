package onlinehilfe.contentbuilder;

public class MetadataEscapedTitleFilenameCreator implements FilenameCreator {
	@Override
	public String buildOutputFileName(ContentMetadata contentMetadata) {
		if (contentMetadata.getTitle()==null) {
			return null;
		}
		
		return buildFilenameFromTitle(contentMetadata.getTitle(), 0);
	}
	
	public static String buildFilenameFromTitle(final String title, int maxlength) {
		if (title == null) {
			return null;
		}
		
		String filename = title.toLowerCase()
				.replace("ä", "ae")
				.replace("ö", "oe")
				.replace("ü", "ue")
				.replace("Ä", "Ae")
				.replace("Ö", "Oe")
				.replace("Ü", "Ue")
				.replace("ß", "ss")
				.replace("–", "-") // so n blöder langer "unstandardisierter" Bindestrich aus Word
				.replaceAll("[@=?`\\\\\\[\\]<>:;^]|[^1-zA-Z0-9_]", "_") //ersetze komische zeichen
				.replaceAll("__+", "_") //ersetze dopplete ersatzzeichen
				;
		
		//camelCasen damit die Unterstriche raus kommen
		filename = toCamelCase(filename, "_");
		
		//wenns noch zu lang ist weiter kürzen
		if (maxlength>0) {
			
			//erstmal von hinten nach vorn die Vokale entfernen 
			while (filename.length()>maxlength) {
				String newFilename = replaceLast(filename, "[aeiou]", "");
				if (filename.length() == newFilename.length()) {
					break;
				}
				filename = newFilename;
			}
			
			//letzte möglichkeit, einfach kürzen
			if (filename.length()>maxlength) {
				filename = filename.substring(0, maxlength);
			}
		}
		
		return filename;
	}
	
	private static String replaceLast(String text, String regex, String replacement) {
        return text.replaceFirst("(?s)"+regex+"(?!.*?"+regex+")", replacement);
    }
	
	private static String toCamelCase(String s, String space){
		   String[] parts = s.split(space);
		   String camelCaseString = "";
		   for (String part : parts){
		      camelCaseString = camelCaseString + part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase();
		   }
		   return camelCaseString;
	}
}
