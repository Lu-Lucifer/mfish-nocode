package cn.com.mfish.scheduler.execute;

import cn.com.mfish.common.core.utils.SpringBeanFactory;
import cn.com.mfish.common.core.utils.StringUtils;
import cn.com.mfish.common.core.utils.Utils;
import cn.com.mfish.scheduler.common.InvokeUtils;
import cn.com.mfish.scheduler.common.JobUtils;
import cn.com.mfish.scheduler.entity.Job;
import cn.com.mfish.scheduler.entity.JobLog;
import cn.com.mfish.scheduler.enums.JobStatus;
import cn.com.mfish.scheduler.enums.JobType;
import cn.com.mfish.scheduler.invoke.BaseInvoke;
import cn.com.mfish.scheduler.service.JobLogService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

/**
 * @description: 抽象job执行
 * @author: mfish
 * @date: 2023/2/7 10:41
 */
@Slf4j
public abstract class AbstractJobExecute extends QuartzJobBean {
    private static ThreadLocal<JobLog> threadLocal = new ThreadLocal<>();
    private static final String OPERATOR = "scheduler_robot";

    /**
     * 执行job
     * 参数支持普通对象数组类型 例如:["****",11] 和 invokeParams对象数组类型
     * 例如：[{"type":"java.lang.String","value":"inner"},{"type":"cn.com.mfish.sys.api.entity.SysLog","value":{"title":"aaaa"}}]
     *
     * @param context
     */
    @Override
    protected void executeInternal(JobExecutionContext context) {
        JobDataMap jobMap = context.getMergedJobDataMap();
        Job job = JSON.parseObject(jobMap.get(JobUtils.JOB_DATA_MAP).toString(), Job.class);
        String strParams = job.getParams();
        List<?> list = null;
        if (!StringUtils.isEmpty(strParams)) {
            //默认转化为invokeParams类型，如果转换失败，直接转换为普通数组
            try {
                list = JSON.parseArray(strParams, InvokeUtils.InvokeParams.class);
            } catch (Exception e) {
                list = JSON.parseArray(strParams);
            }
        }
        beginExecute(job);
        boolean success = false;
        String error = null;
        try {
            execute(JobType.getJob(job.getJobType()), job.getClassName(), job.getMethodName(), list);
            success = true;
        } catch (Exception ex) {
            log.error("任务执行异常:", ex);
            success = false;
            error = ex.getMessage();
            //数据库设置长度1000，截取999位
            if (error.length() >= 1000) {
                error = error.substring(0, 999);
            }
        } finally {
            executeCallBack(success, error);
        }
    }

    /**
     * 任务开始执行
     *
     * @param job
     * @return
     */
    private void beginExecute(Job job) {
        JobLog jobLog = new JobLog().setId(Utils.uuid32())
                .setJobId(job.getId())
                .setJobGroup(job.getJobGroup())
                .setJobName(job.getJobName())
                .setJobType(job.getJobType())
                .setClassName(job.getClassName())
                .setMethodName(job.getMethodName())
                .setParams(job.getParams())
                .setStatus(JobStatus.开始.getValue());
        jobLog.setCreateBy(OPERATOR);
        threadLocal.set(jobLog);
        JobLogService jobLogService = SpringBeanFactory.getBean(JobLogService.class);
        if (jobLogService.save(jobLog)) {
            log.info(MessageFormat.format("任务:{0}调度成功,任务ID:{1}", job.getJobName(), job.getId()));
            return;
        }
        log.error(MessageFormat.format("任务:{0}调度失败,任务ID:{1}", job.getJobName(), job.getId()));
    }

    /**
     * 任务回调
     *
     * @param success 任务是否成功
     * @param remark  错误信息
     */
    public static void executeCallBack(boolean success, String remark) {
        JobLog jobLog = threadLocal.get();
        threadLocal.remove();
        if (jobLog == null) {
            log.error("错误:未获取到任务日志");
            return;
        }
        JobLogService jobLogService = SpringBeanFactory.getBean(JobLogService.class);
        JobStatus jobStatus = success ? JobStatus.成功 : JobStatus.失败;
        log.info(MessageFormat.format("任务{0}执行{1},任务ID:{2}", jobLog.getJobName(), jobStatus, jobLog.getId()));
        jobLog.setStatus(jobStatus.getValue()).setCostTime(new Date().getTime() - jobLog.getCreateTime().getTime()).setRemark(remark);
        jobLog.setUpdateBy(OPERATOR);
        jobLogService.updateById(jobLog);
    }

    protected abstract <T> void execute(BaseInvoke baseJob, String className, String methodName, List<T> params);
}
