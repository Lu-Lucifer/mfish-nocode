package cn.com.mfish.sys.service;

import cn.com.mfish.common.core.web.PageResult;
import cn.com.mfish.common.core.web.ReqPage;
import cn.com.mfish.common.core.web.Result;
import cn.com.mfish.sys.entity.DictCategory;
import cn.com.mfish.sys.req.ReqDictCategory;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @description: 树形分类
 * @author: mfish
 * @date: 2024-03-12
 * @version: V1.2.0
 */
public interface DictCategoryService extends IService<DictCategory> {

    /**
     * 分页查询分类树
     *
     * @param reqDictCategory 查询参数
     * @param reqPage             分页参数
     * @return
     */
    Result<PageResult<DictCategory>> queryCategoryTree(ReqDictCategory reqDictCategory, ReqPage reqPage);

    /**
     * 查询分类树
     *
     * @param reqDictCategory 查询参数
     * @return
     */
    Result<List<DictCategory>> queryCategoryTree(ReqDictCategory reqDictCategory);

    /**
     * 查询分类
     *
     * @param reqDictCategory 查询参数
     * @return
     */
    List<DictCategory> queryCategory(ReqDictCategory reqDictCategory);

    /**
     * 分页查询一级分类
     *
     * @param reqDictCategory 查询参数
     * @return
     */
    PageResult<DictCategory> queryOneLevelCategory(ReqDictCategory reqDictCategory, ReqPage reqPage);


    /**
     * 插入分类
     *
     * @param category 分类
     * @return
     */
    Result<DictCategory> insertCategory(DictCategory category);

    /**
     * 删除分类
     *
     * @param categoryId 分类id
     * @return
     */
    Result<Boolean> deleteCategory(String categoryId);

    /**
     * 修改分类
     *
     * @param category 分类
     * @return
     */
    Result<DictCategory> updateCategory(DictCategory category);
}
