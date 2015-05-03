package whitespell.model;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         2/13/15
 *         whitespell.model
 */
public class ErrorObject {

    int statusCode;
    int errorId;
    String errorMessage;

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setErrorId(int errorId) {
        this.errorId = errorId;
    }
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}
