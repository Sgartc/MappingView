package com.mappingupdate.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Service
public class FileUploadService {

    // 从配置文件中读取上传路径（替代硬编码）
    @Value("${upload.path}")  // 这里注入application.properties中的upload.path配置
    private String uploadPath;

    @Value("${pyPath.path}")
    private String PYTHON_SCRIPT_PATH;

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
    public void previewFile(Map<String, Object> result, String fileName,Map<String, Object> data){
        // 创建保存目录（使用注入的路径）
        String filePath = uploadPath+fileName;
        Path path = Paths.get(filePath);
        if (!ifExists(path)) {
            result.put("code", 1);
            result.put("msg", "文件不存在");
        }
        try {
        // 读取文件内容（对于大文件建议使用流式读取）
        byte[] fileContent = Files.readAllBytes(path);
        String content = new String(fileContent, StandardCharsets.UTF_8);
        // 封装返回数据
        data.put("content", content);
        data.put("fileName", fileName);
        data.put("fileSize", Files.size(path));
        result.put("code", 0);
        result.put("msg", "文件预览成功");
        result.put("data", data);
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", "文件读取失败: " + e.getMessage());
        }
    }

    public Map<String, Object> parse(Map<String, Object> result, String fileName,Map<String, Object> data){
            //调用Python处理
            String filePath = uploadPath+fileName;
            Path path = Paths.get(filePath);
            if (!ifExists(path)) {
                result.put("code", 1);
                result.put("msg", "解析文件不存在");
                return result;
            }
            if (!ifExists(Paths.get(PYTHON_SCRIPT_PATH))) {
                result.put("code", 1);
                result.put("msg", "python文件不存在");
                return result;
            }
            // 调用Python脚本
            String[] cmd = {
                    "python",  // 或者 "python3"，根据系统配置
                    PYTHON_SCRIPT_PATH,
                    filePath  // 传递文件路径参数
            };
        try {
            ProcessBuilder builder = new ProcessBuilder(cmd);
            Process process = builder.start();
            // 读取Python脚本输出
            StringBuilder output = new StringBuilder();
            StringBuilder error = new StringBuilder();
            // 读取正常输出
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
            // 读取错误输出
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        error.append(line).append("\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
            // 等待进程执行完成
            int exitCode = process.waitFor();
            // 检查执行结果
            if (exitCode != 0) {
                // 执行失败
                result.put("code", 1);
                result.put("msg", "解析失败，Python脚本执行错误");
                data.put("errorDetails", error.toString());
                result.put("data", data);
                return result;
            }
            // 检查是否有错误输出
            if (error.length() > 0) {
                result.put("code", 1);
                result.put("msg", "解析过程中出现警告");
                data.put("errorDetails", error.toString());
                result.put("data", data);
                return result;
            }
            // 解析成功，获取Excel文件路径
            String excelPath = output.toString().trim();
            if (excelPath.isEmpty()) {
                result.put("code", 1);
                result.put("msg", "解析成功，但未获取到Excel文件路径");
                return result;
            }
            // 返回成功结果
            result.put("code", 0);
            result.put("msg", "文件解析成功");
            data.put("excelPath", excelPath);
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", "解析失败: " + e.getMessage());
            data.put("errorDetails", e.getMessage());
            result.put("data", data);
            e.printStackTrace();
        }
        return result;
    }


    //判断文件是否存在公共方法
    public boolean ifExists(Path path){
        return Files.exists(path);
    }
}
