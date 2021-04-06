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
    var friendCount = taskCenter.attr('friendCount');
    var totalAmount = taskCenter.attr('totalAmount');//今日总收入
    var commissionAmount = taskCenter.attr('commissionAmount');
    var imgUrl = 'http://www.nodespower.com/img/redbag.jpg';
    // if (typeof attachment != 'undefined' && attachment != null && attachment != '') {
    //     imgUrl = attachment;
    // }
    parent.fission.sharePYC(state, '快来抢红包啦，分享还可赚佣金。'+nickName+'已经抢了¥' + totalAmount + '，获得佣金¥'+commissionAmount+'，认识了'+friendCount+'个朋友。', imgUrl);
    parent.fission.shareCR(state, '快来抢红包啦，分享可赚佣金。', imgUrl,nickName+' 已经抢了¥' + totalAmount + '，获得佣金¥'+commissionAmount+'，认识了'+friendCount+'个朋友。');


});