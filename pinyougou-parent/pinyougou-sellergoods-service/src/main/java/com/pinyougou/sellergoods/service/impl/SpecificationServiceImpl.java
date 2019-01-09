package com.pinyougou.sellergoods.service.impl;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;

import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSpecificationMapper;
import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.pojo.TbSpecificationExample;
import com.pinyougou.pojo.TbSpecificationExample.Criteria;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojo.TbSpecificationOptionExample;
import com.pinyougou.pojogroup.Specification;
import com.pinyougou.sellergoods.service.SpecificationService;

import entity.PageResult;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SpecificationServiceImpl implements SpecificationService {

	@Autowired
	private TbSpecificationMapper specificationMapper;
	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSpecification> findAll() {
		return specificationMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSpecification> page=   (Page<TbSpecification>) specificationMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSpecification specification) {
		specificationMapper.insert(specification);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(Specification specification){
		//保存修改的规格
		TbSpecification tbSpecification = specification.getSpecification();
		specificationMapper.updateByPrimaryKey(tbSpecification);
		
		TbSpecificationOptionExample example = new TbSpecificationOptionExample();
		 com.pinyougou.pojo.TbSpecificationOptionExample.Criteria createCriteria = example.createCriteria();
		 createCriteria.andSpecIdEqualTo(tbSpecification.getId());
		 //删除存在的规格
		 specificationOptionMapper.deleteByExample(example);
		//循环插入规格选项
		for(TbSpecificationOption option : specification.getSpecificationOptionList()) {
			//设置插入的规格id
			option.setSpecId(specification.getSpecification().getId());
			specificationOptionMapper.insert(option);
			}
		
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Specification findOne(Long id){
		//获取到规格名称实体类
		TbSpecification tbSpecification = specificationMapper.selectByPrimaryKey(id);
		TbSpecificationOptionExample example = new TbSpecificationOptionExample();
		 com.pinyougou.pojo.TbSpecificationOptionExample.Criteria createCriteria = example.createCriteria();
		 createCriteria.andSpecIdEqualTo(id);
		 //得到规格列表
		 List<TbSpecificationOption> optionList = specificationOptionMapper.selectByExample(example);
		//构建组合实体类返回结果
		 Specification spec=new Specification();
		 spec.setSpecification(tbSpecification);
		 spec.setSpecificationOptionList(optionList); 
		 return spec;
		
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			specificationMapper.deleteByPrimaryKey(id);
			//删除原有的规格选项 
			TbSpecificationOptionExample example=new TbSpecificationOptionExample();
			com.pinyougou.pojo.TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
			criteria.andSpecIdEqualTo(id);//指定规格 ID 为条件
			specificationOptionMapper.deleteByExample(example);//删除
		}		
	}
	
	
	@Override
	public PageResult findPage(TbSpecification specification, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSpecificationExample example=new TbSpecificationExample();
		Criteria criteria = example.createCriteria();
		
		if(specification!=null){			
						if(specification.getSpecName()!=null && specification.getSpecName().length()>0){
				criteria.andSpecNameLike("%"+specification.getSpecName()+"%");
			}
	
		}
		
		Page<TbSpecification> page= (Page<TbSpecification>)specificationMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

		@Override
		public void add(Specification specification) {
			//插入规格
			specificationMapper.insert(specification.getSpecification());
			//循环插入规格选项
			for(TbSpecificationOption option : specification.getSpecificationOptionList()) {
				//设置插入的规格id
				option.setSpecId(specification.getSpecification().getId());
				specificationOptionMapper.insert(option);
			}
		}

		@Override
		public List<Map> selectOptionList() {
			// TODO Auto-generated method stub
			return specificationMapper.selectOptionList();
		}
}
