package com.mok.framework.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mok.framework.common.PageParam;
import com.mok.framework.common.PageResult;
import com.mok.framework.model.dto.DepartmentDTO;
import com.mok.framework.model.entity.DepartmentEntity;

import java.util.List;

/**
 * @description: 部门 Service 接口
 * @author: mok
 * @date: 2026/7/15
 **/
public interface DepartmentService extends IService<DepartmentEntity> {

    /**
     * 分页查询部门列表
     */
    PageResult<DepartmentEntity> getPageList(PageParam param);

    /**
     * 获取部门树
     */
    List<DepartmentDTO> getDeptTree();

    /**
     * 根据ID获取部门
     */
    DepartmentEntity getDeptById(String id);

    /**
     * 新增部门
     */
    String createDept(DepartmentDTO dto);

    /**
     * 更新部门
     */
    boolean updateDept(DepartmentDTO dto);

    /**
     * 删除部门（检查子部门+用户关联）
     */
    boolean deleteDept(String id);

    /**
     * 获取当前用户部门范围内的部门树（非管理员只返回所在部门+子部门）
     */
    List<DepartmentDTO> getScopedDeptTree(String userId);

    /**
     * 获取用户在部门树上的可见部门ID列表（包含子部门，用于数据筛选）
     */
    List<String> getDeptScopeIds(String userId);

    /**
     * 根据部门ID列表批量查询部门名称映射
     */
    java.util.Map<String, String> getDeptNameMap(java.util.Collection<String> deptIds);
}
