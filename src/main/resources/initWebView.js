/**
 * Created by sebastianradics on 05.03.17.
 */
var dataBeans = databeans.getDataBeans();


for (i=0;i<dataBeans.length;i++){

    weekData[i]={
        "costOfDelay":Number(dataBeans[i].getCostOfDelay()),
        "name":dataBeans[i].getName(),
        "week":Number(dataBeans[i].getWeek()),
        "currentFeatureInProgress":dataBeans[i].getCurrentFeatureName()
    };

}

for (i=0;i<weekData.length;i++){
    console.log("{\n   \"costOfDelay\":"+weekData[i].costOfDelay+
        ",\n   \"name\":\""+weekData[i].name+"\",\n   "+
        "\"week\":"+weekData[i].week+",\n   "+
        "\"currentFeatureInProgress\":\""+weekData[i].currentFeatureInProgress+"\""
        +"\n},");
}
