package com.pinyougou.manager.controller;

import java.util.List;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;

import entity.PageResult;
import entity.Result;

@RestController
@RequestMapping("/brand")
public class BrandController {

	@Reference
	private BrandService brandService;
	
	
	@RequestMapping("/findAll")
	public List<TbBrand> findAll(){
		return brandService.findAll();		
	}
	
	// 分页查询
	@RequestMapping("/findPage")
	public PageResult findPage(int page,int rows){ 
		
		return brandService.findPage(page, rows);
	}
	//添加品牌
	@RequestMapping("/add")
	public Result add(@RequestBody TbBrand brand) {
		 try {
			brandService.add(brand);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
		
	}
	
	//根据id查询用户
	@RequestMapping("/findOne")
	public TbBrand findOne(Long id) {
		return brandService.findOne(id);
	}
	
	//根据id修改信息
	@RequestMapping("/update")
	public Result update(@RequestBody TbBrand brand) {
		 try {
			brandService.update(brand);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
		
	}
	
	//根据id删除信息
		@RequestMapping("/delete")
		public Result delete(Long[] ids) {
			 try {
				brandService.delete(ids);
				return new Result(true, "删除成功");
			} catch (Exception e) {
				
				e.printStackTrace();
				return new Result(false, "删除失败");
			}
			
		}
		
		//模糊查询
		@RequestMapping("/search")
		public PageResult search(@RequestBody TbBrand brand, int page, int rows ){
			
			return brandService.findPage(brand, page, rows); 
		}
		
}
