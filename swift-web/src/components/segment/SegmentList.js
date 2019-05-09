import React from 'react';
import {Datagrid, Filter, List, TextField, TextInput} from 'react-admin';
import Table from '@material-ui/core/Table';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Paper from '@material-ui/core/Paper';

const SegmentFilter = (props) => (
    <Filter {...props}>
        <TextInput label="Search" source="query" alwaysOn/>
    </Filter>
);

const SegmentLocationPanel = ({id, record, resource}) => (
    <Paper>
        <Table>
            <TableHead>
                <TableRow>
                    <TableCell align="right">name</TableCell>
                    <TableCell align="right">type</TableCell>
                    <TableCell align="right">remark</TableCell>
                    <TableCell align="right">precision</TableCell>
                    <TableCell align="right">scale</TableCell>
                    <TableCell align="right">columnId</TableCell>
                </TableRow>
            </TableHead>
        </Table>
    </Paper>
);

const SegmentList = props => (
    <List {...props} filters={<SegmentFilter/>}>
        <Datagrid expand={<SegmentLocationPanel/>}>
            <TextField source="id"/>
            <TextField source="sourceKey"/>
            {/*<ReferenceField source="sourceKey" reference="table">*/}
            {/*<TextField source="id"/>*/}
            {/*</ReferenceField>*/}
            <TextField source="storeType"/>
            <TextField source="order"/>
            <TextField source="swiftSchema"/>
        </Datagrid>
    </List>
);

export default SegmentList;