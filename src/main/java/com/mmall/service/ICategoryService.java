package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;
import java.util.List;

public interface ICategoryService {

    //添加分类
    ServerResponse addCategory(String categoryName, Integer parentId);

    //更新分类名称
    ServerResponse updateCategoryName(Integer categoryId,String categoryName);

    //查询子节点的分类信息
    ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId);

    //查询当前节点及其子节点的id
    ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId);
}




