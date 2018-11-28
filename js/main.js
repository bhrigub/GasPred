$(document).ready(function(){
 $('.header').height($(window).height())
})

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
 $("#submitButton").click(function() {
   alert("Processing");
 });
 /*$('#userInputForm').submit(function() {
    var email = $('#userInputForm :input');
    var values = {};
    $inputs.each(function() {
        values[this.name] = $(this).val();
    });
    $(#viewvariables).html(values[]);
});*/

$(document).ready(function(){
$("form").submit(function() {
    var email1 = $('#emailinput').val();
    //alert($(this).val());
    var startdate1 =  $('#startdateinput').val();
    var daycount1 = $('#daycountinput').val();
    var dateArr= startdate1.split('-');
    var newDate = dateArr[1] + dateArr[2] + dateArr [0];
    var link= "http://localhost:55555/gas/gasPrice?startDate="+ newDate +"&noOfDays=" + daycount1;
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
       var valReceived=[[]];
       console.log(valReceived);
       for (var i=0; i < lenLoop; i++){
         valReceived.push([dateArr[1] +'-'+ String(Number(dateArr[2]) + i) +'-'+ dateArr [0], Number(data.regular[i]), Number(data.midgrade[i]), Number(data.premium[i]), Number(data.deisel[i])]);
         /*valReceived[i][0]= dateArr[1] + (dateArr[2] + i) + dateArr [0];
         valReceived[i][1]=data.regular[i];
         valReceived[i][2]=data.midgrade[i];
         valReceived[i][3]=data.premium[i];
         valReceived[i][4]=data.deisel[i];*/
     }
     valReceived[0]=['Date', 'Regular', 'Midgrade', 'Premium', 'Diesel'];
          //valReceived.pop(0);
     console.log(valReceived);
      // localStorage.setItem("valReceived", JSON.stringify(valReceived));

      $("#chart_div2").show(function() {
        var jsonData=valReceived;
        /* var jsonData=[
        ['Date', 'Regular', 'Midgrade', 'Premium' ],
        ['04-02-2013',  100,      200,      300],
        ['10-22-2014',  117,      260,      360],
        ['03-12-2015',  260,      112,      400],
        ['03-12-2015',  240,      252,      357],
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
