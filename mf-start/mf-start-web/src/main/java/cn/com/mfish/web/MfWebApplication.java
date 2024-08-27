package cn.com.mfish.web;

import cn.com.mfish.common.cloud.annotation.AutoCloud;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;

/**
 * @author: mfish
 * @description: 其他web业务服务参考类
 * @date: 2022/12/16 10:01
 */
@Slf4j
@AutoCloud
public class MfWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(MfWebApplication.class, args);
        log.info("\n\t----------------------------------------------------------\n\t" +
                "\n\t--------------------摸鱼其他web业务服务启动成功-----------------------\n\t");
    }
}
