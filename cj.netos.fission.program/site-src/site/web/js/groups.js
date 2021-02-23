$(document).ready(function () {
    $('.leading a').on('click',function () {
        history.go(-1);
        return false;
    });
    $('.group-bottom , .group-info').on('click',function () {
        window.location.href = './nav-download.html?title=聊天群服务&content=即时互动，推荐条件限定，比如，你只想认识女性朋友等，则需要使用地微app。&location=地微app->桌面';
    });
});