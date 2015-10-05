package main.com.whitespell.peak.logic;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import main.com.whitespell.peak.logic.config.Config;
import main.com.whitespell.peak.logic.logging.Logging;

/**
 * @author Cory McAn(cmcan), Whitespell LLC
 *         10/5/15
 *         main.com.whitespell.peak.logic
 */
public class DeleteHelper {
    static BasicAWSCredentials aws;
    static AmazonS3Client s3;

    public DeleteHelper(){
        aws = new BasicAWSCredentials(Config.AWS_API_KEY_ID, Config.AWS_API_SECRET);
        s3 = new AmazonS3Client(aws);
    }

    public static void deleteVideo(String filename){

        try{
            s3.deleteObject(Config.AWS_API_VID_BUCKET, filename);
        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException.");
            System.out.println("Error Message: " + ace.getMessage());
        }

    }

    public static void deleteAWSThumbnail(String filename){
        try{
            s3.deleteObject(Config.AWS_API_IMG_BUCKET, filename);
        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException.");
            System.out.println("Error Message: " + ace.getMessage());
        }
    }

    public static void deleteCloudinaryThumbnail(String publicId){
        try{
            /**
             * Cloudinary REST delete, provide app key and secret in request
             */

            HttpResponse<String> stringResponse = Unirest.delete("https://" + Config.CL_API_KEY_ID + ":" + Config.CL_API_SECRET +
                    "@api.cloudinary.com/v1_1/" + Config.CL_API_FOLDER + "/resources/image/upload?public_ids=" + publicId)
                    .header("accept", "application/json")
                    .asString();

            System.out.println(stringResponse.getBody());

        }catch(Exception e){
            Logging.log("High", e); //don't throw an error on client side
        }
    }

    public static void main(String[] args){
        DeleteHelper d = new DeleteHelper();
        d.deleteVideo("134_3_1443100376064_MP4_20150924_091228_1366050621.mp4");
        d.deleteAWSThumbnail("134_3_1443100376064_MP4_20150924_091228_1366050621.jpg");
        d.deleteCloudinaryThumbnail("h");
    }
}
