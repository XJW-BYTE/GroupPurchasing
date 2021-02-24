package com.share.GroupPurchasing.utils;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.share.GroupPurchasing.model.PageRequest;
import com.share.GroupPurchasing.model.PageResult;

import java.util.List;

public class PageUtil {

    public static PageResult getPageResult(PageRequest pageRequest, PageInfo<?> pageInfo) {
        PageResult pageResult = new PageResult();
        pageResult.setPageNum(pageInfo.getPageNum());
        pageResult.setPageSize(pageInfo.getPageSize());
        pageResult.setTotalSize(pageInfo.getTotal());
        pageResult.setTotalPages(pageInfo.getPages());
        pageResult.setContent(pageInfo.getList());
        return pageResult;
    }


}
