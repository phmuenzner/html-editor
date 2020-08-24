package onlinehilfe.contentbuilder;

public class MetadataEscapedTitleFilenameCreator implements FilenameCreator {
	@Override
	public String buildOutputFileName(ContentMetadata contentMetadata) {
		if (contentMetadata.getTitle()!=null)
			return contentMetadata.getTitle().replace(" ", "_");
		return null;
	}
}
