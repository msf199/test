package main.com.whitespell.peak.model;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         2/13/15
 *         whitespell.model
 */
public class ContentTypeObject {

    int contentTypeId;
    String contentTypeName;
    public ContentTypeObject(int contentTypeId, String contentTypeName) {

        this.contentTypeId = contentTypeId;
        this.contentTypeName = contentTypeName;
    }

    public int getContentTypeId() {
        return contentTypeId;
    }

    public String getContentTypeName() {
        return contentTypeName;
    }
}