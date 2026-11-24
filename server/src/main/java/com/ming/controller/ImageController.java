package com.ming.controller;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.image.ImageGeneration;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImageOptions;
import org.springframework.ai.image.ImageOptionsBuilder;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.util.List;

/**
 * 文生图：通义万相 wanx-v1
 *
 * @author Ming
 */
@RequestMapping("/api/v1/image")
@RestController
@CrossOrigin
public class ImageController {

    private final ImageModel imageModel;

    public ImageController(ImageModel imageModel) {
        this.imageModel = imageModel;
    }

    @GetMapping({"base", "stream"})
    public void base(@RequestParam("msg") String msg, HttpServletResponse resp) throws IOException {
        // 创建一个 ImageOptions 对象，使用 ImageOptionsBuilder 构建器模式进行配置
        ImageOptions options = ImageOptionsBuilder.builder()
                // 设置生成图片的宽度为 1024 像素
                .width(1024)
                // 设置生成图片的高度为 1024 像素
                .height(1024)
                // 指定使用的图像生成模型为 "wanx-v1"，是阿里云通义万相模型中的基础文生图模型
                .model("wanx-v1")
                // 设置生成图片的数量为 1 张
                .N(1)
                // 构建 ImageOptions 对象
                .build();

        // msg 是用户传入的生成图片的提示文本，options 是前面配置好的图片生成选项
        ImagePrompt imagePrompt = new ImagePrompt(msg, options);
        // 调用 imageModel 发起图片生成请求，结果封装在 ImageResponse 中
        ImageResponse imageResponse = imageModel.call(imagePrompt);

        // 从 ImageResponse 对象中获取生成的图片结果列表
        List<ImageGeneration> results = imageResponse.getResults();
        // 检查生成结果列表是否为空
        if (results.isEmpty()) {
            throw new RuntimeException("No results found");
        }

        // 从结果列表中获取第一个生成结果，并取出图片的 URL
        String imageUrl = results.get(0).getOutput().getUrl();
        // 读取图片到内存
        BufferedImage bufferedImage = ImageIO.read(URI.create(imageUrl).toURL());

        // try-with-resources 确保输出流自动关闭
        try (ServletOutputStream outputStream = resp.getOutputStream()) {
            // 以 JPEG 格式写回客户端
            ImageIO.write(bufferedImage, "jpg", outputStream);
            outputStream.flush();
        }
    }
}
