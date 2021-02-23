$(document).ready(function () {
    $('.leading a').on('click', function () {
        history.go(-1);
        return false;
    });
    $('.bill-bottom a').on('click', function () {
        window.location.href = './nav-download.html?title=账单服务&content=看明细、月统计，更专业，请在地微app中查看。&location=地微app->桌面->点你的头像->钱包->裂变游戏·交个朋友->收益及明细账';
    })
});