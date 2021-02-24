package com.share.GroupPurchasing.utils;

import com.share.GroupPurchasing.model.ResEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class UploadPicturesUtil {

    public static ResEntity upload(HttpServletRequest request , String uploadFilePath, String photoPath,String finalPath) throws IOException {


        ResEntity resEntity = new ResEntity();
//        String picPath = "";

        List<MultipartFile> files = ((MultipartHttpServletRequest) request).getFiles("files");

        if(files.size() == 0 || files == null) {

            resEntity.setReturnCode(10022);
            resEntity.setErrMsg("上传图片为空");
            log.info("{上传图片为空}");
            return resEntity;
        }


        for (int i = 0; i < files.size(); i++) {

            MultipartFile file = files.get(i);
            if (file.isEmpty()) {
                resEntity.setReturnCode(10022);
                resEntity.setErrMsg("上传的第"+i+"个图片为空 没有找到相对应的文件");
                log.error("上传的第"+i+"个图片为空 没有找到相对应的文件");
                return resEntity;
            }

            log.info("成功获取图片");
            String fileName = file.getOriginalFilename();
            String type = null;
            String destPath = null;
            type = fileName.indexOf(".") != -1 ? fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()) : null;
            log.info("{图片初始名称为：" + fileName + " 类型为：" + type+"}");

            if (type != null) {
                if ("GIF".equals(type.toUpperCase())||"PNG".equals(type.toUpperCase())||"JPG".equals(type.toUpperCase()) || "JPEG".equals(type.toUpperCase())) {


                    Long timeMillis = System.currentTimeMillis();

                    LocalDateTime dateTime = LocalDateTime.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                    destPath = uploadFilePath + "/" + photoPath + "/" + dateTime.format(formatter) + "_" +timeMillis + file.getOriginalFilename();

                    File dir = new File(uploadFilePath+ "/" + photoPath);
                    if (!dir.isDirectory()) {
                        dir.mkdirs();
                    }

                    String reqPath = request.getScheme() + "s://" +
                            request.getServerName() +
                            "/backend"
                            + "/" + photoPath +"/" + dateTime.format(formatter) + "_" +timeMillis + file.getOriginalFilename();

                    log.info("{reqPath：" + reqPath+"}");
                    String tmpDestPath = destPath+"_temp";
                    file.transferTo(new File(tmpDestPath));
                    FileUtil.commpressPicForScale(tmpDestPath,destPath,500,0.8);
                    FileUtil.delteTempFile(new File(tmpDestPath));
//                    Thumbnails.of(FileUtil.multipartFileToFile(file)).scale(0.65f).toFile(destPath);
                    log.info("{图片成功上传到指定目录下 "+destPath+"}");

                    if(i == files.size()-1){
                        finalPath = finalPath + reqPath;
                    }else {
                        finalPath = finalPath + reqPath + ",";
                    }
                    resEntity.setReturnCode(200);
                    log.info("{上传图片最终请求链接为 "+finalPath+"}");
                }else {
                    resEntity.setReturnCode(10014);
                    resEntity.setErrMsg("图片格式错误，限定为jpg png gif jpeg");
                    log.error("{图片格式错误，限定为jpg png gif jpeg}");
                    return resEntity;
                }
            }else {
                resEntity.setReturnCode(10022);
                resEntity.setErrMsg("上传files图片为空");
                log.error("{上传files图片为空}");
                return resEntity;
            }
        }


        if("".equals(finalPath)) {
            resEntity.setReturnCode(10022);
            resEntity.setErrMsg("上传files图片失败");
            log.error("{上传files图片失败,访问地址为空}");
            return resEntity;
        }

        return resEntity;

    }
}
