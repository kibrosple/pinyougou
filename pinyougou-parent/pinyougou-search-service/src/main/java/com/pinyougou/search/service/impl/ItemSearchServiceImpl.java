package com.pinyougou.search.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FilterQuery;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.HighlightQuery;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.GroupResult;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
@Service
@SuppressWarnings("all")
public class ItemSearchServiceImpl implements ItemSearchService {
	@Autowired
	private SolrTemplate solrTemplate;
	
	@Override
	public Map<String, Object> search(Map searchMap) {
		//去除搜索关键字空格
		String keywords = (String) searchMap.get("keywords");
		searchMap.put("keywords", keywords.replace(" ", ""));
		Map<String,Object> map= new HashMap();
		//把高亮选项结果存入查询的map中
		map.putAll(searchList(searchMap));
		//2.根据关键字查询商品分类
		List categoryList = searchCategoryList(searchMap);
		map.put("categoryList",categoryList);
		//3.查询品牌和规格列表
		String category= (String) searchMap.get("category"); //获取分类
		if(!category.equals("")){			//如果有分类的话			
			map.putAll(searchBrandAndSpecList(category)); //按照分类去查询他所有的品牌,所有的规格
		}else{
			if(categoryList.size()>0){			
				map.putAll(searchBrandAndSpecList((String)categoryList.get(0)));
			}	
		}
		return map;
	}
	/**
	 * 高亮显示搜索集合
	 * 
	 */
	private Map searchList(Map searchMap) {
		//创建map接收高亮显示结果
		Map map = new HashMap();
		//创建高亮查询对象
		HighlightQuery query=new SimpleHighlightQuery();
		//构建高亮选项对象,并添加对应的域
		HighlightOptions highlightOptions=new HighlightOptions().addField("item_title");//设置高亮的域
		//高亮前缀
		highlightOptions.setSimplePrefix("<em style='color:red'>");
		//高亮后缀
		highlightOptions.setSimplePostfix("</em>");
		//设置高亮选项,放入查询对象中
		query.setHighlightOptions(highlightOptions);
		
		
		//1.1按照关键字查询
		Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
		//查询条件放入查询对象中
		query.addCriteria(criteria);
		
		//1.2 按商品分类过滤
		if(!"".equals(searchMap.get("category"))  )	{
			//如果用户选择了分类 
			FilterQuery filterQuery=new SimpleFilterQuery();
			Criteria filterCriteria=new Criteria("item_category").is(searchMap.get("category"));
			filterQuery.addCriteria(filterCriteria);
			query.addFilterQuery(filterQuery);			
		}
		
		//1.3 按品牌过滤
		if(!"".equals(searchMap.get("brand"))  )	{
			//如果用户选择了品牌
			FilterQuery filterQuery=new SimpleFilterQuery();
			Criteria filterCriteria=new Criteria("item_brand").is(searchMap.get("brand"));
			filterQuery.addCriteria(filterCriteria);
			query.addFilterQuery(filterQuery);			
		}
		
		//1.4 按规格过滤
		if(searchMap.get("spec")!=null){			
			Map<String,String> specMap= (Map<String, String>) searchMap.get("spec");
			for(String key :specMap.keySet()){		
				FilterQuery filterQuery=new SimpleFilterQuery();
				Criteria filterCriteria=new Criteria("item_spec_"+key).is(specMap.get(key));
				filterQuery.addCriteria(filterCriteria);
				query.addFilterQuery(filterQuery);					
						
				}		
					
		}
		//1.5按价格过滤
		if(!"".equals(searchMap.get("price"))&&!"null".equals(searchMap.get("price"))){//如果价格不是空字符串或者null
			String[] price = ((String) searchMap.get("price")).split("-");//去除搜索的空格
			if(!price[0].equals("0")){ //如果最低价格不等于0
				FilterQuery filterQuery=new SimpleFilterQuery();
				Criteria filterCriteria=new Criteria("item_price").greaterThanEqual(price[0]);
				filterQuery.addCriteria(filterCriteria);
				query.addFilterQuery(filterQuery);	
			}
			if(!price[1].equals("*")){ //如果最高价格不等于*
				FilterQuery filterQuery=new SimpleFilterQuery();
				Criteria filterCriteria=new Criteria("item_price").lessThanEqual(price[1]);
				filterQuery.addCriteria(filterCriteria);
				query.addFilterQuery(filterQuery);	
			}			
		}
		

		//1.6 分页  前端传入当前页,和每页显示记录数
		Integer pageNo= (Integer) searchMap.get("pageNo");//获取页码
		if(pageNo==null){
			pageNo=1; //默认为第一页
		}
		Integer pageSize= (Integer) searchMap.get("pageSize");//获取页大小
		if(pageSize==null){
			pageSize=20; //默认每页20条记录
		}
		
		query.setOffset( (pageNo-1)*pageSize  );//起始索引
		query.setRows(pageSize);//每页记录数
		
		
		//1.7 排序
		
		String sortValue= (String)searchMap.get("sort");//升序ASC 降序DESC
		String sortField=  (String)searchMap.get("sortField");//排序字段
		
		if(sortValue!=null && !sortValue.equals("")){
			
			if(sortValue.equals("ASC")){
				Sort sort=new Sort(Sort.Direction.ASC, "item_"+sortField);
				query.addSort(sort);				
			}
			if(sortValue.equals("DESC")){
				Sort sort=new Sort(Sort.Direction.DESC, "item_"+sortField);
				query.addSort(sort);				
			}
		}
		
		
		
		
		//创建高亮分页对象
		HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query,TbItem.class);
		//获取高亮入口集合
		for(HighlightEntry<TbItem> h: page.getHighlighted()) {
			//获得原来的实体类
			TbItem item = h.getEntity();
			//h.getHighlights(),高亮列表
			/**
			 * //获取高亮列表,如果有多个域对象
				List<Highlight> highlights = h.getHighlights();
				for (Highlight hh : highlights) {
					List<String> snipplets = hh.getSnipplets();//获取高亮域
					//将高亮的字段重新设置到title中
					for (String title : snipplets) {
						tbItem.setTitle(title);
					}
				}
			 */
			//h.getHighlights().get(0).getSnipplets(),获取高亮域,这里只有一个域
			if(h.getHighlights().size()>0 && h.getHighlights().get(0).getSnipplets().size()>0){
					//设置高亮的结果
					//将高亮的字段重新设置到title中
					item.setTitle(h.getHighlights().get(0).getSnipplets().get(0));
					} 
		}
		//把高亮结果存入到高亮选项中
		map.put("rows",page.getContent());
		map.put("totalPages", page.getTotalPages());//返回总页数
		map.put("total", page.getTotalElements());//返回总记录数
		return map;
	}
	
	/**
	 * 分类列表展示
	 */
	private List searchCategoryList(Map searchMap) {
		//创建集合接收分类
		List<String> list=new ArrayList();
		//构建查询对象
		Query query=new SimpleQuery(); 
		//按照关键字查询
		Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		//创建并设置分组选项
		GroupOptions groupOptions=new GroupOptions().addGroupByField("item_category");
		query.setGroupOptions(groupOptions);
		//从solr得到分组页
		GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
		//根据列得到分组结果集
		GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
		//得到分组结果入口页
		Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
		//得到分组入口集合
		List<GroupEntry<TbItem>> content = groupEntries.getContent();
		
		for(GroupEntry<TbItem> entry:content){
				list.add(entry.getGroupValue());//将分组结果的名称封装到返回值中
			}
		return list;
		
	}
	
	
	@Autowired
	private RedisTemplate redisTemplate;
	/**
	 * 查询品牌和规格列表
	 * @param category
	 * @return
	 */
	
	private Map searchBrandAndSpecList(String category) {
		Map map = new HashMap();
		//第一步,从缓存中获取模板id,存放时,分类对应的是模板id
		Long typeId = (Long)redisTemplate.boundHashOps("itemCat").get(category);
		//对模板id进行判断
		if(typeId!=null){
			//根据模板 ID 查询品牌列表
			List brandList = (List)redisTemplate.boundHashOps("brandList").get(typeId);
			map.put("brandList", brandList);//返回值添加品牌列表
			//根据模板 ID 查询规格列表
			List specList = (List)redisTemplate.boundHashOps("specList").get(typeId);
			map.put("specList", specList); 
			} 
			return map;
	}
	@Override
	public void importList(List list) {
		solrTemplate.saveBeans(list);
		solrTemplate.commit();
		
	}
	@Override
	public void deleteByGoodsIds(List goodsIdList) {
		System.out.println("删除商品 ID"+goodsIdList);
		Query query=new SimpleQuery(); 
		Criteria criteria=new Criteria("item_goodsid").in(goodsIdList);
		query.addCriteria(criteria);
		solrTemplate.delete(query);
		solrTemplate.commit();
		
	}

}
 