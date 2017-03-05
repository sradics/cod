console.log("scripts.js loaded");

function getTextWidth(text, font) {
    // re-use canvas object for better performance
    var canvas = getTextWidth.canvas || (getTextWidth.canvas = document.createElement("canvas"));
    var context = canvas.getContext("2d");
    context.font = font;
    var metrics = context.measureText(text);
    return metrics.width;
}

function getTextSize(text,height,maxTextWidth){
    textSize = Math.min(11,height-2);
    counter=0;
    while(true){
        currentWidth=getTextWidth(text, "normal "+textSize+"px sans-serif");
        if (currentWidth>maxTextWidth){
            textSize=textSize-1;
            console.log("reduce textSize from:"+(textSize+1)+" to: "+textSize+" currentWidth:"+currentWidth+
                " maxWidth:"+maxTextWidth+" counter:"+counter);
            counter++;
        }else{
            break;
        }
        if (counter>5 ||textSize<=6){
            break;
        }
    }
    if (textSize<6) {
        textSize=6;
    }
    return textSize;
}