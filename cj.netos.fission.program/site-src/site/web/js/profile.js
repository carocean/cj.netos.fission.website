$(document).ready(function () {
    $('.leading a').on('click', function () {
        history.go(-1);
        return false;
    });
    $('.card[pay] .card-item-nav[operator]').on('click', function () {
        window.location.href = 'fission-manager.html';
    })

    //开始充到红包
    function rechargeToRedBag(realAmount) {
        $.get('./pay-trade.service', {amount: realAmount}, function (data) {
            var record = $.parseJSON(data);
            alert('充值成功');
            $.get('./get-balance.service', {}, function (data) {
                var obj = $.parseJSON(data);
                var balanceLong = obj.balanceLong;
                var balance = obj.balance;
                //然后再充红包
                $('.card[pay] .card-item-summary .card-item-summary-value >span').html(balance + '');
            }).error(function (e) {
                alert(e.responseText);
            });
        }).error(function (e) {
            alert(e.responseText);
        });
    }

    $('.balance-mgr .recharge').on('click', function () {
        $('.profile-mask').toggle();

    });
    $('.profile-mask .mask-confirm').on('click', function () {
        if ($(this).hasClass('grey')) {
            return;
        }
        $('.profile-mask').toggle();
        var lowest = 100.00;
        var val = $('.profile-mask .mask-input input').val();
        if (val == '' || val == null || typeof val == "undefined") {
            return;
        }
        var amount;
        try {
            var amountYuan = parseFloat(val);
            // alert(amountYuan);
            // if (amountYuan < lowest) {
            //     alert('起充金额至少100元');
            //     return;
            // }
            amount = parseInt(amountYuan * 100);
            // alert(amount);
        } catch (e) {
            alert('格式不正确。');
            return;
        }
        var portsUrl = './recharge.service';
        $.get(portsUrl, {
            amount: amount,
            note: '微信支付',
        }, function (json) {
            var map = $.parseJSON(json);
            var record_sn = map['record_sn'];
            // alert('appId='+map['appId']+';'+'timeStamp='+map['timestamp']+';'+'nonceStr='+map['nonceStr']+';'+'package='+map['package']+';'+'signType='+map['signType']+';'+'paySign='+map['paySign']+';');
            parent.WeixinJSBridge.invoke(
                'getBrandWCPayRequest', {
                    "appId": map['appId'],     //公众号名称，由商户传入
                    "timeStamp": map['timestamp'],      //时间戳，自1970年以来的秒数，注意：在苹果手机上，如果是数字时间的话会报缺少此参数错误
                    "nonceStr": map['nonceStr'], //随机串
                    "package": map['package'],
                    "signType": map['signType'],       //微信签名方式：
                    "paySign": map['paySign'] //微信签名
                },
                function (res) {
                    if (res.err_msg == "get_brand_wcpay_request:ok") {
                        // 使用以上方式判断前端返回,微信团队郑重提示：
                        //res.err_msg将在用户支付成功后返回ok，但并不保证它绝对可靠。
                        //等待充值记录完成，而后再充到红包，再刷新红包余额
                        var timers = 1;
                        var handler;
                        var exitLoop = false;
                        handler = setInterval(function () {
                            //每0.5秒查一次记录状态，如果超过10次则取消
                            if (timers > 10) {
                                clearInterval(handler);
                                return;
                            }
                            if (exitLoop) {
                                clearInterval(handler);
                                return;
                            }
                            $.get('./get-recharge-record.service', {recordSn: record_sn}, function (data) {
                                var record = $.parseJSON(data);
                                if (record.state == 1) {
                                    clearInterval(handler);
                                    exitLoop = true;
                                    var realAmount = record.realAmount;
                                    //开始充红包
                                    rechargeToRedBag(realAmount);
                                }
                            }).error(function (e) {
                                alert(e.responseText);
                            });
                        }, 500);

                        return;
                    }
                    if (res.err_msg == 'get_brand_wcpay_request:cancel') {
                        //支付过程取消
                        alert('支付已取消');
                        return;
                    }
                    if (res.err_msg == 'get_brand_wcpay_request:fail') {
                        //支付失败
                        alert('支付失败');
                        return;
                    }
                });
            // parent.wx.chooseWXPay({
            //     timestamp: map['timestamp'], // 支付签名时间戳，注意微信jssdk中的所有使用timestamp字段均为小写。但最新版的支付后台生成签名使用的timeStamp字段名需大写其中的S字符
            //     nonceStr: map['nonceStr'], // 支付签名随机串，不长于 32 位
            //     package:map['package'],// 统一支付接口返回的prepay_id参数值，提交格式如：prepay_id=\*\*\*）
            //     signType: map['signType'], // 签名方式，默认为'SHA1'，使用新版支付需传入'MD5'
            //     paySign: map['paySign'],// 支付签名
            //     success: function (res) {
            //         // 支付成功后的回调函数
            //         alert(res);
            //     }
            // });
        }).error(function (e) {
            alert(e.responseText);
        });
    });
    $('.profile-mask .mask-cancel').on('click', function () {
        $(this).parents('.profile-mask').toggle();
    });
    $('.profile-mask .mask-input input').on('keyup', function (e) {
        var val = $(this).val();
        if (val == '') {
            $('.mask-confirm').addClass('grey');
            return;
        }
        var reg = new RegExp('^[0-9]+\.?[0-9]{0,2}$');
        if (!reg.test(val)) {
            $('.mask-confirm').addClass('grey');
            return;
        }
        $('.mask-confirm').removeClass('grey');
    });

    $('.balance-mgr .withdraw').on('click', function () {
        window.location.href = './nav-download.html?title=提现服务&content=提现服务需要使用地微app，提现无任何门坎。&location=地微app->桌面->点你的头像->钱包->零钱->提现';
    });
    $('.card[pay] .card-item-nav[bill]').on('click', function () {
        window.location.href ='./cashier-bill.html';
    });
    $('.card[friends] .card-item-nav[members]').on('click',function () {
        window.location.href='./members.html';
    });
    $('.card[friends] .card-item-nav[groups]').on('click',function () {
        window.location.href='./groups.html';
    });
    $('.face .my-tag-box .my-tag-arrow').on('click',function () {
        window.location.href='./prop-tag.html';
    });
});

if (typeof parent.WeixinJSBridge == "undefined") {
    if (parent.document.addEventListener) {
        parent.document.addEventListener('WeixinJSBridgeReady', parent.onBridgeReady, false);
    } else if (parent.document.attachEvent) {
        parent.document.attachEvent('WeixinJSBridgeReady', parent.onBridgeReady);
        parent.document.attachEvent('onWeixinJSBridgeReady', parent.onBridgeReady);
    }
} else {
    parent.onBridgeReady();
}