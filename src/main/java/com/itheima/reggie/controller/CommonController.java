package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.Servlet;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {
    @Value("${reggie.path}")
    private String basePath;
    @PostMapping("/upload")
    public R<String> stringR    (MultipartFile file){
         log.info("file = {}",file.toString());
//        创建目录对象
         File dir = new File(basePath);
         if(!dir.exists()){
             dir.mkdirs();
         }
//            使用UUID重新生成文件名，防止文件名称重复
        String originalFilename = file.getOriginalFilename();
//            获取文件后缀
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));//
        String fileName = UUID.randomUUID().toString() + suffix;
        //将文件转存到指定位置
        try {

            file.transferTo(new File(basePath + fileName));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return R.success(fileName);

    }
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){
        log.info("download");
        try{
//          输入流读取文件内容
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));
//          输出流将文件写回浏览器，在浏览器展示图片
            ServletOutputStream outputStream = response.getOutputStream();

//          设置响应的文件类型
            response.setContentType("image/jpeg");
            int len = 0 ;
            byte[] bytes = new byte[1024];
            while((len = fileInputStream.read(bytes)) != -1){
                outputStream.write(bytes,0,len);
                outputStream.flush();

            }
            outputStream.close();
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
