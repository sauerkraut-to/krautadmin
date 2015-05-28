$(document).ready(function() {
    redirectIfSessionAvailableElseDisplayBody();
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
        onSubmit: function(){
            var isValid = $(this).form('validate');
            if (isValid && !$('#form-login').data('submitted')){
                $('#form-login').data('submitted', true);
            	$.messager.progress({
            	    'interval': (loginDelayMilliseconds / 10),
            	    'title': 'Bitte einen Augenblick Geduld',
                    'msg': 'Aus Sicherheitsgründen findet der Login verzögert statt...'
            	});
            	$.ajax({
                    type: 'POST',
                    url: '/rest/session/login',
                    data: $('#form-login').serialize(),
                    success: function(response) {
                        $.messager.progress('close');
                        location.href = '/mykraut/index.html';
                    },
                    error: function(errorObject) {
                        $.messager.progress('close');
                        $.messager.alert({
                            'title': 'Login nicht erfolgreich',
                            'msg': errorObject.responseJSON.exception.message,
                            'icon': 'error',
                            'fn': (function() {
                                $('input.textbox-text').first().focus();
                                $('#form-login').data('submitted', false);
                            })
                        });
                    },
                    dataType: 'json'
                });
            }
            return false;
        }
    }).keypress(function(e) {
        if(e.which == 13) {
            $(this).submit();
        }
    });
});

function redirectIfSessionAvailableElseDisplayBody() {
    $.getJSON('/rest/application/hasSession', function(response) {
        location.href = '/mykraut/index.html';
    }).fail(function() {
        showBody();
    });
}
