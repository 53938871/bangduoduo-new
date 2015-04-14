package com.bangduoduo.web.servlet;

import com.bangduoduo.utils.PropertiesUtil;
import com.bangduoduo.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by cpeng2 on 2/23/2015.
 */
@MultipartConfig(location = "C:\\develop\\tomcat-7\\webapps\\images",maxFileSize = 1024 * 2 * 1000)
@WebServlet(name="upload",urlPatterns = "/upload",initParams = {@WebInitParam(name="fileWebPath",value = "http://localhost:8090/images/")})
public class FileUploadServlet extends HttpServlet{

    private String fileWebPath;
    public void init(ServletConfig config) throws ServletException {
        fileWebPath = config.getInitParameter("fileWebPath");
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }

    public static final Map<String,String> extMap = new HashMap<String,String>();
    static {
        extMap.put("image", "gif,jpg,jpeg,png,bmp");
        extMap.put("flash", "swf,flv");
        extMap.put("media", "swf,flv,mp3,wav,wma,wmv,mid,avi,mpg,asf,rm,rmvb");
        extMap.put("file", "doc,docx,xls,xlsx,ppt,htm,html,txt,zip,rar,gz,bz2");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        final String type = req.getParameter("dir");
        MultipartConfig config = this.getClass().getAnnotation(MultipartConfig.class);
        String date = Utils.formatDate("yyyy-MM-dd");
        String extFolder = type + File.separator + date;
        String path = config.location() + File.separator + extFolder;
        File file = new File(path);
        PrintWriter writer = resp.getWriter();
        if(!file.exists()) {
            file.mkdirs();
        }
        try {
            final Part filePart = req.getPart("imgFile");
            String originalFileName = getFileName(filePart);
            String errorMsg = checkUpload(filePart,type,originalFileName);
            if(!StringUtils.isEmpty(errorMsg)){
                writer.write(getError("1",errorMsg));
                writer.flush();
                return;
            }
            String newName = getNewName(originalFileName);
            filePart.write(path + File.separator + newName);
            writer.write(successMessage(fileWebPath + type + "/" + date + "/" + newName));
            writer.flush();
        }catch(Exception e) {
            writer.write(getError("1","系统错误!"));
            writer.flush();
        } finally {
            writer.close();
        }
    }

    private String getFileName(final Part part) {
        final String partHeader = part.getHeader("content-disposition");
        for (String content : part.getHeader("content-disposition").split(";")) {
            if (content.trim().startsWith("filename")) {
                return content.substring(
                     content.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }

    private String getNewName(String name) {
        String ext = name.substring(name.indexOf("."));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmssSSS");
        return sdf.format(new java.util.Date()) + ext;
    }

    private String successMessage(String url) {
        JSONObject obj = new JSONObject();
        obj.put("error", 0);
        obj.put("url", url);
        return obj.toJSONString();
    }

    private String getError(String code,String message) {
        JSONObject obj = new JSONObject();
        obj.put("error",code);
        obj.put("message",message);
        return obj.toString();
    }

    private String checkUpload(Part filePart,String type, String originalFileName){
        if(filePart==null) {
            return "请选择文件!";
        }
        String fileExt = originalFileName.substring(originalFileName.lastIndexOf(".") + 1).toLowerCase();
        if(!Arrays.<String>asList(extMap.get(type).split(",")).contains(fileExt)){
            return "上传文件扩展名是不允许的扩展名.\\n只允许" + extMap.get(type) + "格式.";
        }
        return null;
    }
}
