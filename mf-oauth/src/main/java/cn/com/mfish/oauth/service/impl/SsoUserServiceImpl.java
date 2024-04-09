package cn.com.mfish.oauth.service.impl;

import cn.com.mfish.common.core.exception.MyRuntimeException;
import cn.com.mfish.common.core.utils.AuthInfoUtils;
import cn.com.mfish.common.core.utils.StringUtils;
import cn.com.mfish.common.core.utils.Utils;
import cn.com.mfish.common.core.web.Result;
import cn.com.mfish.common.oauth.api.entity.UserInfo;
import cn.com.mfish.common.oauth.api.entity.UserRole;
import cn.com.mfish.common.oauth.api.vo.TenantVo;
import cn.com.mfish.common.oauth.entity.SimpleUserInfo;
import cn.com.mfish.oauth.cache.common.ClearCache;
import cn.com.mfish.oauth.cache.temp.*;
import cn.com.mfish.oauth.common.PasswordHelper;
import cn.com.mfish.common.oauth.entity.SsoUser;
import cn.com.mfish.oauth.mapper.SsoUserMapper;
import cn.com.mfish.common.oauth.req.ReqSsoUser;
import cn.com.mfish.common.oauth.service.SsoUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * @author: mfish
 * @date: 2020/2/13 16:51
 */
@Service
@Slf4j
public class SsoUserServiceImpl extends ServiceImpl<SsoUserMapper, SsoUser> implements SsoUserService {
    @Resource
    PasswordHelper passwordHelper;
    @Resource
    UserTempCache userTempCache;
    @Resource
    Account2IdTempCache account2IdTempCache;
    @Resource
    UserRoleTempCache userRoleTempCache;
    @Resource
    UserPermissionTempCache userPermissionTempCache;
    @Resource
    HashedCredentialsMatcher hashedCredentialsMatcher;
    @Resource
    UserTenantTempCache userTenantTempCache;
    @Resource
    ClearCache clearCache;

    /**
     * 修改用户密码
     *
     * @param userId
     * @param newPwd
     * @return
     */
    @Override
    public Result<Boolean> changePassword(String userId, String oldPwd, String newPwd) {
        Result<Boolean> result = verifyPassword(newPwd);
        if (!result.isSuccess()) {
            return result;
        }
        SsoUser user = userTempCache.getFromCacheAndDB(userId);
        if (user == null) {
            String error = MessageFormat.format("错误:用户id{0}未获取到用户信息!", userId);
            log.error(error);
            return Result.fail(false, error);
        }
        result = verifyOldPwd(user, oldPwd);
        if (!result.isSuccess()) {
            return result;
        }
        user.setOldPassword(setOldPwd(user.getOldPassword(), user.getPassword()));
        user.setPassword(passwordHelper.encryptPassword(userId, newPwd, user.getSalt()));
        if (user.getOldPassword().contains(user.getPassword())) {
            return Result.fail(false, "错误:密码5次内不得循环使用");
        }
        if (baseMapper.updateById(user) == 1) {
            //更新用户信息,刷新redis 用户缓存
            userTempCache.updateCacheInfo(user, userId);
            return Result.ok(true, "用户密码-修改密码成功");
        }
        return Result.fail(false, "错误:用户密码-修改密码失败");
    }

    private Result<Boolean> verifyOldPwd(SsoUser ssoUser, String oldPwd) {
        //老密码为null时不校验老密码
        if (StringUtils.isEmpty(oldPwd)) {
            return Result.ok();
        }
        SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(
                ssoUser.getId(), //用户名
                ssoUser.getPassword(), //密码
                ByteSource.Util.bytes(ssoUser.getId() + ssoUser.getSalt()),
                ""  //调用基类realm
        );
        UsernamePasswordToken token = new UsernamePasswordToken(ssoUser.getAccount(), oldPwd);
        boolean result = hashedCredentialsMatcher.doCredentialsMatch(token, authenticationInfo);
        if (result) {
            return Result.ok(true, "密码校验正确");
        }
        return Result.fail(false, "错误:旧密码校验失败");
    }

    /**
     * 密码校验 密码长度必须8~16位
     * 密码必须由数字、字母、特殊字符组合
     *
     * @param password
     * @return
     */
    private Result<Boolean> verifyPassword(String password) {
        if (StringUtils.isEmpty(password)) {
            return Result.fail(false, "密码不允许为空");
        }
        if (password.length() < 8 || password.length() > 16) {
            return Result.fail(false, "密码长度必须8~16位");
        }
        if (!password.matches("(?=.*\\d)(?=.*[a-zA-Z])(?=.*[^a-zA-Z0-9]).{8,16}")) {
            return Result.fail(false, "密码必须由数字、字母、特殊字符组合");
        }
        return Result.ok(true, "校验成功");
    }

    /**
     * 设置旧密码 将最后一次密码添加到最近密码列表中，密码逗号隔开
     * 只存储最近5次密码
     *
     * @param oldPwd
     * @param pwd
     * @return
     */
    private String setOldPwd(String oldPwd, String pwd) {
        String[] pwds = StringUtils.isEmpty(oldPwd) ? new String[0] : oldPwd.split(",");
        List<String> list = new ArrayList<>(Arrays.asList(pwds));
        if (list.size() >= 5) {
            list.remove(0);
        }
        if (!StringUtils.isEmpty(pwd)) {
            list.add(pwd);
        }
        return StringUtils.join(list.iterator(), ",");
    }

    @Override
    @Transactional
    public Result<SsoUser> insertUser(SsoUser user) {
        validateUser(user, "新增");
        //如果外部已经赋值过id就不重新赋值了
        if (StringUtils.isEmpty(user.getId())) {
            user.setId(Utils.uuid32());
        }
        //如果外部已经设置过盐，此处不设置
        if (StringUtils.isEmpty(user.getSalt())) {
            user.setSalt(PasswordHelper.buildSalt());
        }
        //短信直接创建用户可以初始不设置密码此处密码允许为空
        if (!StringUtils.isEmpty(user.getPassword())) {
            user.setPassword(passwordHelper.encryptPassword(user.getId(), user.getPassword(), user.getSalt()));
        }
        if (null == user.getStatus()) {
            user.setStatus(0);
        }
        if (null == user.getDelFlag()) {
            user.setDelFlag(0);
        }
        int res = baseMapper.insert(user);
        if (res > 0) {
            insertUserOrg(user.getId(), user.getOrgIds());
            insertUserRole(user.getId(), user.getRoleIds());
            userTempCache.updateCacheInfo(user, user.getId());
            return Result.ok(user, "用户信息-新增成功");
        }
        throw new MyRuntimeException("错误:用户信息-新增失败!");
    }

    @Override
    @Transactional
    public Result<SsoUser> updateUser(SsoUser user) {
        validateUser(user, "修改");
        //帐号名称密码不在此处更新
        String account = user.getAccount();
        user.setAccount(null);
        user.setPassword(null);
        int res = baseMapper.updateById(user);
        if (res > 0) {
            user.setAccount(account);
            if (null != user.getOrgIds()) {
                log.info(MessageFormat.format("删除用户组织数量:{0}条", baseMapper.deleteUserOrg(user.getId(), null)));
                insertUserOrg(user.getId(), user.getOrgIds());
            }
            if (null != user.getRoleIds()) {
                log.info(MessageFormat.format("删除用户角色数量:{0}条", baseMapper.deleteUserRole(user.getId())));
                insertUserRole(user.getId(), user.getRoleIds());
            }
            //移除缓存下次登录时会自动拉取
            CompletableFuture.runAsync(() -> clearCache.removeUserCache(user.getId()));
            return Result.ok(user, "用户信息-更新成功");
        }
        throw new MyRuntimeException("错误:未找到用户信息更新数据");
    }

    /**
     * 校验用户帐号 账号，手机，email均不允许重复
     *
     * @param user    用户
     * @param operate 操作
     */
    private void validateUser(SsoUser user, String operate) {
        if (isAccountExist(user.getAccount(), user.getId())) {
            throw new MyRuntimeException("错误:帐号已存在-" + operate + "失败!");
        }
        if (isAccountExist(user.getPhone(), user.getId())) {
            throw new MyRuntimeException("错误:手机号已存在-" + operate + "失败!");
        }
        if (isAccountExist(user.getEmail(), user.getId())) {
            throw new MyRuntimeException("错误:email已存在-" + operate + "失败!");
        }
        if (!AuthInfoUtils.isSuper(user.getId()) && AuthInfoUtils.isContainSuperAdmin(user.getRoleIds())) {
            throw new MyRuntimeException("错误:不允许设置为超户!");
        }
        if (!StringUtils.isEmpty(user.getAccount())) {
            if (user.getAccount().length() > 30) {
                throw new MyRuntimeException("错误:帐号字符不要超过30个字符");
            }
            if (!StringUtils.isMatch("^[a-zA-Z0-9]+$", user.getAccount())) {
                throw new MyRuntimeException("错误:帐号必须只允许为数字和字母");
            }
        }
        if (!StringUtils.isEmpty(user.getPhone()) && !StringUtils.isPhone(user.getPhone())) {
            throw new MyRuntimeException("错误:手机号不正确");
        }
    }

    @Override
    public boolean removeUser(String id) {
        SsoUser ssoUser = new SsoUser();
        ssoUser.setDelFlag(1).setId(id);
        if (baseMapper.updateById(ssoUser) == 1) {
            CompletableFuture.runAsync(() -> clearCache.removeUserCache(id));
            log.info(MessageFormat.format("删除用户成功,用户ID:{0}", id));
            return true;
        }
        log.error(MessageFormat.format("删除用户失败,用户ID:{0}", id));
        return false;
    }


    @Override
    public SsoUser getUserByAccount(String account) {
        String userId = account2IdTempCache.getFromCacheAndDB(account);
        return getUserById(userId);
    }

    @Override
    public SsoUser getUserById(String userId) {
        return userTempCache.getFromCacheAndDB(userId);
    }

    public UserInfo getUserByIdNoPwd(String userId) {
        SsoUser ssoUser = userTempCache.getFromCacheAndDB(userId);
        UserInfo userInfo = new UserInfo();
        BeanUtils.copyProperties(ssoUser, userInfo);
        return userInfo;
    }

    @Override
    public List<UserInfo> getUserList(ReqSsoUser reqSsoUser) {
        return baseMapper.getUserList(reqSsoUser);
    }

    @Override
    public List<UserRole> getUserRoles(String userId, String tenantId) {
        return userRoleTempCache.getFromCacheAndDB(userId, tenantId);
    }

    @Override
    public Set<String> getUserPermissions(String userId, String tenantId) {
        return userPermissionTempCache.getFromCacheAndDB(userId, tenantId);
    }

    @Override
    public List<TenantVo> getUserTenants(String userId) {
        return userTenantTempCache.getFromCacheAndDB(userId);
    }

    @Override
    public boolean isAccountExist(String account, String userId) {
        return baseMapper.isAccountExist(account, userId) > 0;
    }

    @Override
    public int insertUserRole(String userId, List<String> roles) {
        if (roles == null || roles.size() == 0) {
            return 0;
        }
        int count = baseMapper.insertUserRole(userId, roles);
        if (count > 0) {
            return count;
        }
        throw new MyRuntimeException("错误:插入用户角色失败");
    }

    @Override
    public int insertUserOrg(String userId, List<String> orgList) {
        if (orgList == null || orgList.isEmpty()) {
            return 0;
        }
        int count = baseMapper.insertUserOrg(userId, orgList);
        if (count > 0) {
            return count;
        }
        throw new MyRuntimeException("错误:插入用户组织失败");
    }

    @Override
    public int deleteUserOrg(String userId, String... orgList) {
        return baseMapper.deleteUserOrg(userId, Arrays.asList(orgList));
    }

    @Override
    public boolean isExistUserOrg(String userId, String orgId) {
        return baseMapper.isExistUserOrg(userId, orgId) > 0;
    }

    @Override
    public List<SimpleUserInfo> searchUserList(String condition) {
        //最多检索50条
        PageHelper.startPage(1, 50);
        return baseMapper.searchUserList(condition);
    }
}
