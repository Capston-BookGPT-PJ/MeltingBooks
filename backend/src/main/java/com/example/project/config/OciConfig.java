package com.example.project.config;

import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.ConfigFileReader.ConfigFile;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class OciConfig {

    @Bean
public ObjectStorageClient objectStorageClient() {
    try {
        System.out.println("=== OCI Config Loading Start ===");

        ConfigFile config = ConfigFileReader.parseDefault();
        System.out.println("Config file loaded successfully: " + config);

        AuthenticationDetailsProvider provider =
                new ConfigFileAuthenticationDetailsProvider(config);
        System.out.println("Authentication provider created successfully.");

        ObjectStorageClient client = new ObjectStorageClient(provider);
        System.out.println("ObjectStorageClient created successfully.");

        return client;
    } catch (IOException e) {
        e.printStackTrace();
        throw new RuntimeException("Failed to create OCI ObjectStorageClient: check config file", e);
    } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("Unexpected error while creating ObjectStorageClient", e);
    }
}


}
