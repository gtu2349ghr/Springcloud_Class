package com.xuecheng.content.api;

import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.serviceClient.MediaServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController("/test99")
@CrossOrigin
public class testController {
//    @Autowired
//    MediaServiceClient mediaServiceClient;
    @RequestMapping("Cont")
    public String mioewhdu(){
//        File file = new File("D:\\BBB\\111.html");
//        String upload="lll";
//        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
//        try {
//            upload = mediaServiceClient.upload(multipartFile, "course/111.html");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//            return  upload;
        return "iojoih";
    }

}
