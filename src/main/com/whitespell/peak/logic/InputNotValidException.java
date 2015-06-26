package main.com.whitespell.peak.logic;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         6/25/15
 *         main.com.whitespell.peak.logic
 */
public class InputNotValidException extends Throwable {

    public String getDetailMessage() {
        return detailMessage;
    }

    private final String detailMessage;

    public InputNotValidException(String detailMessage) {
        this.detailMessage = detailMessage;
    }
}
