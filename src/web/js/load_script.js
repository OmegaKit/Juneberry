// load another js file from url using given id
load_script = function(id, url) {
    var s = document.getElementById(id);
    // _test.clean existing one
    if (s) {
        document.body.removeChild(s);
    }
    s = document.createElement("script");
    s.id = id;
    s.src = url;
    //alert(url);
    document.body.appendChild(s);
}
