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