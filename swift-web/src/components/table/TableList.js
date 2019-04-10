import React from 'react';
import {Datagrid, Filter, List, TextField, TextInput} from 'react-admin';
import TableShow from './TableShow'

const TableFilter = (props) => (
    <Filter {...props}>
        <TextInput label="Search" source="query" alwaysOn/>
    </Filter>
);

const TableList = props => (
    <List {...props} filters={<TableFilter/>}>
        <Datagrid expand={<TableShow/>}>
            <TextField source="id"/>
            <TextField source="remark"/>
            <TextField source="swiftDatabase"/>
            <TextField source="schemaName"/>
            <TextField source="tableName"/>
        </Datagrid>
    </List>
);

export default TableList;