package com.filedownloader.FileDownloader;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/")
public class Controller {
    @Autowired
    MyFeignClient myFeignClient;
    @Autowired
    FilesDBMock filesDBMock;
    @Autowired
    FileUploaderService fileUploaderService;


    @GetMapping("download")
    public ResponseEntity<String> download(){
        try{
            byte[] arrayOfBytes=myFeignClient.getFile();
            log.debug("Writing in file start");
            FileUtils.writeByteArrayToFile(new File(".\\target\\file-sample_100kB.doc"), arrayOfBytes);
            System.out.println("Writing in file completed");
            String tempDir = System.getProperty("java.io.tmpdir");
            System.out.println("Temp directory::"+tempDir);
            return new ResponseEntity<>("Download successfull", HttpStatusCode.valueOf(200));
        }catch (Exception e){
            System.out.println(e.getMessage());
            return new ResponseEntity<>("error", HttpStatusCode.valueOf(500));
        }

    }
    @GetMapping("availablefiles")
    public ResponseEntity<List<FileModel>> availablefiles(){
        return ResponseEntity.ok().body(filesDBMock.availableFiles);

    }

    @Operation(
            summary = "Uploads the file",
            description = "uploads a single file",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "File upload done"

                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Server error"
                    )
            }
    )
    @PostMapping("/single-file-upload")
    public ResponseEntity<Map<String, String>> handleFileUploadUsingCurl(
            @RequestParam("file") MultipartFile file) throws IOException {

        Map<String, String> map = new HashMap<>();

        // Populate the map with file details
        map.put("fileName", file.getOriginalFilename());
        map.put("fileSize", String.valueOf(file.getSize()));
        map.put("fileContentType", file.getContentType());

        // File upload is successful
        map.put("message", "File upload done");
        return ResponseEntity.ok(map);
    }

    @Operation(
            summary = "Uploads multiple files",
            description = "Uploads multiple files edited by dpk",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful retrieval",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = FileModel.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Server error"
                    )
            }
    )
    @PostMapping("/uploadMultiple")
    public ResponseEntity<List<FileModel>> handleMultipleFilesUploadUsingCurl(
            @RequestParam("files") MultipartFile[] files) throws IOException {

        log.info("Number of files received"+files.length);
        List<FileModel> fileModels=new ArrayList<>();
        log.info("Files available before upload::"+filesDBMock.availableFiles.size());
        for(MultipartFile file: files){
            log.debug("Writing in file start");
            FileUtils.writeByteArrayToFile(new File(".\\target\\"+file.getOriginalFilename()), file.getBytes());
            log.debug("Writing in file completed");
            FileModel fileModel1=new FileModel();
            fileModel1.setId(UUID.randomUUID().toString());
            fileModel1.setName(file.getOriginalFilename());
            fileModel1.setSize(String.valueOf(file.getSize()));
            fileModel1.setContentType(file.getContentType());
            fileModel1.setFilePath(".\\target\\"+file.getOriginalFilename());
            fileModel1.setLookupUrl("http://localhost:8080/downloadToLocal/"+fileModel1.getId());
            fileModels.add(fileModel1);
            fileUploaderService.save(file);
            filesDBMock.addFileDtls(fileModel1);
        }
        log.info("Files available after upload::"+filesDBMock.availableFiles.size());

        return ResponseEntity.ok(fileModels);
    }

    @GetMapping("downloadToLocal/{id}")
    public ResponseEntity<InputStreamResource> downloadToLocal(@PathVariable("id") String fid) throws IOException {
        List<FileModel> foundFileModels=filesDBMock.getFiles(fid);
        if(foundFileModels.size()!=0){
            FileModel foundFile=foundFileModels.get(0);
            foundFile.getFilePath();
//            FileUtils.readFileToByteArray(new File(foundFile.getFilePath()));
            File file= new File(foundFile.getFilePath());
//            MimetypesFileTypeMap mimetypesFileTypeMap= new MimetypesFileTypeMap();
//            String mime=mimetypesFileTypeMap.getContentType(file);
            Tika tika = new Tika();
            String mimeType;
            byte[] byteArray=FileUtils.readFileToByteArray(file);
            try {
                mimeType = tika.detect(byteArray);
                log.info("mimteype::"+mimeType);
            } catch (Exception e) {
                // Handle the error and possibly set a default MIME type
                mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE; // Default binary stream type
            }
            // Create an InputStream from the byte array
            InputStream imageStream = new ByteArrayInputStream(byteArray);

            // Wrap the InputStream with InputStreamResource
            InputStreamResource resource = new InputStreamResource(imageStream);
            //Returning the byte array directly without wrapping it in an InputStream or without setting the content type may lead to issues.
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(mimeType))
                    .body(resource);
            //return ResponseEntity.ok(FileUtils.readFileToByteArray(file));
        }else {
            return ResponseEntity.notFound().build();
        }
    }

    @Async
    @PostMapping("/uploadAsync")
    public CompletableFuture<ResponseEntity<String>> handleConcurrentFilesUpload(
            @RequestParam("files") MultipartFile[] files) throws IOException, InterruptedException {

        // Handle empty file error
        if (files.length == 0) {
            return CompletableFuture
                    .completedFuture(ResponseEntity.badRequest().build());
        }
        // File upload process is submitted
        else {
            List<FileModel> fileModels=new ArrayList<>();
            for(MultipartFile file: files){
                fileUploaderService.save(file);
                //FileUtils.writeByteArrayToFile(new File(".\\target\\"+file.getOriginalFilename()), file.getBytes());
                FileModel fileModel1=new FileModel();
                fileModel1.setId(UUID.randomUUID().toString());
                fileModel1.setName(file.getOriginalFilename());
                fileModel1.setSize(String.valueOf(file.getSize()));
                fileModel1.setContentType(file.getContentType());
                fileModel1.setFilePath(".\\target\\"+file.getOriginalFilename());
                fileModel1.setLookupUrl("http://localhost:8080/downloadToLocal/"+fileModel1.getId());
                fileModels.add(fileModel1);
                System.out.println(file.getOriginalFilename() + " is uploaded at " + LocalDateTime.now());
                filesDBMock.addFileDtls(fileModel1);
            }
            return CompletableFuture.completedFuture(
                    ResponseEntity.ok().body("Queued for Uploading"));
        }
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<List<FileModel>> delete(@PathVariable("id") String fid) throws IOException {
        List<FileModel> foundFileModels=filesDBMock.getFiles(fid);
        String s=foundFileModels.size()>1 ? "great" : "Not great";
        if(foundFileModels.size()!=0){
            FileModel foundFile=foundFileModels.get(0);
            foundFile.getFilePath();
            File file= new File(foundFile.getFilePath());
            FileUtils.delete(file);
            ResponseEntity<List<FileModel>> re= filesDBMock.deleteFile(fid) ? ResponseEntity.ok().body(foundFileModels)
                : ResponseEntity.internalServerError().body(foundFileModels);
            return re;
        }else {
            return ResponseEntity.notFound().build();
        }
    }

}

