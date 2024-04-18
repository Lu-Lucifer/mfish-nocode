package cn.com.mfish.common.web.aspect;

import cn.com.mfish.common.core.annotation.GlobalException;
import cn.com.mfish.common.core.constants.RPCConstants;
import cn.com.mfish.common.core.exception.OAuthValidateException;
import cn.com.mfish.common.core.utils.ServletUtils;
import cn.com.mfish.common.core.utils.StringUtils;
import cn.com.mfish.common.core.web.Result;
import cn.com.mfish.common.oauth.validator.TokenValidator;
import cn.com.mfish.common.web.annotation.InnerUser;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;


/**
 * @author: mfish
 * @description: 内部用户校验切面
 * @date: 2021/12/3 20:28
 */
@Aspect
@Component
@GlobalException
public class InnerUserAspect {
    @Resource
    TokenValidator tokenValidator;

    @Around("@annotation(innerUser)")
    public Object innerAround(ProceedingJoinPoint point, InnerUser innerUser) throws Throwable {
        HttpServletRequest request = ServletUtils.getRequest();
        if (request == null) {
            throw new OAuthValidateException("错误:未获取到请求信息");
        }
        String source = request.getHeader(RPCConstants.REQ_ORIGIN);
        if (StringUtils.isEmpty(source)) {
            throw new OAuthValidateException("错误:内部接口禁止外部直接访问");
        }
        // 内部请求验证
        if (RPCConstants.INNER.equals(source) && !innerUser.validateUser()) {
            return point.proceed();
        }
        Result<?> result = tokenValidator.validator(request);
        if (!result.isSuccess()) {
            throw new OAuthValidateException(result.getMsg());
        }
        return point.proceed();
    }
}
