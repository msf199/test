package main.com.whitespell.peak.model;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         2/13/15
 *         whitespell.model
 */
public class ErrorObject {

    String className;
    int httpStatusCode;

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getErrorId() {
        return errorId;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public String getClassName() {
        return className;
    }

    int errorId;
    String errorMessage;

    public void setClassName(String className) {
        this.className = className;
    }

    public void setHttpStatusCode(int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public void setErrorId(int errorId) {
        this.errorId = errorId;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}
