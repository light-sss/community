$(function(){
    $("#uploadForm").submit(upload);
});
//加载页面时要调用此方法
function upload(){
//发送Ajax请求之前，提前将CSRF令牌设置到请求的消息头中
    	var token=$("meta[name='_csrf']").attr("content");
    	var header=$("meta[name='_csrf_header']").attr("content");
    	//在发送请求之前对页面进行设置   xhr：发送异步请求的核心对象
    	$(document).ajaxSend(function(e,xhr,options){
    	    xhr.setRequestHeader(header,token);  //设置请求头
    	});
    $.ajax({
        url:"http://upload-z1.qiniup.com",
        method:"post",
        //不要将表单内容转成字符串
        processData:false,
        //不让jQuery设置上传类型
        contentType:false,
        data:new FormData($("#uploadForm")[0]),
        success:function(data){
            if(data&&data.code==0){
                //更新头像访问路径
                $.post(
                    CONTEXT_PATH+"/user/header/url",
                    {"fileName":$("input[name='key']").val()},
                    function(data){
                        data=$.parseJSON(data);
                        if(data.code==0){
                            window.location.reload();
                        }else{
                            alert(data.msg);
                        }
                    }
                );
            }else{
                alert("上传失败！");
            }
        }
    });
    return false;
}