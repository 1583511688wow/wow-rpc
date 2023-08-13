package com.ljh.spi;


import com.ljh.config.ObjectWrapper;
import com.ljh.exceptions.SpiException;
import com.ljh.loadbalancer.AbstractLoadBalancer;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 实现一个简易版本的spi
 * @author ljh
 *
 */
public class SpiHandler {

    private static final Logger log = LoggerFactory.getLogger(SpiHandler.class);

    //定义一个basePath
    private static final String BASE_PATH = "META-INF/rpc-services";

    //定义一个缓存，保存spi相关原始内容
    private static final Map<String, List<String>> SPI_CONTENT = new ConcurrentHashMap<>(8);


    //每一个接口所构建的实例
    private static final Map<Class<?>, List<ObjectWrapper<?>>> SPI_IMPLEMENT = new ConcurrentHashMap<>();

    static {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL fileUrl = classLoader.getResource(BASE_PATH);
        if (fileUrl != null) {
            File file = new File(fileUrl.getPath());
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    String key = child.getName();
                    List<String> value = getImplNames(child);
                    SPI_CONTENT.put(key, value);
                }
            }


        }

    }


    /**
     * 获取第一个和当前服务相关的实例
     *
     * @param clazz 一个服务接口的class实例
     * @param <T>
     * @return 实现类的实例
     */
    public synchronized static <T> ObjectWrapper<T> get(Class<T> clazz) {

        //优先走缓存
        List<ObjectWrapper<?>> impl = SPI_IMPLEMENT.get(clazz);
        if (impl != null && impl.size() > 0) {

            return (ObjectWrapper<T>)impl.get(0);
        }

        //构建缓存
         buildCache(clazz);

        List<ObjectWrapper<?>>  result = SPI_IMPLEMENT.get(clazz);
        if (result == null || result.size() == 0){
            return null;
        }

        return (ObjectWrapper<T>) result.get(0);
    }


    /**
     * 获取所有和当前服务相关的实例
     *
     * @param clazz 一个服务接口的class实例
     * @param <T>
     * @return 实现类的实例集合
     */
    public synchronized static <T> List<ObjectWrapper<T>> getList(Class<T> clazz) {

        //优先走缓存
        List<ObjectWrapper<?>> impl = SPI_IMPLEMENT.get(clazz);
        if (impl != null && impl.size() > 0) {
           return impl.stream().map(objectWrapper -> (ObjectWrapper<T>) objectWrapper)
                    .collect(Collectors.toList());
        }


        buildCache(clazz);
        impl = SPI_IMPLEMENT.get(clazz);
        if (impl != null && impl.size() > 0) {
            return impl.stream().map(objectWrapper -> (ObjectWrapper<T>) objectWrapper)
                    .collect(Collectors.toList());
        }



        return new ArrayList<>();


    }


    /**
     * 构建clazz相关的缓存
     * @param clazz
     * @param <?>
     * @return
     */
    private static void buildCache(Class<?> clazz) {

        //建立缓存
        String name = clazz.getName();
        List<String> strings = SPI_CONTENT.get(name);
        if (strings == null || strings.size() == 0){
            return;
        }

        List<ObjectWrapper<?>> impls = new ArrayList<>();
        Object instance = null;
        for (String implName : strings) {
            try {

                // 首先进行分割
                String[] codeAndTypeAndName = implName.split("-");
                if(codeAndTypeAndName.length != 3){
                    throw new SpiException("您配置的spi文件不合法");
                }
                Byte code = Byte.valueOf(codeAndTypeAndName[0]);
                String type = codeAndTypeAndName[1];
                String implementName = codeAndTypeAndName[2];

                Class<?> aClass = Class.forName(implementName);
                Object impl = aClass.getConstructor().newInstance();

                ObjectWrapper<?> objectWrapper = new ObjectWrapper<>(code,type,impl);

                impls.add(objectWrapper);
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                log.error("spi实例化失败");
                e.printStackTrace();
            }

        }
        SPI_IMPLEMENT.put(clazz, impls);

    }



    /**
     * 获取一个文件所有实现的名称
     * @param child
     * @return
     */
    private static List<String> getImplNames(File child) {

        try (
                FileReader fileReader = new FileReader(child);
                BufferedReader bufferedReader = new BufferedReader(fileReader)
        ) {
            List<String> implNames = new ArrayList<>();
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null || "".equals(line)) break;
                implNames.add(line);
            }
            return implNames;
        } catch (IOException e) {
            log.error("读取spi文件时发生异常.", e);
        }
        return null;


    }



}
