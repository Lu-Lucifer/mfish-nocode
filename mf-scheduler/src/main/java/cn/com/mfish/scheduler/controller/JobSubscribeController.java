package cn.com.mfish.scheduler.controller;

import cn.com.mfish.common.log.annotation.Log;
import cn.com.mfish.common.core.enums.OperateType;
import cn.com.mfish.common.core.web.Result;
import cn.com.mfish.scheduler.entity.JobSubscribe;
import cn.com.mfish.scheduler.req.ReqJobSubscribe;
import cn.com.mfish.scheduler.service.JobSubscribeService;
import cn.com.mfish.common.web.page.PageResult;
import cn.com.mfish.common.web.page.ReqPage;
import com.github.pagehelper.PageHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.Arrays;

/**
 * @description: 任务订阅表
 * @author: mfish
 * @date: 2023-02-20
 * @version: V1.0.0
 */
@Slf4j
@Api(tags = "任务订阅表")
@RestController
@RequestMapping("/jobSubscribe")
public class JobSubscribeController {
	@Resource
	private JobSubscribeService jobSubscribeService;

	/**
	 * 分页列表查询
	 *
	 * @param reqJobSubscribe 任务订阅表请求参数
	 * @return 返回任务订阅表-分页列表
	 */
	@ApiOperation(value = "任务订阅表-分页列表查询", notes = "任务订阅表-分页列表查询")
	@GetMapping
	public Result<PageResult<JobSubscribe>> queryPageList(ReqJobSubscribe reqJobSubscribe, ReqPage reqPage) {
        PageHelper.startPage(reqPage.getPageNum(), reqPage.getPageSize());
	    return Result.ok(new PageResult<>(jobSubscribeService.list()), "任务订阅表-查询成功!");
	}

	/**
	 * 添加
	 *
	 * @param jobSubscribe 任务订阅表对象
	 * @return 返回任务订阅表-添加结果
	 */
	@Log(title = "任务订阅表-添加", operateType = OperateType.INSERT)
	@ApiOperation("任务订阅表-添加")
	@PostMapping
	public Result<JobSubscribe> add(@RequestBody JobSubscribe jobSubscribe) {
		if (jobSubscribeService.save(jobSubscribe)) {
			return Result.ok(jobSubscribe, "任务订阅表-添加成功!");
		}
        return Result.fail(jobSubscribe, "错误:任务订阅表-添加失败!");
	}

	/**
	 * 编辑
	 *
	 * @param jobSubscribe 任务订阅表对象
	 * @return 返回任务订阅表-编辑结果
	 */
	@Log(title = "任务订阅表-编辑", operateType = OperateType.UPDATE)
	@ApiOperation("任务订阅表-编辑")
	@PutMapping
	public Result<JobSubscribe> edit(@RequestBody JobSubscribe jobSubscribe) {
		if (jobSubscribeService.updateById(jobSubscribe)) {
		    return Result.ok(jobSubscribe, "任务订阅表-编辑成功!");
		}
		return Result.fail(jobSubscribe, "错误:任务订阅表-编辑失败!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id 唯一ID
	 * @return 返回任务订阅表-删除结果
	 */
	@Log(title = "任务订阅表-通过id删除", operateType = OperateType.DELETE)
	@ApiOperation("任务订阅表-通过id删除")
	@DeleteMapping("/{id}")
	public Result<Boolean> delete(@ApiParam(name = "id", value = "唯一性ID") @PathVariable String id) {
		if (jobSubscribeService.removeById(id)) {
			return Result.ok(true, "任务订阅表-删除成功!");
		}
		return Result.fail(false, "错误:任务订阅表-删除失败!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids 批量ID
	 * @return 返回任务订阅表-删除结果
	 */
	@Log(title = "任务订阅表-批量删除", operateType = OperateType.DELETE)
	@ApiOperation("任务订阅表-批量删除")
	@DeleteMapping("/batch")
	public Result<Boolean> deleteBatch(@RequestParam(name = "ids") String ids) {
		if (this.jobSubscribeService.removeByIds(Arrays.asList(ids.split(",")))) {
		    return Result.ok(true, "任务订阅表-批量删除成功!");
		}
		return Result.fail(false, "错误:任务订阅表-批量删除失败!");
	}

	/**
	 * 通过id查询
	 *
	 * @param id 唯一ID
	 * @return 返回任务订阅表对象
	 */
	@ApiOperation("任务订阅表-通过id查询")
	@GetMapping("/{id}")
	public Result<JobSubscribe> queryById(@ApiParam(name = "id", value = "唯一性ID") @PathVariable String id) {
		JobSubscribe jobSubscribe = jobSubscribeService.getById(id);
		return Result.ok(jobSubscribe, "任务订阅表-查询成功!");
	}
}