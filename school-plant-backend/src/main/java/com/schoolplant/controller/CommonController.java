package com.schoolplant.controller;

import com.schoolplant.common.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Common", description = "通用接口")
@RestController
@RequestMapping("/common")
public class CommonController {

    @Value("${school-plant.profile:./uploads/}")
    private String uploadPath;

    @Value("${server.port:8080}")
    private String port;
    
    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    @Operation(summary = "文件上传")
    @PostMapping("/upload")
    public R<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return R.fail("上传文件不能为空");
        }

        try {
            // 1. 创建上传目录
            // 使用绝对路径，或者基于项目根目录的路径
            // 如果 uploadPath 以 ./ 开头，将其转换为绝对路径
            File dir;
            if (uploadPath.startsWith("./") || uploadPath.startsWith("../")) {
                dir = new File(System.getProperty("user.dir"), uploadPath);
            } else {
                dir = new File(uploadPath);
            }
            
            // 打印实际路径，方便调试
            System.out.println("Upload Path: " + dir.getAbsolutePath());
            
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                     System.out.println("Failed to create directory: " + dir.getAbsolutePath());
                     // Try fallback to temp dir if permissions fail
                     // But for now let's just log it
                }
            }

            // 2. 生成文件名
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String fileName = UUID.randomUUID().toString() + suffix;

            // 3. 保存文件
            // 使用 getAbsolutePath 确保 transferTo 写入正确位置
            File dest = new File(dir.getAbsolutePath(), fileName);
            file.transferTo(dest);

            // 4. 返回访问URL
            String path = contextPath;
            if (path == null || "/".equals(path)) {
                path = "";
            }
            String url = path + "/profile/" + fileName;
            
            Map<String, String> data = new HashMap<>();
            data.put("url", url);
            data.put("fileName", fileName);
            data.put("newFileName", fileName);
            data.put("originalFilename", originalFilename);

            return R.ok(data);

        } catch (IOException e) {
            e.printStackTrace();
            return R.fail("文件上传失败: " + e.getMessage());
        }
    }
}
