import React, {Component} from 'react';
import {BooleanField, Datagrid, List, Show, SimpleShowLayout, TextField} from 'react-admin';
import ClusterState from "./ClusterState";
import MasterNode from "./MasterNode";
import CurrentNode from "./CurrentNode";

const styles = {
    flex: {display: 'flex'},
    flexColumn: {display: 'flex', flexDirection: 'column'},
    leftCol: {flex: 1, marginRight: '1em'},
    rightCol: {flex: 1, marginLeft: '1em'},
    singleCol: {marginTop: '2em', marginBottom: '2em'},
};

class System extends Component {

    state = {};

    render() {
        const {
            clusterState,
            masterNode,
            currentNode
        } = this.state;
        return (
            <div style={styles.flex}>
                <div style={styles.leftCol}>
                    <div style={styles.flex}>
                        <ClusterState value={clusterState}/>
                        <MasterNode value={masterNode}/>
                        <CurrentNode value={currentNode}/>
                    </div>
                    <div style={styles.singleCol}>
                    </div>
                    <div style={styles.singleCol}>
                    </div>
                </div>
            </div>
        )
    }
}

const SystemShow = (props) => (
    <Show {...props}>
        <SimpleShowLayout>
            <TextField source="id"/>
            <TextField source="clusterId"/>
            <BooleanField source="singleton"/>
            <TextField source="service"/>
            <TextField source="serviceInfo"/>
        </SimpleShowLayout>
    </Show>
);

const SystemList = (props) => (
    <List {...props}>
        <Datagrid>
            <TextField source="id"/>
            <TextField source="clusterId"/>
            <BooleanField source="singleton"/>
            <TextField source="service"/>
            <TextField source="serviceInfo"/>
        </Datagrid>
    </List>
);


export {SystemList, SystemShow};