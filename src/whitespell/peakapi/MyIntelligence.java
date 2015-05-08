package whitespell.peakapi;

import whitespell.model.baseapi.WhitespellIntelligence;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         2/4/15
 *         whitespell.peakapi
 */
public class MyIntelligence extends WhitespellIntelligence {

    public MyIntelligence(String apiKey) {
        super(apiKey);
    }

    @Override
    public void start() throws Exception {
       // this.addHandler(new GenerateNewsFeedAction());

    }

}
