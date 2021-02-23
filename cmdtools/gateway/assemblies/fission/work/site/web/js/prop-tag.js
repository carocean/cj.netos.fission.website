$(document).ready(function () {
    $('.leading a').on('click', function () {
        history.go(-1);
        return false;
    });
    function removeSelectedTag(tagId){
        $.get('./prop-tag-manager.service',{action:'remove',tagId:tagId},function (data) {
            $('.tag-selected .tag-ul .tag-label[tag-id=' + tagId + ']').remove();
        }).error(function (e) {
            alert(e.responseText);
        });
    }
    function selectTag(tagId,the){
        $.get('./prop-tag-manager.service',{action:'select',tagId:tagId},function (data) {
            $('.tag-selected .tag-ul').append(the);
        }).error(function (e) {
            alert(e.responseText);
        });
    }
    $('.tag-panel .tag-ul .tag-label').on('click', function () {
        var tagId = $(this).attr('tag-id');
        var list = $('.tag-selected .tag-ul .tag-label');
        var hasTag=false;
        for (var i = 0; i < list.length; i++) {
            var exists = list[i];
            var existsTagId=$(exists).attr('tag-id');
            if (existsTagId== tagId) {
                //移除
                removeSelectedTag(tagId);
                hasTag=true;
                break;
            }
        }
        if (hasTag) {
            return;
        }
        //添加
        selectTag(tagId,$(this).clone());

    });
    $('.tag-selected').undelegate('.tag-ul .tag-label','click');
    $('.tag-selected').delegate('.tag-ul .tag-label','click',function () {
        var tagId = $(this).attr('tag-id');
        //移除
        removeSelectedTag(tagId);
    });
});