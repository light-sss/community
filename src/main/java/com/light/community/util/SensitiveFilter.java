package com.light.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author light
 * @Description
 * 前缀树特点：
 *
 * 1.根节点为空
 *
 * 2.除了根节点以外的每一个节点只包含一个字符
 *
 * 3.从根节点到某一个节点的路径上连接起来就是当前所对应的字符串
 *
 * 4.每个结点包含的字符不同，如果相同则要合并
 *
 * @create 2023-04-15 22:52
 */

@Component
public class SensitiveFilter {

	private static final Logger logger= LoggerFactory.getLogger(SensitiveFilter.class);

	//替换符
	private static final String  REPLACEMENT="***";

	//定义前缀树数据结构
	private class TireNode{
		//定义关键词的结束标识：true--->是；false--->不是
		private boolean isKeywordEnd=false;

		//定义子节点：key--->子结点的值；value：表示当前子节点类型
		private Map<Character,TireNode> subNodes=new HashMap<>();

		public boolean isKeywordEnd() {
			return isKeywordEnd;
		}

		public void setKeywordEnd(boolean keywordEnd) {
			isKeywordEnd = keywordEnd;
		}
		//添加子节点
		public void addSubNode(Character c,TireNode node){
			subNodes.put(c,node);
		}

		//获取子节点
		public TireNode getSubNode(Character c){
			return subNodes.get(c);
		}
	}

	//创建前缀树
	private TireNode rootNode=new TireNode();

	//初始化前缀树
	@PostConstruct //当类初始化调用构造参数时调用此方法
	public void init(){
		//获取敏感词字符
		try(
				InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive.txt");
				BufferedReader reader=new BufferedReader(new InputStreamReader(is));//用缓冲流读取文件（先将字节流转换成字符流
				){
			//通过reader读取敏感词
			String keyword;
			while((keyword=reader.readLine())!=null){
				//将敏感词加入前缀树中
				this.addKeyword(keyword);
			}
		}catch (IOException e){
			logger.error("获取敏感词失败："+e.getMessage());
		}

	}

	//将敏感词添加到前缀树中
	private void addKeyword(String keyword){
		//定义指针指向当前前缀树节点
		TireNode tempNode=rootNode;
		for(int i=0;i<keyword.length();i++){
			char c=keyword.charAt(i);
			TireNode subNode=tempNode.getSubNode(c);//判断当敏感词是否在前缀树中
			if(subNode==null){//当前敏感词不在前缀树中
				subNode=new TireNode();//初始化子节点
				tempNode.addSubNode(c,subNode);//挂载子节点
			}
			tempNode=subNode;//指针移动到子节点，进入下一级循环
			if(i==keyword.length()-1){
				//声明敏感词结束标识
				tempNode.setKeywordEnd(true);
			}

		}
	}

	/**
	 * 过滤敏感词
	 * @param text 待过滤的文本
	 * @return  过滤后的文本
	 */
	public String filter(String text){
		if(StringUtils.isBlank(text)){
			return null;
		}
		//定义三个指针
		//指针1：指向rootNode
		TireNode tempNode=rootNode;
		//指针2：敏感词起始位置
		int begin=0;
		//指针3：敏感词终止位置
		int position=0;
		//定义过滤后的文本
		StringBuilder sb=new StringBuilder();
		while(position<text.length()){
			char c=text.charAt(position);
			//判断是否要跳过特殊字符
			if(isSymbol(c)){//是特殊字符
				//首先判断特殊字符出现的位置，如果特殊字符出现在敏感字前的话不进行过滤
				//若特殊字符出现在敏感词中间的话，要进行过滤

				if(tempNode==rootNode){//特殊字符出现在敏感字前,话不进行过滤
					sb.append(c);
					begin++;
				}

				position++; //若特殊字符出现在敏感词中间的话，要进行过滤
				continue;//继续获取下一个字符

			}
			tempNode=tempNode.getSubNode(c);
			if(tempNode==null){//不是敏感词
				sb.append(text.charAt(begin));
				position=++begin;
				tempNode=rootNode;//重新回到根节点
			}else if(tempNode.isKeywordEnd()){ //是敏感词并且在结尾
				//将begin~position位置的字符替换为***
				sb.append(REPLACEMENT);
				begin=++position;
				tempNode=rootNode;//重新回到根节点
			}else{
				position++;  //是敏感词但不是结尾：begin不移动，position继续后移
			}
		}
		//将末尾不是敏感词的文本也加入到sb中
		sb.append(text.substring(begin));
		return sb.toString();
	}
	//判断是否是特殊字符
	private boolean isSymbol(Character c){
		// 0x2E80~0x9FFF 是东亚文字范围
		return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
	}


}
