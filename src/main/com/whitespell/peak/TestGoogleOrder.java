package main.com.whitespell.peak;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import com.google.api.services.androidpublisher.model.SubscriptionPurchase;
import main.com.whitespell.peak.logic.config.Config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cory on 11/12/15.
 */
public class TestGoogleOrder {

    public static void main(String args[]){
        String purchaseToken = "";
        try {
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            JsonFactory jsonFactory = new JacksonFactory();

            List<String> scopes = new ArrayList<>();
            scopes.add(AndroidPublisherScopes.ANDROIDPUBLISHER);

            Credential credential = new GoogleCredential.Builder().setTransport(httpTransport).setJsonFactory(jsonFactory)
                    .setServiceAccountId(Config.GOOGLE_CLIENT_ID)
                    .setServiceAccountPrivateKeyFromP12File(new File(Config.GOOGLE_PRIVATE_KEY_PATH))
                    .setServiceAccountScopes(scopes).build();
            AndroidPublisher publisher = new AndroidPublisher.Builder(httpTransport, jsonFactory, credential).build();
            AndroidPublisher.Purchases purchases = publisher.purchases();
            final AndroidPublisher.Purchases.Get request = purchases.get(Config.GOOGLE_PACKAGE_NAME, Config.GOOGLE_PURCHASE_99, purchaseToken);
            System.out.println(request);
            System.out.println(request.getSubscriptionId());

        }catch(Exception e){
            e.printStackTrace();
        }
    }


}
