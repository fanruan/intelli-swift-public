import React from "react";
import {BooleanField, Datagrid, List, TextField} from 'react-admin';

const ServiceList = (props) => (
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

export default ServiceList;