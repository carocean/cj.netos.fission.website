$(document).ready(function () {
    $('.leading a').on('click',function () {
        history.go(-1);
        return false;
    });
    $('.member-bottom , .member-info').on('click',function () {
        window.location.href = './nav-download.html?title=聊天群服务&content=即时互动、精准获客，比如，你只想加想买房的朋友，则需要使用地微app。&location=地微app->桌面';
    });
});