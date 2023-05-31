$(function(){
	$("#sendBtn").click(send_letter);
	$(".close").click(delete_msg);
});

function send_letter() {
	$("#sendModal").modal("hide");

	   //发送Ajax请求之前，提前将CSRF令牌设置到请求的消息头中
        	var token=$("meta[name='_csrf']").attr("content");
        	var header=$("meta[name='_csrf_header']").attr("content");
        	//在发送请求之前对页面进行设置   xhr：发送异步请求的核心对象
        	$(document).ajaxSend(function(e,xhr,options){
        	    xhr.setRequestHeader(header,token);  //设置请求头
        	});


	var toName=$("#recipient-name").val();
	var content=$("#message-text").val();

	$.post(
	    CONTEXT_PATH + "/letter/send",
	    {"toName":toName,"content":content},
	    function(data){
	        data=$.parseJSON(data);
	        if(data.code==0){
	            $("#hintBody").text("发送成功！");
	        }else{
	            $("#hintBody").text(data.msg);
	        }

	        $("#hintModal").modal("show");
            	setTimeout(function(){
            		$("#hintModal").modal("hide");
            		location.reload();
            	}, 2000);
	    }
	);

}

function delete_msg() {
	// TODO 删除数据
	$(this).parents(".media").remove();
}