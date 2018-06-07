window.addEventListener('load', function(){
 
    Array.from(document.getElementsByClassName('latex')).forEach(
        function(element, index, array) {
            element.addEventListener('touchstart', function(e){
                AndroidLatex.onTouch(1);
            }, false)

            element.addEventListener('touchmove', function(e){
                AndroidLatex.onTouch(2);
            }, false)

            element.addEventListener('touchend', function(e){
                AndroidLatex.onTouch(3);
            }, false)
        }
    )
 
}, false)