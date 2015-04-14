package com.bangduoduo.web.servlet;

import com.bangduoduo.utils.PropertiesUtil;
import com.sun.org.apache.xalan.internal.xsltc.runtime.*;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Hashtable;

/**
 * Created by cpeng2 on 3/3/2015.
 */
@WebServlet(name="fileManager",urlPatterns = "/fileManager",
        initParams = {@WebInitParam(name="location",value ="C:\\develop\\tomcat-7\\webapps\\images"),
                      @WebInitParam(name="fileWebPath",value = "http://localhost:8090/images/")
        })
public class FileManagerServlet extends HttpServlet {
    private String location;
    private String fileWebPath;
    private String[] fileTypes = new String[]{"gif", "jpg", "jpeg", "png", "bmp"};
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter writer = resp.getWriter();
        String type = req.getParameter("dir");
        String rootPath = "";
        String rootUrl = "";
        if(!StringUtils.isEmpty(type)) {
            if(!Arrays.<String>asList(new String[]{"image","flash","media","file"}).contains(type)) {
                writer.write("无效的目录名称!");
                writer.flush();
                return;
            }
            rootPath = location + (File.separator + type + File.separator);
            rootUrl = fileWebPath + (type + "/");
            File dirFile = new File(rootPath);
            if(!dirFile.exists()) {
                dirFile.mkdirs();
            }
        }
        //根据path参数，设置各路径和URL
        String path = req.getParameter("path") != null ? req.getParameter("path") : "";
        String currentPath = rootPath + path;
        String currentUrl = rootUrl + path;
        String currentDirPath = path;
        String moveupDirPath = "";
        if(StringUtils.isNotBlank(path)) {
            String str = currentDirPath.substring(0, currentDirPath.length() - 1);
            moveupDirPath = str.lastIndexOf("/") >= 0 ? str.substring(0, str.lastIndexOf("/") + 1) : "";
        }
        //排序形式，name or size or type
        String order = req.getParameter("order") != null ? req.getParameter("order").toLowerCase() : "name";
        //目录不存在或不是目录
        File currentPathFile = new File(currentPath);
        String errorMsg = validatePath(path,currentPath);
        if(StringUtils.isNotBlank(errorMsg)) {
            writer.write(errorMsg);
            writer.flush();
            writer.close();
            return;
        }
        //遍历目录取的文件信息
        List<Hashtable> fileList = iteratorFile(currentPathFile);


        if ("size".equals(order)) {
            Collections.sort(fileList, new SizeComparator());
        } else if ("type".equals(order)) {
            Collections.sort(fileList, new TypeComparator());
        } else {
            Collections.sort(fileList, new NameComparator());
        }
        JSONObject result = new JSONObject();
        result.put("moveup_dir_path", moveupDirPath);
        result.put("current_dir_path", currentDirPath);
        result.put("current_url", currentUrl);
        result.put("total_count", fileList.size());
        result.put("file_list", fileList);

        resp.setContentType("application/json; charset=UTF-8");
        writer.write(result.toJSONString());
        writer.flush();
        writer.close();
    }

    private List<Hashtable> iteratorFile(File currentPathFile) {
        //遍历目录取的文件信息
        List<Hashtable> fileList = new ArrayList<Hashtable>();
        if(currentPathFile.listFiles() != null) {
            for (File file : currentPathFile.listFiles()) {
                Hashtable<String, Object> hash = new Hashtable<String, Object>();
                String fileName = file.getName();
                if(file.isDirectory()) {
                    hash.put("is_dir", true);
                    hash.put("has_file", (file.listFiles() != null));
                    hash.put("filesize", 0L);
                    hash.put("is_photo", false);
                    hash.put("filetype", "");
                } else if(file.isFile()){
                    String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
                    hash.put("is_dir", false);
                    hash.put("has_file", false);
                    hash.put("filesize", file.length());
                    hash.put("is_photo", Arrays.<String>asList(fileTypes).contains(fileExt));
                    hash.put("filetype", fileExt);
                }
                hash.put("filename", fileName);
                hash.put("datetime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(file.lastModified()));
                fileList.add(hash);
            }
        }
        return fileList;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
    }

    private String validatePath(String path, String currentPath) {
        //不允许使用..移动到上一级目录
        if (path.indexOf("..") >= 0) {
           return "没有权限.";
        }
        //最后一个字符不是/
        if (!"".equals(path) && !path.endsWith("/")) {
            return "参数无效.";
        }
        //目录不存在或不是目录
        File currentPathFile = new File(currentPath);
        if(!currentPathFile.isDirectory()){
            return "目录不存在.";
        }
        return null;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        location = config.getInitParameter("location");
        fileWebPath = config.getInitParameter("fileWebPath");
    }
    public class NameComparator implements Comparator {
        public int compare(Object a, Object b) {
            Hashtable hashA = (Hashtable)a;
            Hashtable hashB = (Hashtable)b;
            if (((Boolean)hashA.get("is_dir")) && !((Boolean)hashB.get("is_dir"))) {
                return -1;
            } else if (!((Boolean)hashA.get("is_dir")) && ((Boolean)hashB.get("is_dir"))) {
                return 1;
            } else {
                return ((String)hashA.get("filename")).compareTo((String)hashB.get("filename"));
            }
        }
    }
    public class SizeComparator implements Comparator {
        public int compare(Object a, Object b) {
            Hashtable hashA = (Hashtable)a;
            Hashtable hashB = (Hashtable)b;
            if (((Boolean)hashA.get("is_dir")) && !((Boolean)hashB.get("is_dir"))) {
                return -1;
            } else if (!((Boolean)hashA.get("is_dir")) && ((Boolean)hashB.get("is_dir"))) {
                return 1;
            } else {
                if (((Long)hashA.get("filesize")) > ((Long)hashB.get("filesize"))) {
                    return 1;
                } else if (((Long)hashA.get("filesize")) < ((Long)hashB.get("filesize"))) {
                    return -1;
                } else {
                    return 0;
                }
            }
        }
    }
    public class TypeComparator implements Comparator {
        public int compare(Object a, Object b) {
            Hashtable hashA = (Hashtable)a;
            Hashtable hashB = (Hashtable)b;
            if (((Boolean)hashA.get("is_dir")) && !((Boolean)hashB.get("is_dir"))) {
                return -1;
            } else if (!((Boolean)hashA.get("is_dir")) && ((Boolean)hashB.get("is_dir"))) {
                return 1;
            } else {
                return ((String)hashA.get("filetype")).compareTo((String)hashB.get("filetype"));
            }
        }
    }
}
