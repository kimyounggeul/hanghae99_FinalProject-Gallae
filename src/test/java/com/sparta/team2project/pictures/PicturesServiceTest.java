package com.sparta.team2project.pictures;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@Testcontainers
public class PicturesServiceTest {
    private static final DockerImageName LOCALSTACK_IMAGE = DockerImageName.parse("localstack/localstack:latest");

    @Container
    LocalStackContainer localStackContainer = new LocalStackContainer(LOCALSTACK_IMAGE)
            .withServices(S3);

    public AmazonS3 setUp() {
        AmazonS3 amazonS3 = AmazonS3ClientBuilder
                .standard()
                .withEndpointConfiguration(localStackContainer.getEndpointConfiguration(S3))
                .withCredentials(localStackContainer.getDefaultCredentialsProvider())
                .build();
        return amazonS3;
    }

    @Test
    void test(){
        AmazonS3 amazonS3 = setUp();
        String bucketName = "foo";
        amazonS3.createBucket(bucketName);
        System.out.println(bucketName +" 버킷 생성");

        String key = "foo-key";
        String content = "foo-content";
        amazonS3.putObject(bucketName, key, content);
        System.out.println("파일을 업로드하였습니다. key=" + key +", content=" + content);

        S3Object object = amazonS3.getObject(bucketName, key);
        System.out.println("파일을 가져왔습니다. = " + object.getKey());
        assertEquals(key, object.getKey());

    }
}