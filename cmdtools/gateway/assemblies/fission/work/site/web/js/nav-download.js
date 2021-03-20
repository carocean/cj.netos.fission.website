$(document).ready(function () {
    $('.nav-close').on('click',function () {
        parent.wx.closeWindow();
    });
    $('.leading a').on('click',function () {
        history.go(-1);
        return false;
    });
    $('.nav-fixed span[download]').on('click',function () {
       parent.location.href='http://nodespower.com/qrslice/downloads.html';
    });
});