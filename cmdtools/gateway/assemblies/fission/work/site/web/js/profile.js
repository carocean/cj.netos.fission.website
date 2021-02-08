$(document).ready(function(){
    $('.leading a').on('click',function () {
        history.go(-1);
    });
    $('.fission-balance').on('click',function () {
        window.location.href='fission-balance.html';
    })
    $('.cashier-balance').on('click',function () {
        window.location.href='cashier-balance.html';
    })
});