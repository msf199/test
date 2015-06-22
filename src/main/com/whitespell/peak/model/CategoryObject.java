package main.com.whitespell.peak.model;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         2/13/15
 *         whitespell.model
 */
public class CategoryObject {
    public int getCategory_id() {
        return category_id;
    }

    public String getCategory_name() {
        return category_name;
    }

    public String getCategory_thumbnail() {
        return category_thumbnail;
    }

    public int getCategory_followers() {
        return category_followers;
    }

    public int getCategory_publishers() {
        return category_publishers;
    }

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
