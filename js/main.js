$(document).ready(function(){
 $('.header').height($(window).height())
})
var valReceived1=[[]];
var valReceived2=[[]];
var valReceived3=[[]];
var valReceived4=[[]];
var valReceived5=[[]];
var jsonData;
var newDate1;
var startdate2;
$(".btn a").click(function(){
  $("body,html").animate({
   scrollTop:$("#" + $(this).data('value')).offset().top
 },1000);
 })

$(".navbar a").click(function(){
  $("body,html").animate({
   scrollTop:$("#" + $(this).data('value')).offset().top
 },1000);
 })
$("#chart_div2").hide();
 /*$("#submitButton").click(function() {
   alert("Processing");
 });*/
 $("#chart_div3").hide();
 
 /*$('#userInputForm').submit(function() {
    var email = $('#userInputForm :input');
    var values = {};
    $inputs.each(function() {
        values[this.name] = $(this).val();
    });
    $(#viewvariables).html(values[]);
});*/
function addDays(date, days) {
    var result = new Date(date);
    result.setDate(date.getDate() + days);
    return result;
}
function formatDate(date) {
    return (date.getMonth() + 1) + '/' + date.getDate() + '/' + date.getFullYear();
}
$(document).ready(function(){
$("form").submit(function() {
    var email1 = $('#emailinput').val();
    //alert($(this).val());
    var startdate1 =  $('#startdateinput').val();
    var daycount1 = $('#daycountinput').val();
    var daycount2 = parseInt(daycount1);
    console.log(daycount2);
    var dateArr= startdate1.split('-');
    var newDate = dateArr[1] + dateArr[2] + dateArr [0];
    var link= "http://localhost:8080/gas/gasPrice?startDate="+ newDate +"&noOfDays=" + daycount2;
    $.ajax({
        url: link
    }).then(function(data) {
       console.log(data);
       $('#viewvariables1').append(data.regular);
       $('#viewvariables2').append(data.midgrade);
       $('#viewvariables3').append(data.premium);
       //$('#viewvariables1').append(data.days);
       dataLoop=data.regular;
       lenLoop=dataLoop.length;
       console.log(lenLoop);
       valReceived1=[[]];
       valReceived2=[[]];
       valReceived3=[[]];
       valReceived4=[[]];
       valReceived5=[[]];
       console.log(valReceived1);
       for (var i=0; i < lenLoop; i++){
         /*valReceived1.push([dateArr[1] +'-'+ String(Number(dateArr[2]) + i) +'-'+ dateArr [0], Number(data.regular[i]), Number(data.midgrade[i]), Number(data.premium[i]), Number(data.deisel[i])]);
         valReceived2.push([dateArr[1] +'-'+ String(Number(dateArr[2]) + i) +'-'+ dateArr [0], Number(data.regular[i])]);
         valReceived3.push([dateArr[1] +'-'+ String(Number(dateArr[2]) + i) +'-'+ dateArr [0], Number(data.midgrade[i])]);
         valReceived4.push([dateArr[1] +'-'+ String(Number(dateArr[2]) + i) +'-'+ dateArr [0], Number(data.premium[i])]);
         valReceived5.push([dateArr[1] +'-'+ String(Number(dateArr[2]) + i) +'-'+ dateArr [0], Number(data.deisel[i])]);
         */

         startdate2 = new Date(startdate1);
         var newDate1=formatDate(addDays(startdate2,i));
         valReceived1.push([newDate1, Number(data.regular[i]), Number(data.midgrade[i]), Number(data.premium[i]), Number(data.deisel[i])]);
         valReceived2.push([newDate1, Number(data.regular[i])]);
         valReceived3.push([newDate1, Number(data.midgrade[i])]);
         valReceived4.push([newDate1, Number(data.premium[i])]);
         valReceived5.push([newDate1, Number(data.deisel[i])]);
         

         /*valReceived[i][0]= dateArr[1] + (dateArr[2] + i) + dateArr [0];
         valReceived[i][1]=data.regular[i];
         valReceived[i][2]=data.midgrade[i];
         valReceived[i][3]=data.premium[i];
         valReceived[i][4]=data.deisel[i];
         */
     }
     valReceived1[0]=['Date', 'Regular', 'Midgrade', 'Premium', 'Diesel'];
     valReceived2[0]=['Date', 'Regular'];
     valReceived3[0]=['Date', 'Midgrade'];
     valReceived4[0]=['Date', 'Premium'];
     valReceived5[0]=['Date', 'Diesel'];
          //valReceived.pop(0);
     console.log(valReceived1);
     console.log(valReceived2);
     console.log(valReceived3);
     console.log(valReceived4);
     console.log(valReceived5);
      // localStorage.setItem("valReceived", JSON.stringify(valReceived));
      $("#chart_div3").show(function() {

        });
      $("#chart_div2").show(function() {
        var jsonData=valReceived1;
        /* var jsonData=[
        ['Date', 'Regular', 'Midgrade', 'Premium' ],
        ['04-02-2013',  100,      200,      300],
        ['10-22-2014',  117,      260,      360],
      ]; */
      drawChart2(jsonData);
    });
    });

    //console.log(email1);
    //console.log(newDate);
    //console.log(daycount1);

    //console.log(link);
    //console.log(data.days);
    //console.log(valReceived);

   //$("#viewvariables1").append(email1);
   //$("#viewvariables2").append(newDate);
   //$("#viewvariables3").append(daycount1);
 });
});
$("#option1").click(function() {
   $("#lab1").addClass("active");
   $("#lab2").removeClass("active");
   $("#lab3").removeClass("active");
   $("#lab4").removeClass("active");
   $("#lab5").removeClass("active");
   var jsonData=valReceived1;
   drawChart2(jsonData);
 });
$("#option2").click(function() {
   $("#lab2").addClass("active");
   $("#lab1").removeClass("active");
   $("#lab3").removeClass("active");
   $("#lab4").removeClass("active");
   $("#lab5").removeClass("active");
   var jsonData=valReceived2;
   drawChart2(jsonData);
 });
$("#option3").click(function() {
   $("#lab3").addClass("active");
   $("#lab1").removeClass("active");
   $("#lab2").removeClass("active");
   $("#lab4").removeClass("active");
   $("#lab5").removeClass("active");
   var jsonData=valReceived3;
   drawChart2(jsonData);
 });
$("#option4").click(function() {
   $("#lab4").addClass("active");
   $("#lab1").removeClass("active");
   $("#lab2").removeClass("active");
   $("#lab3").removeClass("active");
   $("#lab5").removeClass("active");
   var jsonData=valReceived4;
   drawChart2(jsonData);
 });
$("#option5").click(function() {
   $("#lab5").addClass("active");
   $("#lab1").removeClass("active");
   $("#lab2").removeClass("active");
   $("#lab3").removeClass("active");
   $("#lab4").removeClass("active");
   var jsonData=valReceived5;
   drawChart2(jsonData);
 });