package com.schoolplant.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class DifyService {

    @Value("${dify.api.key}")
    private String apiKey;

    @Value("${dify.api.url}")
    private String apiUrl;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    /**
     * Analyze plant abnormality image
     * @param imageUrl Publicly accessible URL (if available)
     * @param imageFile Local file (if no public URL)
     * @return Analysis result
     */
    public String analyzeImage(String imageUrl, File imageFile) {
        try {
            String fileId = null;
            if (imageFile != null) {
                fileId = uploadFile(imageFile);
            }

            // Create Chat Message
            JSONObject requestBody = new JSONObject();
            requestBody.put("inputs", new JSONObject());
            requestBody.put("query", "请分析图片中的植物异常，识别异常类型（如病虫害、缺水等），并给出3-5个解决方案。");
            requestBody.put("response_mode", "blocking");
            requestBody.put("user", "sys-user");

            if (fileId != null) {
                JSONObject fileObj = new JSONObject();
                fileObj.put("type", "image");
                fileObj.put("transfer_method", "local_file");
                fileObj.put("upload_file_id", fileId);
                requestBody.put("files", Collections.singletonList(fileObj));
            } else if (imageUrl != null) {
                JSONObject fileObj = new JSONObject();
                fileObj.put("type", "image");
                fileObj.put("transfer_method", "remote_url");
                fileObj.put("url", imageUrl);
                requestBody.put("files", Collections.singletonList(fileObj));
            }

            RequestBody body = RequestBody.create(
                    requestBody.toJSONString(), MediaType.parse("application/json"));

            Request request = new Request.Builder()
                    .url(apiUrl + "/chat-messages")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("Dify API Error: {}", response.body().string());
                    return "AI分析服务暂时不可用";
                }
                String respStr = response.body().string();
                JSONObject respJson = JSON.parseObject(respStr);
                return respJson.getString("answer");
            }

        } catch (Exception e) {
            log.error("Dify Analysis Failed", e);
            return "AI分析失败: " + e.getMessage();
        }
    }

    private String uploadFile(File file) throws IOException {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(file, MediaType.parse("image/jpeg")))
                .addFormDataPart("user", "sys-user")
                .build();

        Request request = new Request.Builder()
                .url(apiUrl + "/files/upload")
                .header("Authorization", "Bearer " + apiKey)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Upload failed: " + response.body().string());
            }
            JSONObject json = JSON.parseObject(response.body().string());
            return json.getString("id");
        }
    }
}
