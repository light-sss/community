$(function(){  //在刚初始化完html页面时加载该方法
    $("#topBtn").click(setTop);
    $("#wonderfulBtn").click(setWonderful);
    $("#deleteBtn").click(setDelete);
});




function like(btn,entityType,entityId,entityUserId,postId){

    //发送Ajax请求之前，提前将CSRF令牌设置到请求的消息头中
    	var token=$("meta[name='_csrf']").attr("content");
    	var header=$("meta[name='_csrf_header']").attr("content");
    	//在发送请求之前对页面进行设置   xhr：发送异步请求的核心对象
    	$(document).ajaxSend(function(e,xhr,options){
    	    xhr.setRequestHeader(header,token);  //设置请求头
    	});

    $.post(
        CONTEXT_PATH+"/like",
        {"entityType":entityType,"entityId":entityId,"entityUserId":entityUserId,"postId":postId},
        function(data){
            data=$.parseJSON(data);
            if(data.code==0){
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus==1?'已赞':'赞');
            }else{
                alert(data.msg);
            }
        }
    );
}


//置顶与取消置顶
function setTop(){
 //发送Ajax请求之前，提前将CSRF令牌设置到请求的消息头中
    	var token=$("meta[name='_csrf']").attr("content");
    	var header=$("meta[name='_csrf_header']").attr("content");
    	//在发送请求之前对页面进行设置   xhr：发送异步请求的核心对象
    	$(document).ajaxSend(function(e,xhr,options){
    	    xhr.setRequestHeader(header,token);  //设置请求头
    	});
    $.post(
        CONTEXT_PATH+"/discuss/top",
        {"id":$("#postId").val()},
        function(data){
            data=$.parseJSON(data);
            if(data.code==0){
                $("#topBtn").text(data.type==1?'取消置顶':'置顶');
            }else{
                alert(data.msg);
            }
        }

    );
}

//加精与取消加精
function setWonderful(){
 //发送Ajax请求之前，提前将CSRF令牌设置到请求的消息头中
    	var token=$("meta[name='_csrf']").attr("content");
    	var header=$("meta[name='_csrf_header']").attr("content");
    	//在发送请求之前对页面进行设置   xhr：发送异步请求的核心对象
    	$(document).ajaxSend(function(e,xhr,options){
    	    xhr.setRequestHeader(header,token);  //设置请求头
    	});
    $.post(
        CONTEXT_PATH+"/discuss/wonderful",
        {"id":$("#postId").val()},
        function(data){
            data=$.parseJSON(data);
            if(data.code==0){
                $("#wonderfulBtn").text(data.status==1?'取消加精':'加精');
            }else{
                alert(data.msg);
            }
        }

    );
}

//删除
function setDelete(){
 //发送Ajax请求之前，提前将CSRF令牌设置到请求的消息头中
    	var token=$("meta[name='_csrf']").attr("content");
    	var header=$("meta[name='_csrf_header']").attr("content");
    	//在发送请求之前对页面进行设置   xhr：发送异步请求的核心对象
    	$(document).ajaxSend(function(e,xhr,options){
    	    xhr.setRequestHeader(header,token);  //设置请求头
    	});
    $.post(
        CONTEXT_PATH+"/discuss/delete",
        {"id":$("#postId").val()},
        function(data){
            data=$.parseJSON(data);
            if(data.code==0){
                //删除成功返回到首页
                location.href=CONTEXT_PATH+"/index";
            }else{
                alert(data.msg);
            }
        }

    );
}
