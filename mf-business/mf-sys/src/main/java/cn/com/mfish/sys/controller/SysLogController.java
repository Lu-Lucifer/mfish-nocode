package cn.com.mfish.sys.controller;

import cn.com.mfish.common.core.annotation.Log;
import cn.com.mfish.common.core.enums.OperateType;
import cn.com.mfish.common.core.web.Result;
import cn.com.mfish.sys.entity.SysLog;
import cn.com.mfish.sys.req.ReqSysLog;
import cn.com.mfish.sys.service.SysLogService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;

/**
 * @Description: 系统日志
 * @Author: mfish
 * @Date: 2022-09-02
 * @Version: V1.0
 */
@Slf4j
@Api(tags = "系统日志")
@RestController
@RequestMapping("/sysLog")
public class SysLogController {
    @Resource
    private SysLogService sysLogService;

    /**
     * 分页列表查询
     *
     * @param reqSysLog
     * @param pageNo
     * @param pageSize
     * @return
     */
    @ApiOperation(value = "系统日志-分页列表查询", notes = "系统日志-分页列表查询")
    @GetMapping
    public Result<IPage<SysLog>> queryPageList(ReqSysLog reqSysLog,
                                               @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                               @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        IPage<SysLog> pageList = sysLogService.page(new Page<>(pageNo, pageSize));
        return Result.ok(pageList, "查询成功!");
    }

    /**
     * 添加
     *
     * @param sysLog
     * @return
     */
    @Log(title = "系统日志-添加", operateType = OperateType.INSERT)
    @ApiOperation(value = "系统日志-添加", notes = "系统日志-添加")
    @PostMapping
    public Result<SysLog> add(@RequestBody SysLog sysLog) {
        if (sysLogService.save(sysLog)) {
            return Result.ok(sysLog, "添加成功!");
        }
        return Result.fail("错误:添加失败!");
    }

    /**
     * 编辑
     *
     * @param sysLog
     * @return
     */
    @Log(title = "系统日志-编辑", operateType = OperateType.UPDATE)
    @ApiOperation(value = "系统日志-编辑", notes = "系统日志-编辑")
    @PutMapping
    public Result<?> edit(@RequestBody SysLog sysLog) {
        if (sysLogService.updateById(sysLog)) {
            return Result.ok("编辑成功!");
        }
        return Result.fail("错误:编辑失败!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @Log(title = "系统日志-通过id删除", operateType = OperateType.DELETE)
    @ApiOperation(value = "系统日志-通过id删除", notes = "系统日志-通过id删除")
    @DeleteMapping("/{id}")
    public Result<?> delete(@ApiParam(name = "id", value = "唯一性ID") @PathVariable String id) {
        if (sysLogService.removeById(id)) {
            return Result.ok("删除成功!");
        }
        return Result.fail("错误:删除失败!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @Log(title = "系统日志-批量删除", operateType = OperateType.DELETE)
    @ApiOperation(value = "系统日志-批量删除", notes = "系统日志-批量删除")
    @DeleteMapping("/batch")
    public Result<?> deleteBatch(@RequestParam(name = "ids") String ids) {
        if (this.sysLogService.removeByIds(Arrays.asList(ids.split(",")))) {
            return Result.ok("批量删除成功!");
        }
        return Result.fail("错误:批量删除失败!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "系统日志-通过id查询", notes = "系统日志-通过id查询")
    @GetMapping("/{id}")
    public Result<SysLog> queryById(@ApiParam(name = "id", value = "唯一性ID") @PathVariable String id) {
        SysLog sysLog = sysLogService.getById(id);
        return Result.ok(sysLog, "查询成功!");
    }
}
