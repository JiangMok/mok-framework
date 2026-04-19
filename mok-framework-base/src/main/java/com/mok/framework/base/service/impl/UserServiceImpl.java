package com.mok.baseframe.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mok.baseframe.base.service.PermissionService;
import com.mok.baseframe.base.service.RoleService;
import com.mok.baseframe.base.service.UserService;
import com.mok.baseframe.common.PageParam;
import com.mok.baseframe.common.PageResult;
import com.mok.baseframe.dao.UserMapper;
import com.mok.baseframe.dao.UserRoleMapper;
import com.mok.baseframe.entity.PermissionEntity;
import com.mok.baseframe.entity.RoleEntity;
import com.mok.baseframe.entity.UserEntity;
import com.mok.baseframe.common.utils.LogUtils;
import com.mok.baseframe.security.utils.SecurityUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author: JN
 * @date: 2026/1/6
 */

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {
    private static final Logger log = LogUtils.getLogger(UserServiceImpl.class);

    private final PermissionService permissionService;
    private final SecurityUtils securityUtils;
    private final RoleService roleService;

    public UserServiceImpl(PermissionService permissionService,
                           SecurityUtils securityUtils,
                           UserRoleMapper userRoleMapper, RoleService roleService) {
        this.permissionService = permissionService;
        this.securityUtils = securityUtils;
        this.roleService = roleService;
    }

    /**
     * @description: 分页查询USER数据
     * @author: JN
     * @date: 2025/12/31 21:15
     * @param: [param]
     * @return: com.mok.securityframework.common.PageResult<com.mok.securityframework.entity.User>
     **/
    @Override
    public PageResult<UserEntity> getPageList(PageParam param) {
        // 获取当前登录用户
        UserEntity currentUserEntity = securityUtils.getCurrentUser();
        //创建 Mybatis Plus 的分页对象
        //  Page<User> : 分页对象
        //  参数1: param.getPageNum() 当前页码
        //  参数2: param.getPageSize() 每页大小
        Page<UserEntity> page = new Page<>(param.getPageNum(), param.getPageSize());
        //创建 Lambda 查询条件包装器
        //  LambdaQueryWrapper<User>：支持Lambda表达式的查询条件包装器
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        //添加查询条件
        //  只查询未删除的用户(逻辑删除)
        wrapper.eq(UserEntity::getIsDeleted, 0);
        // ==================== 新增：根据用户权限添加过滤条件 ====================
        if (currentUserEntity != null) {
            // 判断当前用户是否为超级管理员
            boolean isSuperAdmin = isSuperAdmin(currentUserEntity);
            if (!isSuperAdmin) {
                // 普通用户：只能看到自己创建的用户
                wrapper.and(w -> w.eq(UserEntity::getCreateBy, currentUserEntity.getId())
                        .or().eq(UserEntity::getId, currentUserEntity.getId()));
                log.debug("用户 {} 查看用户列表，应用数据权限过滤：只能看到自己创建的用户",
                        currentUserEntity.getUsername());
            } else {
                // 超级管理员：可以看到所有用户
                log.debug("超级管理员查看所有用户，不进行数据权限过滤");
            }
        } else {
            // 未登录用户：不应该有权限查询用户列表
            wrapper.eq(UserEntity::getId, "0");
            log.warn("未登录用户尝试查询用户列表，返回空结果");
        }
        //添加排序条件:按创建时间的降序排序
        //  支持的链式方法：
        // .eq()：等于
        // .ne()：不等于
        // .gt()：大于
        // .ge()：大于等于
        // .lt()：小于
        // .le()：小于等于
        // .like()：模糊查询
        // .between()：区间查询
        // .in()：IN查询
        // .orderByAsc()：升序排序
        // .orderByDesc()：降序排序
        // .last()：追加SQL片段
        // .select()：指定查询字段
        if (param.getKeyword() != null) {
            wrapper.like(UserEntity::getUsername, param.getKeyword());
        }
        if (param.getStatus() != null) {
            wrapper.eq(UserEntity::getStatus, param.getStatus());
        }
        wrapper.orderByDesc(UserEntity::getCreateTime);
        //执行分页查询
        //  baseMapper.selectPage : 调用父类的的BaseMapper来执行分页查询
        //  参数1: page分页对象,里面包含分页的具体数据
        //  参数2: wrapper 条件查询包装器
        IPage<UserEntity> result = baseMapper.selectPage(page, wrapper);
        //将 Mybatis Plus 的分页结果转换为自定义的分页结果
        //  PageResult.fromIPage() : 静态方法,将IPage转换为P阿哥Result
        return PageResult.fromIPage(result);
    }


    /**
     * @description: 根据 ID 获取权限列表
     * @author: JN
     * @date: 2026/1/2 13:13
     * @param: [userId]
     * @return: java.util.List<java.lang.String>
     **/
    @Override
    public List<String> getPermissionsByUserId(String userId) {
        List<String> permissions = new ArrayList<>();
        List<PermissionEntity> permissionEntityList = permissionService.getPermissionsByUserId(userId);
        if (!permissionEntityList.isEmpty()) {
            for (PermissionEntity permissionEntity : permissionEntityList) {
                permissions.add(permissionEntity.getPermissionCode());
            }
        }
        return permissions;
    }

    @Override
    public PageResult<UserEntity> getPageListWithPermission(PageParam param, boolean applyDataPermission) {
        if (!applyDataPermission) {
            // 如果不应用数据权限，调用原来的方法
            return getPageList(param);
        }
        // 应用数据权限
        return getPageList(param);
    }

    @Override
    public boolean canViewUser(String targetUserId) {
        if (targetUserId == null) {
            return false;
        }

        // 获取当前登录用户
        UserEntity currentUserEntity = securityUtils.getCurrentUser();
        if (currentUserEntity == null) {
            return false;
        }

        // 超级管理员可以查看所有用户
        if ("admin".equals(currentUserEntity.getUsername())) {
            return true;
        }

        // 查询目标用户
        UserEntity targetUserEntity = getById(targetUserId);
        if (targetUserEntity == null) {
            return false;
        }

        // 普通用户可以查看自己创建的用户，以及自己本身
        return currentUserEntity.getId().equals(targetUserEntity.getCreateBy())
                || currentUserEntity.getId().equals(targetUserId);
    }

    @Override
    public boolean canEditUser(String targetUserId) {
        if (targetUserId == null) {
            return false;
        }

        // 获取当前登录用户
        UserEntity currentUserEntity = securityUtils.getCurrentUser();
        if (currentUserEntity == null) {
            return false;
        }

        //如果是超级管理员角色
        if (isSuperAdmin(currentUserEntity)) {
            //查询目标用户
            UserEntity targetUserEntity = getById(targetUserId);
            //目标用户是admin吗?
            if ("admin".equals(targetUserEntity.getUsername())) {
                //只有当前登录用户是 admin 时才可以修改
                return "admin".equals(currentUserEntity.getUsername());
            }
            //目标不是admin,超级管理员可以各种修改
            return true;
        }

        UserEntity targetUserEntity = getById(targetUserId);
        return targetUserEntity != null &&
                // 普通用户只能编辑自己创建的用户
                (currentUserEntity.getId().equals(targetUserEntity.getCreateBy()) ||
                        //和自己
                        currentUserEntity.getId().equals(targetUserId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer updateUserPwdById(UserEntity userEntity) {
        return baseMapper.updateUserPwdById(userEntity);
    }

    /**
     * @description: 判断是否具有超级管理的角色
     * @author: JN
     * @date: 2026/1/15 15:26
     * @param: [user]
     * @return: boolean
     **/
    private boolean isSuperAdmin(UserEntity user) {
        List<RoleEntity> roleList = roleService.getRolesByUserId(user.getId());
        return roleList.stream()
                .anyMatch(role -> "ROLE_ADMIN".equals(role.getRoleCode()));
    }

}
