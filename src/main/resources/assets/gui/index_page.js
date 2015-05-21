function updateClock ( ) {
    var dayNames = ["Sonntag","Montag","Dienstag","Mittwoch","Donnerstag","Freitag","Samstag"];
    var currentTime = new Date ( );
    var currentHours = withLeadingZero(currentTime.getHours());
    var currentMinutes = withLeadingZero(currentTime.getMinutes());
    var currentSeconds = withLeadingZero(currentTime.getSeconds());

    // Compose the string for display
    var currentTimeString = dayNames[currentTime.getDay()] + ", " + currentHours + ":" + currentMinutes + " Uhr";

    $(".live-clock").html(currentTimeString);
}

function withLeadingZero(number) {
    return (number < 10 ? "0" : "") + number;
}

$(document).ready(function() {
    updateClock();
    setInterval('updateClock()', 1000);
    $('input.textbox-text').first().focus();

    $('#form-login').form({
        url: '/rest/session/login',
        onSubmit: function(){
            var isValid = $(this).form('validate');
            if (isValid){
            	$.messager.progress({
            	    'interval': 310,
            	    title:'Bitte einen Augenblick Geduld',
                    msg:'Aus Sicherheitsgründen findet der Login verzögert statt...'
            	});
            }
            return isValid;
        },
        success:function(response) {
            $.messager.progress('close');

            var response = eval('(' + response + ')');  // convert the JSON string to javascript object

            if (!response.success) {
                $.messager.alert({
                	'title': 'Login nicht erfolgreich',
                	'msg': response.exception.message,
                	'icon': 'error',
                	'fn': (function() {
                	    $('input.textbox-text').first().focus();
                	})
                });
            } else {
                window.location.href('/mykraut/index.html');
            }
        }
    }).keypress(function(e) {
        if(e.which == 13) {
            $(this).submit();
        }
    });
});
