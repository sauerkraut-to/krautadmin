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
});
