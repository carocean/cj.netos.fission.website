$(document).ready(function () {


    $('.leading a').on('click', function () {
        // history.go(-1);
        $('.panel-dialog',parent.document).hide();
        return false;
    });

    $('.rb-info .rb-box .rb-go-wallet').on('click',function () {
        alert('提现服务\n\n提现服务需要使用地微app，提现无任何门坎。\n\n服务位置：地微app->桌面->点你的头像->钱包->交个朋友->提现');
    });

    var record_sn;
    var timerPayRecordDone = 0;

    function waitingForRecord() {
        $.get('./getPayRecordById.service', {'record_sn': record_sn}, function (data) {
            var obj = $.parseJSON(data);
            if (obj.status != '200') {
                return;
            }
            clearInterval(timerPayRecordDone);
            var record = obj.record;
            var rbAmountPanel = $('.rb-info .rb-card .rb-amount');
            rbAmountPanel.empty();
            rbAmountPanel.append('<span style="font-size: 60px;">' + obj.amount + '</span><label>元</label>');
            var recordHtml = obj.recordHtml;
            var personsE = $('.rb-payee-box .rb-payee-persons');
            if (typeof personsE.attr('rended') == 'undefined') {
                personsE.attr('rended', true);
                personsE.prepend("<li class=\"rb-payee-li-deliver\"></li>");
                personsE.prepend(recordHtml);
            }
        }).error(function (e) {
            alert(e);
        });
    }

    var redBagOwner = $('.rb-info').attr('red-bag-owner');
    $.get('./snatchEnvelope.service', {'person': redBagOwner}, function (data) {
        var obj = $.parseJSON(data);
        record_sn = obj.id;
        timerPayRecordDone = setInterval(waitingForRecord, 1000);
    }).error(function (e) {
        alert(e);
    });

});