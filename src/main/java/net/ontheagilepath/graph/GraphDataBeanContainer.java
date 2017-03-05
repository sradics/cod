package net.ontheagilepath.graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sebastianradics on 05.03.17.
 */
public class GraphDataBeanContainer {
    private List<GraphDataBean> dataBeansList = new ArrayList<GraphDataBean>();

    public GraphDataBeanContainer(){

    }

    public void addDataBean(GraphDataBean bean){
        dataBeansList.add(bean);
    }


    public GraphDataBean[] getDataBeans(){
        return dataBeansList.toArray(new GraphDataBean[]{});
    }
}
