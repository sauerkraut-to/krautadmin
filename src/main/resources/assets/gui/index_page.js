$(document).ready(function() {
    var defaultAdditionalClientSideDelayMilliseconds = 100;
    var loginDelayMilliseconds = 3100;
    updateClock();
    setInterval('updateClock()', 1000);
    setInterval('keepAlive()', 30000);

    $.getJSON('/rest/application/loginDelayMilliseconds', function(response) {
        if (response.success) {
            loginDelayMilliseconds = response.payload + defaultAdditionalClientSideDelayMilliseconds;
        }
    });

    $('input.textbox-text').first().focus();

    $('#form-login').form({
        url: '/rest/session/login',
        onSubmit: function(){
            var isValid = $(this).form('validate');
            if (isValid){
            	$.messager.progress({
            	    'interval': (loginDelayMilliseconds / 10),
            	    'title': 'Bitte einen Augenblick Geduld',
                    'msg': 'Aus Sicherheitsgründen findet der Login verzögert statt...'
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
                location.href = '/mykraut/index.html';
            }
        }
    }).keypress(function(e) {
        if(e.which == 13) {
            $(this).submit();
        }
    });
});
