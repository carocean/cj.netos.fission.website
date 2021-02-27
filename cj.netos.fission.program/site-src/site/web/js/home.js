if (typeof window.fission == 'undefined') {
    window.fission = {}
    window.fission.sharePYC = function (state, title, imgUrl) {
        // alert('share state:'+state);
        var link = 'http://nodespower.com/fission-mf-website/?state=' + state;
        //分享到朋友圈
        wx.updateTimelineShareData({//updateTimelineShareData
            title: title, // 分享标题
            link: link, // 分享链接，
            //注意：该链接域名或路径必须与当前页面对应的公众号JS安全域名一致   //不要写错了 不然会提示  系统错误，错误码：63002,invalid signature
            imgUrl: imgUrl, // 分享图标
            success: function () {
                //这接口银神奇，点分享没对话框，必须点微信浏览器的右上选"分享到朋友圈"，这个接口分享成功的的信息实际上仅是填充浏览器分享，它不会调起分享对话框
                // alert("分享成功");//不要用alert，不然错误无法wx.error无法执行
                console.log("分享成功");
                // playnum = 1; //应该在后台设置并获取
                // $('.playnum').html('1');
            },
            cancel: function () {
                console.log("分享取消");
            }
        });
    }
    window.fission.shareCR = function (state, title, imgUrl, desc) {
        // alert('share state:'+state);
        var link = 'http://nodespower.com/fission-mf-website/?state=' + state;
        wx.updateAppMessageShareData({
            title: title, // 分享标题
            desc: desc, // 分享描述
            link: link, // 分享链接，该链接域名或路径必须与当前页面对应的公众号JS安全域名一致
            imgUrl: imgUrl, // 分享图标
            success: function () {
                // 设置成功
                // alert("分享成功");
            }
        })
    }
}

$(document).ready(function () {
    var recommendE = $('.recommends');
    var state = recommendE.attr('state');
    $.get('get-sign.json', {state: state}, function (data) {
        var obj = $.parseJSON(data);
        wx.config({
            debug: false, // 开启调试模式,调用的所有api的返回值会在客户端alert出来，若要查看传入的参数，可以在pc端打开，参数信息会通过log打出，仅在pc端时才会打印。
            appId: obj.appId, // 必填，公众号的唯一标识
            timestamp: obj.timestamp, // 必填，生成签名的时间戳
            nonceStr: obj.nonceStr, // 必填，生成签名的随机串
            signature: obj.signature,// 必填，签名，见附录1
            jsApiList: obj.jsApiList // 必填，需要使用的JS接口列表，所有JS接口列表见附录2
        });

        wx.ready(function () {
            // config信息验证后会执行ready方法，所有接口调用都必须在config接口获得结果之后，config是一个客户端的异步操作，所以如果需要在页面加载时就调用相关接口，则须把相关接口放在ready函数中调用来确保正确执行。对于用户触发时才调用的接口，则可以直接调用，不需要放在ready函数中。
            // 必须是在用户已经授权的情况下调用
            // alert('ready');
            var avatar = recommendE.attr('avatar');
            // var attachment = recommendE.attr('attachment');
            var nickName = recommendE.attr('nickName');
            var balance = recommendE.attr('balance');
            var distance = recommendE.attr('distance');
            var totalAmount = recommendE.attr('totalAmount');//今日总收入
            var imgUrl = 'http://www.nodespower.com/img/redbag.jpg';
            // if (typeof attachment != 'undefined' && attachment != null && attachment != '') {
            //     imgUrl = attachment;
            // }
            fission.sharePYC(state, nickName+'邀请你抢红包！他今天抢了¥' + totalAmount + '元。上亿元红包等你抢！', imgUrl);
            fission.shareCR(state, '上亿元红包等你抢！', imgUrl,nickName+'邀请你抢红包！他今天抢了¥' + totalAmount + '元。');
            wx.getLocation({
                type: 'wgs84', // 默认为wgs84的gps坐标，如果要返回直接给openLocation用的火星坐标，可传入'gcj02'
                success: function (res) {
                    var latitude = res.latitude; // 纬度，浮点数，范围为90 ~ -90
                    var longitude = res.longitude; // 经度，浮点数，范围为180 ~ -180。
                    var speed = res.speed; // 速度，以米/每秒计
                    var accuracy = res.accuracy; // 位置精度
                    $.get('./pages/update-location.service',{latitude:latitude,longitude:longitude},function (data) {

                    }).error(function (e) {
                        alert(e.responseText);
                    });
                }
            });
        });
        wx.error(function (res) {
            alert('err:' + res);
            // config信息验证失败会执行error函数，如签名过期导致验证失败，具体错误信息可以打开config的debug模式查看，也可以在返回的res参数中查看，对于SPA可以在这里更新签名。

        });
        wx.checkJsApi({
            jsApiList: ['chooseImage', 'onMenuShareTimeline'], // 需要检测的JS接口列表，所有JS接口列表见附录2,
            success: function (res) {
                // 以键值对的形式返回，可用的api值true，不可用为false
                // 如：{"checkResult":{"chooseImage":true},"errMsg":"checkJsApi:ok"}
                // alert('success:'+res);
            }
        });
    });

    var checkNextGroup = function () {
        var isShowGroupButtom = $('.person').length == $('.person .person-mask:visible').length;
        if (isShowGroupButtom) {
            $('.next-group').css('display', 'inline-block');
        } else {
            $('.next-group').css('display', 'none');
        }
    }
    checkNextGroup();

    $('.actions a[action=profile]').on('click', function () {
        var url='pages/profile.html';
        // window.location.href = 'pages/profile.html';
        var dialog = $('.panel-dialog');
        var iframe = dialog.find('>iframe');
        var tipsHtml = "<div style='position: relative;height: 100%;width: 100%;text-align: center;'><span style='position: absolute;top:50%;left: 50%;margin-top: -20px;margin-left: -50px;display: inline-block;font-size: 16px;color: red;'>正在加载...</span></div>";
        iframe.contents().find("body").html(tipsHtml);
        dialog.show();
        iframe.attr('src', url);
    });

    $('.person').on('click', function () {
        var li = $(this);
        var mask = li.find(".person-mask");
        if (mask.is(':visible')) {
            return;
        }
        mask.show();
        var person = li.attr('person');
        var url = './pages/red-bag.html?person=' + person + '&state=' + state;
        // window.location.href=url;
        var dialog = $('.panel-dialog');
        var iframe = dialog.find('>iframe');
        var tipsHtml = "<div style='position: relative;height: 100%;width: 100%;text-align: center;'><span style='position: absolute;top:50%;left: 50%;margin-top: -20px;margin-left: -50px;display: inline-block;font-size: 16px;color: red;'>正在抢包，请稍候...</span></div>";
        iframe.contents().find("body").html(tipsHtml);
        dialog.show();
        iframe.attr('src', url);
        checkNextGroup();
    });

    $('.panel-dialog .dialog-toolbar span').on('click', function () {
        var dialog = $('.panel-dialog');
        var iframe = dialog.find('>iframe');
        iframe.removeAttr("src");
        iframe.contents().find("body").empty();
        dialog.hide();
    })

    $('.next-group').on('click', function () {
        $(this).hide();
        // window.location.href = './home.html';
        window.location.reload();
        return false;
    });
    $('.empty-panel .prop-tag-settings a').on('click',function () {
        window.location.href='./pages/prop-tag.html';
    });
    $('.empty-panel .payee-settings a').on('click',function () {
        window.location.href = './pages/nav-download.html?title=聊天群服务&content=即时互动，推荐条件限定，比如，你只想认识女性朋友等，则需要使用地微app。&location=地微app->桌面';
    });
});