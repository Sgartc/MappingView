package com.mappingupdate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import com.mappingupdate.service.FileUploadService;

@RestController
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    @PostMapping("/upload")
    public Map<String, Object> uploadFile(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();

        // 在 FileUploadController.uploadFile 方法开头添加
        System.out.println("文件是否为空：" + file.isEmpty());
        System.out.println("原始文件名：" + file.getOriginalFilename());
        System.out.println("文件大小：" + file.getSize() + " bytes");

        // 1. 验证文件是否为空
        if (file.isEmpty()) {
            result.put("code", 1);
            result.put("msg", "文件为空，请重新选择文件");
            return result;
        }

        // 2. 验证文件后缀（关键：后端二次校验）
        // 修改 FileUploadController.java 中的文件名处理部分
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isEmpty()) {
            result.put("code", 1);
            result.put("msg", "文件名异常");
            return result;
        }
        // 提取后缀（忽略路径，直接找最后一个"."）
        int dotIndex = originalFileName.lastIndexOf(".");
        if (dotIndex == -1) { // 无后缀文件
            result.put("code", 1);
            result.put("msg", "文件无后缀，仅支持.sql和.prc文件");
            return result;
        }
        String suffix = originalFileName.substring(dotIndex).toLowerCase();
        if (!suffix.equals(".sql") && !suffix.equals(".prc")) {
            result.put("code", 1);
            result.put("msg", "仅支持.sql和.prc文件，当前文件后缀：" + suffix);
            return result;
        }

        // 3. 调用服务层保存文件
        try {
            String saveResult = fileUploadService.saveFile(file);
            result.put("code", 0);
            result.put("msg", saveResult);
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", "上传失败：" + e.getMessage());
        }

        return result;
    }

    @RequestMapping("/previewFile")
    public Map<String, Object> previewFile(@RequestParam("fileName") String fileName){
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        fileUploadService.previewFile(result,fileName,data);
        return result;
    }

    @RequestMapping("/parse")
    public Map<String, Object> parse(@RequestParam("fileName") String fileName){
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> pythonResult=fileUploadService.parse(result,fileName,data);
        return pythonResult;
    }
}