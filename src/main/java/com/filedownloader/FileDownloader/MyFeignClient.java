package com.filedownloader.FileDownloader;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(value = "download", url = "https://file-examples.com/storage/fe39d25af966e2a599a643c/2017/02/file-sample_100kB.doc", configuration = FeignConfig.class)
public interface MyFeignClient {
    @RequestMapping(method = RequestMethod.GET)
    byte[] getFile();
}
