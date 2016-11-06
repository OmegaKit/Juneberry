var juneberry = {};

juneberry.toggle = function(id) {
    var el = document.getElementById(id);
    if (!el) {
        alert("Internal inconsistency: element attributes missing.");
        return false;
    }
    if (el.style.display == "") {
        el.style.display = "none";
    } else {
        el.style.display = "";
    }
    return true;
}

juneberry.show_attributes = function(obj) {
    var el = document.getElementById("attributes");
    if (!el) {
        alert("Internal inconsistency: element attributes missing.");
        return false;
    }
    el.innerHTML = obj;
    return true;
}
