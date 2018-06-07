window.addEventListener('load', function(){
 
    Array.from(document.getElementsByClassName('latex')).forEach(
        function(element, index, array) {
            element.addEventListener('touchstart', function(e){
                if (element.scrollWidth - element.scrollWidth > element.scrollLeft) {
                    AndroidLatex.onTouch(2)
                } else {
                    AndroidLatex.onTouch(3);
                }
            }, false)

            element.addEventListener('touchmove', function(e){
                if (element.scrollWidth - element.scrollWidth > element.scrollLeft) {
                    AndroidLatex.onTouch(2)
                } else {
                    AndroidLatex.onTouch(3);
                }
            }, false)

            element.addEventListener('touchend', function(e){
                AndroidLatex.onTouch(3);
            }, false)
        }
    )
 
}, false)