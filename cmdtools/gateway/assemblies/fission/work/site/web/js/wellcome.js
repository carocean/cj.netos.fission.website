$(document).ready(function(){
    function getUrlParam(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)"); //构造一个含有目标参数的正则表达式对象
        var r = window.location.search.substr(1).match(reg); //匹配目标参数
        if (r != null)
        {return decodeURIComponent(r[2]); }
        return null; //返回参数值
    }
    var referrer=getUrlParam('person');
    var url='http://nodespower.com/fission-mf-website/wechat/auth.html';
    if(typeof referrer!='undefined'&&referrer!=null&&referrer!=''){
        url=url+'?referrer='+referrer;
    }
    url=encodeURIComponent(url);
    var stateParam=getUrlParam('state');
    var state='';
    if (typeof stateParam == 'undefined' || stateParam == null || stateParam == '') {
        state='rdm:'+Math.uuid().replace('-','');
    }else{
        state=stateParam;
    }
    window.location.href='https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx900c306e2b59c066&redirect_uri='+url+'&response_type=code&scope=snsapi_userinfo&state='+state+'#wechat_redirect';
});