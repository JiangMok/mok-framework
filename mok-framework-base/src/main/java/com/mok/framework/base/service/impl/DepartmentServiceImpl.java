package com.mok.framework.base.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mok.framework.base.mapper.DepartmentMapper;
import com.mok.framework.base.mapper.UserMapper;
import com.mok.framework.base.service.DepartmentService;
import com.mok.framework.base.service.RoleService;
import com.mok.framework.common.BusinessException;
import com.mok.framework.common.PageParam;
import com.mok.framework.common.PageResult;
import com.mok.framework.common.utils.LogUtils;
import com.mok.framework.model.dto.DepartmentDTO;
import com.mok.framework.model.entity.DepartmentEntity;
import com.mok.framework.model.entity.RoleEntity;
import com.mok.framework.model.entity.UserEntity;
import org.slf4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @description: 部门 Service 实现
 * @author: mok
 * @date: 2026/7/15
 **/
@Service
public class DepartmentServiceImpl
        extends ServiceImpl<DepartmentMapper, DepartmentEntity>
        implements DepartmentService {

    private static final Logger log = LogUtils.getLogger(DepartmentServiceImpl.class);

    private final UserMapper userMapper;
    private final RoleService roleService;

    public DepartmentServiceImpl(UserMapper userMapper, RoleService roleService) {
        this.userMapper = userMapper;
        this.roleService = roleService;
    }

    @Override
    public PageResult<DepartmentEntity> getPageList(PageParam param) {
        Page<DepartmentEntity> page = new Page<>(param.getPageNum(), param.getPageSize());
        LambdaQueryWrapper<DepartmentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DepartmentEntity::getIsDeleted, 0);

        // 关键词搜索：部门名称 / 部门编码
        if (StringUtils.hasText(param.getKeyword())) {
            wrapper.and(w -> w
                    .like(DepartmentEntity::getDeptName, param.getKeyword())
                    .or()
                    .like(DepartmentEntity::getDeptCode, param.getKeyword()));
        }

        // 状态筛选
        if (param.get("status") != null) {
            wrapper.eq(DepartmentEntity::getStatus, param.get("status"));
        }

        // 排序
        wrapper.orderByAsc(DepartmentEntity::getSort)
                .orderByDesc(DepartmentEntity::getCreateTime);

        IPage<DepartmentEntity> result = baseMapper.selectPage(page, wrapper);
        return PageResult.fromIPage(result);
    }

    @Override
    public List<DepartmentDTO> getDeptTree() {
        List<DepartmentEntity> allDepts = lambdaQuery()
                .eq(DepartmentEntity::getIsDeleted, 0)
                .orderByAsc(DepartmentEntity::getSort)
                .orderByDesc(DepartmentEntity::getCreateTime)
                .list();

        return buildDeptTree(allDepts, "0");
    }

    @Override
    public DepartmentEntity getDeptById(String id) {
        return baseMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createDept(DepartmentDTO dto) {
        if (dto == null) {
            throw new BusinessException("部门信息不能为空");
        }

        // 检查部门编码是否重复
        Long count = lambdaQuery()
                .eq(DepartmentEntity::getDeptCode, dto.getDeptCode())
                .eq(DepartmentEntity::getIsDeleted, 0)
                .count();
        if (count > 0) {
            throw new BusinessException("部门编码已存在");
        }

        DepartmentEntity entity = new DepartmentEntity();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(IdUtil.simpleUUID());

        // 计算 ancestors
        if (!"0".equals(dto.getParentId())) {
            DepartmentEntity parent = getDeptById(dto.getParentId());
            if (parent == null) {
                throw new BusinessException("父部门不存在");
            }
            entity.setAncestors(parent.getAncestors() + "," + parent.getId());
        } else {
            entity.setAncestors("0");
        }

        save(entity);
        log.info("部门创建成功: {} ({})", entity.getDeptName(), entity.getId());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDept(DepartmentDTO dto) {
        if (dto == null || dto.getId() == null) {
            throw new BusinessException("部门信息不能为空");
        }

        DepartmentEntity entity = getDeptById(dto.getId());
        if (entity == null) {
            throw new BusinessException("部门不存在");
        }

        // 检查编码是否重复（排除自己）
        Long count = lambdaQuery()
                .eq(DepartmentEntity::getDeptCode, dto.getDeptCode())
                .ne(DepartmentEntity::getId, dto.getId())
                .eq(DepartmentEntity::getIsDeleted, 0)
                .count();
        if (count > 0) {
            throw new BusinessException("部门编码已存在");
        }

        // 不能把父部门设为自己或自己的子部门
        if (!"0".equals(dto.getParentId())) {
            if (dto.getId().equals(dto.getParentId())) {
                throw new BusinessException("父部门不能是自己");
            }
            DepartmentEntity parent = getDeptById(dto.getParentId());
            if (parent == null) {
                throw new BusinessException("父部门不存在");
            }
            // 检查是否把子部门设为父部门
            if (parent.getAncestors() != null && parent.getAncestors().contains(dto.getId())) {
                throw new BusinessException("不能将父部门设置为自己的子部门");
            }
        }

        String oldParentId = entity.getParentId();
        BeanUtils.copyProperties(dto, entity);

        // 如果父部门变了，重新计算 ancestors（同时更新所有子部门的 ancestors）
        if (!oldParentId.equals(dto.getParentId())) {
            if (!"0".equals(dto.getParentId())) {
                DepartmentEntity parent = getDeptById(dto.getParentId());
                entity.setAncestors(parent.getAncestors() + "," + parent.getId());
            } else {
                entity.setAncestors("0");
            }
            // 更新所有子部门的 ancestors
            updateChildrenAncestors(entity.getId(), entity.getAncestors() + "," + entity.getId());
        }

        boolean result = updateById(entity);
        log.info("部门更新成功: {} ({})", entity.getDeptName(), entity.getId());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDept(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new BusinessException("部门ID不能为空");
        }

        DepartmentEntity entity = getDeptById(id);
        if (entity == null) {
            throw new BusinessException("部门不存在");
        }

        // 检查是否有子部门
        Long childrenCount = lambdaQuery()
                .eq(DepartmentEntity::getParentId, id)
                .eq(DepartmentEntity::getIsDeleted, 0)
                .count();
        if (childrenCount > 0) {
            throw new BusinessException("存在子部门，无法删除");
        }

        // 检查是否有用户关联
        Long userCount = userMapper.selectCount(
                new LambdaQueryWrapper<UserEntity>()
                        .eq(UserEntity::getDeptId, id)
                        .eq(UserEntity::getIsDeleted, 0)
        );
        if (userCount > 0) {
            throw new BusinessException("该部门下还有" + userCount + "个用户，无法删除");
        }

        // 逻辑删除
        DepartmentEntity deleteEntity = new DepartmentEntity();
        deleteEntity.setId(id);
        deleteEntity.setIsDeleted(1);
        boolean result = removeById(deleteEntity);
        log.info("部门删除成功: {} ({})", entity.getDeptName(), id);
        return result;
    }

    /**
     * 递归构建部门树
     */
    private List<DepartmentDTO> buildDeptTree(List<DepartmentEntity> deptList, String parentId) {
        List<DepartmentDTO> tree = new ArrayList<>();
        for (DepartmentEntity dept : deptList) {
            if (dept.getParentId().equals(parentId)) {
                DepartmentDTO dto = new DepartmentDTO();
                BeanUtils.copyProperties(dept, dto);
                // 递归构建子节点
                List<DepartmentDTO> children = buildDeptTree(deptList, dept.getId());
                if (!children.isEmpty()) {
                    dto.setChildren(children);
                }
                tree.add(dto);
            }
        }
        return tree;
    }

    /**
     * 递归更新所有子孙部门的 ancestors
     */
    private void updateChildrenAncestors(String parentId, String newAncestorPrefix) {
        List<DepartmentEntity> children = lambdaQuery()
                .eq(DepartmentEntity::getParentId, parentId)
                .eq(DepartmentEntity::getIsDeleted, 0)
                .list();

        for (DepartmentEntity child : children) {
            child.setAncestors(newAncestorPrefix);
            updateById(child);
            // 递归更新子部门
            updateChildrenAncestors(child.getId(), newAncestorPrefix + "," + child.getId());
        }
    }

    // ==================== 数据权限范围 ====================

    @Override
    public List<DepartmentDTO> getScopedDeptTree(String userId) {
        List<DepartmentEntity> allDepts = lambdaQuery()
                .eq(DepartmentEntity::getIsDeleted, 0)
                .orderByAsc(DepartmentEntity::getSort)
                .list();

        if (isAdmin(userId)) {
            return buildDeptTree(allDepts, "0");
        }

        UserEntity user = userMapper.selectById(userId);
        if (user == null || user.getDeptId() == null || user.getDeptId().isEmpty()) {
            return Collections.emptyList();
        }

        DepartmentEntity userDept = getDeptById(user.getDeptId());
        if (userDept == null) {
            return Collections.emptyList();
        }

        String ancestorPrefix = userDept.getAncestors() + "," + userDept.getId();
        List<DepartmentEntity> scoped = allDepts.stream()
                .filter(d -> d.getId().equals(userDept.getId())
                        || d.getAncestors().startsWith(ancestorPrefix))
                .toList();

        return buildScopedTree(scoped, userDept.getId());
    }

    @Override
    public List<String> getDeptScopeIds(String userId) {
        if (isAdmin(userId)) {
            return lambdaQuery()
                    .eq(DepartmentEntity::getIsDeleted, 0)
                    .list()
                    .stream().map(DepartmentEntity::getId).toList();
        }

        UserEntity user = userMapper.selectById(userId);
        if (user == null || user.getDeptId() == null || user.getDeptId().isEmpty()) {
            return Collections.emptyList();
        }

        DepartmentEntity userDept = getDeptById(user.getDeptId());
        if (userDept == null) {
            return Collections.emptyList();
        }

        String prefix = userDept.getAncestors() + "," + userDept.getId();
        return lambdaQuery()
                .eq(DepartmentEntity::getIsDeleted, 0)
                .list()
                .stream()
                .filter(d -> d.getId().equals(userDept.getId())
                        || d.getAncestors().startsWith(prefix))
                .map(DepartmentEntity::getId)
                .toList();
    }

    @Override
    public Map<String, String> getDeptNameMap(Collection<String> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) return Collections.emptyMap();
        return lambdaQuery()
                .in(DepartmentEntity::getId, deptIds)
                .eq(DepartmentEntity::getIsDeleted, 0)
                .list()
                .stream()
                .collect(Collectors.toMap(DepartmentEntity::getId, DepartmentEntity::getDeptName));
    }

    /**
     * 判断是否为超级管理员
     */
    private boolean isAdmin(String userId) {
        try {
            List<RoleEntity> roles = roleService.getRolesByUserId(userId);
            return roles.stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getRoleCode()));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 构建以指定部门为根的子树（该部门作为根节点）
     */
    private List<DepartmentDTO> buildScopedTree(List<DepartmentEntity> deptList, String rootId) {
        DepartmentEntity root = deptList.stream()
                .filter(d -> d.getId().equals(rootId))
                .findFirst().orElse(null);
        if (root == null) return Collections.emptyList();

        DepartmentDTO rootDto = new DepartmentDTO();
        BeanUtils.copyProperties(root, rootDto);
        List<DepartmentDTO> children = buildDeptTree(deptList, root.getId());
        if (!children.isEmpty()) {
            rootDto.setChildren(children);
        }
        List<DepartmentDTO> result = new ArrayList<>();
        result.add(rootDto);
        return result;
    }
}
