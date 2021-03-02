$(document).ready(function () {
    $('.task-close').on('click',function () {
        parent.wx.closeWindow();
    });

    var taskCenter=$('.task-center');
    var state = taskCenter.attr('state');
    var avatar = taskCenter.attr('avatar');
    // var attachment = taskCenter.attr('attachment');
    var nickName = taskCenter.attr('nickName');
    var balance = taskCenter.attr('balance');
    var distance = taskCenter.attr('distance');
    var friendCount = recommendE.attr('friendCount');
    var totalAmount = taskCenter.attr('totalAmount');//今日总收入
    var imgUrl = 'http://www.nodespower.com/img/redbag.jpg';
    // if (typeof attachment != 'undefined' && attachment != null && attachment != '') {
    //     imgUrl = attachment;
    // }
    parent.fission.sharePYC(state, '来交个朋友吧！还有红包哦。'+nickName+'已经抢了¥' + totalAmount + '元，认识了'+friendCount+'个朋友。', imgUrl);
    parent.fission.shareCR(state, '来交个朋友吧！还有红包哦。', imgUrl,nickName+' 已经抢了¥' + totalAmount + '元，认识了'+friendCount+'个朋友。');


});