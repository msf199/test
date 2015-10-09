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
    static BasicAWSCredentials aws = new BasicAWSCredentials(Config.AWS_API_KEY_ID, Config.AWS_API_SECRET);
    static AmazonS3Client s3 = new AmazonS3Client(aws);

    public DeleteHelper(){

    }

    /**
     * Delete content from AWS, either video or thumbnail
     * @param bucket
     * @param filename
     * @return true if delete succeeds, false otherwise
     */
    public static boolean deleteAWSContent(String bucket, String filename){

        /**
         * Attempt to delete the object in the bucket
         */
        try{

            s3.deleteObject(bucket, filename);
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

        /**
         * Get object metadata for deleted object, ensure it returns 404.
         */
        try{
            s3.getObjectMetadata(bucket, filename);
        }catch (AmazonServiceException ase) {
            /**
             * Catch the ASE which will return 404 if delete succeeded
             */
            System.out.println(ase.getErrorMessage());
            if(ase.getErrorCode().contains("404")){
                return true;
            }
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException.");
            System.out.println("Error Message: " + ace.getMessage());
        }
        return false;
    }

    /**
     * Delete image from cloudinary
     * @param publicId image name in cloudinary
     * @return true if delete succeeds, false otherwise.
     */
    public static boolean deleteCloudinaryImage(String publicId){
        try{
            /**
             * Cloudinary REST delete, provide app key and secret in request
             */

            HttpResponse<String> stringResponse;

            /**
             * Attempt to delete the image by its publicId
             */
            Unirest.delete("https://" + Config.CL_API_KEY_ID + ":" + Config.CL_API_SECRET +
                    "@api.cloudinary.com/v1_1/" + Config.CL_API_FOLDER + "/resources/image/upload?public_ids=" + publicId)
                    .header("accept", "application/json")
                    .asString();

            /**
             * Get the image information, if delete succeeded will return "Resource not found"
             */
            stringResponse = Unirest.get("https://" + Config.CL_API_KEY_ID + ":" + Config.CL_API_SECRET +
                    "@api.cloudinary.com/v1_1/" + Config.CL_API_FOLDER + "/resources/image/upload/" + publicId)
                    .header("accept", "application/json")
                    .asString();

            if(stringResponse.getBody().contains("Resource not found")){
                return true;
            }

        }catch(Exception e){
            Logging.log("High", e); //don't throw an error on client side
        }
        return false;
    }

    public static void main(String[] args){
        String cloudImg = "dto7xnjps2nxlbixyah0";
        String awsImg = "134_1_1442781285085_20150918_155716.jpg";
        String awsVid = "134_1_1442947038421_MP4_20150922_143704_-1753158175.mp4";


        if(deleteCloudinaryImage(cloudImg)){
            System.out.println("delete " + cloudImg + " success");
        }

        if(deleteAWSContent(Config.AWS_API_IMG_BUCKET,awsImg)){
            System.out.println("delete " + awsImg + " success");
        }

        if(deleteAWSContent(Config.AWS_API_VID_BUCKET,awsVid)){
            System.out.println("delete " + awsVid + " success");
        }
    }
}
