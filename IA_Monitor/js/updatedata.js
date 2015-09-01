function thisClock() {
	var thisDate = new Date();
	var thisHour = thisDate.getHours();
	var thisMinute = thisDate.getMinutes();
	thisHour = (thisHour < 10 ? "0" : "") + thisHour;
	thisMinute = (thisMinute < 10 ? "0" : "") + thisMinute;
	
	var thisClock = thisHour + ":" + thisMinute;

	$("#thisClock").html(thisClock);
}

// Update the data from JSON array
function update() {
var url = "/iamonitor/rest/doctor/1"; // HARDCODED UserID = 1 
	
	$.getJSON(url, function (json) {
	
		var activity = json[0].activity;
		// console.log('activity : ', activity);	
		var location = json[0].location;
		// console.log('location : ', location);
		var interruptibility = json[0].interruptibility;
		// console.log('interruptibility : ', interruptibility);
		var timestamp = json[0].timestamp;
		// console.log('timestamp : ', timestamp);
		var currenttime = Date.now();
		// console.log('current time : ', currenttime);
		var minutes_since = Math.floor((currenttime-timestamp)/60000);
		
		// If the server and the smartphone doesn't
		// have the same time synchronization it might result in minus time, eg. -1 minute
		if(minutes_since < 0){
			minutes_since = 0;
		}
	
		var minute_string = ' MINUTES AGO:';
		if(minutes_since == '1'){
		minute_string = ' MINUTE AGO:';
		}
		
		// Update the activity, location, time texts and interruptibility image
		$('#activity').text(activity);
		$('#location').text(location);
		$('#minutes').html('<b>'+minutes_since+'</b>'+minute_string);
		
		if(interruptibility == '1') {
			$('#user1').attr('src','images/green.png');
		}else if(interruptibility == '-1') {
			$("#user1").attr('src','images/red.png');
		}else{
			$("#user1").attr('src','images/yellow.png');
		}

	});
}

$(document).ready(function() { 
	setInterval('thisClock()', 1000); 
	setInterval('update()',2000);
});