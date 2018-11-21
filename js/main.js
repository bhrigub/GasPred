$(document).ready(function(){
 $('.header').height($(window).height())
})
$(".navbar a").click(function(){
  $("body,html").animate({
   scrollTop:$("#" + $(this).data('value')).offset().top
 },1000);
 })
 $("#submitButton").click(function(){
   alert("hi")
 });
 $('#userInputForm').submit(function() {
    var $inputs = $('#userInputForm :input');
    var values = {};
    $inputs.each(function() {
        values[this.name] = $(this).val();
    });
});
