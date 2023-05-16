package com.light.community.entity;

/**
 * @author light
 * @Description 封装分页相关的信息
 *
 * 利用这个对象让服务端接受页面传入的信息
 *
 * @create 2023-03-23 18:28
 */

public class Page {
    //当前页码
    private int current=1;
    //页面显示上限
    private  int limit=10;
    /*
    前两个是页面传入的信息；后两个变量是服务器自身要使用的数据
     */
    //数据总数（用于计算总的页数）  服务端查出来的
    private int rows;

    //查询路径（复用分页链接
    private String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if(current>=1){
            this.current = current;
        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if(limit>=1&&limit<=100){

            this.limit = limit;
        }
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if(rows>=0){

            this.rows = rows;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
    //额外提供条件：数据库查询需要用到的、页面显示需要用到的

    //获取当前页的起始行：页码*当前页上限-上限
    public int getOffset(){
        //current*limit-limit
        return (current-1)*limit;
    }
    //用来获取总的页数
    public int getTotal(){
        if(rows%limit==0){

            return rows/limit;
        }else{
            return (rows/limit)+1;
        }
    }

    //从···页到···页
    public int getFrom(){
        //获取起始页码:只显示当前页的前两呀页
        int from=current-2;

        return from<1?1:from;
    }

    public int getTo(){
        //获取终止页码：只显示当前页的后两页
        int to=current+2;
        return to>getTotal()?getTotal():to;

    }
}
