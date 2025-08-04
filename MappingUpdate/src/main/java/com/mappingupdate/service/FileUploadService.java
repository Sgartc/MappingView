package com.mappingupdate.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;

@Service
public class FileUploadService {

    // 从配置文件中读取上传路径（替代硬编码）
    @Value("${upload.path}")  // 这里注入application.properties中的upload.path配置
    private String uploadPath;

    public String saveFile(MultipartFile file) throws IOException {
        // 创建保存目录（使用注入的路径）
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs(); // 目录不存在则创建
        }

        // 保存文件
        String fileName = file.getOriginalFilename();
        File dest = new File(uploadPath + File.separator + fileName);
        try {
            file.transferTo(dest);
            return "文件上传成功：" + fileName;
        } catch (IOException e) {
            e.printStackTrace();
            return "文件上传失败";
        }
    }
}
