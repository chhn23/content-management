package com.filedownloader.FileDownloader;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FilesDBMock {
    public List<FileModel> availableFiles=new ArrayList<>();
    public List<FileModel> addFileDtls(FileModel fm){
        availableFiles.add(fm);
        return availableFiles;
    }

    public List<FileModel> getFiles(String fid){
        return availableFiles.stream().filter(f-> f.getId().trim().equals(fid.trim())).collect(Collectors.toList());
    }
}
