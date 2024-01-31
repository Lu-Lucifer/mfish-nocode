package cn.com.mfish.sys;

import cn.com.mfish.common.web.annotation.AutoWeb;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;

/**
 * @author: mfish
 * @description: 系统业务中心启动类
 * @date: 2022/9/2
 */

@Slf4j
@AutoWeb
public class MfSysApplication {
    public static void main(String[] args) {
        SpringApplication.run(MfSysApplication.class, args);
        log.info("\n\t----------------------------------------------------------\n\t" +
                "\n\t--------------------摸鱼系统业务中心启动成功-----------------------\n\t");
    }
}
