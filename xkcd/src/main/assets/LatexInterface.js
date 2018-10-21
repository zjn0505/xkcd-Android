window.addEventListener('load', function(){
 
    Array.from(document.getElementsByClassName('latex')).forEach(
        function(element, index, array) {
            var lastX;
            element.addEventListener('touchmove', function(e){
                var currentX = e.changedTouches[0].clientX
                if (currentX > lastX) {
                    // pre
                    if (element.scrollLeft == 0) {
                        AndroidLatex.onTouch(3)
                        return
                    }
                } else if (lastX > currentX) {
                    // next
                    if (element.scrollWidth - element.clientWidth == element.scrollLeft) {
                        AndroidLatex.onTouch(3)
                        return
                    }
                }
                lastX = currentX
                if (element.scrollWidth - element.clientWidth > element.scrollLeft) {
                    AndroidLatex.onTouch(2)
                } else {
                    AndroidLatex.onTouch(3)
                }
            }, false)
            element.addEventListener('touchend', function(e){
                 AndroidLatex.onTouch(3);
            }, false)
        }
    )
 
}, false)