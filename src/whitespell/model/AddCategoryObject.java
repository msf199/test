package whitespell.model;

/**
 * @author Josh Lipson(mrgalkon)
 */
public class AddCategoryObject {

    public boolean isCategoryAdded() {
        return this.category_added;
    }

    public void setCategoryAdded(boolean category_added) {
        this.category_added = category_added;
    }

    private boolean category_added;

}
