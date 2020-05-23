package synchronizer;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.*;


public class S3Helper {
	private static AmazonS3 s3 = null;
	private final static String bucketName = "wgk01";
	private final static String accessKey = "9DB1E7FFEE34D0D8706E";
	private final static String secretKey = "WzhBQUY5NTNENEJGMEZDRkM4RkRFNzc0Mzc4RUZFRDVCRjBBNDgzQzVd";
	private final static String serviceEndpoint = 
	"http://scuts3.depts.bingosoft.net:29999";
	private final static String signingRegion = "";
	
	public static void init() {
		final BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        final ClientConfiguration ccfg = new ClientConfiguration().
                withUseExpectContinue(false);

        final EndpointConfiguration endpoint = new EndpointConfiguration(serviceEndpoint, signingRegion);

        s3 = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withClientConfiguration(ccfg)
                .withEndpointConfiguration(endpoint)
                .withPathStyleAccessEnabled(true)
                .build();

	}
	
	/**
	 * 
	 * @param file_path: 本地上传文件的完整路径
	 * @param target_path: S3 中完整的路径，传入的如果是目录，末尾不需要带 '/'
	 */
	public static void upload(Path file_path, String target_path) {
		 
		 final File file = file_path.toFile();
		 if(file == null) return;

	     for (int i = 0; i < 2; i++) {
	         try {
	            if(file.isDirectory()) {
	            	target_path += "/";
	            	System.out.format("Uploading %s to S3 bucket %s...\n", file_path,target_path);
	            	s3.putObject(bucketName, target_path, "");
	            }
	            else {
	            	System.out.format("Uploading %s to S3 bucket %s...\n", file_path,target_path);
	            	s3.putObject(bucketName,target_path,file);
	            }
	               	break;
	            } catch (AmazonServiceException e) {
	                if (e.getErrorCode().equalsIgnoreCase("NoSuchBucket")) {
	                    s3.createBucket(bucketName);
	                    continue;
	                }

	                System.err.println(e.toString());
	                System.exit(1);
	            } catch (AmazonClientException e) {
	                try {
	                    // detect bucket whether exists
	                    s3.getBucketAcl(bucketName);
	                } catch (AmazonServiceException ase) {
	                    if (ase.getErrorCode().equalsIgnoreCase("NoSuchBucket")) {
	                        s3.createBucket(bucketName);
	                        continue;
	                    }
	                } catch (Exception ignore) {
	                }

	                System.err.println(e.toString());
	                System.exit(1);
	            }
	        }

	        System.out.println("Done!");
	    }
	
	
	public static void uploadDir(Path src_prefix, String dest_prefix, String file_name) {
		System.out.println("targetpath:"+dest_prefix+file_name);
		
		Path src_path = src_prefix.resolve(file_name);
		File file = src_path.toFile();
		if(file == null) return;

		if(!file.isDirectory()) {
			upload(src_path, dest_prefix+file_name);
		}
		else {
			upload(src_path, dest_prefix+file_name);
			for(File sub_file : file.listFiles()) {
				uploadDir(src_path, dest_prefix+file_name+"/",sub_file.getName());
			}
		}
	}
	
	
	public static void deleteFile(String target_path) {
		  try {
	            s3.deleteObject(new DeleteObjectRequest(bucketName, target_path));
	            System.out.format("delete %s\n",target_path);
	        } catch (AmazonServiceException e) {
	            // The call was transmitted successfully, but Amazon S3 couldn't process 
	            // it, so it returned an error response.
	            e.printStackTrace();
	        } catch (SdkClientException e) {
	            // Amazon S3 couldn't be contacted for a response, or the client
	            // couldn't parse the response from Amazon S3.
	            e.printStackTrace();
	        }
	}
	
	/**
	 * 
	 * @param dest_prefix 删除该文件及以该文件为目录的所有文件
	 */
	public static void deleteDir(String dest_prefix) {
//		if(!dest_prefix.endsWith("/") && dest_prefix.length() != 0)
//			throw new IllegalArgumentException("dest_prefix must end with '/'");
		
		System.out.format("delete dir: %s\n",dest_prefix);
		ObjectListing ol = s3.listObjects(bucketName);
		List<S3ObjectSummary> objects = ol.getObjectSummaries();
		for(S3ObjectSummary o : objects) {
			System.out.format("iter: %s\n",o.getKey());
			if(o.getKey().startsWith(dest_prefix+"/") || o.getKey().equals(dest_prefix))
				deleteFile(o.getKey());
		}
	}
	
	
}
