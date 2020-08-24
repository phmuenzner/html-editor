package onlinehilfe.contentbuilder;

public class MetadataIdFilenameCreator implements FilenameCreator {
	@Override
	public String buildOutputFileName(ContentMetadata contentMetadata) {
		if (contentMetadata.getId()!=null)
			return contentMetadata.getId();
		return null;
	}
}
