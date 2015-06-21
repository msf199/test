package main.com.whitespell.peak.model;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         2/13/15
 *         whitespell.model
 */
public class CategoryObject {

    int category_id;
    String category_name;
    String category_thumbnail;
    int category_followers;
    int category_publishers;
    public CategoryObject(int category_id, String category_name, String category_thumbnail, int category_followers, int category_publishers) {

        this.category_id = category_id;
        this.category_name = category_name;
        this.category_thumbnail = category_thumbnail;
        this.category_followers = category_followers;
        this.category_publishers = category_publishers;
    }

}
