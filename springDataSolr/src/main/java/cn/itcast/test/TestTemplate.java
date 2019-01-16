package cn.itcast.test;

import java.math.BigDecimal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cn.itcast.pojo.TbItem;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:applicationContext-solr.xml")
public class TestTemplate {
	@Autowired
	private SolrTemplate solrTemplate;
	@Test
	public void testAdd() {
		TbItem item=new TbItem();
		item.setId(1L);
		item.setBrand("��Ϊ");
		item.setCategory("�ֻ�");
		item.setGoodsId(1L);
		item.setSeller("��Ϊ 2 ��ר����");
		item.setTitle("��Ϊ Mate9");
		item.setPrice(new BigDecimal(2000));
		solrTemplate.saveBean(item);
		solrTemplate.commit();
		
	}
	
	@Test
	public void testFindOne(){
		TbItem item = solrTemplate.getById(1, TbItem.class);
		System.out.println(item.getTitle());
		}

}