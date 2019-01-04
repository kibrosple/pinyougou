package com.pinyougou.sellergoods.service;

import java.util.List;

import com.pinyougou.pojo.TbBrand;

import entity.PageResult;

/**
 * 品牌接口
 * @author Administrator
 *
 */
public interface BrandService {
	//	查询所有
	public List<TbBrand> findAll();
	//通过页面查询	
	public PageResult findPage(int pageNum,int pageSize);
	//	添加品牌信息
	public void add(TbBrand brand);
	// 通过id找到用户
	/**
	 * 根据ID查询实体
	 * @param id
	 * @return
	 */
	public TbBrand findOne(Long id);
	
	/**
	 * 修改
	 * @param brand
	 */
	public void update(TbBrand brand);
	
	
}
