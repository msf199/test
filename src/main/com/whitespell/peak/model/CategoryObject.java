package main.com.whitespell.peak.model;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         2/13/15
 *         whitespell.model
 */
public class CategoryObject {
    public int getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getCategoryThumbnail() {
        return categoryThumbnail;
    }

    public int getCategoryFollowers() {
        return categoryFollowers;
    }

    public int getCategoryPublishers() {
        return categoryPublishers;
    }

    int categoryId;
    String categoryName;
    String categoryThumbnail;
    int categoryFollowers;
    int categoryPublishers;
    public CategoryObject(int categoryId, String categoryName, String categoryThumbnail, int categoryFollowers, int categoryPublishers) {

        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.categoryThumbnail = categoryThumbnail;
        this.categoryFollowers = categoryFollowers;
        this.categoryPublishers = categoryPublishers;
    }

}
