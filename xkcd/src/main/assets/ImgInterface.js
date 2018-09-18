window.addEventListener('load', function(){
 
    Array.from(document.getElementsByClassName('illustration')).forEach(
        function(element, index, array) {
            var longpress = false;
            var presstimer = null;
            var startClientY;
            var cancel = function(e) {
                if (presstimer !== null) {
                    clearTimeout(presstimer);
                    presstimer = null;
                }
                this.classList.remove("longpress");
            };
            var click = function(e) {
                if (presstimer !== null) {
                    clearTimeout(presstimer);
                    presstimer = null;
                }
                this.classList.remove("longpress");
                if (longpress) {
                    return false;
                }
            };
            var start = function(e) {
                if (e.type === "click" && e.button !== 0) {
                    return;
                }

                if (e.touches != undefined) {
                    startClientY = e.touches[0].clientY;
                }

                longpress = false;
                this.classList.add("longpress");
                if (presstimer === null) {
                    presstimer = setTimeout(function() {
                        longpress = true;
                        AndroidImg.doLongPress(element.title);
                    }, 500);
                }
                return false;
            };
            var move = function(e) {
                if (e.touches != undefined) {
                    var newY = e.touches[0].clientY;
                    if (Math.abs(startClientY - newY) > 150) {
                        clearTimeout(presstimer);
                    }

                }
            }
            element.addEventListener("mousedown", start);
            element.addEventListener("touchstart", start);
            element.addEventListener("click", click);
            element.addEventListener("mouseout", cancel);
            element.addEventListener("touchend", cancel);
            element.addEventListener("touchleave", cancel);
            element.addEventListener("touchcancel", cancel);
            element.addEventListener("touchmove", move);
            element.addEventListener("mousemove", move);
        }
    )
}, false)