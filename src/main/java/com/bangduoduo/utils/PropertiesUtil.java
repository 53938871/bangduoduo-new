package com.bangduoduo.utils;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by cpeng2 on 3/30/2015.
 */
public class PropertiesUtil {
    private static Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);
    private static PropertiesConfiguration configuration;
    private static PropertiesUtil instance = new PropertiesUtil();
    private PropertiesUtil(){
        try {
            configuration = new PropertiesConfiguration("servlet.properties");
        } catch (ConfigurationException e) {
            logger.error("servlet.properties error");
        }
    }

    public static String getProperty(String key) {
        Object value = instance.configuration.getProperty(key);
        return value == null?null:value.toString();
    }

}
