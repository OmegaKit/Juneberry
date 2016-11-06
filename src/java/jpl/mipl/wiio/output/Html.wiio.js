/* author: Xing */

var wiio = {};

wiio.get_leaf = function(id, entityType, prefix) {
    var el = document.getElementById(id);
    if (!el) {
        alert("Internal inconsistency");
        return false;
    }
    var value = el.value;
    //alert(value+" "+entityType+" "+prefix);
    if (entityType == "meta") {
        document.location.href = prefix + value + "/";
    } else {
        document.location.href = prefix + value + "[]?output=gif";
    }
}

wiio.get_leaf_proc = function(id, entityType, prefix) {
    var el = document.getElementById(id);
    if (!el) {
        alert("Internal inconsistency");
        return false;
    }
    var value = el.value;
    //alert(value);
    var encodedValue = base64.encode(value, false, true);
    //alert(encodedValue);
    //alert(value+" "+encodedValue+" "+entityType+" "+prefix);
    if (entityType == "meta") {
        document.location.href = prefix + encodedValue + "/";
    } else {
        document.location.href = prefix + encodedValue + "[]?output=gif";
    }
}
