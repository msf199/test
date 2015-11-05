package main.com.whitespell.peak.logic;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         10/17/15
 *         main.com.whitespell.peak.logic
 */
public class VideoProcessingManager {
    public static final int MAX_QUEUE_PER_NODE = 3; // 1 video takes 5 mins, max 15 mins processing time.

    public static final String secret = "-9kW9GP-if42nl13SOixq7or";
    public static final String id = "151139168536-8kgls858tsb61eligkkp33dtqde3u3nq.apps.googleusercontent.com";
    public static final String auth = "4/xmVz65rVVAu5v0vpe1f7Uyya1DwH6cz4OpI3DXzvY5o";
    public static final String command = "";
    class GoogleOauth {

    }
}


/**

 POST to https://www.googleapis.com/compute/v1/projects/whitespell-gc/zones/us-central1-a/instances

 Authorization: Bearer ya29.EAIh0nuTStMDTzvsUxByphXWl1GFId2hDp2l251IhBpqR1XMMCvuw0dGshPo48hOekU-

 {
 "name": "example-instance",
 "machineType": "zones/us-central1-a/machineTypes/f1-micro",
 "disks": [{
 "autoDelete": "true",
 "boot": "true",
 "type": "PERSISTENT",
 "initializeParams": {
 "sourceImage": "projects/whitespell-gc/global/images/test"

 }
 }],
 "networkInterfaces": [{
 "accessConfigs": [{
 "type": "ONE_TO_ONE_NAT",
 "name": "External NAT"
 }],
 "network": "global/networks/default"
 }]
 }
 */

