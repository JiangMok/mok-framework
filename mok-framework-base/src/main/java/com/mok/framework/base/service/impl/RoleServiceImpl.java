package com.mok.baseframe.base.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mok.baseframe.base.service.RoleService;
import com.mok.baseframe.common.BusinessException;
import com.mok.baseframe.common.PageParam;
import com.mok.baseframe.common.PageResult;
import com.mok.baseframe.dao.PermissionMapper;
import com.mok.baseframe.dao.RoleMapper;
import com.mok.baseframe.dao.RolePermissionMapper;
import com.mok.baseframe.dao.UserRoleMapper;
import com.mok.baseframe.dto.RoleDTO;
import com.mok.baseframe.entity.*;
import com.mok.baseframe.security.service.PermissionCacheService;
import com.mok.baseframe.common.utils.LogUtils;
import com.mok.baseframe.security.utils.SecurityUtils;
import org.slf4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @description: 角色service接口实现类
 * @author: JN
 * @date: 2026/1/1 18:22
 **/

@Service

public class RoleServiceImpl extends ServiceImpl<RoleMapper, RoleEntity> implements RoleService {
    private static final Logger log = LogUtils.getLogger(RoleServiceImpl.class);

    private final UserRoleMapper userRoleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;
    private final SecurityUtils securityUtils;
    private final PermissionCacheService permissionCacheService;

    public RoleServiceImpl(UserRoleMapper userRoleMapper,
                           RolePermissionMapper rolePermissionMapper,
                           PermissionMapper permissionMapper,
                           SecurityUtils securityUtils,
                           PermissionCacheService permissionCacheService) {
        this.userRoleMapper = userRoleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.permissionMapper = permissionMapper;
        this.securityUtils = securityUtils;
        this.permissionCacheService = permissionCacheService;
    }

    @Override
    public PageResult<RoleEntity> getPageList(PageParam param) {
        //创建角色查询器
        List<RoleEntity> roleEntityList = baseMapper.selectRolesByUserId(securityUtils.getCurrentUserId());
        List<String> currentUserRoleIds = new ArrayList<>();
        for (RoleEntity roleEntity : roleEntityList) {
            currentUserRoleIds.add(roleEntity.getId());
        }
        //创建分页对象
        Page<RoleEntity> page = new Page<>(param.getPageNum(), param.getPageSize());
        //创建lambda查询包装器
        LambdaQueryWrapper<RoleEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoleEntity::getIsDeleted, 0);
        // 如果不是管理员角色，只查询当前用户拥有的角色
        // 判断是否是管理员
        boolean isAdmin = isAdminRole(currentUserRoleIds);
        if (!isAdmin && currentUserRoleIds != null && !currentUserRoleIds.isEmpty()) {
            wrapper.in(RoleEntity::getId, currentUserRoleIds);
        }
        //根据角色名搜索或者角色编码查询
        if (StringUtils.hasText(param.getKeyword())) {
            wrapper.like(RoleEntity::getRoleName, param.getKeyword())
                    .or().like(RoleEntity::getRoleCode, param.getKeyword());
        }
        //按状态查询
        if (param.get("status") != null) {
            wrapper.eq(RoleEntity::getStatus, param.get("status"));
        }
        if (param.getOrderBy() != null) {
            if ("asc".equalsIgnoreCase(param.getOrder())) {
                wrapper.orderByAsc(RoleEntity::getCreateTime);
            } else {
                wrapper.orderByDesc(RoleEntity::getCreateTime);
            }
        } else {
            //默认排序:先按sort升序,再按createTime降序
            wrapper.orderByAsc(RoleEntity::getSort).orderByDesc(RoleEntity::getCreateTime);
        }
        //执行分页查询
        IPage<RoleEntity> result = baseMapper.selectPage(page, wrapper);
        //转换为自定义的分页结果
        return PageResult.fromIPage(result);
    }

    /**
     * @description: 获取所用启用角色
     * @author: JN
     * @date: 2026/1/2 11:27
     * @param: []
     * @return: java.util.List<com.mok.securityframework.entity.Role>
     **/
    @Override
    public List<RoleEntity> getAllActiveRoles() {

        //创建条件查询
        LambdaQueryWrapper<RoleEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoleEntity::getStatus, 1)
                .eq(RoleEntity::getIsDeleted, 0)
                .orderByAsc(RoleEntity::getSort);
        return baseMapper.selectList(wrapper);
    }

    /**
     * @description: 根绝用户 ID 获取角色列表
     * @author: JN
     * @date: 2026/1/2 11:31
     * @param: [userId]
     * @return: java.util.List<com.mok.securityframework.entity.Role>
     **/
    @Override
    public List<RoleEntity> getRolesByUserId(String userId) {
        return baseMapper.selectRolesByUserId(userId);
    }

    /**
     * @description: 通过 userId 给某一用户添加角色
     * @author: JN
     * @date: 2026/1/2 11:33
     * @param: [userId, roleIds]
     * @return: boolean
     **/
    @Override
    //@Transactional 注解 : 开启事务
    //  作用 : 方法内的数据库操作在一个事务中执行
    @Transactional(rollbackFor = Exception.class)
    public boolean assignUserRoles(String userId, List<String> roleIds) {
        // 修改：添加参数校验
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        // 修改：检查用户是否存在
        // 这里需要UserService，暂时注释
        // if (!userService.existsById(userId)) {
        //     throw new BusinessException("用户不存在");
        // }

        // 检查角色是否存在且有效
        if (roleIds != null && !roleIds.isEmpty()) {
            for (String roleId : roleIds) {
                RoleEntity role = getById(roleId);
                if (role == null || role.getIsDeleted() == 1) {
                    throw new BusinessException("角色不存在或已被删除: " + roleId);
                }
                if (role.getStatus() != 1) {
                    throw new BusinessException("角色已被禁用: " + role.getRoleName());
                }
            }
        }

        // 先删除用户现有角色
        LambdaQueryWrapper<UserRoleEntity> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(UserRoleEntity::getUserId, userId);
        userRoleMapper.delete(deleteWrapper);

        // 添加新角色
        if (roleIds != null && !roleIds.isEmpty()) {
            List<UserRoleEntity> userRoleEntities = roleIds.stream()
                    .map(roleId ->
                            new UserRoleEntity()
                                    .setId(IdUtil.simpleUUID())
                                    .setUserId(userId)
                                    .setRoleId(roleId)
                                    .setCreateTime(LocalDateTime.now())
                    )
                    .toList();

            try {
                userRoleMapper.insertBatch(userRoleEntities);
            } catch (Exception e) {
                // 回退方案：逐条插入
                log.warn("批量插入失败，转为逐条插入", e);
                for (UserRoleEntity userRole : userRoleEntities) {
                    userRoleMapper.insert(userRole);
                }
            }
        }

        // 修改：记录操作日志
        log.info("为用户 {} 分配角色: {}", userId, roleIds);
        return true;
    }

    /**
     * @description: 删除角色 >>> 逻辑删除
     * @author: JN
     * @date: 2026/1/2 12:14
     * @param: [roleId]
     * @return: boolean
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRole(String roleId) {
        // 修改：添加参数校验
        if (roleId == null || roleId.trim().isEmpty()) {
            throw new IllegalArgumentException("角色ID不能为空");
        }

        // 检查角色是否存在
        RoleEntity roleEntity = getById(roleId);
        if (roleEntity == null) {
            throw new BusinessException("角色不存在");
        }

        // 修改：检查是否为超级管理员角色
        if ("ROLE_ADMIN".equals(roleEntity.getRoleCode())) {
            throw new BusinessException("不能删除超级管理员角色");
        }
        //检查当前角色是否被使用
        LambdaQueryWrapper<UserRoleEntity> wrapper = new LambdaQueryWrapper<>();
        //创建查询条件
        wrapper.eq(UserRoleEntity::getRoleId, roleId);
        //执行查询并统计数量 .selectCount(wrapper)
        Long roleUseCount = userRoleMapper.selectCount(wrapper);
        if (roleUseCount > 0) {
            throw new BusinessException("角色正在被使用,无法删除");
        }
        //逻辑删除
        RoleEntity roleEntitySave = new RoleEntity();
        roleEntitySave.setId(roleId);
        //逻辑删除 : 1=已删除,0=未删除
        roleEntitySave.setIsDeleted(1);
        return removeById(roleEntitySave);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createRole(RoleDTO roleDTO) {
        if (roleDTO == null) {
            throw new BusinessException("角色信息不能为空");
        }

        UserEntity currentUserEntity = securityUtils.getCurrentUser();
        if (currentUserEntity == null) {
            throw new BusinessException("用户未登录");
        }
        //检查角色编码是否存在
        Long count = lambdaQuery()
                .eq(RoleEntity::getRoleCode, roleDTO.getRoleCode())
                .count();
        if (count > 0) {
            throw new BusinessException("角色编码已存在");
        }
        // 修改：验证权限ID的有效性
        if (roleDTO.getPermissionIds() != null && !roleDTO.getPermissionIds().isEmpty()) {
            List<PermissionEntity> permissions = permissionMapper.selectBatchIds(roleDTO.getPermissionIds());
            if (permissions.size() != roleDTO.getPermissionIds().size()) {
                throw new BusinessException("部分权限不存在");
            }
        }
        RoleEntity roleEntity = new RoleEntity();
        BeanUtils.copyProperties(roleDTO, roleEntity);
        roleEntity.setId(IdUtil.simpleUUID());
        //保存角色
        save(roleEntity);
        if (roleDTO.getPermissionIds() != null && !roleDTO.getPermissionIds().isEmpty()) {
            assignRolePermissions(roleEntity.getId(), roleDTO.getPermissionIds());
        }
        return roleEntity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRole(RoleDTO roleDTO) {
        RoleEntity roleEntity = getById(roleDTO.getId());
        if (roleEntity == null) {
            throw new BusinessException("角色不存在");
        }

        // 检查角色编码是否重复（排除自己）
        Long count = lambdaQuery()
                .eq(RoleEntity::getRoleCode, roleDTO.getRoleCode())
                .ne(RoleEntity::getId, roleDTO.getId())
                .eq(RoleEntity::getIsDeleted, 0)
                .count();

        if (count > 0) {
            throw new BusinessException("角色编码已存在");
        }

        // 更新角色
        BeanUtils.copyProperties(roleDTO, roleEntity);
        updateById(roleEntity);

        // 更新权限
        if (roleDTO.getPermissionIds() != null) {
            assignRolePermissions(roleEntity.getId(), roleDTO.getPermissionIds());
        }

        return true;
    }

    @Override
    public List<String> getRolePermissionIds(String roleId) {
        LambdaQueryWrapper<RolePermissionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(RolePermissionEntity::getPermissionId)
                .eq(RolePermissionEntity::getRoleId, roleId);

        List<RolePermissionEntity> rolePermissionEntities = rolePermissionMapper.selectList(wrapper);
        return rolePermissionEntities.stream()
                .map(RolePermissionEntity::getPermissionId)
                .toList();
    }

    @Override
    public List<PermissionEntity> getRolePermissions(String roleId) {
        return permissionMapper.selectPermissionsByRoleId(roleId);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRolePermissions(String roleId, List<String> permissionIds) {
        // 修改：添加参数校验
        if (roleId == null || roleId.trim().isEmpty()) {
            throw new IllegalArgumentException("角色ID不能为空");
        }

        // 检查角色是否存在
        RoleEntity role = getById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        // 修改：验证权限ID的有效性
        if (permissionIds != null && !permissionIds.isEmpty()) {
            List<PermissionEntity> permissions = permissionMapper.selectBatchIds(permissionIds);
            if (permissions.size() != permissionIds.size()) {
                throw new BusinessException("部分权限不存在");
            }
        }

        // 删除现有权限
        LambdaQueryWrapper<RolePermissionEntity> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(RolePermissionEntity::getRoleId, roleId);
        rolePermissionMapper.delete(deleteWrapper);

        // 添加新权限
        if (permissionIds != null && !permissionIds.isEmpty()) {
            List<RolePermissionEntity> rolePermissionEntities = permissionIds.stream()
                    .map(permissionId -> new RolePermissionEntity()
                            .setId(IdUtil.simpleUUID())
                            .setRoleId(roleId)
                            .setPermissionId(permissionId)
                            .setCreateTime(LocalDateTime.now()))
                    .toList();

            try {
                rolePermissionMapper.insertBatch(rolePermissionEntities);
                //清空redis里缓存的权限
                permissionCacheService.clearAllPermissionCache();
            } catch (Exception e) {
                log.error("批量插入角色权限失败", e);
                throw new BusinessException("分配权限失败");
            }
        }

        log.info("角色{}权限分配完成，分配权限数：{}", roleId,
                permissionIds != null ? permissionIds.size() : 0);
    }

    // 判断是否是管理员角色
    private boolean isAdminRole(List<String> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return false;
        }
        // 查询这些角色中是否有管理员角色编码
        LambdaQueryWrapper<RoleEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(RoleEntity::getId, roleIds)
                .eq(RoleEntity::getRoleCode, "ROLE_ADMIN")
                .eq(RoleEntity::getStatus, 1)
                .eq(RoleEntity::getIsDeleted, 0);
        return baseMapper.selectCount(queryWrapper) > 0;
    }

}
