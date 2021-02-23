$(document).ready(function(){
    $('.leading a').on('click',function () {
        history.back();
    });
    $('.card[attach] .card-item-nav').on('click',function () {
        window.location.href = './nav-download.html?title=广告附件&content=在用户抢你的红包时会弹出你的广告，欲设置广告，请使用地微app。&location=地微app->桌面->点你的头像->钱包->裂变游戏·交个朋友';
    });
    $('.card[operator] .card-item-nav[settings]').on('click',function () {
        window.location.href = './nav-download.html?title=营业参数&content=设置获客平均成本，红包金额概率，等等，欲设置经营参数，请使用地微app。&location=地微app->桌面->点你的头像->钱包->裂变游戏·交个朋友';
    });
    $('.card[operator] .card-item-nav[state] #state').on('click',function () {
        window.location.href = './nav-download.html?title=营业状态&content=停止营业，你将失去为你的群拉成员的功能，如果你想交更多的朋友，请一定保持营业状态。欲设置经营参数，请使用地微app。&location=地微app->桌面->点你的头像->钱包->裂变游戏·交个朋友';
        return false;
    });
});