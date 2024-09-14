package com.filedownloader.FileDownloader;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Component
public class FileModel {
    String id;
    String name;
    String size;
    String contentType;
    String filePath;
    String lookupUrl;
}
