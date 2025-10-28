package com.example.filemonitoring.Controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.io.IOException;
import java.net.URI;

@RestController
@RequestMapping("/files")
public class HomeController {

    private final S3Client s3Client;

    @Value("${app.s3.bucket}")
    private String bucket;

    public HomeController(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<byte[]> download(@PathVariable String filename) {

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(filename)
                .build();

        try (ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(request)) {

            byte[] data = s3Object.readAllBytes();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentLength(data.length);
            headers.set(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + filename + "\"");

            return new ResponseEntity<>(data, headers, HttpStatus.OK);

        } catch (NoSuchKeyException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}