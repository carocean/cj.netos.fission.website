$(document).ready(function(){
    // $('.person').on('click',function () {
    //     var li=$(this);
    //     var person=li.attr('person');
    //     $.get('./snatchEnvelope.service',{'person':person},function (data) {
    //         alert(person);
    //     }).error(function (e) {
    //         alert(e);
    //     })
    // });
    $('header .leading a').on('click',function () {
        history.back();
        return false;
    })
});