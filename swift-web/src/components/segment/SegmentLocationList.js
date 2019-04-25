import React from "react";
import {Datagrid, List, TextField} from 'react-admin';

const SegmentLocationList = props => (
    <List {...props}>
        <Datagrid>
            <TextField source="clusterId"/>
            <TextField source="segmentId"/>
            <TextField source="sourceKey"/>
        </Datagrid>
    </List>
);

export default SegmentLocationList;