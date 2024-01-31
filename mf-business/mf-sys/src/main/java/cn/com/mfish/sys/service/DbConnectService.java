package cn.com.mfish.sys.service;

import cn.com.mfish.common.core.web.Result;
import cn.com.mfish.sys.api.entity.DbConnect;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @description: 数据库连接
 * @author: mfish
 * @date: 2023-03-13
 * @version: V1.2.0
 */
public interface DbConnectService extends IService<DbConnect> {
    Result<DbConnect> insertConnect(DbConnect dbConnect);
    Result<DbConnect> updateConnect(DbConnect dbConnect);
    Result<Boolean> testConnect(DbConnect dbConnect);
    Result<DbConnect> queryById(String id);
}
