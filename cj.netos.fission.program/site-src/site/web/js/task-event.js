$(document).ready(function () {
    $('.task-close').on('click', function () {
        parent.wx.closeWindow();
    });

    var taskCenter=$('.task-center');
    var state = taskCenter.attr('state');
    var avatar = taskCenter.attr('avatar');
    // var attachment = taskCenter.attr('attachment');
    var nickName = taskCenter.attr('nickName');
    var balance = taskCenter.attr('balance');
    var distance = taskCenter.attr('distance');
    var totalAmount = taskCenter.attr('totalAmount');//今日总收入
    var imgUrl = 'http://www.nodespower.com/img/redbag.jpg';
    // if (typeof attachment != 'undefined' && attachment != null && attachment != '') {
    //     imgUrl = attachment;
    // }
    parent.fission.sharePYC(state, nickName+'邀请你抢红包！他今天抢了¥' + totalAmount + '元。上亿元红包等你抢！', imgUrl);
    parent.fission.shareCR(state, '上亿元红包等你抢！', imgUrl,nickName+'邀请你抢红包！他今天抢了¥' + totalAmount + '元。');


});