package org.dromara.photo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * 照片静态资源映射
 * 将 /photo/file/** 映射到本地磁盘上传目录
 *
 * @author photo
 */
@Configuration
public class PhotoResourceConfig implements WebMvcConfigurer {

    @Value("${photo.upload.path:E:/uploads/face_photos}")
    private String uploadPath;

    @Value("${photo.upload.url-prefix:/photo/file}")
    private String urlPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 确保目录存在
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // 映射 /photo/file/** -> file:E:/uploads/face_photos/
        String location = uploadPath.endsWith("/") ? "file:" + uploadPath : "file:" + uploadPath + "/";
        registry.addResourceHandler(urlPrefix + "/**")
            .addResourceLocations(location);
    }
}
