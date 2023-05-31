$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");

	//发送Ajax请求之前，提前将CSRF令牌设置到请求的消息头中
	var token=$("meta[name='_csrf']").attr("content");
	var header=$("meta[name='_csrf_header']").attr("content");
	//在发送请求之前对页面进行设置   xhr：发送异步请求的核心对象
	$(document).ajaxSend(function(e,xhr,options){
	    xhr.setRequestHeader(header,token);  //设置请求头
	});

	//获取标题和内容
	var title=$("#recipient-name").val();//id选择器
	var content=$("#message-text").val();

	//发送异步请求（POST）
	$.post(
	    CONTEXT_PATH+"/discuss/add",
	    {"title":title,"content":content},
	    function(data){
	        data=$.parseJSON(data);
	        //在提示框中返回消息
	        $("#hintBody").text(data.msg);
	        //显示提示框
	        $("#hintModal").modal("show");
	        //两秒后自动隐藏
            setTimeout(function(){
                $("#hintModal").modal("hide");
                //判断是否发送成功
                if(data.code==0){
                    window.location.reload(); //重新加载页面
                }
            }, 2000);

	    }
	);


}