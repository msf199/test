package main.com.whitespell.peak.model;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         2/13/15
 *         whitespell.model
 */
public class ContentTypeObject {

    int content_type_id;
    String content_type_name;
    public ContentTypeObject(int content_type_id, String content_type_name) {

        this.content_type_id = content_type_id;
        this.content_type_name = content_type_name;
    }

    public int getContent_type_id() {
        return content_type_id;
    }

    public String getContent_type_name() {
        return content_type_name;
    }
}