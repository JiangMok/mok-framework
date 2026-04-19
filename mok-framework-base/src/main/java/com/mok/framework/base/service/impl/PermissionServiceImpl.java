package com.mok.baseframe.base.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mok.baseframe.base.service.PermissionService;
import com.mok.baseframe.common.BusinessException;
import com.mok.baseframe.common.PageParam;
import com.mok.baseframe.common.PageResult;
import com.mok.baseframe.dao.PermissionMapper;
import com.mok.baseframe.dao.RoleMapper;
import com.mok.baseframe.dao.RolePermissionMapper;
import com.mok.baseframe.dto.PermissionDTO;
import com.mok.baseframe.entity.PermissionEntity;
import com.mok.baseframe.entity.RoleEntity;
import com.mok.baseframe.entity.RolePermissionEntity;
import com.mok.baseframe.security.service.PermissionCacheService;
import com.mok.baseframe.common.utils.LogUtils;
import com.mok.baseframe.security.utils.SecurityUtils;
import org.slf4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * @description: 权限 service 接口实现类 (需要学习)
 * @author: JN
 * @date: 2026/1/2
 */

@Service

@Transactional
public class PermissionServiceImpl
        extends ServiceImpl<PermissionMapper, PermissionEntity>
        implements PermissionService {
    private static final Logger log = LogUtils.getLogger(PermissionServiceImpl.class);
    private final PermissionMapper permissionMapper;
    private final RoleMapper roleMapper;
    private final SecurityUtils securityUtils;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionCacheService permissionCacheService;

    public PermissionServiceImpl(PermissionMapper permissionMapper,
                                 RoleMapper roleMapper,
                                 SecurityUtils securityUtils,
                                 RolePermissionMapper rolePermissionMapper,
                                 PermissionCacheService permissionCacheService) {
        this.permissionMapper = permissionMapper;
        this.roleMapper = roleMapper;
        this.securityUtils = securityUtils;
        this.rolePermissionMapper = rolePermissionMapper;
        this.permissionCacheService = permissionCacheService;
    }

    @Override
    public PageResult<PermissionEntity> getPageList(PageParam param) {
        Page<PermissionEntity> page = new Page<>(param.getPageNum(), param.getPageSize());
        LambdaQueryWrapper<PermissionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PermissionEntity::getIsDeleted, 0);
        if (param.getParams().get("type") != null) {
            wrapper.eq(PermissionEntity::getType, param.getParams().get("type"));
        }
        //按状态查询
        if (param.get("status") != null) {
            wrapper.eq(PermissionEntity::getStatus, param.get("status"));
        }
        //根据权限名搜索或者权限编码查询
        if (StringUtils.hasText(param.getKeyword())) {
            wrapper.like(PermissionEntity::getPermissionName, param.getKeyword())
                    .or().like(PermissionEntity::getPermissionCode, param.getKeyword());
        }
        if (param.getOrderBy() != null) {
            if ("asc".equalsIgnoreCase(param.getOrder())) {
                wrapper.orderByAsc(PermissionEntity::getCreateTime);
            } else {
                wrapper.orderByDesc(PermissionEntity::getCreateTime);
            }
        } else {
            //默认排序:先按sort升序,再按createTime降序
            wrapper.orderByAsc(PermissionEntity::getSort).orderByDesc(PermissionEntity::getCreateTime);
        }
        //执行分页查询
        // 修改：添加性能监控日志
        long startTime = System.currentTimeMillis();
        IPage<PermissionEntity> result = baseMapper.selectPage(page, wrapper);
        long endTime = System.currentTimeMillis();

        // 记录查询耗时，用于性能优化
        if (log.isDebugEnabled()) {
            long time = endTime - startTime;
            log.debug("权限分页查询耗时: " + time + " ms");
        }
        //转换为自定义的分页结果
        return PageResult.fromIPage(result);
    }

    @Override
    public List<PermissionEntity> getPermissionsByUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new BusinessException("用户ID不能为空");
        }
        return permissionMapper.selectPermissionsByUserId(userId);
    }

    @Override
    public PermissionEntity getById(String id) {
        return baseMapper.selectById(id);
    }


    @Override
    public List<Map<String, Object>> getMenuTreeByUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        List<PermissionEntity> permissionEntities = permissionMapper.selectPermissionsByUserId(userId);
        //筛选菜单类型且状态正常的菜单
        List<PermissionEntity> menus = permissionEntities.stream()
                .filter(p ->
                        p.getType() == 1
                                && p.getStatus() == 1
                                && p.getIsDeleted() == 0
                                && p.getVisible() == 1)
                //先比较 sort ,然后在比较 createTime
                .sorted(Comparator.comparingInt(PermissionEntity::getSort).thenComparing(PermissionEntity::getCreateTime))
                .toList();
        return buildMenuTree(menus, "0");
        //上面的代码和下边是一样的
//        List<Permission> menus = new ArrayList<>();
//            for (Permission permission : permissions) {
//                // 判断条件：菜单类型(type=1)、状态正常(status=1)、未删除(isDeleted=0)、可见(visible=1)
//                if (permission.getType() == 1 &&
//                    permission.getStatus() == 1 &&
//                    permission.getIsDeleted() == 0 &&
//                    permission.getVisible() == 1) {
//                    menus.add(permission);
//                }
//            }
//
//            // 3. 对菜单进行排序
//            // 先按sort升序排序，如果sort相同则按id降序排序
//            menus.sort(new Comparator<Permission>() {
//                @Override
//                public int compare(Permission a, Permission b) {
//                    // 比较sort字段
//                    int sortCompare = Integer.compare(a.getSort(), b.getSort());
//                    if (sortCompare != 0) {
//                        return sortCompare;  // sort不相等，按sort升序
//                    } else {
//                        // sort相等，按id降序
//                        return Long.compare(b.getId(), a.getId());
//                    }
//                }
//            });
//        return buildMenuTree(menus, 0L);
    }

    @Override
    public List<PermissionEntity> getAllPermission() {
        return permissionMapper.selectAllPermissions();
    }

    @Override
    public List<Map<String, Object>> getPermissionTree() {
        List<PermissionEntity> permissionEntities = lambdaQuery()
                .eq(PermissionEntity::getIsDeleted, 0)
                .orderByAsc(PermissionEntity::getSort)
                .orderByDesc(PermissionEntity::getCreateTime)
                .list();
        return buildPermissionTree(permissionEntities, "0");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePermission(String permissionId) {
        // 修改：添加参数校验和事务回滚配置
        if (permissionId == null || permissionId.trim().isEmpty()) {
            throw new BusinessException("权限ID不能为空");
        }

        // 检查是否存在
        PermissionEntity permissionEntity = getById(permissionId);
        if (permissionEntity == null) {
            throw new BusinessException("权限不存在");
        }
        // 检查是否有子权限
        Long childrenCount = lambdaQuery()
                .eq(PermissionEntity::getParentId, permissionId)
                .eq(PermissionEntity::getIsDeleted, 0)
                .count();

        if (childrenCount > 0) {
            throw new BusinessException("存在子权限，无法删除");
        }
        // 逻辑删除
        PermissionEntity permissionEntityDelete = new PermissionEntity();
        permissionEntityDelete.setId(permissionId);
        permissionEntityDelete.setIsDeleted(1);
        //逻辑删除
        return removeById(permissionEntityDelete);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createPermission(PermissionDTO permissionDTO) {
        // 修改：添加参数校验和业务验证
        if (permissionDTO == null) {
            throw new BusinessException("权限信息不能为空");
        }
        // 验证父级权限是否存在（如果不是根节点）
        if (!"0".equals(permissionDTO.getParentId())) {
            PermissionEntity parentPermission = getById(permissionDTO.getParentId());
            if (parentPermission == null) {
                throw new BusinessException("父级权限不存在");
            }
            // 修改：检查父级权限类型是否匹配（比如按钮不能作为菜单的父级）
//            if (permissionDTO.getType() != null && parentPermission.getType() != null) {
//                // 类型验证逻辑...
//            }
        }
        // 检查权限编码是否重复
        Long count = lambdaQuery()
                .eq(PermissionEntity::getPermissionCode, permissionDTO.getPermissionCode())
                .eq(PermissionEntity::getIsDeleted, 0)
                .count();
        if (count > 0) {
            throw new BusinessException("权限编码已存在");
        }
        PermissionEntity permissionEntity = new PermissionEntity();
        BeanUtils.copyProperties(permissionDTO, permissionEntity);
        permissionEntity.setId(IdUtil.simpleUUID());
        save(permissionEntity);
        //查询当前用户的角色
        String userId = securityUtils.getCurrentUserId();
        List<RoleEntity> roleEntityList = roleMapper.selectRolesByUserId(userId);
        //创建角色权限关联的list,方便后续批量插入
        List<RolePermissionEntity> rolePermissionEntityList = new ArrayList<>();
        //判断是否是超级管理员角色
        boolean isAdminRole = roleEntityList.stream()
                .anyMatch(role -> "ROLE_ADMIN".equals(role.getRoleCode()));
        if (!isAdminRole) {
            RolePermissionEntity rolePermissionEntity = new RolePermissionEntity();
            rolePermissionEntity.setId(IdUtil.simpleUUID());
            rolePermissionEntity.setPermissionId(permissionEntity.getId());
            //超级管理员橘色的的ID = 1
            rolePermissionEntity.setRoleId("1");
            rolePermissionEntityList.add(rolePermissionEntity);
        }
        for (RoleEntity roleEntity : roleEntityList) {
            RolePermissionEntity rolePermissionEntity = new RolePermissionEntity();
            rolePermissionEntity.setId(IdUtil.simpleUUID());
            rolePermissionEntity.setRoleId(roleEntity.getId());
            rolePermissionEntity.setPermissionId(permissionEntity.getId());
            rolePermissionEntityList.add(rolePermissionEntity);
        }
        //批量插入
        try {
            rolePermissionMapper.insertBatch(rolePermissionEntityList);
        } catch (Exception e) {
            // 回退方案：逐条插入
            log.warn("批量插入失败，转为逐条插入", e);
            for (RolePermissionEntity rolePermissionEntity : rolePermissionEntityList) {
                rolePermissionMapper.insert(rolePermissionEntity);
            }
        }
        //清空redis里缓存的权限
        permissionCacheService.clearAllPermissionCache();
        return permissionEntity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePermission(PermissionDTO permissionDTO) {
        PermissionEntity permissionEntity = getById(permissionDTO.getId());
        if (permissionEntity == null) {
            throw new BusinessException("权限不存在");
        }
        // 检查权限编码是否重复（排除自己）
        Long count = lambdaQuery()
                .eq(PermissionEntity::getPermissionCode, permissionDTO.getPermissionCode())
                .ne(PermissionEntity::getId, permissionDTO.getId())
                .eq(PermissionEntity::getIsDeleted, 0)
                .count();

        if (count > 0) {
            throw new BusinessException("权限编码已存在");
        }
        BeanUtils.copyProperties(permissionDTO, permissionEntity);
        return updateById(permissionEntity);
    }

    @Override
    public List<PermissionEntity> getApiPermissions(String userId) {
        // 出入固定值3 : 类型3为接口权限
        return selectPermissionsByUserIdByType(3, userId);
    }

    @Override
    public List<PermissionEntity> selectPermissionsByUserIdByType(Integer type, String userId) {
        return baseMapper.selectPermissionsByUserIdByType(userId, type);
    }

    @Override
    public List<Map<String, Object>> getMenuTree() {
        List<PermissionEntity> menus = lambdaQuery()
                .eq(PermissionEntity::getType, 1)
                .eq(PermissionEntity::getIsDeleted, 0)
                .eq(PermissionEntity::getStatus, 1)
                .orderByAsc(PermissionEntity::getSort)
                .list();

        return buildMenuTree(menus, "0");
    }

    @Override
    public List<PermissionEntity> selectPermissionsByRoleId(String roleId) {
        return permissionMapper.selectPermissionsByRoleId(roleId);
    }

    /**
     * @description: 构建菜单树
     * @author: JN
     * @date: 2026/1/2 13:48
     * @param: [menus, parentId]
     * @return: java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     **/
    private List<Map<String, Object>> buildMenuTree(
            List<PermissionEntity> menus,
            String parentId) {
        //创建结果列表
        //  作用 : 存储当前层级的所有菜单节点
        //  类型 : ArrayList,动态数组,便于添加和便利
        List<Map<String, Object>> tree = new ArrayList<>();
        //遍历所有菜单项
        //  使用增强 for 遍历 menus 列表
        for (PermissionEntity menu : menus) {
            if (menu.getParentId().equals(parentId)) {
                //创建菜单节点 map
                //  作用 : 将permission 对象转换为 map,便于前端使用
                Map<String, Object> node = new HashMap<>();
                //设置菜单的节点的各个属性
                node.put("id", menu.getId());
                node.put("name", menu.getPermissionName());
                node.put("code", menu.getPermissionCode());
                node.put("icon", menu.getIcon());
                node.put("path", menu.getPath());
                node.put("component", menu.getComponent());
                node.put("sort", menu.getSort());
                //递归构建子节点
                //  作用 : 查找当前菜单的所有子菜单
                //  参数 :  menus:完整的菜单列表,menu.getId():当前菜单的ID,作为子菜单的 parentId
                List<Map<String, Object>> children = buildMenuTree(menus, menu.getId());
                //如果有子菜单,添加到当前节点
                if (!children.isEmpty()) {
                    //将子菜单列表放入到当前节点的children键中
                    node.put("children", children);
                }
                //将当前节点添加到树种
                tree.add(node);
            }
        }
        //返回构建好的树
        //  如果没有符合条件的菜单,返回空 ArrayList
        return tree;
    }

    /**
     * @description: 构建权限树（包含所有类型）
     * @author: JN
     * @date: 2026/1/5 14:25
     * @param: [permissions, parentId]
     * @return: java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     **/
    private List<Map<String, Object>> buildPermissionTree(List<PermissionEntity> permissionEntities, String parentId) {
        List<Map<String, Object>> tree = new ArrayList<>();
        for (PermissionEntity permissionEntity : permissionEntities) {
            if (permissionEntity.getParentId().equals(parentId)) {
                Map<String, Object> node = new HashMap<>();
                node.put("id", permissionEntity.getId());
                node.put("name", permissionEntity.getPermissionName());
                node.put("code", permissionEntity.getPermissionCode());
                node.put("type", permissionEntity.getType());
                node.put("description", permissionEntity.getDescription());
                node.put("icon", permissionEntity.getIcon());
                node.put("path", permissionEntity.getPath());
                node.put("component", permissionEntity.getComponent());
                node.put("sort", permissionEntity.getSort());
                node.put("visible", permissionEntity.getVisible());
                node.put("status", permissionEntity.getStatus());
                node.put("createTime", permissionEntity.getCreateTime());
                // 递归构建子节点
                List<Map<String, Object>> children = buildPermissionTree(permissionEntities, permissionEntity.getId());
                if (!children.isEmpty()) {
                    node.put("children", children);
                }
                tree.add(node);
            }
        }
        return tree;
    }

    /**
     * 递归构建树（优化版本）
     */
    private List<Map<String, Object>> buildTreeRecursive(
            Map<String, List<PermissionEntity>> menuMap,
            String parentId) {
        List<Map<String, Object>> result = new ArrayList<>();

        List<PermissionEntity> children = menuMap.get(parentId);
        if (children == null || children.isEmpty()) {
            return result;
        }

        // 对子节点排序
        List<PermissionEntity> sortedChildren = children.stream()
                .sorted(Comparator.comparingInt(PermissionEntity::getSort)
                        .thenComparing(PermissionEntity::getCreateTime))
                .toList();

        for (PermissionEntity menu : sortedChildren) {
            Map<String, Object> node = new HashMap<>();
            node.put("id", menu.getId());
            node.put("name", menu.getPermissionName());
            node.put("code", menu.getPermissionCode());
            node.put("icon", menu.getIcon());
            node.put("path", menu.getPath());
            node.put("component", menu.getComponent());
            node.put("sort", menu.getSort());
            node.put("type", menu.getType());  // 修改：添加类型字段

            // 递归构建子节点
            List<Map<String, Object>> childrenNodes = buildTreeRecursive(menuMap, menu.getId());
            if (!childrenNodes.isEmpty()) {
                node.put("children", childrenNodes);
            }

            result.add(node);
        }

        return result;
    }
}


