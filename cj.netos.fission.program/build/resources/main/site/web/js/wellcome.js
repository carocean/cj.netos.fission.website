$(document).ready(function(){
    var url=encodeURIComponent('http://nodespower.com/fission-mf-website/wechat/auth.html');
    var state=Math.uuid().replace('-','');
    window.location.href='https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx900c306e2b59c066&redirect_uri='+url+'&response_type=code&scope=snsapi_userinfo&state='+state+'#wechat_redirect';
});