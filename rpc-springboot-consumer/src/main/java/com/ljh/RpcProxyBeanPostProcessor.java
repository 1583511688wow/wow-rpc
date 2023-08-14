package com.ljh;

import com.ljh.annotation.RpcService;
import com.ljh.proxy.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * @author ljh
 * @createTime 2023-07-30
 */
@Component
public class RpcProxyBeanPostProcessor implements BeanPostProcessor {
    
    // 他会拦截所有的bean的创建，会在每一个bean初始化后被调用
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 想办法给他生成一个代理
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            RpcService rpcService = field.getAnnotation(RpcService.class);
            if(rpcService != null){
                // 获取一个代理
                Class<?> type = field.getType();
                Object proxy = ProxyFactory.getProxy(type);
                field.setAccessible(true);
                try {
                    field.set(bean,proxy);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        
        return bean;
    }
}
