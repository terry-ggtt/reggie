package com.itheima.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

//自定义数据对象处理器,自动填充对象
@Slf4j
@Component //spring 扫描为bean
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("插入数据时，进行数据填充...");
        log.info(metaObject.toString());
        metaObject.setValue("createTime" , LocalDateTime.now());
        metaObject.setValue("updateTime" , LocalDateTime.now());
        metaObject.setValue("createUser" , BaseContext.getCurrentId() );
        metaObject.setValue("updateUser" , BaseContext.getCurrentId() );
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("更新数据时，进行数据填充...");

        log.info(metaObject.toString());
        metaObject.setValue("updateTime" , LocalDateTime.now());
        metaObject.setValue("updateUser" , BaseContext.getCurrentId());
        metaObject.setValue("createTime" , LocalDateTime.now());
        metaObject.setValue("createUser" , BaseContext.getCurrentId());


    }
}