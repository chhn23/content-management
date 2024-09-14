package com.filedownloader.FileDownloader;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
@Slf4j
@Service
@EnableAsync
public class FileUploaderService {
    @SneakyThrows
    @Async
    public void save(MultipartFile file) {
        List<FileModel> fileModels=new ArrayList<>();
        Thread.sleep(new Random().nextLong(4000, 8000));
        //FileUtils.writeByteArrayToFile(new File(".\\target\\"+file.getOriginalFilename()), file.getBytes());
        log.info(file.getOriginalFilename() + " is uploaded at " + LocalDateTime.now());
    }
}
