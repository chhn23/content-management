package com.filedownloader.FileDownloader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class FileDownloaderApplication {

	public static void main(String[] args) {
		SpringApplication.run(FileDownloaderApplication.class, args);
	}

}
