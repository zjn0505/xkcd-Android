window.addEventListener('load', function(){
 
    Array.from(document.getElementsByClassName('refnum')).forEach(
        function(element, index, array) {
            element.addEventListener('click', function(e){
                AndroidRef.refContent(element.nextSibling.innerHTML);
            }, false)
        }
    )
 
}, false)